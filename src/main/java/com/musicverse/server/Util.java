package com.musicverse.server;

import lombok.SneakyThrows;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Util {
    @SneakyThrows
    public static String loadResource(String path) {

        return new String(Objects.requireNonNull(Util.class.getResourceAsStream(path)).readAllBytes());
    }

    public static String hashText(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");

        byte[] messageDigest = md.digest(input.getBytes());

        BigInteger no = new BigInteger(1, messageDigest);

        StringBuilder hashtext = new StringBuilder(no.toString(16));

        while (hashtext.length() < 32) {
            hashtext.insert(0, "0");
        }

        return hashtext.toString();
    }
}
