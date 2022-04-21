package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Wall {
    private Body physicsBody;
    private final float PHYISCS_DIM = .225f;
    public Sprite wSprite;

    public Wall(World world, float startX, float startY) {
        
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(startX,startY);
        
        physicsBody = world.createBody(groundBodyDef);
        PolygonShape box = new PolygonShape();
        box.setAsBox(PHYISCS_DIM,PHYISCS_DIM);
        physicsBody.createFixture(box, 0.0f);
        box.dispose();

        wSprite = new Sprite(new Texture(Gdx.files.internal("wall.png")));
        wSprite.setPosition(startX, startY);
        wSprite.setBounds(0,0,.5f,.5f);
        wSprite.setOriginCenter();

    }

    public Body getBody() {
        return physicsBody;
    }
    
}