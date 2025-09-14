package io.gith.lwjgl3.quadTree;


import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class QuadTree
{
    private ArrayList<Node> nodes;  // [0] - root
    private static final int maxDepth = 18;     //TODO: make it dynamic

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public QuadTree() {
        nodes = new ArrayList<>(100000);
        nodes.add(new Node(new Quad(new Vector2(0, 0), (int)Math.pow(2, maxDepth))));  // 0x7FFF_FFFF int max
    }

    /*
    public void insertBody(int nodeIndex, Vector2 pos, float mass)
    {
        Node node = nodes.get(nodeIndex);
        while(true)
        {
            int quadrantNum = node.getQuad().findQuadrant(pos);
            if (quadrantNum == -1) {
                System.out.println("Body out of bounds of the QuadTree");
                return;  // out of bounds
            }

            if (node.isLeaf()) {    // if leaf
                if (node.getMass() == 0f)   // empty leaf - insert body
                {
                    node.setMass(mass);
                    node.setMassPosition(pos);
                    return;
                }
                else if (node.getQuad().getSize() <= 128) // minimal size of Node - sum masses
                {
                    node.setMass(node.getMass() + mass);
                    return;
                }
                else    // leaf occupied - divide into quadrants
                {
                    Quad[] quadrants = node.getQuad().toQuadrants();
                    int firstChildIndex = nodes.size();
                    node.setFirstChild(firstChildIndex);

                    for (int i = 0; i < 4; i++) {
                        nodes.add(new Node(quadrants[i]));
                    }

                    // move previous body to one of the children


                    //node.setMass(0f);
                    int existingQuadrant = node.getQuad().findQuadrant(node.getMassPosition());
                    insertBody(firstChildIndex + existingQuadrant, node.getMassPosition(), node.getMass());

                    node = nodes.get(firstChildIndex + quadrantNum);
                }
            }
            else    // is not leaf -> go deeper
            {
                node = nodes.get(node.getFirstChild() + quadrantNum);
            }
        }
    }
    */
    public void insertBody(int nodeIndex, Body body)
    {
        Node node = nodes.get(nodeIndex);
        while(true)
        {
            if (node.getQuad().getSize() <= Quad.MIN_QUAD_SIZE) {
                node.getBodies().add(body);
                return;
            }

            int quadrantNum = node.getQuad().findQuadrant(body.getPosition());
            if (quadrantNum == -1) {
                System.out.println("Body out of bounds of the QuadTree");
                return;  // out of bounds
            }

            if (node.isLeaf()) {    // if leaf
                if (node.getBodies().size() < Node.MAX_BODIES_PER_NODE)   // if can add body to the node
                {
                    node.getBodies().add(body);
                    return;
                }
                else    // cannot add body - divide into quadrants
                {
                    Quad[] quadrants = node.getQuad().toQuadrants();
                    int firstChildIndex = nodes.size();
                    node.setFirstChild(firstChildIndex);

                    for (int i = 0; i < 4; i++) {
                        nodes.add(new Node(quadrants[i]));
                    }

                    // move all bodies to the children
                    //ArrayList<Body> oldBodies = new ArrayList<>(node.getBodies());
                    //node.getBodies().clear();

                    for (Body b : node.getBodies()) {
                        int childQuadrant = node.getQuad().findQuadrant(b.getPosition());
                        insertBody(firstChildIndex + childQuadrant, b);
                    }
                    node.getBodies().clear();
                    node = nodes.get(firstChildIndex + quadrantNum);
                }
            }
            else
            {
                node = nodes.get(node.getFirstChild() + quadrantNum);
            }
        }
    }
    public void erase() {
        nodes.clear();
        nodes.add(new Node(new Quad(new Vector2(0, 0), (int)Math.pow(2, maxDepth))));
    }



    private boolean renderLeafSiblings(int nodeIndex) {
        Node node = nodes.get(nodeIndex);

        if (node.isLeaf()) {
            return !node.getBodies().isEmpty();
        }

        int firstChild = node.getFirstChild();
        boolean anyChildHasBody = false;
        boolean[] childHasBody = new boolean[4];

        for (int i = 0; i < 4; i++) {
            childHasBody[i] = renderLeafSiblings(firstChild + i);
            anyChildHasBody |= childHasBody[i];
        }

        for (int i = 0; i < 4; i++) {
            Node child = nodes.get(firstChild + i);
            if (child.isLeaf() && childHasBody[i]) {
                for (int j = 0; j < 4; j++) {
                    Node sibling = nodes.get(firstChild + j);
                    if (sibling.isLeaf() && !sibling.getBodies().isEmpty()) {
                        sibling.getQuad().render();
                    }
                }
                break;
            }
        }

        return anyChildHasBody;
    }

    private void updateMassAndCenter(int nodeIndex)
    {
        Node node = nodes.get(nodeIndex);
        float mass = 0f;
        Vector2 massCenter = new Vector2();

        if (node.isLeaf()) {    // no children  masses are computed by all bodies contained by node

            for (Body b : node.getBodies()) {
                mass += b.getMass();
                massCenter.x += b.getPosition().x * b.getMass();
                massCenter.y += b.getPosition().y * b.getMass();
            }

            if (mass > 0) {
                massCenter.scl(1f / mass); // scale by scalar
            }

            node.setMass(mass); // no mass if leaf has no bodies
            node.setMassPosition(massCenter);
        }
        else    // children are present - masses are computed by bottom-up approach (propagation)
        {
            for (int i = 0; i < 4; i++) {
                int childIndex = node.getFirstChild() + i;
                updateMassAndCenter(childIndex);        // bottom up approach - update children

                Node child = nodes.get(childIndex);
                mass += child.getMass();
                massCenter.x += child.getMassPosition().x * child.getMass();
                massCenter.y += child.getMassPosition().y * child.getMass();
            }

            if (mass > 0) {
                massCenter.scl(1f / mass); // scale by scalar
            }
            node.setMass(mass);
            node.setMassPosition(massCenter);
        }
    }


    public void updateMassDirstribution()
    {
        if (!nodes.isEmpty())
        {
            updateMassAndCenter(0);
        }
    }




    public void renderVisualization() {
        if (nodes.isEmpty()) return;
        renderLeafSiblings(0);
    }

    public void renderRootVisualization() {
        nodes.get(0).getQuad().render();
    }
}

