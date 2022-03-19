package com.mygdx.game;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.PeerInfo;

public class Lobby implements Screen {
    
    final ProjectMain game;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private BitmapFont smallFont;

    private ArrayList<PeerInfo> peer_list;

    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    private String username;
    private String player_id;

    public Lobby(final ProjectMain game, String username, String player_id) {
        this.game = game;
        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        smallFont = new BitmapFont(Gdx.files.internal("fonts/small.fnt"),
            Gdx.files.internal("fonts/small.png"), false);

        peer_list = new ArrayList<PeerInfo>();

        this.username = username;
        this.player_id = player_id;

        mdnsSetup();
        
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
        for(PeerInfo peer : peer_list) {
            yIndex -= 100;
            hudFont.draw(game.batch, peer.ip, -700, yIndex);
            smallFont.draw(game.batch, peer.peer_id, -700, yIndex - 40);
        }
        hudFont.draw(game.batch, "Lobby", 0, 0);
        smallFont.draw(game.batch, player_id, 0, -50);

		game.batch.end();


        if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            game.setScreen(new GameRoom(game));
            
        }

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
                System.out.println("Service removed: " + event.getInfo());
            }
        
            @Override
            public void serviceResolved(ServiceEvent event) {
                //System.out.println("Service resolved: " + event.getName());

                if(event.getName().equals("comp3010FP") && !event.getInfo().getNiceTextString().equals("\\00")) {
                    String newId = event.getInfo().getNiceTextString().substring(1);
                    PeerInfo newPeer = new PeerInfo(serviceInfo.getInet4Addresses()[0] + "",event.getInfo(),newId);

                    //check if they are already here
                    boolean found = false;
                    for(PeerInfo peer : peer_list) {
                        if(newPeer.peer_id.equals(peer.peer_id))
                            found = true;
                    }
                    
                    if(!found) {
                        peer_list.add(newPeer);
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
        //peer_list.add(new PeerInfo(serviceInfo.getInet4Addresses()[0] + "",event.getInfo(),player_id));

        //Broadcast myself as a mDNS service
        try {
            serviceInfo = ServiceInfo.create("_http._tcp.local.", "comp3010FP", 5353, player_id);
            jmdns.registerService(serviceInfo);
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
		jmdns.unregisterService(serviceInfo);
        System.out.println("Hello!");
	}


}
