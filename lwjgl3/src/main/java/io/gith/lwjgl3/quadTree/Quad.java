package io.gith.lwjgl3.quadTree;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.gith.lwjgl3.Renderable;
import io.gith.lwjgl3.Resources;

public class Quad implements Renderable
{
    private Vector2 center;
    private int size;
    private static Texture texture;
    private static Color[] colors;

    public Vector2 getCenter() {return center;}
    public void setCenter(Vector2 center) {this.center = center;}


    public Color getColor() {
        int index = Math.max(0, Math.min(colors.length - 1, (int)(Math.log(size) / Math.log(2))));
        return colors[index];
    }


    public int getSize() {return size;}
    public void setSize(int size) {this.size = size;}
    public static Texture getTexture() {return texture;}

    public static void setTexture(Texture texture) {
        Quad.texture = texture;
    }

    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();

        // lower index = smaller quads (min size = 1)
        colors = new Color[32];

        for (int i = 0; i <= 7; i++) {
            float t = i / 7f;
            float r = 0.784f;
            float g = 0f;
            float b = 0f;
            float alpha = 0.2f + t * 0.7f;
            colors[i] = new Color(r, g, b, alpha);
        }
        for (int i = 8; i <= 15; i++) {
            float t = (i - 8) / 7f;
            t = (float)Math.pow(t, 0.5f);
            float r = 1f;
            float g = 0.5f + t * 0.5f;
            float b = 0f;
            float alpha = 0.9f;
            colors[i] = new Color(r, g, b, alpha);
        }
        for (int i = 16; i <= 23; i++) {
            float t = (i - 16) / 7f;
            t = (float)Math.pow(t, 0.5f);
            float r = t;
            float g = 1f;
            float b = 0f;
            float alpha = 0.9f;
            colors[i] = new Color(r, g, b, alpha);
        }
        for (int i = 24; i <= 31; i++) {
            float t = (i - 24) / 7f;
            float r = 0.8f * (1 - t);
            float g = 1f;
            float b = t;
            float alpha = 0.9f;
            colors[i] = new Color(r, g, b, alpha);
        }
    }

    public Quad(Vector2 center, int size) {
        this.center = center;
        this.size = size;
    }

    public Quad[] toQuadrants() {
        float half = size / 2f;
        return new Quad[] {
            new Quad(new Vector2(center.x - half/2, center.y - half/2), (int) half), // left-bottom
            new Quad(new Vector2(center.x + half/2, center.y - half/2), (int) half), // right-bottom
            new Quad(new Vector2(center.x - half/2, center.y + half/2), (int) half), // left-top
            new Quad(new Vector2(center.x + half/2, center.y + half/2), (int) half)  // right-top
        };
    }

    /**
     * Determines which quadrant of this Quad a given position belongs to.
     *
     * <p>
     * The Quad is divided into four quadrants relative to its center:
     * <ul>
     *     <li>0 = left-bottom</li>
     *     <li>1 = right-bottom</li>
     *     <li>2 = left-top</li>
     *     <li>3 = right-top</li>
     * </ul>
     *
     * @param pos the position to check
     * @return an integer 0-3 representing the quadrant:
     *         0 = left-bottom, 1 = right-bottom, 2 = left-top, 3 = right-top
     */
    public int findQuadrant(Vector2 pos) {
        int xBit = (pos.x > center.x) ? 1 : 0;
        int yBit = (pos.y > center.y) ? 2 : 0;
        return xBit | yBit;
    }

    @Override
    public void render() {
        float x = center.x - size / 2f;
        float y = center.y - size / 2f;

        /*
        size    index
        1   ->  0
        2   ->  1
        4   ->  2
        8   ->  3
        16  ->  4
        */
        int index = Math.max(0, Math.min(colors.length - 1, (int) (Math.log(size) / Math.log(2))));
        Color color = colors[index];

        Resources.batch.setColor(color);
        Resources.batch.draw(texture, x, y, size, size);
    }
}
