package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.ProjectMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "COMP3010 Final Project - Peer to Peer multiplayer";
		config.width = 1440;
		config.height = 810;
		config.resizable = false;

		new LwjglApplication(new ProjectMain(), config);
	}
}
