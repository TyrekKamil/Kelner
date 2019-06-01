package game.astar;


public class Node
{

    protected static final int MOVEMENT_COST = 10;
    private int x;
    private int y;
    private boolean walkable;
    private Node parent;
    private int g;
    private int h;
    private int value;

    public Node(int x, int y, boolean walkable, int value)
    {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
        this.value = value;
    }

    public void setG(Node parent)
    {
        g = (parent.getG() + MOVEMENT_COST);
    }

    public int calculateG(Node parent)
    {
        return (parent.getG() + MOVEMENT_COST);
    }

    public void setH(Node goal)
    {
        h = (Math.abs(getX() - goal.getX()) + Math.abs(getY() - goal.getY())) * MOVEMENT_COST;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public boolean isWalkable()
    {
        return walkable;
    }

    public void setWalkable(boolean walkable)
    {
        this.walkable = walkable;
    }

    public Node getParent()
    {
        return parent;
    }
//

    public void setParent(Node parent)
    {
        this.parent = parent;
    }


    public int getF()
    {
        return g + h;
    }

    public int getG()
    {
        return g;
    }

    public int getH()
    {
        return h;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
            return false;
        if (!(o instanceof Node))
            return false;
        if (o == this)
            return true;

        Node n = (Node) o;
        if (n.getX() == x && n.getY() == y && n.isWalkable() == walkable)
            return true;
        return false;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
