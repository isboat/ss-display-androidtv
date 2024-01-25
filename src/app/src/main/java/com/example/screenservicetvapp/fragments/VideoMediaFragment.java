package com.example.screenservicetvapp.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.screenservicetvapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoMediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoMediaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private VideoView mediaVideoView;

    public VideoMediaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoMediaFragment newInstance(String param1, String param2) {
        VideoMediaFragment fragment = new VideoMediaFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_media, container, false);

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Retrieve data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String assetUrl = bundle.getString("assetUrl");
            mediaVideoView = (VideoView) view.findViewById(R.id.media_video_asset_view);
            if(mediaVideoView != null) {

                // Set up a MediaController to enable play, pause, etc. controls
                MediaController mediaController = new MediaController(this.getContext());
                mediaController.setAnchorView(mediaVideoView);
                mediaVideoView.setMediaController(mediaController);

                //Video Loop
                mediaVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mediaVideoView.start(); //need to make transition seamless.
                    }
                });

                mediaVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });

                // Set the video URL using setVideoPath or setVideoURI
                mediaVideoView.setVideoPath(assetUrl);

                // Alternatively, you can use setVideoURI
                // Uri uri = Uri.parse(assetUrl);
                // mediaVideoView.setVideoURI(uri);

                // Start playing the video
                mediaVideoView.start();
            }
        }
        return view;
    }
}