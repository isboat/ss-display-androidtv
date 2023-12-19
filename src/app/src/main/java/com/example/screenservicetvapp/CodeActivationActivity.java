package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CodeActivationActivity extends AppCompatActivity {
    private static final String ENDPOINT_BASEURL = "http://mydisplay123point.runasp.net/api/";

    private TextView userCodeTextView;
    private TextView codeUrlTextView;

    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_activation);

        userCodeTextView = findViewById(R.id.step_two_code);
        codeUrlTextView = findViewById(R.id.step_one_url);

        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        makeApiRequest();
    }

    private void makeApiRequest() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CodeActivationApiRequest activationApiRequest = retrofit.create(CodeActivationApiRequest.class);
        Call<CodeActivationApiResponse> call = activationApiRequest.deviceCode(new CodeActivationRequestBody("clientid", "user_code"));

        call.enqueue(new Callback<CodeActivationApiResponse>() {
            @Override
            public void onResponse(Call<CodeActivationApiResponse> call, Response<CodeActivationApiResponse> response) {
                if (response.isSuccessful()) {
                    CodeActivationApiResponse responseData = response.body();
                    // Display the JSON data on the screen
                    String userCode = responseData != null ? responseData.getUserCode() : null;
                    String codeUrl = responseData != null ? responseData.getVerificationUrl() : null;
                    if(userCode != null) {
                        displayUserCodeOnScreen(userCode, codeUrl);
                    }
                } else {
                    // Handle unsuccessful API request
                    userCodeTextView.setText("Status Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CodeActivationApiResponse> call, Throwable t) {
                // Handle API request failure
                userCodeTextView.setText("Error: " + t.getMessage());
            }
        });
    }

    private void displayUserCodeOnScreen(String userCode, String codeUrl) {
        if(userCode != null) {
            userCodeTextView.setText(userCode);
        }

        if(codeUrl != null) {
            codeUrlTextView.setText(codeUrl);
        }
    }
}