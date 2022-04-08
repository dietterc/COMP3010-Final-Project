package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.Peer;

public class GameRoom implements Screen {
    
    final ProjectMain game;
    final float WIDTH = 20;
    final float HEIGHT = 11.25f;

    final float SCREEN_WIDTH = 1440;
    final float SCREEN_HEIGHT = 810;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;

    private Body playerBody;
    RopeJoint RJ;
    private Array<Wall> grappleWalls;
    private float ropeLen;

    BitmapFont hudFont;
    BitmapFont nameFont;
    BitmapFont smallFont;

    boolean clicked;
    boolean orbCollected;
    long timeCollected;
    boolean isIt;
    long targetTime;
    int score;
    long scoreTimer;
    long taggedTime;

    //sprites
    Sprite orbSprite;
    Sprite playerSprite;

    //networking
    String username;
    String player_id;
    boolean isHost;
    boolean sendPositionData;
    boolean allPeersHere;
    public static ConcurrentLinkedQueue<String> messageQueue_gameroom;
    public static ArrayList<Peer> peer_list;

    public GameRoom(final ProjectMain game) {
        this.game = game;
        camera = new OrthographicCamera(WIDTH,HEIGHT);
        hudCamera = new OrthographicCamera(1440,810);
        hudFont = new BitmapFont(Gdx.files.internal("fonts/font.fnt"),
            Gdx.files.internal("fonts/font.png"), false);
        hudFont.setColor(0, 0, 0, 1);
        nameFont = new BitmapFont(Gdx.files.internal("fonts/playername.fnt"),
            Gdx.files.internal("fonts/playername.png"), false);
        smallFont = new BitmapFont(Gdx.files.internal("fonts/small.fnt"),
            Gdx.files.internal("fonts/small.png"), false);

        world = new World(new Vector2(0,-9.81f), true);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(world,(float)Lobby.startingX,(float)Lobby.startingY);
        playerBody = player.getBody();

        //otherPlayer = new OtherPlayer(world, 0f,-37.5f);

        rope_init();

        grappleWalls = new Array<Wall>();
        loadMap();

        initalizeSprites();
        orbCollected = false;
        timeCollected = -1;
        score = 0;
        scoreTimer = 0;
        taggedTime = 0;

        clicked = false;

        peer_list = new ArrayList<Peer>();
        messageQueue_gameroom = new ConcurrentLinkedQueue<String>();

        //networking
        username = Lobby.username;
        player_id = Lobby.player_id;
        isHost = Lobby.isHost;
        allPeersHere = false;

        for(Peer p : Lobby.peer_list) {
            peer_list.add(p);
        }
        sendPositionData = false;
        targetTime = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
                sendAllMessage("messagetype:startingInfo," + Lobby.startingX + "," + Lobby.startingY + "," + player_id);
                sendPositionData = true;

                if(isHost) {
                    targetTime = System.currentTimeMillis() + 180000;
                    sendAllMessage("messagetype:targetTime," + targetTime);
                }
            }
         }).start();
    }

    @Override
	public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        
		world.step(1/60f, 6, 2);

        //HUD
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
		game.batch.begin();
        
        String message = "Fuel: " + player.getFuel();
        hudFont.draw(game.batch, message, -700, 375);

        String name = getItPlayerName();
        hudFont.draw(game.batch, "it: " + name, -700, 325);

        //draw scoreboard
        drawScoreboard(game.batch);

        if(targetTime != 0) {
            int seconds = (int)(targetTime - System.currentTimeMillis()) / 1000;
            int min = (seconds % 3600) / 60;
            int sec = seconds % 60;
            String secString = sec + "";
            if(sec < 10) {
                secString = "0" + sec;
            }
            hudFont.draw(game.batch, min + ":" + secString, -75, 375);
        }
        else {
            hudFont.draw(game.batch, "Countdown: 3:00", -75, 375);
        }

        for(Peer p:peer_list) {
            if(p.playerInfo != null){
                float xp = p.playerInfo.getBody().getPosition().x;
                float yp = p.playerInfo.getBody().getPosition().y;

                Vector3 vec = new Vector3(xp, yp, 0);
                camera.project(vec);
                nameFont.draw(game.batch, p.name, vec.x-735, vec.y-355);
            }     
        }

		game.batch.end();


        //player/world drawing
        updateCameraPos();
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
        drawSprites(game.batch);

		game.batch.end();

        //physics
        rope_step();
        player.step();

        //game checks
        if(name.equals("") && !orbCollected) {
            if(orbSprite.getBoundingRectangle().overlaps(playerSprite.getBoundingRectangle())) {
                //since this is local, send a message to everyone stating the exact time you hit it
                timeCollected = System.currentTimeMillis();
                orbCollected = true;
                isIt = true;
                playerSprite.setTexture(new Texture(Gdx.files.internal("itPlayer.png")));

                sendAllMessage("messagetype:orbCollected," + timeCollected + "," + player_id);
                scoreTimer = System.currentTimeMillis();
            }
        }
        incrementScore();
        checkIfTagged();
        checkIfDone();

        //networking
        if(sendPositionData && !playerBody.getLinearVelocity().isZero())
            sendAllMessage("messagetype:position," + player.getBody().getPosition().x + "," + player.getBody().getPosition().y + "," + player_id);

        if(Gdx.input.isKeyPressed(Input.Keys.P)) {
            //otherPlayer.getBody().setTransform(new Vector2(otherPlayer.getBody().getPosition().x + .02f,-37.5f), 0);
            sendAllMessage("messagetype:text," + "HEY");
        }

        for(int i=0;i<peer_list.size() + 1;i++) {
            if(!messageQueue_gameroom.isEmpty()) {
                //process n messages from the queue per step
                //might need to adjust this
                handleMessage(messageQueue_gameroom.remove());
            }
        }

        //check if all peers made it to the game room.
        if(!allPeersHere) {
            int score = 0;
            for(Peer p:peer_list) {
                if(p.playerInfo != null)
                    score += 1;
            }
            if(score == peer_list.size()) {
                allPeersHere = true;
            }
        }


        debugRenderer.render(world, camera.combined);
    }

    private void sendAllMessage(String message) {
        for(Peer p : peer_list) {
            p.sendMessage(message);
        }
    }

    private void handleMessage(String message) {

        try {

            String lines[] = message.split(",");
            String type = lines[0].split(":")[1];
            //System.out.println(message);

            if(type.equals("text")) {
                System.out.println("Message: " + lines[1]);
            }
            else if(type.equals("startingInfo")) {
                String who = lines[3];

                for(Peer p: peer_list) {
                    if(p.peer_id.equals(who)) {
                        p.initGameVars(new OtherPlayer(world, Float.parseFloat(lines[1]), Float.parseFloat(lines[2])));
                    }
                }

            }
            else if(type.equals("position")) {
                moveOtherPlayer(Float.parseFloat(lines[1]),Float.parseFloat(lines[2]),lines[3]);
            }
            else if(type.equals("orbCollected")) {
                String who = lines[2];
                long time = Long.parseLong(lines[1]);
                scoreTimer = System.currentTimeMillis();
                /*
                if(orbCollected) {
                    if(timeCollected < time) {
                        isIt = true;
                        sendAllMessage("messagetype:isIt," + player_id);
                    }
                }*/
                orbCollected = true;
                for(Peer p:peer_list) {
                    if(p.peer_id.equals(who)) {
                        p.isIt = true;
                        p.playerInfo.setItSprite(true);
                    }
                }

            }
            else if(type.equals("targetTime")) {
                targetTime = Long.parseLong(lines[1]);
            }
            else if(type.equals("tagged")) {
                String newIt = lines[1];
                String oldIt = lines[2];
                long taggedTime = Long.parseLong(lines[3]);

                for(Peer p:peer_list) {
                    if(p.peer_id.equals(newIt)) {
                        p.isIt = true;
                        p.playerInfo.setItSprite(true);
                    }
                    if(p.peer_id.equals(oldIt)) {
                        p.isIt = false;
                        p.playerInfo.setItSprite(false);
                        p.stolenTime = taggedTime;
                    }
                }
                if(newIt.equals(player_id)) {
                    isIt = true;
                    playerSprite.setTexture(new Texture(Gdx.files.internal("itPlayer.png")));
                }
            }
            

        }
        catch(Exception e) {
            System.out.println("Invalid message:\n" + message);
        }

    }

    private String getItPlayerName() {
        String retVal = "";
        if(allPeersHere) {
            for(Peer p:peer_list) {
                if(p.isIt) {
                    retVal = p.name;
                }
            }
        }
        if(isIt)
            retVal = username;
        return retVal;
    }

    private void moveOtherPlayer(float x, float y, String who) {

        for(Peer p:peer_list) {
            if(p.peer_id.equals(who)) {
                Body body = p.playerInfo.getBody();
                body.setTransform(x, y, 0);
            }
        }
    }

    private void incrementScore() {
        if((System.currentTimeMillis() - scoreTimer) >= 1000) {
            if(orbCollected) {
                scoreTimer = System.currentTimeMillis();
                if(isIt) {
                    score += 1;
                }
                else {
                    for(Peer p:peer_list) {
                        if(p.isIt) {
                            p.score += 1;
                        }
                    }
                }
            }
        }

    }

    private void drawScoreboard(SpriteBatch batch) {

        class Score {
            int score;
            String name;

            public Score(int s, String n) {
                score = s;
                name = n;
            }
        }
        ArrayList<Score> list = new ArrayList<Score>();
        Comparator<Score> cmp = new Comparator<Score>() {
            @Override
            public int compare(Score s1, Score s2) {
                return Integer.compare(s2.score, s1.score);
            }
        };
        for(Peer p:peer_list) {
            list.add(new Score(p.score,p.name));
        }
        list.add(new Score(score,username));
        Collections.sort(list, cmp);

        smallFont.draw(batch, "Scoreboard:", -700, 275);
        int yIndex = 250;
        for(Score s:list) {
            smallFont.draw(batch, s.name + ": " + s.score, -700, yIndex);
            yIndex -= 25;
        }
    }

    private void checkIfTagged() {
        if(isIt) {
            for(Peer p:peer_list) {
                if(playerSprite.getBoundingRectangle().overlaps(p.playerInfo.sprite.getBoundingRectangle())) {
                    if(System.currentTimeMillis() - p.stolenTime > 3000) {
                        taggedTime = System.currentTimeMillis();
                        isIt = false;
                        p.isIt = true;
                        p.playerInfo.setItSprite(true);
                        playerSprite.setTexture(new Texture(Gdx.files.internal("player.png")));

                        sendAllMessage("messagetype:tagged," + p.peer_id + "," + player_id + "," + taggedTime);
                    }
                }
            }
        }
    }

    private void checkIfDone() {

        if(targetTime != 0 && System.currentTimeMillis() >= targetTime) {

            String winner = username;
            int topScore = score;

            for(Peer p:peer_list) {
                if(p.score > topScore) {
                    topScore = p.score;
                    winner = p.name;
                }
            }
            game.setScreen(new EndScreen(game,winner));

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

    private void initalizeSprites() {

        orbSprite = new Sprite(new Texture(Gdx.files.internal("orb.png")));
        orbSprite.setPosition(0.25f, -16.00f);
        orbSprite.setBounds(0,0,1f,1f);
        //orbSprite.setOriginCenter();

        playerSprite = new Sprite(new Texture(Gdx.files.internal("player.png")));
        playerSprite.setPosition((float)Lobby.startingX, (float)Lobby.startingY);
        playerSprite.setBounds(0,0,.80f,.80f);
        playerSprite.setOriginCenter();

    }

    private void drawSprites(SpriteBatch batch) {
        if(!orbCollected)
            orbSprite.draw(batch);

        playerSprite.setPosition(playerBody.getPosition().x-.4f, playerBody.getPosition().y-.4f);
        playerSprite.draw(batch);

        if(allPeersHere) {
            for(Peer p:peer_list) {
                //draw each peers sprite
                p.playerInfo.step(batch);
            }
        }
        
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

    private void loadMap() {
       

        TiledMap tiledMap = new TmxMapLoader().load("map.tmx");
			//TiledMap tiledMap = new TmxMapLoader().load("levels/testlevel.tmx");

        MapLayers layers = tiledMap.getLayers();

        for(MapLayer layer : layers) {

            MapObjects objects = layer.getObjects();

            for(MapObject object : objects) {

                TextureMapObject obj = (TextureMapObject) object;
                Vector3 pos = new Vector3(obj.getX()+32, 3200-obj.getY()-32, 0);
                camera.unproject(pos);
                grappleWalls.add(new Wall(world,pos.x,pos.y));
                
            }
        }

    }

    //method for testing stuff
    private void rope_init() {
        ropeLen = 1f; 
    }

    //step version of test method
    private void rope_step() {

        //rope methods
        if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            if(ropeLen < 5f)
                ropeLen += .1f;
        }
    
        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            if(ropeLen > 0f) {
                Vector3 mousePos = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
                camera.unproject(mousePos);

                ropeLen -= .05f;
                int pullForce = 5;

                /* FIX LATER
                Vector2 velocity = playerBody.getLinearVelocity();
                float speed = velocity.nor().len();
                if(speed > 3) {
                    playerBody.setLinearVelocity(velocity.x * 3,velocity.y * 3);
                }*/
            
                Vector2 direction = new Vector2((mousePos.x - playerBody.getPosition().x)*pullForce, (mousePos.y - playerBody.getPosition().y)*pullForce);
                playerBody.applyForceToCenter(direction, true);

            }
                
        }

        if(RJ != null) {
            RJ.setMaxLength(ropeLen);
        }

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
            camera.unproject(mousePos);

            clicked = true;
            Body clickedBody = null;
            //find out what body was clicked
            for(Wall wall:grappleWalls) {
                if(mousePos.dst(wall.getBody().getPosition().x,wall.getBody().getPosition().y,0) < .35f) {
                    clickedBody = wall.getBody();
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

        if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && clicked) {
            clicked = false;

            if(RJ != null) {
                world.destroyJoint(RJ);
                RJ = null;
            }

        }


    }

}
