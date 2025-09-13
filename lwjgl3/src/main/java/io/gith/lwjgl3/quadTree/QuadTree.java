package io.gith.lwjgl3.quadTree;

/*
package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleManager implements Renderable, Updatable {

    private ArrayList<Body> particles;

    public ParticleManager() {
        this.particles = new ArrayList<>();

        int n = 1_000_000;
        //int n = 500;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            Body p = new Body(new Vector2(
                random.nextInt(800),
                random.nextInt(600)),
                new Vector2(random.nextFloat(20f) - 10f,
                    random.nextFloat(20f) - 10f),
                0,
                Color.CYAN);

            particles.add(p);
        }
    }


    @Override
    public void render() {
       for (Body particle : particles) {
           particle.render();
       }
    }

    @Override
    public void update(float delta) {
        for (Body particle : particles) {
            particle.update(delta);
        }
    }
}
 */

import com.badlogic.gdx.math.Vector2;
import io.gith.lwjgl3.Main;
import io.gith.lwjgl3.Renderable;
import io.gith.lwjgl3.Updatable;

import java.util.ArrayList;

/*
public class QuadTree implements Updatable
{
    private int root;
    private ArrayList<Node> nodes;

    public int getRoot() {return root;}
    public ArrayList<Node> getNodes() {return nodes;}

    public QuadTree() {
        this.nodes = new ArrayList<>();
    }

    public void buildRoot(ArrayList<Body> bodies)
    {
        if (bodies.isEmpty()) return;

        float xmin = Float.MAX_VALUE;
        float ymin = Float.MAX_VALUE;
        float xmax = Float.MIN_VALUE;
        float ymax = Float.MIN_VALUE;

        for (Body b : bodies) {
            Vector2 p = b.getPosition();
            if (p.x < xmin) xmin = p.x;
            if (p.y < ymin) ymin = p.y;
            if (p.x > xmax) xmax = p.x;
            if (p.y > ymax) ymax = p.y;
        }

        float width = xmax - xmin;
        float height = ymax - ymin;
        float size = Math.max(width, height);
        Vector2 center = new Vector2(xmin + width / 2f, ymin + height / 2f);

        nodes.clear();
        nodes.add(new Node(new Quad(center, (int)size), 0));
        root = 0;
    }


    public void erase() {
        this.nodes.clear();
        this.nodes.add(new Node());
    }

   // /**
     * adds a new body to the quadtree, subdividing nodes as necessary so that each body ends up in the correct leaf.
     * @param pos - position of new body
     * @param mass - mass of new body
   //  */
/*
    private void insert(int nodeIndex, Vector2 pos, float mass) {
        Node node = nodes.get(nodeIndex);

        if (node.isLeaf()) {
            if (node.isEmpty()) {
                node.setMassPosition(pos);
                node.setMass(mass);
            } else {
                // jeśli quad większy niż minimalny -> subdivide
                if (node.getQuad().getSize() > 1) {
                    int childrenIndex = subDivide(nodeIndex);

                    // przenieś istniejący punkt do dziecka
                    Node leaf = nodes.get(nodeIndex);
                    Vector2 existingPos = leaf.getMassPosition();
                    float existingMass = leaf.getMass();
                    int q1 = leaf.getQuad().findQuadrant(existingPos);
                    insert(childrenIndex + q1, existingPos, existingMass);

                    // wyzeruj liść
                    leaf.setMass(0);

                    // wstaw nowy punkt
                    int q2 = leaf.getQuad().findQuadrant(pos);
                    insert(childrenIndex + q2, pos, mass);
                } else {
                    // min size reached, sum masy
                    node.setMass(node.getMass() + mass);
                }
            }
        } else {
            // branch → idź do odpowiedniego dziecka
            int childIndex = node.getFirstChild() + node.getQuad().findQuadrant(pos);
            insert(childIndex, pos, mass);
        }
    }


/*
   // /**
     * Subdivides the given node into four child nodes, representing the four quadrants
     * of the node's current square (Quad).
     *
     * <p>
     * The "next" field for each child is set as follows:
     * <ul>
     *     <li>Child 0 → next = index of child 1</li>
     *     <li>Child 1 → next = index of child 2</li>
     *     <li>Child 2 → next = index of child 3</li>
     *     <li>Child 3 → next = parent node's next (can be 0 if no next node exists)</li>
     *
     * @param nodeIndex the index of the node to subdivide in the quadtree's node list
     * @return the index of the first child node created
     */
/*
    public int subDivide(int nodeIndex) {
        System.out.println("sub1");
        Node node = nodes.get(nodeIndex);

        int children = nodes.size();    // idx of first child
        node.setFirstChild(children);

        Quad[] quads = node.getQuad().toQuadrants();

        int[] nexts = new int[] {
            children + 1,
            children + 2,
            children + 3,
            node.getNext()
        };

        for (int i = 0; i < 4; i++) {
            nodes.add(new Node(quads[i], nexts[i]));
            System.out.println("sub");
        }
        return children;
    }

    public void propagate() {
        // bottom-up approach
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);

            if (node.isLeaf()) continue; // leaf nodes already store mass and position

            int firstChild = node.getFirstChild();

            // sum weighted positions of children
            Vector2 bodyPos = new Vector2(0, 0);
            float totalMass = 0f;
            for (int j = 0; j < 4; j++) {
                Node child = nodes.get(firstChild + j);
                Vector2 childPos = new Vector2(child.getMassPosition());
                bodyPos.add(childPos.scl(child.getMass()));
                totalMass += child.getMass();
            }

            // compute (scale) center of mass       (multiplying vectors)
            bodyPos.scl(1.0f / totalMass);

            node.setMassPosition(bodyPos);
            node.setMass(totalMass);
        }
    }

    /*
    public Vector2 acceleration(Vector2 bodyPosition, float theta, float epsilon) {
        Vector2 acc = new Vector2(0, 0);

        float t_sq = theta * theta;
        float e_sq = epsilon * epsilon; // softening

        ArrayList<Integer> stack = new ArrayList<>();
        stack.add(root);

        // DFS traversal
        while (!stack.isEmpty()) {
            int nodeIndex = stack.remove(stack.size() - 1);
            Node n = nodes.get(nodeIndex);

            Vector2 d = new Vector2(n.getMassPosition()).sub(bodyPosition);
            float d_sq = d.x*d.x + d.y*d.y;

            // treat mass as single point (center of mass)
            if (n.isLeaf() || n.getQuad().getSize() * n.getQuad().getSize() < d_sq * t_sq) {
                float denom = (d_sq + e_sq) * (float)Math.sqrt(d_sq);
                acc.add(new Vector2(d).scl(Math.min(n.getMass() / denom, Float.MAX_VALUE)));
            }
            else {
                for (int i = 0; i < 4; i++)     // push children onto stack to traverse deeper
                {
                    stack.add(n.getFirstChild() + i);
                }
            }
        }
        return acc;
    }
    */

    // faster acceleration method
/*
    public Vector2 acceleration(Vector2 pos, float theta, float epsilon) {
        Vector2 acc = new Vector2(0, 0);
        float t_sq = theta * theta;
        float e_sq = epsilon * epsilon;

        int nodeIndex = root;

        while (true) {
            Node n = nodes.get(nodeIndex);

            Vector2 d = new Vector2(n.getMassPosition()).sub(pos);  // vector from body to the node's center of mass
            float d_sq = d.len2();

            // if leaf node or node is sufficiently far away, approximate as single mass
            if (n.isLeaf() || n.getQuad().getSize() * n.getQuad().getSize() < d_sq * t_sq) {
                float denom = (d_sq + e_sq) * (float)Math.sqrt(d_sq);
                acc.add(d.scl(Math.min(n.getMass() / denom, Float.MAX_VALUE)));

                // move to next node at the same level, or stop if no next
                if (n.getNext() == 0) {
                    break;
                }
                nodeIndex = n.getNext();
            }
            else {
                nodeIndex = n.getFirstChild();     // traverse to first child to process sub-quadrants
            }
        }

        return acc;
    }

    public void renderTree() {
        if (nodes.isEmpty()) return;
        renderNode(nodes.get(root));
    }

    private void renderNode(Node node) {
        if (node.getQuad() != null) {
            node.getQuad().render();
        }

        if (!node.isLeaf()) {
            int firstChild = node.getFirstChild();
            for (int i = 0; i < 4; i++) {
                renderNode(nodes.get(firstChild + i));
                System.out.println(i);
            }
        }
    }


    @Override
    public void update(float delta) {
        ArrayList<Body> bodies = Main.getInstance().getParticles();
        if (bodies.isEmpty()) return;

        buildRoot(bodies);
        for (Body b : bodies) {
            insert(root, b.getPosition(), b.getMass());
        }

        propagate();
    }
    */

public class QuadTree
{
    private ArrayList<Node> nodes;  // [0] - root

    public QuadTree() {
        nodes = new ArrayList<>();
        nodes.add(new Node(new Quad(new Vector2(0, 0), (int)Math.pow(2, 11))));  // 0x7FFF_FFFF int max
    }

    public void insertBody(int nodeIndex, Vector2 pos, float mass)
    {
        Node node = nodes.get(nodeIndex);
        System.out.println("insertBody call: " + nodeIndex + " " + pos + " " + mass + " size: " + node.getQuad().getSize());
        while(true)
        {
            int quadrantNum = node.getQuad().findQuadrant(pos);
            if (quadrantNum == -1) return;  // out of bounds

            if (node.isLeaf()) {    // if leaf
                if (node.getMass() == 0f)   // empty leaf - insert body
                {
                    node.setMass(mass);
                    node.setMassPosition(pos);
                    return;
                }
                else if (node.getQuad().getSize() <= 1) // minimal size of Node - sum masses
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
                    Vector2 existingPos = new Vector2(node.getMassPosition());
                    float existingMass = node.getMass();
                    node.setMass(0f);

                    int existingQuadrant = node.getQuad().findQuadrant(existingPos);
                    insertBody(firstChildIndex + existingQuadrant, existingPos, existingMass);

                    node = nodes.get(firstChildIndex + quadrantNum);
                    System.out.println(node.getQuad().getSize());
                }
            }
            else    // is not leaf -> go deeper
            {
                node = nodes.get(node.getFirstChild() + quadrantNum);
                System.out.println(":" + node.getQuad().getSize());
            }
        }
    }

    public void erase() {
        nodes.clear();
        nodes.add(new Node(new Quad(new Vector2(0, 0), (int)Math.pow(2, 11))));
    }

    public void renderVisualization()
    {
        nodes.get(0).getQuad().render();
    }

}

