package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private Body physicsBody;
    private CircleShape circle;
    private final int MAX_FUEL = 500;
    private int currentFuel;

    public Player(World world, float startX, float startY) {
        
        init_physics(startX, startY, world);
        currentFuel = MAX_FUEL;

    }

    public void step() {
        boolean useFuel = false;
        if(currentFuel > 0) {
            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                Vector2 force = new Vector2(10f,0);
                physicsBody.applyForceToCenter(force, true);
                useFuel = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                Vector2 force = new Vector2(-10f,0);
                physicsBody.applyForceToCenter(force, true);
                useFuel = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.W)) {
                Vector2 force = new Vector2(0,10f);
                physicsBody.applyForceToCenter(force, true);
                useFuel = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.S)) {
                Vector2 force = new Vector2(0,-10f);
                physicsBody.applyForceToCenter(force, true);
                useFuel = true;
            }

            if(useFuel) {
                currentFuel -= 3;
            }
        }
        
        if(!useFuel && currentFuel < MAX_FUEL) {
            currentFuel += 2;
        }
        
    }

    public Body getBody() {
        return physicsBody;
    }

    public int getFuel() {
        return currentFuel;
    }

    public void init_physics(float startX, float startY, World world) {
        //Set up physics body as a circle
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY); //starting position
        physicsBody = world.createBody(bodyDef);
        //set rotation
        physicsBody.setFixedRotation(true);
        physicsBody.setSleepingAllowed(false);

        circle = new CircleShape();
        circle.setRadius(.35f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.0f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.0f;
        physicsBody.createFixture(fixtureDef);
    }
    
}
