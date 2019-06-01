package game.entity;


import lombok.Getter;
import lombok.Setter;
import ui.UI;
import vg16.VG16;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Food {

    @Getter @Setter
    private int x;

    @Getter @Setter
    private int y;

    @Getter @Setter
    private int sx;

    @Getter @Setter
    private int sy;

    @Getter @Setter
    private BufferedImage image;

    public Food(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Getter @Setter
    private File file;

    public Food(int x, int y, BufferedImage image, File file){
        this.x = x;
        this.y = y;
        this.image = image;
        sx = 0;
        sy = 0;
        image = resize(image, 32, 32);
        this.file = file;


    }
    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    public void show(Graphics2D g)
    {
        g.drawImage(image, this.x, this.y, null);
    }

    public void checkFood() throws Exception {
        UI ui = new UI();
        ui.recogniseFood(this.file);
    }

}
