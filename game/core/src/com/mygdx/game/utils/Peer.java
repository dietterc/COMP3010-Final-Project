package com.mygdx.game.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.OtherPlayer;


public class Peer {

    public String ip;
    public String peer_id;
    public String name;
    public int port;
    public boolean ready;
    public boolean isHost;

    private Socket socket;
    private BufferedWriter out;

    //Game room stuff
    public OtherPlayer playerInfo;
    public boolean isIt;

    public Peer(String ip, String peer_id, int port, String serverId, String name) {
        this.ip = ip;
        this.peer_id = peer_id;
        this.name = name;
        this.port = port;

        ready = false;
        isHost = false;

        try {
            socket = new Socket(ip, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write("Inital Connect:" + serverId);
            out.newLine();
            out.flush();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        playerInfo = null;
        isIt = false;

    }

    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void initGameVars(OtherPlayer playerInfo) {
        this.playerInfo = playerInfo;

    }

    public void close() {
        try {
            socket.close();
        }
        catch(Exception e) {

        }
        
    }
    
}
