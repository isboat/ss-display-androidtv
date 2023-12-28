package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MenuOnlyActivity extends AppCompatActivity {

    private String currency;
    private String description;
    private String title;
    private String iconUrl;
    private ContentDataMenuItem[] menuItems;
    TableLayout tableLayout;

    ImageView menuTopIconImageView;
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_only);
        menuTopIconImageView = findViewById(R.id.menu_only_activity_menu_icon_image_view);

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
        boolean displayMenuIcon = !ObjectExtensions.isNullOrEmpty(iconUrl);
        if(displayMenuIcon)
        {
            Picasso.get().load(iconUrl).into(menuTopIconImageView);
        } else {
            menuTopIconImageView.setVisibility(View.GONE);
        }

        for (ContentDataMenuItem obj : menuItems) {
            TableRow tableRow = new TableRow(this);

            // Create TextViews for each field
            ImageView imageView = createItemIcon(obj.getIconUrl());
            TextView textView1 = createTextView(obj.getName());
            TextView textView2 = createPriceTextView(obj.getPrice(), obj.getDiscountPrice());
            TextView textView3 = createTextView(obj.getDescription());

            // Add TextViews to the TableRow
            tableRow.addView(imageView);
            tableRow.addView(textView1);
            tableRow.addView(textView2);
            tableRow.addView(textView3);

            // Add TableRow to the TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private ImageView createItemIcon(String iconUrl) {
        ImageView imageView = new ImageView(this);

        Picasso.get().load(iconUrl).into(imageView);
        TableRow.LayoutParams params = new TableRow.LayoutParams(150, 150);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setPadding(16,16,16,16);

        return imageView;
    }

    private TextView createPriceTextView(String price, String discountPrice) {
        TextView textView;
        if(ObjectExtensions.isNullOrEmpty(discountPrice)) {
            textView = createTextView(price);
            return textView;
        }

        String displayText = price + " " + discountPrice;

        textView = createTextView(displayText);
        textView.setText(displayText, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) textView.getText();
        spannable.setSpan(STRIKE_THROUGH_SPAN, 0, price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return textView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}