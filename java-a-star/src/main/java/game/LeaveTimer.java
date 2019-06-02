package game;

import java.util.TimerTask;

public class LeaveTimer extends TimerTask {
    private Game game;

    public LeaveTimer (Game game) {
        this.game = game;
    }
    public void run() {
        game.clientsStartLeaving();
    }
}
