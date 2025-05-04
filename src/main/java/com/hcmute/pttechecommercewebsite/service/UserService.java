package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.CartDTO;
import com.hcmute.pttechecommercewebsite.dto.UserDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.User;
import com.hcmute.pttechecommercewebsite.repository.UserRepository;
import com.hcmute.pttechecommercewebsite.util.JwtUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private FacebookAuthService facebookAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    // Thư mục lưu trữ hình ảnh người dùng
    private String uploadDir = "upload-images/users";

    // URL công khai để truy cập hình ảnh
    private String uploadUrl = "http://localhost:8081/images/users";

    // Gửi email xác thực
    private void sendVerificationEmail(User user) {
        emailService.sendVerificationEmail(user);
    }

    // Gửi email reset mật khẩu
    private void sendPasswordResetEmail(User user, boolean isAdmin, boolean isMobile) {
        emailService.sendPasswordResetEmail(user, isAdmin, isMobile);
    }

    // Gửi thông báo email tới người dùng
    public String sendNotificationEmail(String subject, String content, List<String> userIds) {
        List<User> users;

        // Kiểm tra nếu danh sách userIds rỗng, lấy tất cả người dùng chưa bị xóa
        if (userIds == null || userIds.isEmpty()) {
            users = userRepository.findAllByIsDeletedFalse();
        } else {
            // Lấy danh sách người dùng theo id được truyền vào
            users = userRepository.findAllById(userIds);
        }

        if (users.isEmpty()) {
            throw new RuntimeException("Không có người dùng nào để gửi thông báo.");
        }

        // Duyệt qua từng người dùng
        int emailSentCount = 0;
        for (User user : users) {
            // Kiểm tra nếu người dùng đã đăng ký nhận thông báo
            if (user.isSubscribedToEmails()) {
                try {
                    emailService.sendNotificationEmail(subject, content, user.getEmail());
                    emailSentCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Có lỗi xảy ra khi gửi email tới người dùng: " + user.getEmail());
                }
            }
        }

        return "Đã gửi email thông báo tới " + emailSentCount + " người dùng đã đăng ký nhận thông báo.";
    }

    // Lấy tất cả người dùng
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllByIsDeletedFalse();
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy người dùng theo ID
    public Optional<UserDTO> getUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::convertToDTO);
    }

    // Tìm kiếm người dùng theo tên (username)
    public List<UserDTO> searchUsersByUsername(String username) {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getUsername().contains(username))
                .collect(Collectors.toList());
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Thêm mới người dùng
    public UserDTO addUser(User user) {
        user.setDeleted(false);
        user.setBlocked(false);
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);

        createCartForUser(savedUser);

        return convertToDTO(savedUser);
    }

    // Cập nhật thông tin người dùng
    public UserDTO updateUser(String id, User user) {
        if (userRepository.existsById(id)) {
            User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encodedPassword = passwordEncoder.encode(user.getPassword());
                existingUser.setPassword(encodedPassword);
            }

            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhoneNumber(user.getPhoneNumber());
            existingUser.setAvatar(user.getAvatar());
            existingUser.setAddress(user.getAddress());
            existingUser.setVerified(user.isVerified());
            existingUser.setVerificationToken(user.getVerificationToken());
            existingUser.setVerificationExpiry(user.getVerificationExpiry());
            existingUser.setDeleted(user.isDeleted());
            existingUser.setBlocked(user.isBlocked());
            existingUser.setBlockReason(user.getBlockReason());
            existingUser.setRoles(user.getRoles());
            existingUser.setSubscribedToEmails(user.isSubscribedToEmails());
            existingUser.setUpdatedAt(new Date());

            User updatedUser = userRepository.save(existingUser);
            return convertToDTO(updatedUser);
        }
        return null;
    }

    // Xóa người dùng
    public boolean deleteUser(String id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(true);
            user.setUpdatedAt(new Date());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Block người dùng
    public boolean blockUser(String id, String blockReason) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setBlocked(true);
            user.setBlockReason(blockReason);
            user.setUpdatedAt(new Date());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Gỡ block người dùng
    public boolean unblockUser(String id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setBlocked(false);
            user.setBlockReason(null);
            user.setUpdatedAt(new Date());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Phương thức đăng ký tài khoản người dùng (người dùng tự đăng ký)
    public UserDTO registerUser(User user) {
        Optional<User> existingUserOptional = userRepository.findByEmail(user.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            if (existingUser.isDeleted()) {
                existingUser.setDeleted(false);
                existingUser.setBlocked(false);
                existingUser.setVerified(false);
                existingUser.setUpdatedAt(new Date());
                existingUser.setVerificationToken(UUID.randomUUID().toString());

                User.Role customerRole = new User.Role();
                customerRole.setRoleName("Customer");
                customerRole.setPermissions(Arrays.asList("view_products", "add_to_cart"));
                user.setRoles(Collections.singletonList(customerRole));

                existingUser.setAvatar("");
                existingUser.setAddress(new User.Address("","","","",""));

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, 24);
                existingUser.setVerificationExpiry(calendar.getTime());

                String encodedPassword = passwordEncoder.encode(user.getPassword());
                existingUser.setPassword(encodedPassword);

                userRepository.save(existingUser);
                sendVerificationEmail(existingUser);

                return convertToDTO(existingUser);
            } else {
                throw new RuntimeException("Email đã tồn tại và chưa bị xóa. Không thể đăng ký tài khoản mới.");
            }
        } else {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            user.setDeleted(false);
            user.setBlocked(false);
            user.setVerified(false);
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            user.setVerificationToken(UUID.randomUUID().toString());

            User.Role customerRole = new User.Role();
            customerRole.setRoleName("CUSTOMER");
            customerRole.setPermissions(Arrays.asList("view_products", "add_to_cart"));
            user.setRoles(Collections.singletonList(customerRole));

            user.setAvatar("");
            user.setAddress(new User.Address("","","","",""));

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 24);
            user.setVerificationExpiry(calendar.getTime());

            User savedUser = userRepository.save(user);
            sendVerificationEmail(savedUser);
            return convertToDTO(savedUser);
        }
    }

    // Xác thực tài khoản khi người dùng nhấp vào liên kết
    public boolean verifyUser(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Kiểm tra thời gian hết hạn của token
            if (user.getVerificationExpiry().before(new Date())) {
                userRepository.delete(user);
                return false;
            }

            user.setVerified(true);
            user.setVerificationToken(null);
            user.setVerificationExpiry(null);
            user.setUpdatedAt(new Date());
            User savedUser = userRepository.save(user);
            createCartForUser(savedUser);
            return true;
        }
        return false;
    }

    // Phương thức để tự động kiểm tra và xóa user không xác thực sau mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")  // Chạy mỗi ngày lúc 00:00 (midnight)
    public void removeExpiredUsers() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getVerificationToken() != null && user.getVerificationExpiry() != null
                    && user.getVerificationExpiry().before(new Date()) && !user.isVerified()) {
                userRepository.delete(user);
            }
        }
    }

    public String sendPasswordResetEmail(String email, boolean isAdmin, boolean isMobile) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetToken = UUID.randomUUID().toString();
            user.setVerificationToken(resetToken);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 1);
            user.setVerificationExpiry(calendar.getTime());
            userRepository.save(user);

            // Gửi email reset mật khẩu
            sendPasswordResetEmail(user, isAdmin, isMobile);

            return "Email reset mật khẩu đã được gửi tới: " + email;
        } else {
            throw new RuntimeException("Email không tồn tại trong hệ thống.");
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getVerificationExpiry().before(new Date())) {
                throw new RuntimeException("Token đã hết hạn.");
            }

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setVerificationToken(null);
            user.setVerificationExpiry(null);
            user.setUpdatedAt(new Date());
            userRepository.save(user);

            return true;
        }
        return false;
    }

    // Tạo giỏ hàng cho người dùng
    private void createCartForUser(User user) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setUserId(user.getId().toString());

        cartService.addCart(cartDTO);
    }

    public ResponseEntity<?> loginUser(String email, String password) {
        // Tìm người dùng bằng email
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại.");
        }

        User user = userOptional.get();

        // Kiểm tra xem người dùng đã bị xóa chưa
        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn đã bị xóa.");
        }

        // Kiểm tra xem người dùng đã được xác thực chưa
        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
        }

        // Kiểm tra mật khẩu người dùng có khớp không
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mật khẩu không chính xác.");
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());

        // Tạo token JWT
        String jwtToken = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        // Tạo refresh token
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // Trả về token và thông tin người dùng
        return ResponseEntity.ok(Map.of(
                "accessToken", jwtToken,
                "refreshToken", refreshToken,
                "message", "Đăng nhập thành công",
                "userId", user.getId(),
                "username", user.getUsername()
        ));
    }

    // Phương thức đăng nhập bằng Google OAuth2
    public ResponseEntity<?> googleLogin(String tokenId) {
        try {
            // Lấy thông tin người dùng từ Google thông qua tokenId
            Map<String, Object> userInfo = googleAuthService.getUserInfoFromToken(tokenId);

            if (userInfo == null || !userInfo.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy thông tin người dùng từ Google.");
            }

            String email = (String) userInfo.get("email");
            String username = (String) userInfo.get("name");
            String avatarUrl = (String) userInfo.get("picture");

            // Kiểm tra người dùng có tồn tại trong hệ thống chưa
            Optional<User> userOptional = userRepository.findByEmail(email);

            User user;
            if (userOptional.isPresent()) {
                // Nếu người dùng đã tồn tại
                user = userOptional.get();

                if (user.isDeleted()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn đã bị xóa.");
                }

                if (!user.isVerified()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
                }
            } else {
                // Nếu người dùng chưa tồn tại, tạo người dùng mới
                user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setVerified(true);
                user.setAvatar(avatarUrl);
                user.setPhoneNumber("");
                user.setAddress(new User.Address());
                user.getAddress().setStreet("");
                user.getAddress().setCommunes("");
                user.getAddress().setDistrict("");
                user.getAddress().setCity("");
                user.getAddress().setCountry("");
                user.setCreatedAt(new Date());
                user.setUpdatedAt(new Date());

                User.Role customerRole = new User.Role();
                customerRole.setRoleName("CUSTOMER");
                customerRole.setPermissions(Arrays.asList("view_products", "add_to_cart"));
                user.setRoles(Collections.singletonList(customerRole));

                User savedUser = userRepository.save(user);

                createCartForUser(savedUser);

                // Gửi email thông báo đăng nhập thành công
                emailService.sendLoginSuccessNotificationEmail(user, "Google");
            }

            // Tạo JWT token cho người dùng
            String jwtToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toList()));

            // Tạo refresh token
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // Trả về token và thông tin người dùng
            return ResponseEntity.ok(Map.of(
                    "accessToken", jwtToken,
                    "refreshToken", refreshToken,
                    "message", "Đăng nhập thành công",
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "avatar", user.getAvatar()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực với Google: " + e.getMessage());
        }
    }

    public ResponseEntity<?> facebookLogin(String accessToken) {
        try {
            // Kiểm tra accessToken có hợp lệ không
            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token không hợp lệ.");
            }

            // Lấy thông tin người dùng từ Facebook thông qua accessToken
            Map<String, Object> userInfo = facebookAuthService.getUserInfoFromToken(accessToken);

            if (userInfo == null || !userInfo.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy thông tin người dùng từ Facebook.");
            }

            String email = (String) userInfo.get("email");
            String username = (String) userInfo.get("name");
            String avatarUrl = null;

            // Kiểm tra nếu có ảnh đại diện
            Object pictureObj = userInfo.get("picture");
            if (pictureObj instanceof Map) {
                Map<String, Object> pictureMap = (Map<String, Object>) pictureObj;
                Object dataObj = pictureMap.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                    avatarUrl = (String) dataMap.get("url");
                }
            }

            if (avatarUrl == null) {
                avatarUrl = "default-avatar-url";
            }

            // Kiểm tra người dùng có tồn tại trong hệ thống chưa
            Optional<User> userOptional = userRepository.findByEmail(email);

            User user;
            if (userOptional.isPresent()) {
                // Nếu người dùng đã tồn tại
                user = userOptional.get();

                if (user.isDeleted()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn đã bị xóa.");
                }

                if (!user.isVerified()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
                }
            } else {
                // Nếu người dùng chưa tồn tại, tạo người dùng mới
                user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setVerified(true);
                user.setAvatar(avatarUrl);
                user.setPhoneNumber("");
                user.setAddress(new User.Address());
                user.getAddress().setStreet("");
                user.getAddress().setCommunes("");
                user.getAddress().setDistrict("");
                user.getAddress().setCity("");
                user.getAddress().setCountry("");
                user.setCreatedAt(new Date());
                user.setUpdatedAt(new Date());

                User.Role customerRole = new User.Role();
                customerRole.setRoleName("CUSTOMER");
                customerRole.setPermissions(Arrays.asList("view_products", "add_to_cart"));
                user.setRoles(Collections.singletonList(customerRole));

                User savedUser = userRepository.save(user);

                createCartForUser(savedUser);

                // Gửi email thông báo đăng nhập thành công
                emailService.sendLoginSuccessNotificationEmail(user, "Facebook");
            }

            // Tạo JWT token cho người dùng
            String jwtToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toList()));

            // Tạo refresh token
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // Trả về token và thông tin người dùng
            return ResponseEntity.ok(Map.of(
                    "accessToken", jwtToken,
                    "refreshToken", refreshToken,
                    "message", "Đăng nhập thành công",
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "avatar", user.getAvatar()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực với Facebook: " + e.getMessage());
        }
    }

    public ResponseEntity<String> subscribeToEmailNotifications(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Kiểm tra nếu người dùng chưa đăng ký nhận thông báo qua email
            if (!user.isSubscribedToEmails()) {
                user.setSubscribedToEmails(true);
                user.setUpdatedAt(new Date());
                userRepository.save(user);

                // Gửi email cảm ơn người dùng đã đăng ký nhận thông báo
                try {
                    emailService.sendThankYouForSubscriptionEmail(user);
                    return ResponseEntity.ok("Đăng ký nhận thông báo qua email thành công. Email cảm ơn đã được gửi.");
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Đăng ký thành công nhưng có lỗi xảy ra khi gửi email cảm ơn.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email này đã đăng ký nhận thông báo.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Email này chưa có tài khoản. Vui lòng đăng ký.");
        }
    }

    // Tạo một tên tệp duy nhất cho ảnh và lưu vào thư mục
    public String uploadAvatar(MultipartFile file, String userId) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        Path path = Paths.get(uploadDir + File.separator + fileName);

        // Tạo thư mục nếu chưa tồn tại
        Files.createDirectories(path.getParent());

        // Lưu ảnh vào thư mục
        file.transferTo(path);

        // Cập nhật URL của ảnh đại diện vào User
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        User user = optionalUser.get();
        user.setAvatar(uploadUrl + "/" + fileName);
        userRepository.save(user);

        return uploadUrl + "/" + fileName;
    }

    // Xóa avatar của người dùng
    public void deleteUserAvatar(String userId) throws IOException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        User user = optionalUser.get();
        String avatarPath = user.getAvatar();

        if (avatarPath != null && !avatarPath.isEmpty()) {
            Path imagePath = Paths.get(uploadDir + File.separator + avatarPath.substring(avatarPath.lastIndexOf("/") + 1));
            File fileToDelete = imagePath.toFile();

            // Kiểm tra xem tệp có tồn tại không
            if (fileToDelete.exists()) {
                // Xóa tệp
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    throw new IOException("Không thể xóa tệp ảnh: " + avatarPath);
                }
                // Xóa URL avatar trong User
                user.setAvatar(null);
                userRepository.save(user);
            } else {
                throw new IOException("Tệp ảnh không tồn tại: " + avatarPath);
            }
        } else {
            throw new ResourceNotFoundException("Người dùng này không có avatar để xóa");
        }
    }

    public ByteArrayOutputStream exportUsersToExcel(String sortBy, String sortOrder) throws IOException {
        List<UserDTO> users = getAllUsers();

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Định dạng chung cho workbook
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo dòng tiêu đề
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "ID", "Tên người dùng", "Email", "Số điện thoại", "Địa chỉ", "Thành phố", "Quận/Huyện", "Xã/Phường",
                "Quốc gia", "Trạng thái tài khoản", "Vai trò", "Trạng thái xác thực", "Trạng thái xóa", "Trạng thái chặn"
        };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu người dùng vào file Excel
        int rowNum = 1;
        for (UserDTO user : users) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(user.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Tên người dùng
            row.createCell(1).setCellValue(user.getUsername());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Email
            row.createCell(2).setCellValue(user.getEmail());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Số điện thoại
            row.createCell(3).setCellValue(user.getPhoneNumber());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Địa chỉ
            row.createCell(4).setCellValue(user.getAddress() != null ? user.getAddress().getStreet() : "N/A");
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Thành phố
            row.createCell(5).setCellValue(user.getAddress() != null ? user.getAddress().getCity() : "N/A");
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Quận/Huyện
            row.createCell(6).setCellValue(user.getAddress() != null ? user.getAddress().getDistrict() : "N/A");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Xã/Phường
            row.createCell(7).setCellValue(user.getAddress() != null ? user.getAddress().getCommunes() : "N/A");
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Quốc gia
            row.createCell(8).setCellValue(user.getAddress() != null ? user.getAddress().getCountry() : "N/A");
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Trạng thái tài khoản
            row.createCell(10).setCellValue(user.isVerified() ? "Đã xác thực" : "Chưa xác thực");
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Vai trò
            row.createCell(11).setCellValue(user.getRoles() != null ?
                    user.getRoles().stream()
                            .map(role -> role.getRoleName())
                            .collect(Collectors.joining(", ")) : "N/A");
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(12).setCellValue(user.isDeleted() ? "Đã xóa" : "Chưa xóa");
            row.getCell(12).setCellStyle(dataStyle);

            // Cột Trạng thái chặn
            row.createCell(13).setCellValue(user.isBlocked() ? "Đã chặn" : "Chưa chặn");
            row.getCell(13).setCellStyle(dataStyle);
        }

        // Tự động điều chỉnh độ rộng cột theo nội dung
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Tạo OutputStream và ghi workbook vào đó
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    // Chuyển đổi từ User model sang UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO.AddressDTO addressDTO = new UserDTO.AddressDTO(
                user.getAddress().getStreet(),
                user.getAddress().getCommunes(),
                user.getAddress().getDistrict(),
                user.getAddress().getCity(),
                user.getAddress().getCountry()
        );

        List<UserDTO.RoleDTO> roleDTOs = user.getRoles().stream()
                .map(role -> new UserDTO.RoleDTO(role.getRoleName(), role.getPermissions()))
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .address(addressDTO)
                .isVerified(user.isVerified())
                .verificationToken(user.getVerificationToken())
                .verificationExpiry(user.getVerificationExpiry())
                .isDeleted(user.isDeleted())
                .isBlocked(user.isBlocked())
                .blockReason(user.getBlockReason())
                .roles(roleDTOs)
                .isSubscribedToEmails(user.isSubscribedToEmails())
                .build();
    }
}