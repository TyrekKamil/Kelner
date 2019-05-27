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
import java.util.List;

public class Game extends JPanel implements MouseListener
{

	private Map map;
	private Waiter waiter;
	private List<Node> path;
	private Client client;
	private ArrayList<Point> chairs = new ArrayList<>();
	// 2 kuchnia
	// 3 wyjscie

	int[][] m0 = { //
			{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, }, //
			{ 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, },
			{ 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, },
			{ 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, },
			{ 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, },
			{ 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, },};



	public Game()
	{
        Point chair1 = new Point(4, 3);
        Point chair2 = new Point(8, 3);
        Point chair3 = new Point(12, 3);
        Point chair4 = new Point(4, 7);
        Point chair5 = new Point(8, 7);
        Point chair6 = new Point(12, 7);
        chairs.add(chair1);
        chairs.add(chair2);
        chairs.add(chair3);
        chairs.add(chair4);
        chairs.add(chair5);
        chairs.add(chair6);
		int[][] m = m0;

		setPreferredSize(new Dimension(m[0].length * 32, m.length * 32));
		addMouseListener(this);

		map = new Map(m);
		waiter = new Waiter(0, 1);
		client = new Client(2, 2);

	}

	public void update()
	{
		waiter.update();
		client.update();
	}

	public void render(Graphics2D g)
	{
		map.drawMap(g, path);
		g.setColor(Color.GRAY);
		for (int x = 0; x < getWidth(); x += 32)
		{
			g.drawLine(x, 0, x, getHeight());
		}
		for (int y = 0; y < getHeight(); y += 32)
		{
			g.drawLine(0, y, getWidth(), y);
		}

		g.setColor(Color.RED);
		g.fillRect(waiter.getX() * 32 + waiter.getSx(), waiter.getY() * 32 + waiter.getSy(), 32, 32);

		g.setColor(Color.YELLOW);
		g.fillRect(client.getX() * 32 + client.getSx(), client.getY() * 32 + client.getSy(), 32, 32);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		int mx = e.getX() / 32;
		int my = e.getY() / 32;
		if (map.getNode(mx, my).isWalkable())
		{
			path = map.findPath(waiter.getX(), waiter.getY(), mx, my);
			waiter.followPath(path);
		}
		else
		{
			System.out.println("Can't walk to that node!");
		}
		// na ten moment na pale odpalane zeby bylo widac ze idzie tam skurwysyn
		clientPath();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	// wybranie sciezki - na ten moment metoda w kliencie ze stolikiem wolnym
	public void clientPath(){

		Point tableChoice = client.chooseTable(chairs);
		path = map.findPath(client.getX(), client.getY(), tableChoice.x, tableChoice.y);
		client.followPath(path);
	}

}
