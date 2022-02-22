package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

//Networking
import java.net.*;
import java.io.*;

public class ProjectMain extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		try {
			clientSocket = new Socket("127.0.0.1", 6000);
        	out = new PrintWriter(clientSocket.getOutputStream(), true);
        	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(Exception e) {
			System.out.println("Error connecting");
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		networking();

	}
	
	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
	}

	private void networking() {

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
