package com.cardesk.home;

import android.content.Context;
import android.util.Base64;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/** Standard legacy ADB RSA identity, stored only in private, non-backed-up preferences. */
final class AdbIdentity {
    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;
    AdbIdentity(Context context) throws Exception {
        android.content.SharedPreferences prefs = context.getSharedPreferences("adb_identity", Context.MODE_PRIVATE);
        String privateValue = prefs.getString("private", "");
        String publicValue = prefs.getString("public", "");
        if (privateValue.isEmpty() != publicValue.isEmpty()) throw new java.io.IOException("ADB密钥不完整，请清除应用数据后重新授权");
        if (privateValue.isEmpty()) {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair(); privateKey = pair.getPrivate(); publicKey = (RSAPublicKey) pair.getPublic();
            if (!prefs.edit().putString("private", Base64.encodeToString(privateKey.getEncoded(), Base64.NO_WRAP))
                    .putString("public", Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP)).commit())
                throw new java.io.IOException("无法保存ADB密钥");
        } else {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateValue, Base64.NO_WRAP)));
            publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(Base64.decode(publicValue, Base64.NO_WRAP)));
        }
    }
    byte[] sign(byte[] token) throws Exception {
        if (token.length != 20) throw new java.io.IOException("ADB认证令牌长度错误");
        // AUTH already supplies a SHA-1 digest: do not hash it a second time.
        byte[] prefix = {0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14};
        byte[] encoded = new byte[prefix.length + token.length];
        System.arraycopy(prefix, 0, encoded, 0, prefix.length); System.arraycopy(token, 0, encoded, prefix.length, token.length);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(encoded);
    }
    byte[] publicPayload() throws Exception {
        BigInteger n = publicKey.getModulus();
        BigInteger word = BigInteger.ONE.shiftLeft(32);
        BigInteger rr = BigInteger.ONE.shiftLeft(4096).mod(n);
        ByteBuffer buffer = ByteBuffer.allocate(524).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(64); buffer.putInt(n.mod(word).modInverse(word).negate().intValue());
        littleInteger(buffer, n); littleInteger(buffer, rr); buffer.putInt(publicKey.getPublicExponent().intValue());
        return (Base64.encodeToString(buffer.array(), Base64.NO_WRAP) + " cardesk@android\0").getBytes("UTF-8");
    }
    private static void littleInteger(ByteBuffer buffer, BigInteger value) {
        byte[] bytes = value.toByteArray();
        for (int i = 0; i < 256; i++) buffer.put(i < bytes.length ? bytes[bytes.length - 1 - i] : (byte) 0);
    }
}
