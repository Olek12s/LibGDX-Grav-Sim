package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Quad implements Renderable
{
    private Vector2 center;
    private int size;
    private static Texture texture;

    public Vector2 getCenter() {return center;}
    public void setCenter(Vector2 center) {this.center = center;}

    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public Quad(Vector2 center, int size) {
        this.center = center;
        this.size = size;
    }

    @Override
    public void render() {
        float x = center.x - size / 2f;
        float y = center.y - size / 2f;

        Resources.batch.setColor(new Color(150, 50, 0, 0.5f));
        Resources.batch.draw(texture, x, y, size, size);
    }
}
