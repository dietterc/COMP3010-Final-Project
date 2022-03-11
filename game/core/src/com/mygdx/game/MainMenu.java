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

public class MainMenu implements Screen {
    
    final ProjectMain game;
    final int WIDTH = 20;
    final float HEIGHT = 11.25f;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;

    private ArrayList<String> peer_list;

    private JmDNS jmdns;

    public MainMenu(final ProjectMain game) {
        this.game = game;
        camera = new OrthographicCamera(WIDTH,HEIGHT);
        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        hudFont.setColor(0, 0, 0, 1);

        peer_list = new ArrayList<String>();

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
        for(String str : peer_list) {
            hudFont.draw(game.batch, str, -700, yIndex);
            yIndex -= 50;
        }

		game.batch.end();

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
        
		game.batch.begin();

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

                if(event.getName().equals("comp3010FP")) {
                    peer_list.add(event.getInfo().getInet4Addresses()[0] + "");
                    System.out.println("added");
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

        //Broadcast myself as a mDNS service
        try {
            ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "comp3010FP", 1234, "path=index.html");
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
		jmdns.unregisterAllServices();
	}


}
