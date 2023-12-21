package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CodeActivationActivity extends AppCompatActivity {
    private static final String TOKEN_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:access_token";

    private TextView userCodeTextView;
    private TextView codeUrlTextView;

    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    private Handler handler = new Handler();
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 5;

    private TokenStorageService storageService;

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

        storageService = new TokenStorageService(this);

        makeApiRequest();
    }

    private void makeApiRequest() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
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
                    if(responseData != null) {
                        // Display the JSON data on the screen
                        String userCode = responseData.getUserCode();
                        String codeUrl = responseData.getVerificationUrl();
                        if (userCode != null) {
                            displayUserCodeOnScreen(userCode, codeUrl);
                            scheduleStatusApiRequest(responseData);
                        }
                    }
                    else {
                        userCodeTextView.setText("Status Error: ResponseData is null");
                    }
                } else {
                    // Handle unsuccessful API request
                    userCodeTextView.setText("Status Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CodeActivationApiResponse> call, Throwable t) {
                // Handle API request failure
                navigateToErrorActivity("CodeActivation Network Error", "technical error occurred when connecting to the server, try again later.");
            }
        });
    }

    private void scheduleStatusApiRequest(CodeActivationApiResponse requestData) {
        if(requestData != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Make a periodic API request for status
                    makeStatusApiRequest(requestData);
                }
            }, requestData.getInterval()); // 5000 Schedule the next request after 5 seconds
            retryCount++;

            // Display an error if the maximum retry count is reached
            if (retryCount >= MAX_RETRY_COUNT) {
                // show error page; textViewResult.setText("Error: Maximum retry count reached");
            }
        }
    }
    private void makeStatusApiRequest(CodeActivationApiResponse requestData) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TokenApiRequest tokenApiRequest = retrofit.create(TokenApiRequest.class);
        Call<TokenApiResponse> call = tokenApiRequest.tokenRequest(
                new TokenApiRequestBody(requestData.getClientId(), "string", requestData.getDeviceCode(), TOKEN_GRANT_TYPE));

        call.enqueue(new Callback<TokenApiResponse>() {
            @Override
            public void onResponse(Call<TokenApiResponse> call, Response<TokenApiResponse> response) {
                if (response.isSuccessful()) {
                    TokenApiResponse responseData = response.body();
                    storageService.setAccessToken(responseData.getAccessToken());
                    storageService.setRefreshToken(responseData.getRefreshToken());

                    navigateToContentActivity();
                    // Display the JSON data on the screen

                } else {

                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        Log.d("ContentActErrorTest", error);
                    } catch (Exception e) {
                        Log.d("CodeActivationJSON", e.getMessage());
                    }

                    int responseStatusCode = response.code();
                    switch (responseStatusCode) {
                        case 428:  // authorization_pending
                            try {
                                Thread.sleep(requestData.getInterval()*1000);
                                makeStatusApiRequest(requestData);
                            }
                            catch(InterruptedException ex)
                            {
                                Log.d("ThreadSleep", ex.getMessage());
                                Thread.currentThread().interrupt();
                            }
                            break;
                        case 403:
                            // StatusCodes.Status403Forbidden, "access_denied", "Invalid device code"
                            // or Status403Forbidden, "access_denied", "Forbidden"
                            navigateToErrorActivity("Error occurred","Invalid device code or Forbidden. Status: " + response.code());
                            break;
                        case 400:
                            // StatusCodes.Status400BadRequest, "access_expired", "Access Expired"
                            navigateToErrorActivity("Error occurred","Access Expired. Status: " + response.code());
                            break;
                        default:
                            // Handle unsuccessful API request
                            navigateToErrorActivity("Error occurred","Handle unsuccessful API request. Status " + response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenApiResponse> call, Throwable t) {
                // Handle API request failure
                navigateToErrorActivity("Network Error", "technical error occurred when connecting to the server, try again later.");
            }
        });
    }

    private void navigateToContentActivity() {
        Intent intent = new Intent(this, ContentActivity.class);

        // Start the new activity
        startActivity(intent);

        finish(); // Close the current activity
    }

    private void navigateToErrorActivity(String errorTitle, String errorMessage) {
        Intent intent = new Intent(this, ErrorActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("errorTitle", errorTitle);
        intent.putExtra("errorMessage", errorMessage);

        // Start the new activity
        startActivity(intent);
        finish(); // Close the current activity
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