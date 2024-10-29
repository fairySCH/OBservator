package com.happy.observator.Upbit;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class UpbitApiUtil {
     public static String generateJwtToken(String accessKey, String secretKey, String queryString) {
        try {
            // Generate the SHA-512 hash of the query string
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(queryString.getBytes("utf8"));

            // Convert the hash to a hexadecimal string
            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            // Use HMAC256 algorithm to sign the JWT
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            // Create the JWT token
            String jwtToken = JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", UUID.randomUUID().toString())
                    .withClaim("query_hash", queryHash)
                    .withClaim("query_hash_alg", "SHA512")
                    .sign(algorithm);

            return jwtToken;

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}
