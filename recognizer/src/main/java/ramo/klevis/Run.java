package ramo.klevis;

import org.apache.commons.io.FileUtils;
import ramo.klevis.ml.ui.ProgressBar;
import ramo.klevis.ml.ui.UI;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.zip.Adler32;


public class Run {

    private static final String MODEL_URL = "https://drive.google.com/uc?export=download&confirm=v-b4&id=1Le8UGwofY0_o8fdpoYsni4CHyLYcdOFt";

    public static void main(String[] args) throws Exception {
        //downloadModelForFirstTime();

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

    private static void downloadModelForFirstTime() throws IOException {
        JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        ProgressBar progressBar = new ProgressBar(mainFrame, false);
        File model = new File("resources/model.zip");
        if (!model.exists() || FileUtils.checksum(model, new Adler32()).getValue() != 3082129141l) {
            model.delete();
            downloadModelForFirstTime();
            progressBar.showProgressBar("Downloading model for the first time 500MB!");
            URL modelURL = new URL(MODEL_URL);
            try {
                FileUtils.copyURLToFile(modelURL, model);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to download model");
                throw new RuntimeException(e);
            } finally {
                progressBar.setVisible(false);
                mainFrame.dispose();
            }

        }
    }
}
