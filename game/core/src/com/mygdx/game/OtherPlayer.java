package com.mygdx.game;

 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class OtherPlayer {
    private Body physicsBody;
    private CircleShape circle;
    public Sprite sprite;

    public OtherPlayer(World world, float startX, float startY) {
        
        init_physics(startX, startY, world);
        
        sprite = new Sprite(new Texture(Gdx.files.internal("player.png")));
        sprite.setPosition(physicsBody.getPosition().x, physicsBody.getPosition().y);
        sprite.setBounds(0,0,.80f,.80f);
        sprite.setOriginCenter();
    }

    public void step(SpriteBatch batch) {
        sprite.setPosition(physicsBody.getPosition().x-.4f, physicsBody.getPosition().y-.4f);
        sprite.draw(batch);
    }

    public Body getBody() {
        return physicsBody;
    }

    public void setItSprite(boolean set) {

        if(set) {
            sprite.setTexture(new Texture(Gdx.files.internal("itPlayer.png")));
        }
        else {
            sprite.setTexture(new Texture(Gdx.files.internal("player.png")));
        }

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
