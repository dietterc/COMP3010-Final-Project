package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenu implements Screen {
    
    final ProjectMain game;
    final int WIDTH = 20;
    final float HEIGHT = 11.25f;

    private OrthographicCamera camera;

    public MainMenu(final ProjectMain game) {
        this.game = game;
        camera = new OrthographicCamera(WIDTH,HEIGHT);


        
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
        
		game.batch.begin();

		game.batch.end();

        if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            game.setScreen(new GameRoom(game));
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
		
	}


}
