/*
Main class for libGDX

Not much needs to be done here, its just the class that libGDX runs when the 
game starts, just like a regular main java method

*/

package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProjectMain extends Game {
	SpriteBatch batch;
	private Screen mainMenu;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		mainMenu = new MainMenu(this);
		this.setScreen(mainMenu);
	}

	@Override
	public void render() {
		super.render();

	}
	
	@Override
	public void dispose() {
		batch.dispose();
		mainMenu.dispose();

	}

}
