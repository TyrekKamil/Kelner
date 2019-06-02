package game.timer;
import game.Game;

import java.awt.*;
import java.util.TimerTask;

public class placeClientInOrderTimer extends TimerTask {
    private Game game;

    public placeClientInOrderTimer(Game game) {
        this.game = game;
    }
    public void run() {
        try{
            game.placeClientInOrder();
        }catch (Exception e){
            System.out.println("index out of bound caught");
        }
    }
}
