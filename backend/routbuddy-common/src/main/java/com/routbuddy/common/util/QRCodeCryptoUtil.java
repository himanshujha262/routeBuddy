package com.routbuddy.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public final class QRCodeCryptoUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private QRCodeCryptoUtil() {}

    public static String generateSignedBoardingPass(UUID bookingId, UUID tripId, UUID passengerId, long expiryTimestamp, String secretKey) {
        String payload = String.format("%s|%s|%s|%d", bookingId, tripId, passengerId, expiryTimestamp);
        String signature = calculateHmac(payload, secretKey);
        String combined = payload + "|" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    public static BoardingPassPayload verifyAndExtractBoardingPass(String token, String secretKey) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 5) {
                return null;
            }

            UUID bookingId = UUID.fromString(parts[0]);
            UUID tripId = UUID.fromString(parts[1]);
            UUID passengerId = UUID.fromString(parts[2]);
            long expiryTimestamp = Long.parseLong(parts[3]);
            String expectedSignature = parts[4];

            String payload = String.format("%s|%s|%s|%d", bookingId, tripId, passengerId, expiryTimestamp);
            String actualSignature = calculateHmac(payload, secretKey);

            if (!actualSignature.equals(expectedSignature)) {
                return null;
            }

            if (System.currentTimeMillis() > expiryTimestamp) {
                return null;
            }

            return new BoardingPassPayload(bookingId, tripId, passengerId, expiryTimestamp, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static String calculateHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error computing HMAC signature", e);
        }
    }

    public record BoardingPassPayload(UUID bookingId, UUID tripId, UUID passengerId, long expiryTimestamp, boolean isValid) {}
}
