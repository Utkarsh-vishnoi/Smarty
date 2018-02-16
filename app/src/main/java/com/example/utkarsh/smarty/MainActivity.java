package com.example.utkarsh.smarty;

import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements ConnectionErrorFragment.ConnectionErrorFragmentListener, NoPIFragment.NoPIFragmentListener {

    private Socket mSocket;
    private static String TAG = "MainActivity";
    private TabLayout tabs;
    private ViewPager container;
    private GifImageView loader;
    private NoPIFragment dialog;
    private boolean NoPIFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabs = findViewById(R.id.tabs);
        tabs.setVisibility(View.GONE);
        container = findViewById(R.id.container);
        container.setVisibility(View.GONE);
        loader = findViewById(R.id.loader);

        mSocket = Smarty.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("authenticated", authenticated);
        mSocket.on("No PI", noPI);
        mSocket.on("statusResponse", statusResponse);
        mSocket.connect();
        Toast.makeText(this, "connect initiated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "OnConnect Fired", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    Toast.makeText(MainActivity.this, "OnConnectError", Toast.LENGTH_SHORT).show();
                    mSocket.off(Socket.EVENT_CONNECT, onConnect);
                    mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                    mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
                    mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
                    loader.setVisibility(View.GONE);
                    DialogFragment dialog = new ConnectionErrorFragment();
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "Socket Connection Error");
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
                    dialog = new NoPIFragment();
                    dialog.setCancelable(false);
                    if(!dialog.show(getSupportFragmentManager())) {
                        NoPIFlag = true;
                    }
                }
            });
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(NoPIFlag) {
            dialog.show(getSupportFragmentManager());
        }
    }

    private Emitter.Listener statusResponse = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "call: statusResponse");
            JSONObject data = (JSONObject) args[0];
            try {
                String temperature = data.getString("temperature");
                String humidity = data.getString("humidity");
                JSONObject lights = data.getJSONObject("lights");
                boolean light1 = Boolean.parseBoolean(lights.getString("1"));
                boolean light2 = Boolean.parseBoolean(lights.getString("2"));
                boolean light3 = Boolean.parseBoolean(lights.getString("3"));
                loader.setVisibility(View.GONE);
                tabs.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("authenticated", authenticated);
        mSocket.on("No PI", noPI);
        mSocket.connect();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { finish(); }

    @Override
    public void onNoPIPositiveClick(DialogFragment dialog) {
        mSocket.connect();
    }

    @Override
    public void onNoPINegativeClick(DialogFragment dialog) {
        dialog.dismiss();
        finish();
    }
}
