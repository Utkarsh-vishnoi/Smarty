package com.example.utkarsh.smarty;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.suke.widget.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import io.socket.client.Socket;

public class LightsFragment extends Fragment {
    private boolean lights[];
    public static int backflipFlag = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lights, container, false);
        if(getArguments() != null) {
            lights = getArguments().getBooleanArray("lights");
        }
        assert lights != null;
        ListView switchView = rootView.findViewById(R.id.lightView);
        ArrayList<LightsData> lightList = new ArrayList<>();
        int index = 1;
        for (boolean light:lights) {
            lightList.add(new LightsData("Light " + index++, light));
        }
        lightsAdapter lightAdapter = new lightsAdapter(getContext(), lightList);
        switchView.setAdapter(lightAdapter);
        switchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SwitchButton sb = view.findViewById(R.id.switchButton);
                sb.toggle();
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onMessage(boolean[] lights) {
        backflipFlag = 1;
        int index = 0;
        ListView lightView = getView().findViewById(R.id.lightView);
        for(int i = 0; i < lights.length; i++) {
            SwitchButton sb = lightView.getChildAt(i).findViewById(R.id.switchButton);
            if(lights[i]) {
                if (!sb.isChecked()) {
                    sb.setChecked(true);
                }
            }
            else {
                if (sb.isChecked()) {
                    sb.setChecked(false);
                }
            }
        }
    }

    public static void processEvent(int position, boolean isChecked) {
        if(backflipFlag == 1) {
            backflipFlag = 0;
        }
        else {
            Socket sock = Smarty.getSocket();
            sock.emit("switch", position, isChecked);
        }
    }
}

class lightsAdapter extends ArrayAdapter<LightsData> {

    lightsAdapter(@NonNull Context context, @NonNull ArrayList<LightsData> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LightsData lightData = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.switches_list_item, null);
        }
        TextView lightLabel = convertView.findViewById(R.id.switchLabel);
        SwitchButton sb = convertView.findViewById(R.id.switchButton);
        assert lightData != null;
        if(lightData.lightState) {
            sb.setChecked(true);
        }
        else {
            sb.setChecked(false);
        }
        sb.setTag(position + 2);
        sb.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                int position = (Integer) view.getTag();
                LightsFragment.processEvent(position, isChecked);
            }
        });
        lightLabel.setText(lightData.lightName);
        return convertView;
    }
}
