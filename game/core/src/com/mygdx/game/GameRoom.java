package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameRoom implements Screen {
    
    final ProjectMain game;
    final int WIDTH = 20;
    final float HEIGHT = 11.25f;

    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;

    private Body testBody;
    private Body testBody2;
    private Body playerBody;
    RopeJoint RJ;
    private Array<Body> grappleBodies;
    private float ropeLen;

    public GameRoom(final ProjectMain game) {
        this.game = game;
        camera = new OrthographicCamera(WIDTH,HEIGHT);
        world = new World(new Vector2(0,-9.81f), true);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(world,-5f,0f);

        test();

        grappleBodies = new Array<Body>();
        grappleBodies.add(testBody);
        grappleBodies.add(testBody2);

    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        updateCameraPos();
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
        
		game.batch.begin();

		game.batch.end();

        //physics
        test_step();

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

    //set the camera to smoothly follow the player
    private void updateCameraPos() {

        //convert player position to a Vector3 so it works with camera
        Vector2 pos2 = playerBody.getPosition();
        Vector3 pos3 = new Vector3(pos2.x, pos2.y, 0);

        //speed to move the cam
        final float speed=0.1f, ispeed= 1.0f-speed;
        
        Vector3 cameraPosition = camera.position;
        cameraPosition.scl(ispeed);
        pos3.scl(speed);
        cameraPosition.add(pos3);
        camera.position.set(cameraPosition);

    }

    //method for testing stuff
    private void test() {

        playerBody = player.getBody();
        ropeLen = 1f;
        
        //Test anchors
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0f, 0f); //starting position
        testBody = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(.35f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        testBody.createFixture(fixtureDef);
        
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(-3f, 0f); //starting position
        testBody2 = world.createBody(bodyDef2);
        CircleShape circle2 = new CircleShape();
        circle2.setRadius(.35f);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = circle2;
        testBody2.createFixture(fixtureDef2);

    }

    //step version of test method
    private void test_step() {

        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            if(ropeLen < 5f)
                ropeLen += .1f;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            if(ropeLen > 0f)
                ropeLen -= .1f;
        }

        if(RJ != null) {
            RJ.setMaxLength(ropeLen);
        }

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Body clickedBody = null;
            Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(pos);
            //find out what body was clicked
            for(Body body:grappleBodies) {
                if(pos.dst(body.getPosition().x,body.getPosition().y,0) < .35f) {
                    clickedBody = body;
                }
            }
            
            if(clickedBody != null) {
                if(RJ != null)
                    world.destroyJoint(RJ);

                RopeJointDef rDef = new RopeJointDef();
                rDef.bodyA = playerBody;
                rDef.bodyB = clickedBody;
                rDef.collideConnected = true;
                rDef.maxLength = playerBody.getPosition().dst(clickedBody.getPosition());
                rDef.localAnchorA.set(0,0);
                rDef.localAnchorB.set(0,0);
                ropeLen = playerBody.getPosition().dst(clickedBody.getPosition());

                RJ = (RopeJoint) world.createJoint(rDef);

            }

        }

    }

}
