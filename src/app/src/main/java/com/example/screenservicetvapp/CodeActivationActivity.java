package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CodeActivationActivity extends AppCompatActivity {
    private static final String ENDPOINT_BASEURL = "http://mydisplay123point.runasp.net/";

    private TextView userCodeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_activation);

        userCodeTextView = findViewById(R.id.step_two_code);

        makeApiRequest();
    }

    private void makeApiRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT_BASEURL)
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
                    displayUserCodeOnScreen(responseData != null ? responseData.getUserCode() : null);
                } else {
                    // Handle unsuccessful API request
                    userCodeTextView.setText("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CodeActivationApiResponse> call, Throwable t) {
                // Handle API request failure
                userCodeTextView.setText("Error: " + t.getMessage());
            }
        });
    }

    private void displayUserCodeOnScreen(String userCode) {
        if(userCode != null) {
            userCodeTextView.setText(userCode);
        }
    }
}