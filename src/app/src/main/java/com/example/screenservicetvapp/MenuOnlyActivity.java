package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.screenservicetvapp.fragments.BasicMenuFragment;
import com.example.screenservicetvapp.fragments.PremiumMenuFragment;

public class MenuOnlyActivity extends AppCompatActivity {


    private ContentDataMenuItem[] menuItems;
    private MenuMetadata menuMetadata;
    private String textFont;
    private String textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_only);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        menuMetadata = intent.getParcelableExtra("menuMetadata");
        textColor = intent.getStringExtra("textColor");
        textFont = intent.getStringExtra("textFont");
        menuItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "menuItems", ContentDataMenuItem.class);

        String subType = menuMetadata.getSubType();
        switch (subType) {
            case "Premium":
                loadMenuFragment(new PremiumMenuFragment());
                break;
            case "Deluxe":
            case "Basic":
            default:
                loadMenuFragment(new BasicMenuFragment());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    private void loadMenuFragment(Fragment fragment) {
        if(menuMetadata == null || menuItems == null) return;

        Bundle bundle = new Bundle();
        bundle.putParcelable("menuMetadata", menuMetadata);
        bundle.putParcelableArray("menuItems", menuItems);
        bundle.putString("textColor", textColor);
        bundle.putString("textFont", textFont);
        bundle.putBoolean("setTransparentBackground", true);
        loadFragment(fragment, bundle, R.id.menu_only_activity_menu_relativeLayout);
    }

    private void loadFragment(Fragment fragment, Bundle bundle, int elementId) {
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(elementId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}