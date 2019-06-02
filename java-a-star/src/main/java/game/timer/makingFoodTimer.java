package game.timer;
import game.Game;

import java.awt.*;
import java.util.TimerTask;

public class makingFoodTimer extends TimerTask {
    private Game game;
    private Graphics2D g;
    public makingFoodTimer(Game game, Graphics2D g) {
        this.game = game;
        this.g = g;
    }
    public void run() {
        try{
            game.makingFood(this.g);
        }catch (Exception e){
            System.out.println("index out of bound caught");
        }
    }
}
