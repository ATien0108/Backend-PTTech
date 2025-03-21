package com.hcmute.pttechecommercewebsite.service;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Service
public class GoogleAuthService {

    private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    public Map<String, Object> getUserInfoFromToken(String tokenId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String jwkSetJson = restTemplate.getForObject(GOOGLE_JWK_SET_URI, String.class);
        JWKSet jwkSet = JWKSet.parse(jwkSetJson);
        JWT jwt = JWTParser.parse(tokenId);
        String kid = (String) jwt.getHeader().toJSONObject().get("kid");
        JWK jwk = jwkSet.getKeyByKeyId(kid);
        if (jwk == null) {
            throw new Exception("Không tìm thấy key tương ứng với kid: " + kid);
        }
        RSAKey rsaKey = (RSAKey) jwk;
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        Map<String, Object> userInfo = claims.getClaims();
        return userInfo;
    }
}