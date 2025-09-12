package io.gith.lwjgl3.quadTree;

import com.badlogic.gdx.math.Vector2;

public class Node
{
    private int children;            // 0 --- node is leaf // n --- nodes n, n+1, n+2, n+3 are children
    private int next;                // idx of the neighbor
    private Vector2 massPosition;
    private float mass;
    private Quad quad;

    public boolean isLeaf() {return children == 0;}
    public boolean isBranch() {return children != 0;}
    public boolean isEmpty() {return mass == 0;}
    public int getChildren() {return children;}
    public void setChildren(int children) {this.children = children;}
    public int getNext() {return next;}
    public void setNext(int next) {this.next = next;}
    public Vector2 getMassPosition() {return massPosition;}
    public void setMassPosition(Vector2 massPosition) {this.massPosition = massPosition;}
    public float getMass() {return mass;}
    public void setMass(float mass) {this.mass = mass;}
    public Quad getQuad() {return quad;}
    public void setQuad(Quad quad) {this.quad = quad;}

    public Node() {
        this.next = 0;
        this.children = 0;
        this.massPosition = new Vector2(0, 0);
        this.mass = 0.0f;
    }

    public Node(Quad quad, int next) {
        this.quad = quad;
        this.next = next;
        this.children = 0;
        this.massPosition = new Vector2(0, 0);
        this.mass = 0.0f;
    }

}
