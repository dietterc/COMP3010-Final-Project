/*
Main menu class

gets the players username and sends them to the lobby
*/

package com.mygdx.game;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenu implements Screen {
    
    final ProjectMain game;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private String username;
    private String ip;
    private int port;
    private boolean normal;

    private Lobby lobby;

    public MainMenu(final ProjectMain game) {
        this.game = game;

        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        hudFont.setColor(0, 0, 0, 1);

        username = "";
        normal = true;
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1);

        //HUD
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
		game.batch.begin();
        
        hudFont.draw(game.batch, "Final Project for COMP3010\nColton Dietterle\n\nPress space to begin", -700, 325);
        hudFont.draw(game.batch, "Main Menu", 0, 0);

		game.batch.end();

		

        if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            getUsername();
        }

        if(Gdx.input.isKeyJustPressed(Keys.L)) {
            getUsernameAndIp();
        }

        if(!username.equals("")) {
            String id = UUID.randomUUID().toString();
            if(normal)
                lobby = new Lobby(game,username,id,true,"",0);
            else
                lobby = new Lobby(game,username,id,false,ip,port);
            game.setScreen(lobby);
        }

        if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    //simple textbox to get a username
    private void getUsername() {
        class TextListener implements Input.TextInputListener {
            @Override
            public void input(String text) {
                username = text;
            }

            @Override
            public void canceled() {
                username = "";
            }
        }
        Gdx.input.getTextInput(new TextListener(),"Enter a Username","","username");
    }

    //special textbox that gets the username, ip, and port of a known peer
    //really just used for debugging/bypassing mDNS
    private void getUsernameAndIp() {
        class TextListener implements Input.TextInputListener {
            @Override
            public void input(String text) {
                String[] split = text.split(",");
                try {
                    username = split[0];
                    ip = split[1];
                    port = Integer.parseInt(split[2]);
                    normal = false;
                }
                catch(Exception e) {
                    username = "";
                }
                
            }

            @Override
            public void canceled() {
                username = "";
            }
        }
        Gdx.input.getTextInput(new TextListener(),"Enter a Username, ip, and port separated by commas","","username,192.168...,25565");
    }

    //required interface methods 
    //can be ignored because this is just a prototype
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
        lobby.dispose();
	}


}
