package com.example.googlebookstesttask.Utils;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;


public class AnyUtils {

    public static byte[] rsaEncrypt(final byte[] decryptedBytes) {
        byte[] encryptedBytes = null;
        try {
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry("MyKeyAlias", null);
            final RSAPublicKey publicKey = (RSAPublicKey)privateKeyEntry.getCertificate().getPublicKey();

            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(decryptedBytes);
            cipherOutputStream.close();

            encryptedBytes = outputStream.toByteArray();

        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        return encryptedBytes;
    }

    public static byte[] rsaDecrypt(final byte[] encryptedBytes) {
        byte[] decryptedBytes = null;
        try {
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry("MyKeyAlias", null);

            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            final CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encryptedBytes), cipher);
            final ArrayList<Byte> arrayList = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1 ) {
                arrayList.add((byte)nextByte);
            }

            decryptedBytes = new byte[arrayList.size()];
            for(int i = 0; i < decryptedBytes.length; i++) {
                decryptedBytes[i] = arrayList.get(i);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return decryptedBytes;
    }

    public static String getDecryptedString(String encryptedString) throws UnsupportedEncodingException {
        byte[] encryptedBytes = Base64.decode(encryptedString, Base64.NO_WRAP);
        final byte[] decryptedBytes = rsaDecrypt(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

}
