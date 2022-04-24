/*
Peer object
Holds the connection to this peers server as well as 
any info needed for the peers

*/

package com.mygdx.game.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

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
    public int score;
    public long stolenTime;
    public long itTime;
    public ArrayList<Integer> scores;

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
            //e.printStackTrace();
            System.out.println("Error connectiong to peer " + peer_id);
        }

        playerInfo = null;
        isIt = false;
        score = 0;
        stolenTime = 0;
        itTime = 0;
        scores = new ArrayList<Integer>();

    }

    //send a message to the peer
    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        }
        catch (IOException e) {
            System.out.println("Error sending message: " + message);
        }
        
    }

    public void initGameVars(OtherPlayer playerInfo) {
        this.playerInfo = playerInfo;

    }

    public void close() {
        try {
            if(playerInfo != null) {
                playerInfo.dispose();
            }
            socket.close();
        }
        catch(Exception e) {
            System.out.println("Error closing peer " + peer_id);
        }
        
    }
    
}
