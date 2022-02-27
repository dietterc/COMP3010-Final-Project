package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Wall {
    private Body physicsBody;
    private final float PHYISCS_DIM = .225f;

    public Wall(World world, float startX, float startY) {
        
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(startX,startY);
        
        physicsBody = world.createBody(groundBodyDef);
        PolygonShape box = new PolygonShape();
        box.setAsBox(PHYISCS_DIM,PHYISCS_DIM);
        physicsBody.createFixture(box, 0.0f);
        box.dispose();

    }

    public Body getBody() {
        return physicsBody;
    }
    
}