package com.example.utkarsh.smarty;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Smarty extends Application {
    private static Socket smarty;

    static {
        try {
            smarty = IO.socket("http://192.168.43.118:8090/smart-user");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket getSocket() {
        return smarty;
    }
}
