package com.mygdx.game;

 
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class OtherPlayer {
    private Body physicsBody;
    private CircleShape circle;

    public OtherPlayer(World world, float startX, float startY) {
        
        init_physics(startX, startY, world);

    }

    public void step() {
        
        
    }

    public Body getBody() {
        return physicsBody;
    }


    public void init_physics(float startX, float startY, World world) {
        //Set up physics body as a circle, specifically a STATIC body. Since I think we will only be setting its pos directly
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
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
