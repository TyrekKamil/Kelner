package game.quartz.job;

import game.Game;
import game.entity.Client;
import javafx.concurrent.Task;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class clientsArrive implements org.quartz.Job {

    Game game = new Game();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {


                game.placeClientInOrder();

    }

}

