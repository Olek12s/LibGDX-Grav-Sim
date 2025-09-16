package io.gith.lwjgl3.quadTree;


import com.badlogic.gdx.math.Vector2;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.*;

public class QuadTree
{
    private ArrayList<Node> nodes;  // [0] - root
    public static final int maxDepth = 16;     //TODO: make it dynamic
    public static float theta = 0.5f;   // 0 - On^2
    public static float epsilon = 5.05f;
    public static float G = 6.67430e-3f;           // original G: G = 6.67430e-11f
    private static ExecutorService executorService;
    private static int threadNum;
    static
    {
        threadNum = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(threadNum);
    }


    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public QuadTree() {
        nodes = new ArrayList<>(100000);
        nodes.add(new Node(new Quad(new Vector2(0, 0), (int)Math.pow(2, maxDepth))));  // 0x7FFF_FFFF int max
    }



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

    private void updateMassAndCenter(int nodeIndex)
    {
        Node node = nodes.get(nodeIndex);
        float mass = 0f;
        //Vector2 massCenter = new Vector2();
        Vector2 massCenter = node.getMassPosition();
        massCenter.set(0, 0);

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

    public void updateGravitationalAcceleration(ArrayList<Body> bodies) {
        for (Body b : bodies) {
            b.getAcceleration().set(0, 0);
            applyForce(0, b, b.getAcceleration());
        }
    }

    public void updateGravitationalAccelerationConcurrent(ArrayList<Body> bodies)
    {
        int chunkSize = (int) Math.ceil((double)bodies.size() / threadNum);
        CountDownLatch latch = new CountDownLatch(threadNum);

        for (int i = 0; i < threadNum; i++) {
            int startIdx = i * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, bodies.size());

            executorService.execute(() -> {
                try
                {
                    for (int j = startIdx; j < endIdx; j++) {
                        Body body = bodies.get(j);
                        body.getAcceleration().set(0, 0);
                        applyForce(0, body, body.getAcceleration());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();  // block main thread till latch is not 0
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    private void applyForce(int nodeIndex, Body body, Vector2 acceleration)
    {
        Node node = nodes.get(nodeIndex);
        if (node.getMass() == 0) return;
        Vector2 d = new Vector2(node.getMassPosition()).sub(body.getPosition());    // distance vector between body and node's mass position
        float dSq = d.len2();   // squared length
        float quadSizeSq = (float)node.getQuad().getSize() * node.getQuad().getSize();
        //if (quadSizeSq != 0) System.out.println("A");
        float thetaSq = theta * theta;
        float epsilonSq = epsilon * epsilon;

        if (node.isLeaf() && node.getBodies().size() == 1 && node.getBodies().get(0) == body) {
            return; // ignore self
        }

        if (node.isLeaf() || quadSizeSq < dSq * thetaSq) {  // compute acceleration by considering node total mass if leaf or met criteria
            if (dSq >= 0) {  // division by zero
                float invDist = 1.0f / (float)Math.sqrt(dSq + epsilonSq);
                float invDist3 = invDist * invDist * invDist;
                acceleration.mulAdd(d, G * node.getMass() * invDist3);  // multiply vec by scalar and add: t + (v * scalar)
            }
        }
        else    // compute acceleration by checking the children, if node is not leaf and does not met criteria of θ (recursion)
        {
            for (int i = 0; i < 4; i++) {
                int childIndex = node.getFirstChild() + i;
                if (childIndex < nodes.size()) {
                    applyForce(childIndex, body, acceleration);
                }
            }
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

    private boolean renderLeafSiblings(int nodeIndex) {
        Node node = nodes.get(nodeIndex);

        if (node.isLeaf()) {
            if (!node.getBodies().isEmpty()) {
                node.getQuad().render();
                return true;
            } else {
                return false;
            }
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

    public void renderRootVisualization() {
        nodes.get(0).getQuad().render();
    }
}

