package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameRoom implements Screen {
    
    final ProjectMain game;
    final int WIDTH = 20;
    final float HEIGHT = 11.25f;

    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;

    public GameRoom(final ProjectMain game) {
        this.game = game;
        camera = new OrthographicCamera(WIDTH,HEIGHT);
        world = new World(new Vector2(0,-9.81f), true);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(world,0f,0f);
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
        
		game.batch.begin();

		game.batch.end();

        //physics
        debugRenderer.render(world, camera.combined);
		world.step(1/60f, 6, 2);
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
