package game.timer;
import game.Game;

import java.awt.*;
import java.util.TimerTask;

public class makingFoodTimer extends TimerTask {
    private Game game;
    public makingFoodTimer(Game game) {
        this.game = game;
    }
    public void run() {
        try{
            game.makingFood();
        }catch (Exception e){
            System.out.println("index out of bound caught");
        }
    }
}
