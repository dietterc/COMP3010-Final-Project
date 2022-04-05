package com.mygdx.game.utils;
import java.io.*;
import java.net.*;

import com.mygdx.game.Lobby;

public class ClientHandlerThread extends Thread {
    private Socket socket;
    private String myId;

    public ClientHandlerThread(Socket socket) {
        this.socket = socket;
        myId = "";
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
 
            String message = "";

            while (!message.equals("CLOSE")) {
                message = reader.readLine();
            
                if(message != null) {
                    if(message.startsWith("Inital Connect:")) {
                        myId = message.split(":")[1];
                    }
                    else {
                        Lobby.messageQueue.add(message);
                    }

                    writer.write("Echo: " + message);
                    writer.newLine();
                    writer.flush();
                }
                
 
            } 
            socket.close();
        } catch (Exception ex) {
            //System.out.println("Server exception: " + ex.getMessage());
            //ex.printStackTrace();
            Peer me = null;
            for(Peer p : Lobby.peer_list) {
                if(p.peer_id.equals(myId)) {
                    me = p;
                }
            }
            if(me != null) {
                Lobby.peer_list.remove(me);
                System.out.println("Removing " + me.peer_id + " from peer list.");

            }
        } 

    }

}
