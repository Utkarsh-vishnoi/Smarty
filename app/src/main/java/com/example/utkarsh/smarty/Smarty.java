package com.example.utkarsh.smarty;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Smarty extends Application {
    private static String url;
    private static Socket smarty;

    public static void setUrl(String url) {
        Smarty.url = url;
    }

    public static String getUrl() {
        return url;
    }

    public static void initiate() {
        try {
            smarty = IO.socket(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket getSocket() {
        return smarty;
    }
}
