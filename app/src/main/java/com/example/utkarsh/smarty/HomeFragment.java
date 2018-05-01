package com.example.utkarsh.smarty;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    String key;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        if(getArguments() != null) {
            key = getArguments().getString("error");
            TextView textView = rootView.findViewById(R.id.textView);
            switch (key) {
                case "no_network":
                    textView.setText(R.string.error_no_network);
                    break;
                case "no_pi":
                    textView.setText(R.string.error_no_pi);
            }
        }
        return rootView;
    }
}
