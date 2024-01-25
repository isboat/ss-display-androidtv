package com.example.screenservicetvapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.screenservicetvapp.ObjectExtensions;
import com.example.screenservicetvapp.R;

public class TextADInformationFragment extends Fragment {

    private String backgroundColor;
    private String description;
    private String name;
    private String textColor;
    private String textFont;

    private TextView textEditorTextView;

    public TextADInformationFragment() {
        // Required empty public constructor
    }

    public static TextADInformationFragment newInstance(String param1, String param2) {
        TextADInformationFragment fragment = new TextADInformationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            description = bundle.getString("description");
            backgroundColor = bundle.getString("backgroundColor");
            name = bundle.getString("name");
            textColor = bundle.getString("textColor");
            textFont = bundle.getString("textFont");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_ad_information, container, false);

        if(ObjectExtensions.isNullOrEmpty(description)) description = "Error: No text found in the data, republish.";
        textEditorTextView = view.findViewById(R.id.text_ad_information_textview);
        textEditorTextView.setText(Html.fromHtml(description));
        return view;
    }
}