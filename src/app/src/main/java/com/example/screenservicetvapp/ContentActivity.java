package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class ContentActivity extends AppCompatActivity {
    private static final String TOKEN_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:access_token";
    private static final String TOKEN_REFRESH_GRANT_TYPE = "refresh_token";


    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    private LocalStorageService storageService;

    private TextView messageTextView;
    private TextView deviceNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        messageTextView = (TextView) findViewById(R.id.content_message_txt);
        deviceNameTextView = (TextView) findViewById(R.id.content_act_device_name_textview);

        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        storageService = new LocalStorageService(this);

        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Log.d("ContentActivity", "accessToken: " + accessToken);
        // Assume you want to start AnotherActivity when a certain condition is met
        if (accessToken == null) {
            //this.tryRefreshAccessToken();
        } else {
            this.makeApiRequest(accessToken);
        }
    }

    private void makeApiRequest(String accessToken) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ContentDataApiRequest apiRequest = retrofit.create(ContentDataApiRequest.class);
        Call<ContentDataApiResponse> call = apiRequest.getData("Bearer " + accessToken);

        call.enqueue(new Callback<ContentDataApiResponse>() {
            @Override
            public void onResponse(Call<ContentDataApiResponse> call, Response<ContentDataApiResponse> response) {
                if (response.isSuccessful()) {
                    ContentDataApiResponse responseData = response.body();
                    if(responseData != null) {

                        ContentDataLayout layout = responseData.getLayout();
                        if(layout != null) {
                            switch (layout.getTemplateKey()) {
                                case "MediaOnly":
                                    navigateToMediaOnlyActivity(responseData);
                                    break;
                                default:
                                    navigateToErrorActivity("No Layout Key", "Layout Key is not set, update screen and republish");
                            }
                        }
                        //navigateToMediaOnlyActivity(responseData);
                    }
                    else {
                        //userCodeTextView.setText("Status Error: ResponseData is null");
                    }
                } else {

                    try {
                        String error = response.errorBody().string();
                        Log.d("ContentActErrorTest", error);
                        //Toast.makeText(getContext(), jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();

                        int responseStatus = response.code();
                        switch (responseStatus){
                            case 404:
                                displayNotFoundMessage(error);
                                break;
                            case 401:
                                refreshAccessToken();
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        Log.d("ContentActivityMakeReq", e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ContentDataApiResponse> call, Throwable t) {
                // Handle API request failure
                navigateToErrorActivity("Content Data Network Error", "Technical error occurred when connecting to the server, try again later.");
            }
        });
    }

    private void navigateToMediaOnlyActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, MediaOnlyActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("mediaAsset", responseData.getMediaAsset());
        intent.putExtra("externalMediaSource", responseData.getExternalMediaSource());

        // Start the new activity
        startActivity(intent);
        finish(); // Close the current activity
    }

    private void refreshAccessToken() {
        String refreshToken = storageService.getRefreshToken();
        if(refreshToken == null) {
            navigateToCodeActivationScreen();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TokenApiRequest tokenApiRequest = retrofit.create(TokenApiRequest.class);
        Call<TokenApiResponse> call = tokenApiRequest.refreshTokenRequest(
                new TokenApiRequestBody("", "string", "", TOKEN_REFRESH_GRANT_TYPE), "Bearer " + refreshToken);

        call.enqueue(new Callback<TokenApiResponse>() {
            @Override
            public void onResponse(Call<TokenApiResponse> call, Response<TokenApiResponse> response) {
                if (response.isSuccessful()) {
                    TokenApiResponse responseData = response.body();
                    storageService.setAccessToken(responseData.getAccessToken());
                    storageService.setRefreshToken(responseData.getRefreshToken());

                    makeApiRequest(responseData.getAccessToken());
                    // Display the JSON data on the screen

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        Log.d("ContentActErrorTest", error);
                    } catch (Exception e) {
                        Log.d("CodeActivationJSON", e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenApiResponse> call, Throwable t) {
                // Handle API request failure
                navigateToErrorActivity("Content Refresh Network Error", "Technical error occurred when connecting to the server, try again later.");
            }
        });
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

    private void navigateToCodeActivationScreen() {
        Intent intent = new Intent(this, CodeActivationActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }

    private void displayNotFoundMessage(String errorCode) {
        String displayMsg;
        boolean showDeviceName = false;
        switch (errorCode) {
            case "no_such_device":
                displayMsg = "No Such Device Found";
                break;
            case "no_screen_id":
                displayMsg = "No Screen Attached";
                break;
            case "no_screen_data_found":
                displayMsg = "No Screen Data Found";
                break;
            default:
                displayMsg = "Error occurred";
                break;
        }
        messageTextView.setText(displayMsg);
        if(showDeviceName) {
            String deviceName = storageService.getData(Constants.DEVICE_NAME);
            if(!ObjectExtensions.isNullOrEmpty(deviceName))
            {
                deviceNameTextView.setText(deviceName);
            }
        }
    }
}