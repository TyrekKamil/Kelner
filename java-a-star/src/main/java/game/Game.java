package game;

import game.astar.Map;
import game.astar.Node;
import game.entity.Client;
import game.entity.Waiter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends JPanel implements MouseListener {

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
    private Map map;
    private Waiter waiter;
    private List<Node> path;
    private Client client;
    private Client client1;
    private Client client2;
    private Client client3;
    private Client client4;
    private Client client5;
    private Client client6;
    private ArrayList<Point> chairs = new ArrayList<>();
    private ArrayList<Point> chairsTaken = new ArrayList<>();

    // 2 kuchnia
    // 3 wyjscie
    private ArrayList<Client> listOfClients = new ArrayList<>();

    public Game() {
        Point chair1 = new Point(4, 3);
        Point chair2 = new Point(8, 3);
        Point chair3 = new Point(12, 3);
        Point chair4 = new Point(4, 7);
        Point chair5 = new Point(8, 7);
        Point chair6 = new Point(12, 7);
        Collections.addAll(chairs, chair1, chair2, chair3, chair4, chair5, chair6);
        int[][] m = m0;

        setPreferredSize(new Dimension(m[0].length * 32, m.length * 32));
        addMouseListener(this);

        map = new Map(m);
        waiter = new Waiter(0, 1);
        client = new Client(0, 8);
        client1 = new Client(0, 8);
        client2 = new Client(0, 8);
        client3 = new Client(0, 8);
        client4 = new Client(0, 8);
        client5 = new Client(0, 8);
        client6 = new Client(0, 8);
        Collections.addAll(listOfClients, client, client1, client2, client3, client4, client5, client6);
        placeClientsInOrder();
    }

    public void update() {
        waiter.update();
        client.update();
        client1.update();
        client2.update();
        client3.update();
        client4.update();
        client5.update();
        client6.update();
    }

    public void render(Graphics2D g) {
        map.drawMap(g, path);
        g.setColor(Color.GRAY);
        for (int x = 0; x < getWidth(); x += 32) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 32) {
            g.drawLine(0, y, getWidth(), y);
        }

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

        g.setColor(Color.PINK);
        g.fillRect(client6.getX() * 32 + client6.getSx(), client6.getY() * 32 + client6.getSy(), 32, 32);


    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX() / 32;
        int my = e.getY() / 32;
        if (map.getNode(mx, my).isWalkable()) {
            path = map.findPath(waiter.getX(), waiter.getY(), mx, my);
            waiter.followPath(path);
        } else {
            System.out.println("Can't walk to that node!");
        }
        // na ten moment na pale odpalane zeby bylo widac ze idzie tam skurwysyn
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    private void placeClientsInOrder() {
        for (Client cl : listOfClients) {
            while (true) {
                if (chairsTaken.size() <= 6 && chairs.size() >= 1) {
                    clientPath(cl);
                    break;
                }
            }
        }
    }


    private void clientArrived(Point cl, Client currentClient) {
        callWaiter(cl);
        waiterPlacesOrder();
        waiterBringsFood();
        clientLeaves(cl, currentClient);
        clientServed++;
        System.out.println("served client: " + clientServed);
    }


    private void waiterBringsFood() {
    }

    private void waiterPlacesOrder() {
    }

    // wywolanie follow path do klienta
    private void callWaiter(Point client) {
        path = map.findPath(waiter.getX(), waiter.getY(), (int) client.getX() - 1, (int) client.getY());
        System.out.println("visited client: " + client + "at position: " + client.getX() + " " + client.getY());
        waiter.followPath(path);
    }

    // logika na odejscie - cos na ostatnich klientach sie wyjebuje mimo dodania, do przemyslenia kolejkowanie i client path
    private void clientLeaves(Point client, Client cl) {
        path = map.findPath(cl.getX(), cl.getY(), 0, 1);
        cl.followPath(path);
        chairs.add(client);
        chairsTaken.remove(client);
        System.out.println("client: " + client.getX() + " " + client.getY() + " has left");
        update();
    }

    // wybranie sciezki - na ten moment metoda w kliencie ze stolikiem wolnym
    private void clientPath(Client currentClient) {
        Point tableChoice = currentClient.chooseTable(chairs);
        chairs.remove(tableChoice);
        System.out.println("Chairs free:" + chairs);
        path = map.findPath(currentClient.getX(), currentClient.getY(), tableChoice.x, tableChoice.y);
        currentClient.followPath(path);
        chairsTaken.add(tableChoice);
        clientArrived(tableChoice, currentClient);
        update();
    }

}
