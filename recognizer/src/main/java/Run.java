import ui.ProgressBar;
import ui.UI;

import javax.swing.*;
import java.util.concurrent.Executors;


public class Run {

    public static void main(String[] args) throws Exception {

        JFrame mainFrame = new JFrame();
        ProgressBar progressBar = new ProgressBar(mainFrame, true);
        progressBar.showProgressBar("Loading model, this make take several seconds!");
        UI ui = new UI();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                ui.initUI();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                progressBar.setVisible(false);
                mainFrame.dispose();
            }
        });

    }
}
