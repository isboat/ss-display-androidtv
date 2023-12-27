package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MenuOnlyActivity extends AppCompatActivity {

    private String currency;
    private String description;
    private String title;
    private String iconUrl;
    private ContentDataMenuItem[] menuItems;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_only);

        tableLayout = findViewById(R.id.menu_only_activity_table_layout);

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
        for (ContentDataMenuItem obj : menuItems) {
            TableRow tableRow = new TableRow(this);

            // Create TextViews for each field
            TextView textView1 = createTextView(obj.getName());
            TextView textView2 = createTextView(obj.getPrice());
            TextView textView3 = createTextView(obj.getDescription());
            TextView textView4 = createTextView(obj.getDiscountPrice());

            // Add TextViews to the TableRow
            tableRow.addView(textView1);
            tableRow.addView(textView2);
            tableRow.addView(textView3);
            tableRow.addView(textView4);

            // Add TableRow to the TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        //textView.setGravity(Gravity.CENTER);
        return textView;
    }
}