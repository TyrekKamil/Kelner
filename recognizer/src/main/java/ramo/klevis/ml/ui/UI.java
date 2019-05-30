package ramo.klevis.ml.ui;

import ramo.klevis.ml.vg16.FoodType;
import ramo.klevis.ml.vg16.TrainImageNetVG16;
import ramo.klevis.ml.vg16.VG16;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;


public class UI {

    private JFrame mainFrame;
    private JPanel mainPanel;
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private JLabel predictionResponse;
    private VG16 vg16;
    private File selectedFile;
    private SpinnerNumberModel modelThresholdSize;
    private JSpinner thresholdField;
    private final Font sansSerifBold = new Font("SansSerif", Font.BOLD, 18);

    public UI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));

    }

    public void initUI() throws Exception {

        vg16 = new VG16();
        vg16.loadModel();
        // create main frame
        mainFrame = createMainFrame();

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());


        chooseFile(TrainImageNetVG16.DATA_PATH + "/pizza1.jpg");


        JButton predictButton = new JButton("What type of food?");
        predictButton.addActionListener(e -> {
            try {
                FoodType foodType = vg16.detectBurger(selectedFile, 0.6);
                if (foodType == FoodType.PIZZA) {
                    predictionResponse.setText("It is a Pizza");
                    predictionResponse.setForeground(Color.GREEN);
                } else if (foodType == FoodType.BURGER) {
                    predictionResponse.setText("It is a Burger");
                    predictionResponse.setForeground(Color.GREEN);
                } else if (foodType == FoodType.SALAD) {
                    predictionResponse.setText("It is a Salad");
                    predictionResponse.setForeground(Color.GREEN);
                } else if (foodType == FoodType.SPAGHETTI) {
                    predictionResponse.setText("It is a Spaghetti");
                    predictionResponse.setForeground(Color.GREEN);
                } else {
                    predictionResponse.setText("Not Sure...");
                    predictionResponse.setForeground(Color.RED);
                }
                mainPanel.updateUI();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        });

        fillMainPanel(predictButton);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);

    }

    private void fillMainPanel(JButton predictButton) throws IOException {
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 1;
        c.weighty = 0;
        c.weightx = 0;
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(predictButton);
        mainPanel.add(buttonsPanel, c);

        c.gridy = 3;
        c.weighty = 0;
        c.weightx = 0;
        predictionResponse = new JLabel();
        predictionResponse.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        mainPanel.add(predictionResponse, c);
    }


    public File chooseFile(String filePath) {
        File file = new File(filePath);
        selectedFile = file;
        return selectedFile;
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
        ImageIcon imageIcon = new ImageIcon("icon.png");
        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }

}
