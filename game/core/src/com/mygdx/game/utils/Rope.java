/*
Very simple class for holding info for ropes
The rope is drawn between (x,y) and player 

*/

package com.mygdx.game.utils;

import com.mygdx.game.OtherPlayer;

public class Rope {
    
    public float x;
    public float y;
    public OtherPlayer player;

    public Rope(float x, float y, OtherPlayer player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }

}
