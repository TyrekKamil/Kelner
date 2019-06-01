package ui;

import vg16.FoodType;
import vg16.TrainImageNetVG16;
import vg16.VG16;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;


public class UI {

    private JFrame mainFrame;
    private JPanel mainPanel;
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 150;
    private JLabel predictionResponse;
    private VG16 vg16;

    public UI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
    }

    public void initUI() throws Exception {


        mainFrame = createMainFrame();

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        File foodPicture = new File(TrainImageNetVG16.DATA_PATH + "/burger1.jpg");
        fillMainPanel();
        recogniseFood(foodPicture);

        mainPanel.updateUI();

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);

    }
    private void recogniseFood(File food) throws Exception {
        vg16 = new VG16();
        vg16.loadModel();
        try {
            FoodType foodType = vg16.detectBurger(food, 0.6);
            if (foodType == FoodType.PIZZA){
                predictionResponse.setText("Pizza");
                predictionResponse.setForeground(Color.GREEN);
            } else if (foodType == FoodType.BURGER) {
                predictionResponse.setText("Burger");
                predictionResponse.setForeground(Color.GREEN);
            } else if (foodType == FoodType.SALAD) {
                predictionResponse.setText("Salad");
                predictionResponse.setForeground(Color.GREEN);
            } else if (foodType == FoodType.SPAGHETTI) {
                predictionResponse.setText("Spaghetti");
                predictionResponse.setForeground(Color.GREEN);
            } else {
                predictionResponse.setText("Not Sure...");
                predictionResponse.setForeground(Color.RED);
            }
        }
        catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
    private void fillMainPanel() throws IOException {
        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 3;
        c.weighty = 0;
        c.weightx = 0;
        predictionResponse = new JLabel();
        predictionResponse.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        mainPanel.add(predictionResponse, c);
    }


    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Image Recognizer");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        return mainFrame;
    }
}
