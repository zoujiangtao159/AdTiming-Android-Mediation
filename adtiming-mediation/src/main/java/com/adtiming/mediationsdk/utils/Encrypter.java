// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encrypter {
    private Encrypter() {
    }

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String md5(String data) {
        return md5(data, UTF_8);
    }

    public static String md5(String data, Charset charset) {
        return byte2hex(encrypt("MD5", data.getBytes(charset)));
    }

    public static String sha(String data) {
        return sha(data, UTF_8);
    }

    public static String sha(String data, Charset charset) {
        return byte2hex(encrypt("SHA", data.getBytes(charset)));
    }

    public static String sha1(String data) {
        return sha1(data, UTF_8);
    }

    public static String sha1(String data, Charset charset) {
        return byte2hex(encrypt("SHA-1", data.getBytes(charset)));
    }

    public static byte[] encrypt(String algorithm, byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(data);
        } catch (Exception gse) {
            throw new RuntimeException(gse);
        }
    }

    public static String hamc(String data, String secret) {
        return hamc(data, secret, UTF_8);
    }

    public static String hamc(String data, String secret, Charset charset) {
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(charset), "HmacMD5");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes(charset));
            return byte2hex(bytes);
        } catch (Exception gse) {
            throw new RuntimeException(gse);
        }
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }

    public static String md5(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(byteBuffer);
            return Encrypter.byte2hex(md.digest());
        } catch (Exception e) {
           DeveloperLog.LogD("Encrypter", e);
            CrashUtil.getSingleton().saveException(e);
            return "";
        } finally {
            IOUtil.closeQuietly(in);
        }
    }

}