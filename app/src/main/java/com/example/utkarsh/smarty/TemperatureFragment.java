package com.example.utkarsh.smarty;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TemperatureFragment extends Fragment {

    private final SimpleDateFormat s = new SimpleDateFormat("dd-MM HH:mm", Locale.US);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);
        LineChart lc = rootView.findViewById(R.id.chart);
        if (getArguments() != null) {
            String temperatures = getArguments().getString("temperatures");
            String timestamps = getArguments().getString("timestamps");
            String timeDifference = getArguments().getString("timeDifference");
            String lastUpdated = getArguments().getString("lastUpdated");
            try {
                JSONArray temperatureArray = new JSONArray(temperatures);
                JSONArray unixTimeArray = new JSONArray(timestamps);
                final int baseStamp = unixTimeArray.getInt(0);
                JSONArray timeDifferenceArray = new JSONArray(timeDifference);
                List<Entry> entries = new ArrayList<>();
                DecimalFormat df = new DecimalFormat("#.####");
                for (int i = 0; i < temperatureArray.length(); i++) {
                    entries.add(new Entry(timeDifferenceArray.getInt(i), Float.parseFloat(df.format(temperatureArray.getDouble(i)))));
                }
                LineDataSet dataSet = new LineDataSet(entries, "Temperature");
                dataSet.setColor(Color.RED);
                LineData lineData = new LineData(dataSet);
                XAxis timeAxis = lc.getXAxis();
                timeAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                timeAxis.setLabelRotationAngle(-45);
                timeAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        int stamp = baseStamp + (int)value;
                        Date d = new Date(stamp * 1000L);
                        return s.format(d);
                    }
                });
                lc.setData(lineData);
                lc.setScaleEnabled(false);
                lc.setHighlightPerTapEnabled(false);
                lc.setHighlightPerDragEnabled(false);
                lc.setGridBackgroundColor(Color.rgb(221, 221, 221));
                Description desc = new Description();
                desc.setText("Last Updated: " + lastUpdated);
                lc.setDescription(desc);
                lc.invalidate();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rootView;
    }
}
