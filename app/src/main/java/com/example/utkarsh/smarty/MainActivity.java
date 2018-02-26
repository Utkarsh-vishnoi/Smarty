package com.example.utkarsh.smarty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private static String TAG = "MainActivity";
    private TabLayout tabs;
    private ViewPager container;
    private GifImageView loader;
    private static boolean snackbarFlag = false;
    private static boolean reSocketFlag = false;
    private static BroadcastReceiver networkBroadcastReceiver = new NetworkChangeReceiver();

    private View coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        coordinatorLayout = findViewById(R.id.main_content);
        registerReceiver(networkBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        IntentFilter intentFilter = new IntentFilter("com.example.utkarsh.smarty.network");
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean status = intent.getBooleanExtra("networkState", false);
                if (!status) {
                    snackbarFlag = true;
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else if (!reSocketFlag) {
                    reSocketFlag = true;
                    mSocket.connect();
                }
                if(snackbarFlag && status) {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.connected, Snackbar.LENGTH_INDEFINITE).setAction(R.string.snackbar_connected, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSocket.connect();
                        }
                    });
                    snackbar.show();
                }
            }
        }, intentFilter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabs = findViewById(R.id.tabs);
        container = findViewById(R.id.container);
        loader = findViewById(R.id.loader);
        Smarty.setUrl(sharedPreferences.getString("server-url", null));
        mSocket = Smarty.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("authenticated", authenticated);
        mSocket.on("No PI", noPI);
        mSocket.on("statusResponse", statusResponse);
    }

    @Override
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

                        }
                    });
                    dialog.show();
                }
            });
        }
    };

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
                    mSocket.disconnect();
                    loader.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(R.string.no_pi_err_title);
                    dialog.setMessage(R.string.no_pi_err_message);
                    dialog.setIcon(R.drawable.ic_error_message);
                    dialog.setPositiveButton(R.string.no_pi_err_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
            });
        }
    };

    private Emitter.Listener statusResponse = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tabs.setVisibility(View.VISIBLE);
                    container.setVisibility(View.VISIBLE);
                }
            });
            Log.d(TAG, "call: statusResponse");
            JSONObject data = (JSONObject) args[0];
            try {
                String temperature = data.getString("temperature");
                String humidity = data.getString("humidity");
                JSONObject lights = data.getJSONObject("lights");
                boolean light1 = Boolean.parseBoolean(lights.getString("1"));
                boolean light2 = Boolean.parseBoolean(lights.getString("2"));
                boolean light3 = Boolean.parseBoolean(lights.getString("3"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
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
    }
}
