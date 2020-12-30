package com.example.googlebookstesttask;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.example.googlebookstesttask.Model.AccessTokenResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
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

import static com.example.googlebookstesttask.Utils.AnyUtils.rsaDecrypt;
import static com.example.googlebookstesttask.Utils.AnyUtils.rsaEncrypt;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(v -> signIn());

        try {
            //Generate a keypair and store it in the KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 10);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                    .setAlias("MyKeyAlias")
                    .setSubject(new X500Principal("CN=MyKeyName, O=Android Authority"))
                    .setSerialNumber(new BigInteger(1024, new Random()))
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    //.setEncryptionRequired() //on API level 18, encrypted at rest, requires lock screen to be set up, changing lock screen removes key
                    .build();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();

            //Encryption test
            final byte[] encryptedBytes = rsaEncrypt("666666666666666666666666666666666666666666666".getBytes("UTF-8"));
            final byte[] decryptedBytes = rsaDecrypt(encryptedBytes);
            final String decryptedString = new String(decryptedBytes, "UTF-8");
            Log.e("MyApp", "Decrypted string is " + decryptedString);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        System.out.println("Аккаунт: "+account);
        if (account!=null) {
            System.out.println("СурверАусКод" + account.getServerAuthCode());
           // getAccessToken(account.getServerAuthCode());
        }
//        updateUI(account);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            getAccessToken(account.getServerAuthCode());
            // Signed in successfully, show authenticated UI.
            // updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    public void getAccessToken(String authCode) {
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

            }
        });


    }



}