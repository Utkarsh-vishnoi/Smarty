package com.example.utkarsh.smarty;

import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private ViewPager vpPager;
    private static String TAG = "MainActivity";
    private GifImageView loader;
    private static boolean snackbarFlag = false;
    private static BroadcastReceiver networkBroadcastReceiver = new NetworkChangeReceiver();
    private SharedPreferences sharedPreferences;
    FragmentPagerAdapter adapterViewPager;

    private JSONArray temperatures = new JSONArray();
    private JSONArray humidities = new JSONArray();
    private JSONArray timestamps = new JSONArray();
    private JSONArray timeDifference = new JSONArray();

    private String lastUpdated;

    private View coordinatorLayout;
    private boolean[] lights = new boolean[3];

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        setupBottomNavigation();

        coordinatorLayout = findViewById(R.id.main_content);
        registerReceiver(networkBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        IntentFilter intentFilter = new IntentFilter(".network");
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean status = intent.getBooleanExtra("networkState", false);
                if (!status) {
                    snackbarFlag = true;
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else {
                    mSocket.connect();
                }

                if(snackbarFlag && status) {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.connected, Snackbar.LENGTH_INDEFINITE).setAction(R.string.snackbar_connected, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loader.setVisibility(View.VISIBLE);
                            mSocket.connect();
                        }
                    });
                    snackbar.show();
                }
            }
        }, intentFilter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        vpPager = findViewById(R.id.fragment_frame);
        adapterViewPager = new LayoutAdapter(getSupportFragmentManager());
//        vpPager.setAdapter(adapterViewPager);

        loader = findViewById(R.id.loader);
        Smarty.setUrl(sharedPreferences.getString("server-url", null));
        Smarty.initiate();
        mSocket = Smarty.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("authenticated", authenticated);
        mSocket.on("No PI", noPI);
        mSocket.on("statusResponse", statusResponse);
        mSocket.on("switch_backflip", onBackFlip);
        Bundle bundle = new Bundle();
        bundle.putString("error", "");
        loadHomeFragment(bundle);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.action_temperature:
                        vpPager.setCurrentItem(0);
                        return true;
                    case R.id.action_humidity:
                        vpPager.setCurrentItem(1);
                        return true;
                    case R.id.action_lights:
                        vpPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loader.setVisibility(View.VISIBLE);
                }
            });
            try {
                mSocket.emit("authenticate", new JSONObject("{\"identifier\":\"#5521SHCBUV\"}").toString());
            } catch (JSONException e) {
                Log.e("MainActivity", "Unexpected JSON exception", e);
            }
        }
    };

    private  Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: onDisconnect");
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private  Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: onConnectError");
                    mSocket.off(Socket.EVENT_CONNECT, onConnect);
                    mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                    mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
                    mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
                    loader.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(R.string.conn_err_title);
                    dialog.setMessage(R.string.conn_err_message);
                    dialog.setIcon(R.drawable.ic_error_message);
                    dialog.setPositiveButton(R.string.conn_err_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle bundle = new Bundle();
                            bundle.putString("error", "no_network");
                            loadHomeFragment(bundle);
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
        }
    };

    private void loadHomeFragment(Bundle bundle) {
        Fragment fragment = new HomeFragment();
        fragment.setArguments(bundle);
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, fragment);
        fragmentTransaction.commit();
    }

    private Emitter.Listener authenticated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mSocket.emit("requestStatus");
        }
    };

    private Emitter.Listener noPI = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "call: noPI");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loader.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(R.string.no_pi_err_title);
                    dialog.setMessage(R.string.no_pi_err_message);
                    dialog.setIcon(R.drawable.ic_error_message);
                    dialog.setPositiveButton(R.string.no_pi_err_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle bundle = new Bundle();
                            bundle.putString("error", "no_pi");
                            loadHomeFragment(bundle);
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
        }
    };

    private Emitter.Listener statusResponse = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "call: statusResponse");
            JSONObject data = (JSONObject) args[0];
            try {
                JSONObject lightsObj = data.getJSONObject("lights");
                JSONObject graph = data.getJSONObject("graph");
                temperatures = graph.getJSONArray("temperature");
                humidities = graph.getJSONArray("humidity");
                JSONObject time = graph.getJSONObject("time");
                timestamps = time.getJSONArray("unix");
                timeDifference = time.getJSONArray("timeDifference");
                lastUpdated = time.getString("lastUpdated");
                lights[0] = lightsObj.getBoolean("1");
                lights[1] = lightsObj.getBoolean("2");
                lights[2] = lightsObj.getBoolean("3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    vpPager.setCurrentItem(0);
                    vpPager.setAdapter(adapterViewPager);
                    loader.setVisibility(View.GONE);
                }
            });
        }
    };

    private Emitter.Listener onBackFlip = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        lights[0] = data.getBoolean("1");
                        lights[1] = data.getBoolean("2");
                        lights[2] = data.getBoolean("3");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    EventBus.getDefault().post(lights);
                }
            });
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        String url = sharedPreferences.getString("server-url", null);
        assert url != null;
        if(!url.equals(Smarty.getUrl()) ){
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            Smarty.setUrl(url);
            Socket socket = Smarty.getSocket();
            socket.disconnect();
            socket.connect();
        }
    }

    public class LayoutAdapter extends FragmentPagerAdapter {

        LayoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle b = new Bundle();
            switch (position) {
                case 0:
                    b.putString("temperatures", temperatures.toString());
                    b.putString("timeDifference", timeDifference.toString());
                    b.putString("timestamps", timestamps.toString());
                    b.putString("lastUpdated", lastUpdated);
                    break;
                case 1:
                    b.putString("humidities", humidities.toString());
                    b.putString("timeDifference", timeDifference.toString());
                    b.putString("timestamps", timestamps.toString());
                    b.putString("lastUpdated", lastUpdated);
                    break;
                case 2:
                    b.putBooleanArray("lights", lights);
                    break;
            }
            switch (position) {
                case 0:
                    fragment = new TemperatureFragment();
                    fragment.setArguments(b);
                    return fragment;
                case 1:
                    fragment = new HumidityFragment();
                    fragment.setArguments(b);
                    return fragment;
                case 2:
                    fragment = new LightsFragment();
                    fragment.setArguments(b);
                    return fragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
