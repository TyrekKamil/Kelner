package game.entity;

import game.astar.Node;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Client {
    @Getter @Setter
    private int x;
    @Getter @Setter
    private int y;
    @Getter @Setter
    private int sx;
    @Getter @Setter
    private int sy;

    private int speed;

    private boolean walking;
    private boolean fixing;
    private List<Node> path;

    public Client(int x, int y)
    {
        this.x = x;
        this.y = y;
        sx = 0;
        sy = 0;
        speed = 2;

        walking = false;
        fixing = false;
        path = null;
    }

    public void update()
    {
        if (fixing)
        {
            fix();
        }
        if (walking)
        {
            walk();
        }
    }

    public void followPath(List<Node> path)
    {
        this.path = path;
        if (walking)
        {
            fixing = true;
            walking = false;
        }
        else
        {
            walking = true;
        }
    }

    private void fix()
    {
        if (sx > 0)
        {
            sx -= speed;
            if (sx < 0)
            {
                sx = 0;
            }
        }
        if (sx < 0)
        {
            sx += speed;
            if (sx > 0)
            {
                sx = 0;
            }
        }
        if (sy > 0)
        {
            sy -= speed;
            if (sy < 0)
            {
                sy = 0;
            }
        }
        if (sy < 0)
        {
            sy += speed;
            if (sy > 0)
            {
                sy = 0;
            }
        }
        if (sx == 0 && sy == 0)
        {
            fixing = false;
            walking = true;
        }
    }

    private void walk()
    {
        if (path == null)
        {
            walking = false;
            return;
        }
        if (path.size() <= 0)
        {
            walking = false;
            path = null;
            return;
        }
        Node next = ((LinkedList<Node>) path).getFirst();
        if (next.getX() != x)
        {
            sx += (next.getX() < x ? -speed : speed);
            if (sx % 32 == 0)
            {
                ((LinkedList<Node>) path).removeFirst();
                if (sx > 0)
                    x++;
                else
                    x--;
                sx %= 32;
            }
        }
        else if (next.getY() != y)
        {
            sy += (next.getY() < y ? -speed : speed);
            if (sy % 32 == 0)
            {
                ((LinkedList<Node>) path).removeFirst();
                if (sy > 0)
                    y++;
                else
                    y--;
                sy %= 32;
            }
        }
    }
    // wybranie stolika randomowego z listy, i wysyla ze do niego idzie
    public Point chooseTable(ArrayList<Point> chairs) {
        Random ran = new Random();
        int randomNumber = ran.ints(1, 0, chairs.size()).findFirst().getAsInt();
        Point tableChoice = chairs.get(randomNumber);
        System.out.println("Client goes to tile number: " + chairs.get(randomNumber));
        chairs.remove(randomNumber);
        return tableChoice;
    }

}
