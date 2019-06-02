package game;
import java.util.TimerTask;

public class Timer extends TimerTask {
    private Game game;

    public Timer (Game game) {
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
