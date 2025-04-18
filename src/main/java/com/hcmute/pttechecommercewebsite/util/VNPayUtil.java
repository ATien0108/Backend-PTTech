package com.hcmute.pttechecommercewebsite.util;

import com.hcmute.pttechecommercewebsite.config.VNPayConfig;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VNPayUtil {

    public static String getPaymentUrl(String orderId, double amount, VNPayConfig config) throws UnsupportedEncodingException {
        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String vnpCurrCode = "VND";
        String vnpLocale = "vn";
        String vnpOrderType = "other";
        String vnpIpAddr = "192.168.100.180";

        // Thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());

        // Chuyển amount x100
        long amountInVNPayFormat = (long) (amount * 100);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", config.getVnpTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInVNPayFormat));
        vnpParams.put("vnp_CurrCode", vnpCurrCode);
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnpParams.put("vnp_OrderType", vnpOrderType);
        vnpParams.put("vnp_Locale", vnpLocale);
        vnpParams.put("vnp_ReturnUrl", config.getVnpReturnUrl());
        vnpParams.put("vnp_IpAddr", vnpIpAddr);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Sort theo tên key
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            String value = vnpParams.get(name);

            // Encode value trong hashData (VNPAY yêu cầu!)
            hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));

            query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII.toString()))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));

            if (i < fieldNames.size() - 1) {
                hashData.append('&');
                query.append('&');
            }
        }

        // Tạo secure hash
        String secureHash = hmacSHA512(config.getVnpHashSecret(), hashData.toString());

        // In ra để debug nếu cần
        System.out.println(">>> HashData: " + hashData);
        System.out.println(">>> SecureHash: " + secureHash);

        return config.getVnpPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(bytes).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo chữ ký hash HmacSHA512", e);
        }
    }
}