package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AppleServiceImpl implements AppleService {
    @Override
    public MemberResponseDto.AppleUserInfoDto getUserInfo(String identityToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // public key로 서명 검증
            if (!isValidAppleIdentityToken(signedJWT)) {
                throw new GeneralException(ErrorStatus.INVALID_APPLE_TOKEN);
            }

            String email = claims.getStringClaim("email");

            return MemberResponseDto.AppleUserInfoDto.builder()
                    .email(email)
                    .build();
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INVALID_APPLE_TOKEN);
        }
    }

    private boolean isValidAppleIdentityToken(SignedJWT signedJWT) {
        try {
            // 헤더에서 key id(kid)와 알고리즘 추출
            JWSHeader header = signedJWT.getHeader();
            String kid = header.getKeyID();
            String alg = header.getAlgorithm().getName();

            // Apple 공개키 가져오기
            RSAPublicKey publicKey = getApplePublicKey(kid, alg);

            // 서명 검증
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            // 검증 실패
            return false;
        }
    }

    private RSAPublicKey getApplePublicKey(String kid, String alg) {
        try {
            String url = "https://appleid.apple.com/auth/keys";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JSONObject json = new JSONObject(response.getBody());
            JSONArray keys = json.getJSONArray("keys");

            for (int i = 0; i < keys.length(); i++) {
                JSONObject key = keys.getJSONObject(i);
                if (key.getString("kid").equals(kid) && key.getString("alg").equals(alg)) {
                    BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(key.getString("n")));
                    BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(key.getString("e")));

                    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
                }
            }

            throw new GeneralException(ErrorStatus.INVALID_APPLE_PUBLIC_KEY);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INVALID_APPLE_PUBLIC_KEY);
        }
    }
}
