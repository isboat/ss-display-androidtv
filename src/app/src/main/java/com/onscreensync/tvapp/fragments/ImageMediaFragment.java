package com.onscreensync.tvapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.onscreensync.tvapp.R;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageMediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageMediaFragment extends Fragment {

    private ImageView mediaImageView;

    public ImageMediaFragment() {
        // Required empty public constructor
    }

    public static ImageMediaFragment newInstance(String param1, String param2) {
        ImageMediaFragment fragment = new ImageMediaFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_media, container, false);
        // Retrieve data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String assetUrl = bundle.getString("assetUrl");
            mediaImageView = (ImageView) view.findViewById(R.id.media_image_asset);
            if(mediaImageView != null) {
                Picasso.get().load(assetUrl).into(mediaImageView);
            }
        }

        return view;
    }
}