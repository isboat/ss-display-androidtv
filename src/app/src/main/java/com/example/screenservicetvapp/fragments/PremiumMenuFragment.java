package com.example.screenservicetvapp.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.screenservicetvapp.ContentDataMenuItem;
import com.example.screenservicetvapp.MenuMetadata;
import com.example.screenservicetvapp.ObjectExtensions;
import com.example.screenservicetvapp.R;
import com.example.screenservicetvapp.UiHelper;
import com.squareup.picasso.Picasso;

public class PremiumMenuFragment extends Fragment {

    private MenuMetadata menuMetadata;
    private String textFont;
    private String textColor;
    private ContentDataMenuItem[] menuItems;

    TableLayout tableLayout;
    ImageView menuTopIconImageView;

    private boolean setTransparentBackground;
    private String backgroundOpacity;
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();

    public PremiumMenuFragment() {
        // Required empty public constructor
    }

    public static PremiumMenuFragment newInstance() {
        PremiumMenuFragment fragment = new PremiumMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            textFont = bundle.getString("textFont");
            textColor = bundle.getString("textColor");
            setTransparentBackground = bundle.getBoolean("setTransparentBackground", false);
            backgroundOpacity = bundle.getString("backgroundOpacity");

            menuMetadata = bundle.getParcelable("menuMetadata");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                menuItems = bundle.getParcelableArray("menuItems", ContentDataMenuItem.class);
            } else {
                menuItems = (ContentDataMenuItem[]) bundle.getParcelableArray("menuItems");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_premium_menu, container, false);

        menuTopIconImageView = view.findViewById(R.id.premium_menu_fragment_menu_icon_image_view);
        tableLayout = view.findViewById(R.id.premium_menu_fragment_table_layout);

        if(menuItems != null) {
            if(menuItems.length == 0) {

            } else {
                createMenu();
            }
        }

        if(setTransparentBackground && !ObjectExtensions.isNullOrEmpty(backgroundOpacity)) {
            int opacityInt = ObjectExtensions.convertToInt(backgroundOpacity);
            if(opacityInt > 0) {
                view.getBackground().setAlpha(opacityInt + 150);
            } else {
                if(opacityInt == 0) view.getBackground().setAlpha(0);
            }
        }

        return view;
    }
    private void createMenu() {
        boolean displayMenuIcon = !ObjectExtensions.isNullOrEmpty(menuMetadata.getIconUrl());
        if(displayMenuIcon)
        {
            Picasso.get().load(menuMetadata.getIconUrl()).into(menuTopIconImageView);
        } else {
            menuTopIconImageView.setVisibility(View.GONE);
        }

        for (ContentDataMenuItem obj : menuItems) {
            TableRow tableRow = new TableRow(this.getContext());

            // Create TextViews for each field
            ImageView imageView = createItemIcon(obj.getIconUrl());

            TextView nameTextView = createTextView(obj.getName());
            UiHelper.setTextViewFont(nameTextView, textFont);
            UiHelper.setTextViewColor(nameTextView, textColor);
            nameTextView.setBackgroundResource(R.drawable.premium_border);

            TextView priceTextView = createPriceTextView(obj.getPrice(), obj.getDiscountPrice());
            UiHelper.setTextViewFont(priceTextView, textFont);
            UiHelper.setTextViewColor(priceTextView, textColor);

            TextView descTextView = createTextView(obj.getDescription());
            UiHelper.setTextViewFont(descTextView, textFont);
            UiHelper.setTextViewColor(descTextView, textColor);
            descTextView.setBackgroundResource(R.drawable.premium_border);

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
        ImageView imageView = new ImageView(this.getContext());

        if(!ObjectExtensions.isNullOrEmpty(iconUrl)) Picasso.get().load(iconUrl).into(imageView);
        TableRow.LayoutParams params = new TableRow.LayoutParams(150, 150);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setPadding(16,16,16,16);

        return imageView;
    }

    private TextView createPriceTextView(String price, String discountPrice) {
        TextView textView;
        String currency = menuMetadata.getCurrency();
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
        TextView textView = new TextView(this.getContext());
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}