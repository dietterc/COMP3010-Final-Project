/*
Thread for handling a client connection 
Gets messages and passes it to either the lobby or gameRoom 

Can also detect when a client disconnects 

*/

package com.mygdx.game.utils;
import java.io.*;
import java.net.*;

import com.mygdx.game.GameRoom;
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
                        if(Lobby.inLobby)
                            Lobby.messageQueue.add(message);
                        else
                            GameRoom.messageQueue_gameroom.add(message);
                    }

                    writer.write("Echo: " + message);
                    writer.newLine();
                    writer.flush();
                }
                
 
            } 
            socket.close();
        } catch (Exception ex) {
            //close the peer, do anything else needed

            Peer disconnectedPeer = null;
            if(Lobby.inLobby) {
                for(Peer p : Lobby.peer_list) {
                    if(p.peer_id.equals(myId)) {
                        disconnectedPeer = p;
                    }
                }
                if(disconnectedPeer != null) {
                    Lobby.peer_list.remove(disconnectedPeer);
                    System.out.println("Removing " + disconnectedPeer.peer_id + " from peer list.");

                    for(Peer p : Lobby.peer_list) {
                        p.sendMessage("messagetype:chooseNewHost," + Lobby.player_id + "," + Lobby.startTime);
                    }
                    disconnectedPeer.close();
                }
            }
            else {
                for(Peer p : GameRoom.peer_list) {
                    if(p.peer_id.equals(myId)) {
                        disconnectedPeer = p;
                    }
                }
                if(disconnectedPeer != null) {
                    GameRoom.peer_list.remove(disconnectedPeer);
                    System.out.println("Removing " + disconnectedPeer.peer_id + " from peer list.");

                    for(Peer p : GameRoom.peer_list) {
                        p.sendMessage("messagetype:chooseNewHost," + Lobby.player_id + "," + Lobby.startTime);
                    } 
                    disconnectedPeer.close();
                }
            }
        } 

    }

}
