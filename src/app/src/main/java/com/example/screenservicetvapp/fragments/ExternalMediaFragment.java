package com.example.screenservicetvapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.screenservicetvapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExternalMediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExternalMediaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private WebView externalWebView;

    public ExternalMediaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExternalMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExternalMediaFragment newInstance(String param1, String param2) {
        ExternalMediaFragment fragment = new ExternalMediaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_external_media, container, false);

        // Retrieve data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String externalMediaSource = bundle.getString("externalMediaSource");
            externalWebView = (WebView) view.findViewById(R.id.external_media_web_view);
            externalWebView.setWebChromeClient(new WebChromeClient());

            WebSettings ws = externalWebView.getSettings();
            ws.setBuiltInZoomControls(true);
            ws.setJavaScriptEnabled(true);

            externalWebView.loadUrl(externalMediaSource);
        }

        return view;
    }
}