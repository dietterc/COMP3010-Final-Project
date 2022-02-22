package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		this.setScreen(new MainMenu(this));
		//networkingInit();
	}

	@Override
	public void render() {
		super.render();

		//networkingStep();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
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

		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			//System.out.println("test");
			
			try {
				out.println("hello server");
        		String resp = in.readLine();
        		System.out.println(resp);
			} 
			catch(Exception e) {
				System.out.println("Error");
			}

		}
	}



}
