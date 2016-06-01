package com.ran.capturewalk.capturewalk;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutUsFragment extends Fragment {
    private MainActivity mCtx;

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_us, container, false);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mCtx = (MainActivity) activity;
    }
}
