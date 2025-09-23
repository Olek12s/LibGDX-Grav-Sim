package io.gith.lwjgl3.quadTree;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Node
{
    private int firstChild;              // n --- nodes n, n+1, n+2, n+3 are firstChild --- -1 - no firstChild
    //private int next;                // idx of the neighbor
    private float mass;
    private Vector2 massPosition;
    private Quad quad;
    public static final int MAX_BODIES_PER_NODE = 512;
    private ArrayList<Body> bodies;

    public Quad getQuad() {return quad;}
    public int getFirstChild() {return firstChild;}
    public float getMass() {return mass;}
    public Vector2 getMassPosition() {return massPosition;}
    public boolean isLeaf() {return firstChild == -1;}

    public ArrayList<Body> getBodies() {
        return bodies;
    }

    public void setBodies(ArrayList<Body> bodies) {
        this.bodies = bodies;
    }

    public void setFirstChild(int firstChild) {
        this.firstChild = firstChild;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setMassPosition(Vector2 massPosition) {
        this.massPosition = massPosition;
    }

    public void setQuad(Quad quad) {
        this.quad = quad;
    }

    public Node(Quad quad) {
        this.quad = quad;
        this.firstChild = -1;
        this.massPosition = new Vector2();
        this.mass = 0.0f;
        this.bodies = new ArrayList<>(MAX_BODIES_PER_NODE);
    }
}
