package com.example.googlebookstesttask;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.googlebookstesttask.Model.AccessTokenResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Random;
import javax.security.auth.x500.X500Principal;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.googlebookstesttask.Utils.AnyUtils.getDecryptedString;
import static com.example.googlebookstesttask.Utils.AnyUtils.rsaDecrypt;
import static com.example.googlebookstesttask.Utils.AnyUtils.rsaEncrypt;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private int RC_SIGN_IN = 1;
    public static final String APP_PREFERENCES_ACCESS_TOKEN = "accessToken";
    public static final String APP_PREFERENCES_REFRESH_TOKEN = "refreshToken";
    public static SharedPreferences mSettings;
    public static String accessToken;
    public static String refreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = findViewById(R.id.sign_in_button);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signInButton.setOnClickListener(v -> signIn());

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias("MyKeyAlias")) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 10);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias("MyKeyAlias")
                        .setSubject(new X500Principal("CN=MyKeyName, O=Android Authority"))
                        .setSerialNumber(new BigInteger(1024, new Random()))
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                keyPairGenerator.initialize(spec);
                keyPairGenerator.generateKeyPair();

                //Encryption test
                final byte[] encryptedBytes = rsaEncrypt("666666666666666666666666666666666666666666666".getBytes("UTF-8"));
                final byte[] decryptedBytes = rsaDecrypt(encryptedBytes);
                final String decryptedString = new String(decryptedBytes, "UTF-8");
                Log.e("MyApp", "Decrypted string is " + decryptedString);
            }
            else {
                if (mSettings.contains(APP_PREFERENCES_ACCESS_TOKEN))
                    accessToken = getDecryptedString(mSettings.getString(APP_PREFERENCES_ACCESS_TOKEN, ""));
                if (mSettings.contains(APP_PREFERENCES_REFRESH_TOKEN))
                    refreshToken = getDecryptedString(mSettings.getString(APP_PREFERENCES_REFRESH_TOKEN, ""));
                System.out.println(accessToken);
                System.out.println(refreshToken);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account!=null && refreshToken!=null) {
            Intent intent = new Intent(this, BookSearchActivity.class);
            startActivity(intent);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            getAccessToken(account.getServerAuthCode(), this);
        } catch (ApiException e) {
            Toast.makeText(this, "Error "+e, Toast.LENGTH_LONG).show();
        }
    }

    public void getAccessToken(String authCode, Context context) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("code", authCode)
                .add("client_id", getString(R.string.server_client_id))
                .add("client_secret", getString(R.string.client_secret))
                .add("grant_type", "authorization_code")
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
                System.out.println(accessTokenResponse.getAccess_token());
                System.out.println(accessTokenResponse.getExpires_in());
                System.out.println(accessTokenResponse.getRefresh_token());
                System.out.println(accessTokenResponse.getScope());
                System.out.println(accessTokenResponse.getToken_type());

                SharedPreferences.Editor edit = mSettings.edit();
                if (accessTokenResponse.getAccess_token()!=null) {
                    accessToken = accessTokenResponse.getAccess_token();
                    String accessTokenEncrypted = Base64.encodeToString(rsaEncrypt(accessToken.getBytes("UTF-8")), Base64.NO_WRAP);
                    edit.putString(APP_PREFERENCES_ACCESS_TOKEN, accessTokenEncrypted);
                }
                if (accessTokenResponse.getRefresh_token()!=null) {
                    refreshToken = accessTokenResponse.getRefresh_token();
                    String refreshTokenEncrypted = Base64.encodeToString(rsaEncrypt(refreshToken.getBytes("UTF-8")), Base64.NO_WRAP);
                    edit.putString(APP_PREFERENCES_REFRESH_TOKEN, refreshTokenEncrypted);
                }
                edit.apply();

//                if (refreshToken!=null) {
                Intent intent = new Intent(context, BookSearchActivity.class);
                startActivity(intent);
//                }
//                else
//                    Toast.makeText(context, "Refresh token is empty", Toast.LENGTH_LONG).show();


//                System.out.println("ACCESS: "+ mSettings.getString(APP_PREFERENCES_ACCESS_TOKEN, "нихуа"));
//                System.out.println("REFRESH: "+ mSettings.getString(APP_PREFERENCES_REFRESH_TOKEN, "нихуа"));
//                byte[] array = Base64.decode(mSettings.getString(APP_PREFERENCES_REFRESH_TOKEN, "нихуа"), Base64.NO_WRAP);
//                final byte[] decryptedBytes = rsaDecrypt(array);
//                final String decryptedString = new String(decryptedBytes, "UTF-8");
//                System.out.println("REFRESH_DECRYPTED: "+ decryptedString);
//                System.out.println("СОВПАЛО!!!! "+decryptedString.equals("1//0cYyWCJMLVpSMCgYIARAAGAwSNwF-L9IrQnhWrKh2EFvaDF7ssNpFSRJqKfWiU-SGNjXjvJII-Q1PZADCKSeedscY42gdsiCEEoo"));
            }
        });


    }



}