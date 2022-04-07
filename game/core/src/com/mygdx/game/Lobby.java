package com.mygdx.game;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.ClientHandlerThread;
import com.mygdx.game.utils.Peer;
import com.mygdx.game.utils.PeerInfo;


public class Lobby implements Screen {
    
    final ProjectMain game;
    final int startingPort = 25565;
    final int COUNTDOWN = 6000;
    final double xSpawnPoints[] = {0.25,1.5,2.75,4.25,5.50,6.85,8.25,16.25,17.5,18.9,20.25,21.5,22.9,24.25};
    final double ySpawnPoint = -15.25;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private BitmapFont smallFont;
    private BitmapFont statusFont;

    public static ArrayList<Peer> peer_list;

    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int activePort = startingPort;
    public static ConcurrentLinkedQueue<String> messageQueue;
    public static boolean inLobby;
    private boolean ready;
    private long startTime;
    public static boolean isHost;

    public static String username;
    public static String player_id;

    public static double startingX;
    public static double startingY;

    private GameRoom gm = null;

    //startup
    long countdownTarget;
    boolean countingDown;

    public Lobby(final ProjectMain game, String given_username, String given_player_id) {
        this.game = game;
        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        smallFont = new BitmapFont(Gdx.files.internal("fonts/small.fnt"),
            Gdx.files.internal("fonts/small.png"), false);
        statusFont = new BitmapFont(Gdx.files.internal("fonts/status.fnt"),
            Gdx.files.internal("fonts/status.png"), false);

        peer_list = new ArrayList<Peer>();

        username = given_username;
        player_id = given_player_id;
        ready = false;
        startTime = System.currentTimeMillis();
        isHost = false;

        messageQueue = new ConcurrentLinkedQueue<String>();
        inLobby = true;

        tcpSetup();
        mdnsSetup();

        countdownTarget = 0;
        countingDown = false;

        startingX = 0;
        startingY = 0;
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1);

        //HUD
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
		game.batch.begin();

        int yIndex = 375;
        hudFont.draw(game.batch, "Connected Peers:", -700, yIndex);
        for(Peer peer : peer_list) {
            yIndex -= 110;

            String name = peer.name;
            if(peer.isHost) 
                name += " (Host)";
            hudFont.draw(game.batch, name, -700, yIndex);
            smallFont.draw(game.batch, peer.ip, -700, yIndex - 40);
            smallFont.draw(game.batch, peer.peer_id, -700, yIndex - 60);
            if(peer.ready) {
                statusFont.setColor(Color.GREEN);
                statusFont.draw(game.batch, "Ready", -700, yIndex - 82);
            }
            else {
                statusFont.setColor(Color.RED);
                statusFont.draw(game.batch, "Not Ready", -700, yIndex - 82);
            }
        }

        //https://stackoverflow.com/questions/2255500/can-i-multiply-strings-in-java-to-repeat-sequences
        String dots = new String(new char[(int)(System.currentTimeMillis() / 1000) % 4]).replace("\0", ".");
        smallFont.draw(game.batch, "Searching" + dots, -700, 325);

        hudFont.draw(game.batch, "Lobby", 0, 0);
        smallFont.draw(game.batch, ("Me: " + username + ", " + player_id), 0, -50);
        if(ready) {
            statusFont.setColor(Color.GREEN);
            statusFont.draw(game.batch, "Ready", 0, -80);
        }
        else {
            statusFont.setColor(Color.RED);
            statusFont.draw(game.batch, "Not Ready", 0, -80);
        }
        if(isHost)
            smallFont.draw(game.batch, "You are the host.", 0, -110);


        //countdown timer
        if(countingDown) {
            int time = (int)(countdownTarget - System.currentTimeMillis()) / 1000;

            if(time >= 0) {
                hudFont.draw(game.batch, "Starting in: " + time, -100, -200);
            }
            else {
                hudFont.draw(game.batch, "Starting game...", -100, -200);
                //move to game room here
                inLobby = false;
                game.setScreen(new GameRoom(game));
            }
        }

        //test info
        smallFont.draw(game.batch, "debug info", -50, 375);
        smallFont.draw(game.batch, "X: " + startingX, -50, 350);
        smallFont.draw(game.batch, "Y: " + startingY, -50, 325);

		game.batch.end();


        if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            //game.setScreen(new GameRoom(game));
            ready = !ready;
            sendAllMessage("messagetype:status," + ready + "," + player_id);
            checkReadyStatus();

            if(!ready && countingDown) {
                countingDown = false;
                sendAllMessage("messagetype:abortCountdown");
            }
        }

        if(!messageQueue.isEmpty()) {
            //process one message from the queue per step
            //might need to change this
            String message = messageQueue.remove();
            handleMessage(message);
        }

        if(isHost && peer_list.size() == 0) {
            isHost = false;
        }

    }

    void sendAllMessage(String message) {
        for(Peer p : peer_list) {
            p.sendMessage(message);
        }
    }

    void handleMessage(String message) {
        //System.out.println("Message: " + message);
        try {

            String lines[] = message.split(",");
            String type = lines[0].split(":")[1];

            if(type.equals("text")) {
                System.out.println("Message: " + lines[1]);
            }
            else if(type.equals("status")) {
                String who = lines[2];
                boolean status = Boolean.parseBoolean(lines[1]);

                for(Peer p : peer_list) {
                    if(p.peer_id.equals(who)) {
                        p.ready = status;
                    }
                }

                checkReadyStatus();
            }
            else if(type.equals("chooseHost")) {
                String who = lines[2];
                long time = Long.parseLong(lines[1]);

                for(Peer p : peer_list) {
                    if(p.peer_id.equals(who)) {
                        if(time < startTime) {
                            //they are the host
                            p.sendMessage("messagetype:setHost," + who);
                            p.isHost = true;
                        }
                        else {
                            p.sendMessage("messagetype:setHost," + player_id);
                            isHost = true;
                        }
                    }
                }
            }
            else if(type.equals("setHost")) {
                String who = lines[1];

                if(who.equals(player_id)) {
                    isHost = true;
                }
                else {
                    for(Peer p : peer_list) {
                        if(p.peer_id.equals(who)) {
                            p.isHost = true;  
                        }
                    }
                }
            } 
            else if(type.equals("startCountdown")) {
                System.out.println("startCountDown");
                startCountdown(Long.parseLong(lines[1]));
            }
            else if(type.equals("verifyLevel")) {
                int myHash = calculateHash();

                if(myHash != Integer.parseInt(lines[1])) {
                    //abort starting countdown
                    ready = false;
                    sendAllMessage("messagetype:abortCountdown");
                }
            }
            else if(type.equals("abortCountdown")) {
                System.out.println("ABORT");
                ready = false;
                countingDown = false;
                sendAllMessage("messagetype:status," + ready + "," + player_id);
            }//startingInfo
            else if(type.equals("startingInfo")) {
                //x, y, it
                setStartingData(Double.parseDouble(lines[1]), Double.parseDouble(lines[2]));
            }
            else if(type.equals("checkIfConnected")) {
                //check if we are connected to this peer (or if its us)
                //if its not, connect to it
                //this allows all peers to connect to everyone as long as someone discovers at least 1 peer
                String who = lines[1];
                int port = Integer.parseInt(lines[2]);
                String userN = lines[3];
                String ip = lines[4];

                boolean found = false;
                for(Peer p:peer_list) {
                    if(p.peer_id.equals(who)) {
                        found = true;
                    }
                }

                if(!found && !who.equals(player_id)) {
                    PeerInfo newPeer = new PeerInfo(ip, who, port, userN); 
                    
                    System.out.println("Client " + player_id + " discovered: " + newPeer.peer_id);
                    connectToPeer(newPeer);

                }
            }


        }
        catch(Exception e) {
            System.out.println("Invalid message:\n" + message);
        }
        
    }

    //needs to be fixed so its not platform dependent
    private int calculateHash() {
        //simple hash function (sum the chars)
        String file = Gdx.files.local("map.tmx").readString();
        int sum = 0;
        for(int i=0;i<file.length();i++) {
            sum += 1;
        }
        return 5;
    }

    private void checkReadyStatus() {

        int score = 0;
        boolean hostFound = false;
        for(Peer p : peer_list) {
            if(p.ready)
                score++;

            if(p.isHost)
                hostFound = true;
        }
        if(isHost)
            hostFound = true;

        if(score == peer_list.size() && ready && hostFound) {
            System.out.println("All peers ready!");

            if(score > -1) {
                long time = System.currentTimeMillis() + COUNTDOWN;
                sendAllMessage("messagetype:startCountdown," + time);
                
                startCountdown(time);
            }   
        }
    }

    private void startCountdown(long time) {
        countdownTarget = time;
        countingDown = true;

        //send level data to peers to verify it matches everyone
        int levelHash = calculateHash();
        sendAllMessage("messagetype:verifyLevel," + levelHash);

        //host duties
        if(isHost) {

            double[][] spawnPoints = new double[peer_list.size()+1][2]; 
            List<Double> xPoints = new ArrayList<Double>();
            for(int i=0;i<xSpawnPoints.length;i++) {
                xPoints.add(xSpawnPoints[i]);
            }
            Collections.shuffle(xPoints);

            for(int i=0;i<peer_list.size()+1;i++) {
                spawnPoints[i][0] = xPoints.get(i);
                spawnPoints[i][1] = ySpawnPoint;
            }

            for(int i=0;i<peer_list.size();i++) {
                peer_list.get(i).sendMessage("messagetype:startingInfo," + spawnPoints[i][0] + "," + spawnPoints[i][1]);
            }
            setStartingData(spawnPoints[peer_list.size()][0],spawnPoints[peer_list.size()][1]);
        }

    }

    void setStartingData(double x, double y) {
        startingX = x;
        startingY = y;
    }

    private void tcpSetup() {

        //start a tcp server on a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
               
                try {
                    boolean portFound = false;

                    while(!portFound) {
                        try {
                            serverSocket = new ServerSocket(activePort);
                            portFound = true;
                        }
                        catch(BindException e) {
                            activePort += 1;
                        }
                    }
                    while(true) {
                        clientSocket = serverSocket.accept();
                        new ClientHandlerThread(clientSocket).start();
                    }
                }
                catch(Exception e) {
                    System.out.println("Disconnecting from peers");
                }
            }
         }).start();

    }

    private void mdnsSetup() {
        
        //set up listener/listener class for incoming connections
        class mDNSListener implements ServiceListener {
    
            @Override
            public void serviceAdded(ServiceEvent event) {
                //System.out.println("Service added: " + event.getName());
            }
        
            @Override
            public void serviceRemoved(ServiceEvent event) {
                //System.out.println("Service removed: " + event.getInfo());
            }
        
            @Override
            public void serviceResolved(ServiceEvent event) {
                //System.out.println("Service resolved: " + event.getName());

                if(event.getName().equals("comp3010FP") && !event.getInfo().getNiceTextString().equals("\\00")) {
                    String data = event.getInfo().getNiceTextString().substring(1);
                    String ipUnformatted = "";

                    for(int i=0;i<event.getInfo().getInet4Addresses().length;i++){
                        if(!(event.getInfo().getInet4Addresses()[i]+"").startsWith("/127")) {
                            ipUnformatted = event.getInfo().getInet4Addresses()[i]+"";
                        }
                        
                    }

                    PeerInfo newPeer = new PeerInfo(ipUnformatted,event.getInfo(),data);

                    //check if they are already here
                    boolean found = false;
                    for(Peer peer : peer_list) {
                        if(newPeer.peer_id.equals(peer.peer_id))
                            found = true;
                    }
                    
                    if(!found && !newPeer.peer_id.equals(player_id)) {
                        System.out.println("Client " + player_id + " discovered: " + newPeer.peer_id);
                        connectToPeer(newPeer);
                        //peer_list.add(newPeer);
                    }
                    
                }
            }
        
        }
        
		try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
            // Add service listener
            jmdns.addServiceListener("_http._tcp.local.", new mDNSListener());


		} catch (UnknownHostException e) {
            System.out.println(e.getMessage());
		} catch (IOException e) {
            System.out.println(e.getMessage());
        } 

        //add myself to the list of peers

        //Broadcast myself as a mDNS service
        try {
            serviceInfo = ServiceInfo.create("_http._tcp.local.", "comp3010FP", 25567, (player_id + "," + activePort + "," + username));
            
            jmdns.registerService(serviceInfo);
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } 

	}

    //Once a peer is found with mDNS, try to establish a tcp connection
    private void connectToPeer(PeerInfo peer) {

        Peer newPeer = new Peer(peer.ip, peer.peer_id, peer.port, player_id, peer.username);
        //newPeer.sendMessage("Hello, I am " + player_id);

        boolean found = false;
        for(Peer p: peer_list) {
            if(p.peer_id.equals(newPeer.peer_id)){
                found = true;
            }
        }

        if(!found && !player_id.equals(newPeer.peer_id)) {
            peer_list.add(newPeer);
            //send any messages here that you would like to tell the new peer
            String ip = "";
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //ask if this connection is mutual
            newPeer.sendMessage("messagetype:checkIfConnected," + player_id + "," + activePort + "," + username + "," + ip);
            //send them a list of all of our peers too
            for(Peer p: peer_list) {
                newPeer.sendMessage("messagetype:checkIfConnected," + p.peer_id + "," + p.port + "," + p.name + "," + p.ip);
            }

            newPeer.sendMessage("messagetype:status," + ready + "," + player_id);

            if(peer_list.size() == 1) {
                newPeer.sendMessage("messagetype:chooseHost," + startTime + "," + player_id);
            }
            else if(isHost) {
                newPeer.sendMessage("messagetype:setHost," + player_id);
            }
            
        }

    }


    @Override
	public void resize(int width, int height) {

	}

    @Override
	public void show() {

	}

    @Override
	public void hide() {

	}

    @Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

    @Override
	public void dispose() {
        try { 
            for(Peer p: peer_list) {
                //if I was the host, set the first peer I connected to as the new host
                if(isHost)
                    p.sendMessage("messagetype:setHost," + peer_list.get(0).peer_id);
                p.close();
            }
            serverSocket.close();
        }
        catch(Exception e) {

        }

		jmdns.unregisterService(serviceInfo);
        //System.out.println("Hello!");
	}


}
