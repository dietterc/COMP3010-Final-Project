package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private Body physicsBody;
    private CircleShape circle;

    public Player(World world, float startX, float startY) {
        
        //Set up physics body as a circle
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY); //starting position
        physicsBody = world.createBody(bodyDef);
        //set rotation
        physicsBody.setFixedRotation(true);

        circle = new CircleShape();
        circle.setRadius(.35f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.0f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.0f;
        Fixture fixture = physicsBody.createFixture(fixtureDef);

    }

    public Body getBody() {
        return physicsBody;
    }
    
}
