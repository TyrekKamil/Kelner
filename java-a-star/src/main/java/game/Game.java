package game;

import game.astar.Map;
import game.astar.Node;
import game.entity.Client;
import game.entity.Food;
import game.entity.Waiter;
import lombok.Getter;
import ui.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends JPanel{

    int clientServed = 0;
    int[][] m0 = { //
            {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,}, //
            {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1,},
            {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1,},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1,},
            {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1,},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},};
    private Integer counter = 0;
    private Map map;
    private Waiter waiter;
    private List<Node> path;
    private List<Node> leavePath;
    private Client client;
    private Client client1;
    private Client client2;
    private Client client3;
    private Client client4;
    private Client client5;
    private Client client6;
    private ArrayList<Point> chairs = new ArrayList<>();
    private ArrayList<Point> chairsTaken = new ArrayList<>();
    File burgerFile = new File("resources/burger1.jpg");
    File pizzaFile = new File("resources/pizza1.jpg");
    File saladFile = new File("resources/salad1.jpg");
    File spaghettiFile = new File("resources/spaghetti1.jpg");

    UI ui = new UI();
    Food food = new Food(0,0, ui);

    // 2 kuchnia
    // 3 wyjscie
    @Getter
    private ArrayList<Client> listOfClients = new ArrayList<>();

    public Game() throws Exception {

        ui.initUI();
        Point chair1 = new Point(4, 3);
        Point chair2 = new Point(8, 3);
        Point chair3 = new Point(12, 3);
        Point chair4 = new Point(4, 7);
        Point chair5 = new Point(8, 7);
        Point chair6 = new Point(12, 7);
        Collections.addAll(chairs, chair1, chair2, chair3, chair4, chair5, chair6);

        foodImage();
        setPreferredSize(new Dimension(m0[0].length * 32, m0.length * 32));

        map = new Map(m0);
        waiter = new Waiter(0, 1);
        client = new Client(0, 8);
        client1 = new Client(0, 8);
        client2 = new Client(0, 8);
        client3 = new Client(0, 8);
        client4 = new Client(0, 8);
        client5 = new Client(0, 8);
        client6 = new Client(0, 8);
        Collections.addAll(listOfClients, client, client1, client2, client3, client4, client5, client6);
        initQueue();
    }

    public void initQueue() {
        new java.util.Timer().scheduleAtFixedRate(new Timer(this), 1000, 1 * 3000);
    }

    private BufferedImage pizza, spaghetti, salad, burger;

    public void foodImage() {
        try {
            burger = ImageIO.read(burgerFile);
            burger = resize(burger, 32, 32);
            pizza = ImageIO.read(pizzaFile);
            pizza = resize(pizza, 32, 32);
            salad = ImageIO.read(saladFile);
            salad = resize(salad, 32, 32);
            spaghetti = ImageIO.read(spaghettiFile);
            spaghetti = resize(spaghetti, 32, 32);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void update() {
        waiter.update();
        client.update();
        client1.update();
        client2.update();
        client3.update();
        client4.update();
        client5.update();
    }

    public void render(Graphics2D g) throws Exception {
        map.drawMap(g, path);
        g.setColor(Color.GRAY);
        for (int x = 0; x < getWidth(); x += 32) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 32) {
            g.drawLine(0, y, getWidth(), y);
        }

       // super.paintComponent(g);
        food.setImage(burger);
        food.setFile(burgerFile);
        food.show(g);


        g.setColor(Color.RED);
        g.fillRect(waiter.getX() * 32 + waiter.getSx(), waiter.getY() * 32 + waiter.getSy(), 32, 32);

        g.setColor(Color.YELLOW);
        g.fillRect(client.getX() * 32 + client.getSx(), client.getY() * 32 + client.getSy(), 32, 32);

        g.setColor(Color.BLUE);
        g.fillRect(client1.getX() * 32 + client1.getSx(), client1.getY() * 32 + client1.getSy(), 32, 32);

        g.setColor(Color.GREEN);
        g.fillRect(client2.getX() * 32 + client2.getSx(), client2.getY() * 32 + client2.getSy(), 32, 32);

        g.setColor(Color.BLACK);
        g.fillRect(client3.getX() * 32 + client3.getSx(), client3.getY() * 32 + client3.getSy(), 32, 32);

        g.setColor(Color.ORANGE);
        g.fillRect(client4.getX() * 32 + client4.getSx(), client4.getY() * 32 + client4.getSy(), 32, 32);

        g.setColor(Color.CYAN);
        g.fillRect(client5.getX() * 32 + client5.getSx(), client5.getY() * 32 + client5.getSy(), 32, 32);

    }



    public void placeClientInOrder() {
        try {
            Client cl = this.listOfClients.get(0);
            if (chairsTaken.size() <= 6 && chairs.size() >= 1) {
                clientPath(cl);
                this.listOfClients.remove(cl);
                clientLeaves(cl);
            }
        }
        catch (Exception e) {
            System.out.println("no more clients at the tables");
        }

    }


    private void clientArrived(Point cl, Client currentClient) throws Exception {
        callWaiter(cl);
        clientPlacesOrder();
        waiterBringsFood();

        try
        {
            Thread.sleep(4000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        clientServed++;
        System.out.println("served client: " + clientServed);
        if(clientServed == 6)
        {
            waiterGoesToKitchen();
        }
    }


    private void waiterBringsFood() {
    }

    private void clientPlacesOrder() {
    }

    private void waiterGoesToKitchen() throws Exception {
        path = map.findPath(waiter.getX(), waiter.getY(), 1, 0);

        waiter.followPath(path);
        food.checkFood();


    }

    // wywolanie follow path do klienta
    private void callWaiter(Point client) {
        try
        {
            Thread.sleep(4000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        path = map.findPath(waiter.getX(), waiter.getY(), (int) client.getX() - 1, (int) client.getY());
        System.out.println("visited client: " + client + "at position: " + client.getX() + " " + client.getY());
        waiter.followPath(path);
    }

    // lewy dolny idzie przez stolik, a prawa kolumna nie chce dzialac xd
    public void clientLeaves(Client cl) {
        m0[cl.getY()][cl.getX()] = 0;
        map = new Map(m0);
        leavePath = map.findPath(cl.getY(), cl.getX(), 0, 0);
        cl.followPath(leavePath);
        Point client = new Point(cl.getX(),cl.getY());
        System.out.println("!!!client: " + client.getX() + " " + client.getY() + " has left");
        chairs.add(client);
        clientsInTables.remove(cl);
        update();
    }

    // wybranie sciezki - na ten moment metoda w kliencie ze stolikiem wolnym
    private void clientPath(Client currentClient) throws Exception {
        Point tableChoice = currentClient.chooseTable(chairs);
        chairs.remove(tableChoice);
        System.out.printf("Chairs free:%s%n", chairs);
        path = map.findPath(currentClient.getX(), currentClient.getY(), tableChoice.x, tableChoice.y);
        currentClient.followPath(path);
        chairsTaken.add(tableChoice);
        clientsInTables.add(currentClient);
        System.out.println(clientsInTables.size());
        System.out.printf("Chairs taken:%s%n", chairsTaken);
        m0[tableChoice.y][tableChoice.x] = 4;
        map = new Map(m0);
        for (int i = 0; i < m0.length; i++) {
            for (int j = 0; j < m0[i].length; j++) {
                System.out.print(m0[i][j] + " ");
            }
            System.out.println();
        }
        clientArrived(tableChoice, currentClient);
        update();
    }
    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
