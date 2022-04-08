package com.mygdx.game;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class EndScreen implements Screen {
    
    final ProjectMain game;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private String name;


    public EndScreen(final ProjectMain game, String name) {
        this.game = game;

        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        hudFont.setColor(0, 0, 0, 1);
        this.name = name;
        
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1);

        //HUD
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
		game.batch.begin();
        
        hudFont.draw(game.batch, "The end", -100, 300);
        hudFont.draw(game.batch, "The winner is: " + name + "!!!", -100, 250);

		game.batch.end();



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

	}


}
