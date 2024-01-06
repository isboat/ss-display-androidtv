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
    private String textColor;
    private String textFont;
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
        textColor = intent.getStringExtra("textColor");
        textFont = intent.getStringExtra("textFont");
        menuItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "menuItems", ContentDataMenuItem.class);

        if(menuItems != null) {
            if(menuItems.length == 0) {

            } else {
                createMenu();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
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
            TextView nameTextView = createTextView(obj.getName());
            UiHelper.setTextViewFont(nameTextView, textFont);
            UiHelper.setTextViewColor(nameTextView, textColor);

            TextView priceTextView = createPriceTextView(obj.getPrice(), obj.getDiscountPrice());
            UiHelper.setTextViewFont(priceTextView, textFont);
            UiHelper.setTextViewColor(priceTextView, textColor);

            TextView descTextView = createTextView(obj.getDescription());
            UiHelper.setTextViewFont(descTextView, textFont);
            UiHelper.setTextViewColor(descTextView, textColor);

            // Add TextViews to the TableRow
            tableRow.addView(imageView);
            tableRow.addView(nameTextView);
            tableRow.addView(priceTextView);
            tableRow.addView(descTextView);

            // Add TableRow to the TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private ImageView createItemIcon(String iconUrl) {
        ImageView imageView = new ImageView(this);

        if(!ObjectExtensions.isNullOrEmpty(iconUrl)) Picasso.get().load(iconUrl).into(imageView);
        TableRow.LayoutParams params = new TableRow.LayoutParams(150, 150);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setPadding(16,16,16,16);

        return imageView;
    }

    private TextView createPriceTextView(String price, String discountPrice) {
        TextView textView;
        if(ObjectExtensions.isNullOrEmpty(discountPrice)) {
            textView = createTextView(currency+price);
            return textView;
        }

        String displayText = currency + price + " " + currency + discountPrice;

        textView = createTextView(displayText);
        textView.setText(displayText, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) textView.getText();
        spannable.setSpan(STRIKE_THROUGH_SPAN, 0, price.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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