package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//Networking
import java.net.*;
import java.io.*;

public class ProjectMain extends Game {
	SpriteBatch batch;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
	private Screen mainMenu;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		mainMenu = new MainMenu(this);
		this.setScreen(mainMenu);
		//networkingInit();
	}

	@Override
	public void render() {
		super.render();

		networkingStep();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		mainMenu.dispose();

	}


	private void networkingInit() {
		try {
			clientSocket = new Socket("127.0.0.1", 6000);
        	out = new PrintWriter(clientSocket.getOutputStream(), true);
        	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(Exception e) {
			System.out.println("Error connecting");
		}
	}

	private void networkingStep() {

		if(Gdx.input.isKeyJustPressed(Keys.L)) {
			//System.out.println("test");
			
			
		}
	}



}
