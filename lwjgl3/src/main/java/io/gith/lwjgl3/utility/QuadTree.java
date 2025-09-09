package io.gith.lwjgl3.utility;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.gith.lwjgl3.CircleParticle;
import io.gith.lwjgl3.Renderable;
import io.gith.lwjgl3.Updatable;

import java.util.ArrayList;

public class QuadTree implements Renderable, Updatable {

    private Node root;

    @Override
    public void render() {

    }

    @Override
    public void update(float delta) {

    }

    private static class Node {
        private Node[] children;
        private int accumulatedMass;
        private Vector2 centerOfMass;
        ArrayList<CircleParticle> particles;
        private float x, y, size;
        private int limit = 4;

        Node(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.children = new Node[4];
            this.particles = new ArrayList<>();
            this.centerOfMass = new Vector2();
            this.accumulatedMass = 0;
        }

        boolean isLeaf() {
            for (Node child : children)
                if (child != null) return false;
            return true;
        }

        boolean contains(CircleParticle p) {
            Vector2 pos = p.getPosition();
            return pos.x >= x && pos.x < x + size &&
                pos.y >= y && pos.y < y + size;
        }
    }

    public QuadTree(float x, float y, float size) {
        this.root = new Node(x, y, size);
    }

    public void insert(CircleParticle p) {
        insert(root, p);
    }

    private void insert(Node node, CircleParticle p) {
        if (!node.contains(p)) return;

        if (node.isLeaf()) {
            node.particles.add(p);
            updateMass(node, p);

            if (node.particles.size() > node.limit) {
                subdivide(node);
                ArrayList<CircleParticle> temp = new ArrayList<>(node.particles);
                node.particles.clear();
                for (CircleParticle particle : temp) {
                    for (Node child : node.children) {
                        insert(child, particle);
                    }
                }
            }
            return;
        }

        for (Node child : node.children) {
            insert(child, p);
        }

        updateMass(node, p);
    }



    private void subdivide(Node node) {
        float half = node.size / 2f;
        node.children[0] = new Node(node.x,         node.y,         half); // NW
        node.children[1] = new Node(node.x + half,  node.y,         half); // NE
        node.children[2] = new Node(node.x,         node.y + half,  half); // SW
        node.children[3] = new Node(node.x + half,  node.y + half,  half); // SE
    }

    private void updateMass(Node node, CircleParticle p) {
        float totalMass = node.accumulatedMass + p.getMass();
        if (totalMass == 0) return;

        Vector2 weighted = new Vector2(node.centerOfMass).scl(node.accumulatedMass);
        weighted.add(new Vector2(p.getPosition()).scl(p.getMass()));
        node.centerOfMass.set(weighted.scl(1f / totalMass));
        node.accumulatedMass += p.getMass();
    }


    public void updateAll() {
        updateNode(root);
    }

    private void updateNode(Node node) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.particles.size(); i++) {
                CircleParticle p = node.particles.get(i);
                if (!node.contains(p)) {
                    node.particles.remove(i);
                    i--;
                    insert(root, p);
                }
            }
        } else {
            for (Node child : node.children) {
                if (child != null) updateNode(child);
            }
        }
    }


    public void renderDebug(ShapeRenderer renderer) {
        renderDebugNode(renderer, root);
    }

    private void renderDebugNode(ShapeRenderer renderer, Node node) {
        if (node == null) return;
        renderer.setColor(Color.YELLOW);
        renderer.rect(node.x, node.y, node.size, node.size);
        if (!node.isLeaf()) {
            for (Node child : node.children) renderDebugNode(renderer, child);
        }
    }
}
