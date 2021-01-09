package com.example.googlebookstesttask.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.example.googlebookstesttask.Model.AccessTokenResponse;
import com.example.googlebookstesttask.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.googlebookstesttask.MainActivity.APP_PREFERENCES_ACCESS_TOKEN;
import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.MainActivity.mSettings;
import static com.example.googlebookstesttask.MainActivity.refreshToken;


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

    public static void getNewAccessToken(Context context, IRefreshAccessToken iRefreshAccessToken) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", context.getString(R.string.server_client_id))
                .add("client_secret", context.getString(R.string.client_secret))
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();
        Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                AccessTokenResponse accessTokenResponse = gson.fromJson(response.body().string(), AccessTokenResponse.class);
                SharedPreferences.Editor edit = mSettings.edit();
                if (accessTokenResponse.getAccess_token()!=null) {
                    accessToken = accessTokenResponse.getAccess_token();
                    String accessTokenEncrypted = Base64.encodeToString(rsaEncrypt(accessToken.getBytes("UTF-8")), Base64.NO_WRAP);
                    edit.putString(APP_PREFERENCES_ACCESS_TOKEN, accessTokenEncrypted);
                }
                edit.apply();
                iRefreshAccessToken.tokenRefreshed();
            }
        });


    }

}
