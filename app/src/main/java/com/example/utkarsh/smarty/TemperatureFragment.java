package com.example.utkarsh.smarty;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.ThingSpeakLineChart;

import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class TemperatureFragment extends Fragment {

    private double temperature;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);
        final LineChartView chartView = rootView.findViewById(R.id.chart);
        ThingSpeakChannel channel = new ThingSpeakChannel(1, "C5YOQWCLT26M6WF2");
        ThingSpeakLineChart tsChart = new ThingSpeakLineChart(1, 1);
        tsChart.setListener(new ThingSpeakLineChart.ChartDataUpdateListener() {
            @Override
            public void onChartDataUpdated(long channelId, int fieldId, String title, LineChartData lineChartData, Viewport maxViewport, Viewport initialViewport) {
                chartView.setLineChartData(lineChartData);
                chartView.setMaximumViewport(maxViewport);
                chartView.setCurrentViewport(initialViewport);
            }
        });
        tsChart.loadChartData();
        return rootView;
    }
}
