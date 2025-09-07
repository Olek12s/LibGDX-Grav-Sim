package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class CircleParticle implements Renderable, Updatable
{
    private Vector2 position;
    private Vector2 velocity;
    private Color color;
    private static Texture texture;

    static {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fillCircle(size / 2, size / 2, size / 2);
        texture = new Texture(pixmap);
        pixmap.dispose();
        System.out.println("a");
    }

    public CircleParticle(Vector2 position, Vector2 velocity, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.color = color;
    }

    private void createTexture() {

    }

    @Override
    public void render() {
        Resources.batch.setColor(color);
        float size = 64;
        Resources.batch.draw(texture, position.x - size/2, position.y - size/2, size, size);
        Resources.batch.setColor(Color.WHITE);
    }

    @Override
    public void update(float delta) {

    }
}
