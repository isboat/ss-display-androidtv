package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.microsoft.signalr.HubConnection;
import com.onscreensync.tvapp.apirequests.ContentDataApiRequest;
import com.onscreensync.tvapp.apirequests.TokenApiRequest;
import com.onscreensync.tvapp.apirequests.TokenApiRequestBody;
import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse;
import com.onscreensync.tvapp.apiresponses.TokenApiResponse;
import com.onscreensync.tvapp.datamodels.LayoutDataModel;
import com.onscreensync.tvapp.datamodels.LayoutTemplatePropertyDataModel;
import com.onscreensync.tvapp.datamodels.MenuDataModel;
import com.onscreensync.tvapp.datamodels.MenuMetadata;
import com.onscreensync.tvapp.datamodels.PlaylistData;
import com.onscreensync.tvapp.datamodels.SignalrReceivedMessage;
import com.onscreensync.tvapp.services.LocalStorageService;
import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;
import com.onscreensync.tvapp.utils.JsonUtils;
import com.onscreensync.tvapp.utils.ObjectExtensions;

import org.json.JSONObject;

import java.util.Objects;

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
    private Handler handler;

    private boolean isActive;

    private SignalrHubConnectionBuilder signalrHubConnectionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);
        messageTextView = (TextView) findViewById(R.id.content_message_txt);
        deviceNameTextView = (TextView) findViewById(R.id.content_act_device_name_textview);

        storageService = new LocalStorageService(this);

        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        this.startRun();
    }

    private void startRun()
    {
        String deviceName = storageService.getData(Constants.DEVICE_NAME);
        if(!ObjectExtensions.isNullOrEmpty(deviceName))
        {
            deviceNameTextView.setText(deviceName);
        }

        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        String deviceId = storageService.getData((Constants.DEVICE_ID));
        // Assume you want to start AnotherActivity when a certain condition is met
        if (accessToken == null) {
            this.refreshAccessToken();
        } else {

            this.loadContentDataFromApi(accessToken);
            this.establishSignalRConnection(accessToken, deviceId);

            /*
            handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    loadContentDataFromApi(accessToken);
                    handler.postDelayed(this, 5000);
                }
            };

            handler.postDelayed(r, 1000);
            */
        }
    }

    private void establishSignalRConnection(String accessToken, String deviceId) {

        if(this.signalrHubConnectionBuilder == null) {
            this.signalrHubConnectionBuilder = new SignalrHubConnectionBuilder(accessToken, deviceId, (message) -> {
                // Handle incoming message
                runOnUiThread(() -> {
                    SignalrReceivedMessage receivedMessage = JsonUtils.fromJson(message, SignalrReceivedMessage.class);
                    Log.d("SignalrReceived", message);
                    if(receivedMessage != null) {

                        switch (receivedMessage.getMessageType()) {
                            case "content.published":
                                loadContentDataFromApi(accessToken);
                                break;
                            case "app.restart":
                                // loadContentDataFromApi(accessToken);
                                break;
                            case "app.terminate":
                                // shutdown loadContentDataFromApi(accessToken);
                                break;
                            case "app.upgrade.info":
                                makeSnackBarMessage(receivedMessage.getMessageData(), Color.BLACK);
                                break;
                            case "operator.info":
                                makeSnackBarMessage(receivedMessage.getMessageData(), Color.RED);
                                break;
                            case "app.upgrade.force":
                                // App should auto upgrade itself
                                break;
                        }
                    }
                });
            });
        }
    }

    private void makeSnackBarMessage(String message, int color) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, 10000)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(color); // Set text color;

        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    private void loadContentDataFromApi(String accessToken) {

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
                        String checksum = responseData.getChecksum();
                        String storedChecksum = storageService.getData(Constants.CHECKSUM_DATA_KEY);
                        if(Objects.equals(storedChecksum, checksum) && isActive) return;
                        storageService.setData(Constants.CHECKSUM_DATA_KEY, checksum);
                        isActive = true;

                        LayoutDataModel layout = responseData.getLayout();
                        if(layout != null) {
                            switch (layout.getTemplateKey()) {
                                case "MenuOverlay":
                                    navigateToMenuOverlayActivity(responseData);
                                    break;
                                case "MenuOnly":
                                    navigateToMenuOnlyActivity(responseData);
                                    break;
                                case "MediaOnly":
                                    navigateToMediaOnlyActivity(responseData);
                                    break;
                                case "Text":
                                    navigateToTextEditorActivity(responseData);
                                    break;
                                case "CurrentDateTime":
                                    navigateToCurrentDateTimeActivity(responseData);
                                    break;
                                case "MediaPlaylist":
                                    navigateToPlaylistActivity(responseData);
                                    break;
                                default:
                                    navigateToErrorActivity("No Layout Key", "Layout Key is not set, update screen and republish");
                            }
                        }
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

    private void navigateToMenuOverlayActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, MenuOverlayActivity.class);

        LayoutDataModel layout = responseData.getLayout();
        String subType = layout != null ? layout.getSubType() : null;

        // You can also pass data to the new activity using putExtra
        MenuDataModel menu = responseData.getMenu();
        MenuMetadata menuMetadata = new MenuMetadata(menu.getCurrency(), menu.getDescription(), menu.getTitle(), menu.getIconUrl(), subType);
        intent.putExtra("menuMetadata", menuMetadata);
        intent.putExtra("menuItems", menu.getMenuItems());
        intent.putExtra("mediaAsset", responseData.getMediaAsset());
        addLayoutPropertiesToIntentExtra(responseData.getLayout(), intent);

        // Start the new activity
        startIntentActivity(intent);
    }

    private void navigateToMenuOnlyActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, MenuOnlyActivity.class);

        LayoutDataModel layout = responseData.getLayout();
        String subType = layout != null ? layout.getSubType() : null;
        // You can also pass data to the new activity using putExtra
        MenuDataModel menu = responseData.getMenu();
        MenuMetadata menuMetadata = new MenuMetadata(menu.getCurrency(), menu.getDescription(), menu.getTitle(), menu.getIconUrl(), subType);
        intent.putExtra("menuMetadata", menuMetadata);
        intent.putExtra("menuItems", menu.getMenuItems());
        addLayoutPropertiesToIntentExtra(responseData.getLayout(), intent);

        // Start the new activity
        startIntentActivity(intent);
    }

    private void navigateToMediaOnlyActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, MediaOnlyActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("mediaAsset", responseData.getMediaAsset());
        intent.putExtra("externalMediaSource", responseData.getExternalMediaSource());

        // Start the new activity
        startIntentActivity(intent);
    }

    private void navigateToTextEditorActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, TextEditorActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("textEditorData", responseData.getTextEditorData());

        // Start the new activity
        startIntentActivity(intent);
    }

    private void navigateToCurrentDateTimeActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, CurrentDateTimeActivity.class);

        // You can also pass data to the new activity using putExtra
        LayoutDataModel layout = responseData.getLayout();
        String dateTimeFormat = layout != null ? layout.getSubType() : "EEE, d MMM yyyy HH:mm:ss";
        if(ObjectExtensions.isNullOrEmpty(dateTimeFormat)) dateTimeFormat = "EEE, d MMM yyyy HH:mm:ss";

        intent.putExtra("dateTimeFormat", dateTimeFormat);
        addLayoutPropertiesToIntentExtra(layout, intent);

        // Start the new activity
        startIntentActivity(intent);
    }

    private void addLayoutPropertiesToIntentExtra(LayoutDataModel layout, Intent intent) {
        if(layout == null) return;
        LayoutTemplatePropertyDataModel[] templateProperties = layout.getTemplateProperties();
        if(templateProperties != null && templateProperties.length > 0) {
            for (int i = 0; i < templateProperties.length; i++) {
                LayoutTemplatePropertyDataModel property = templateProperties[i];
                intent.putExtra(property.getKey(), property.getValue());
            }
        }
    }

    private void navigateToPlaylistActivity(ContentDataApiResponse responseData) {
        Intent intent = new Intent(this, PlaylistActivity.class);

        // You can also pass data to the new activity using putExtra
        PlaylistData playlistData = responseData.getPlaylistData();

        intent.putExtra("assetItems", playlistData.getItems());
        intent.putExtra("itemsSerialized", playlistData.getItemsSerialized());
        intent.putExtra("itemDuration", playlistData.getItemDuration());

        // Start the new activity
        startIntentActivity(intent);
    }

    private void startIntentActivity(Intent intent) {
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

                    startRun();

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
        boolean showDeviceName = true;
        switch (errorCode) {
            case "no_such_device":
                displayMsg = "No Such Device Found";
                showDeviceName = false;
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
            if (!ObjectExtensions.isNullOrEmpty(deviceName)) {
                deviceNameTextView.setText(deviceName);
            }
        } else {
            deviceNameTextView.setVisibility(View.INVISIBLE);
        }
    }
}