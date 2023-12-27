package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MenuOnlyActivity extends AppCompatActivity {

    private String currency;
    private String description;
    private String title;
    private String iconUrl;
    private ContentDataMenuItem[] menuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_only);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        currency = intent.getStringExtra("currency");
        description = intent.getStringExtra("description");
        title = intent.getStringExtra("title");
        iconUrl = intent.getStringExtra("iconUrl");
        menuItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "menuItems", ContentDataMenuItem.class);

        if(menuItems != null) {
            if(menuItems.length == 0) {

            } else {
                createMenu();
            }
        }
    }

    private void createMenu() {

    }
}