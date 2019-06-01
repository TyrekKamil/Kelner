

package game.astar;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;


public class Map
{

	private int width;
	private int height;
	private Node[][] nodes;
	public Map(int[][] map)
	{
		this.width = map[0].length;
		this.height = map.length;
		nodes = new Node[width][height];

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				nodes[x][y] = new Node(x, y, map[y][x] == 0 || map[y][x] == 2 || map[y][x] == 3 , map[y][x]);
			}
		}
	}

	public void drawMap(Graphics g, List<Node> path)
	{
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (!nodes[x][y].isWalkable() && nodes[x][y].getValue() == 1)
				{
					g.setColor(Color.DARK_GRAY);
				}
				else if (path != null && path.contains(new Node(x, y, true, 0)))
				{
					g.setColor(Color.PINK);
				}
				else
				{
					g.setColor(Color.white);
				}
				g.fillRect(x * 32, y * 32, 32, 32);
			}
		}
	}

	public Node getNode(int x, int y)
	{
		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			return nodes[x][y];
		}
		else
		{
			return null;
		}
	}

	public final List<Node> findPath(int startX, int startY, int goalX, int goalY)
	{

		if (startX == goalX && startY == goalY)
		{

			return new LinkedList<Node>();
		}


		List<Node> openList = new LinkedList<Node>();
		List<Node> closedList = new LinkedList<Node>();

		openList.add(nodes[startX][startY]);

		while (true)
		{
			Node current = lowestFInList(openList);
			openList.remove(current);
			closedList.add(current);

			if ((current.getX() == goalX) && (current.getY() == goalY))
			{
				return calcPath(nodes[startX][startY], current);
			}

			List<Node> adjacentNodes = getAdjacent(current, closedList);
			for (Node adjacent : adjacentNodes)
			{
				if (!openList.contains(adjacent))
				{

					adjacent.setParent(current);
					adjacent.setH(nodes[goalX][goalY]);
					adjacent.setG(current);
					openList.add(adjacent);
				}
				else if (adjacent.getG() > adjacent.calculateG(current))
				{
					adjacent.setParent(current);
					adjacent.setG(current);
				}
			}

			if (openList.isEmpty())
			{
				return new LinkedList<Node>();
			}
		}
	}

	private List<Node> calcPath(Node start, Node goal)
	{
		LinkedList<Node> path = new LinkedList<Node>();

		Node node = goal;
		boolean done = false;
		while (!done)
		{
			path.addFirst(node);
			node = node.getParent();
			if (node.equals(start))
			{
				done = true;
			}
		}
		return path;
	}

	private Node lowestFInList(List<Node> list)
	{
		Node cheapest = list.get(0);
		for (int i = 0; i < list.size(); i++)
		{
			if (list.get(i).getF() < cheapest.getF())
			{
				cheapest = list.get(i);
			}
		}
		return cheapest;
	}

	private List<Node> getAdjacent(Node node, List<Node> closedList)
	{
		List<Node> adjacentNodes = new LinkedList<Node>();
		int x = node.getX();
		int y = node.getY();

		Node adjacent;

		// left
		if (x > 0)
		{
			adjacent = getNode(x - 1, y);
			if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent))
			{
				adjacentNodes.add(adjacent);
			}
		}

		// right
		if (x < width)
		{
			adjacent = getNode(x + 1, y);
			if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent))
			{
				adjacentNodes.add(adjacent);
			}
		}

		// top
		if (y > 0)
		{
			adjacent = this.getNode(x, y - 1);
			if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent))
			{
				adjacentNodes.add(adjacent);
			}
		}

		//  bottom
		if (y < height)
		{
			adjacent = this.getNode(x, y + 1);
			if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent))
			{
				adjacentNodes.add(adjacent);
			}
		}
		return adjacentNodes;
	}

}
