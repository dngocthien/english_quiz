package com.tttv.thiendinh.breakroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DashboardFragment extends Fragment{
    private Button btn_dashboard_left,btn_dashboard_right;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_container,
                    new RecordFragment()).commit();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        btn_dashboard_left = rootView.findViewById(R.id.btn_dashboard_left);
        btn_dashboard_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_container,
                        new RecordFragment()).commit();
                btn_dashboard_left.setBackgroundResource(R.drawable.button_clicked_left_style);
                btn_dashboard_left.setTextColor(Color.parseColor("#ffffff"));
                btn_dashboard_right.setBackgroundResource(R.drawable.button_right_style);
                btn_dashboard_right.setTextColor(Color.parseColor("#33aaee"));
            }
        });
        btn_dashboard_right = rootView.findViewById(R.id.btn_dashboard_right);
        btn_dashboard_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_container,
                        new RankingFragment()).commit();
                btn_dashboard_left.setBackgroundResource(R.drawable.button_left_style);
                btn_dashboard_left.setTextColor(Color.parseColor("#33aaee"));
                btn_dashboard_right.setBackgroundResource(R.drawable.button_clicked_right_style);
                btn_dashboard_right.setTextColor(Color.parseColor("#ffffff"));
            }
        });
        return rootView;
    }
}
