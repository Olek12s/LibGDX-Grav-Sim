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
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.drawPixel(0, 0);
        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
    }

    public CircleParticle(Vector2 position, Vector2 velocity, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.color = color;
    }

    @Override
    public void render() {
        Resources.batch.setColor(color);
        float size = 1;
        Resources.batch.draw(texture, position.x - size/2, position.y - size/2, 1, 1);
        Resources.batch.setColor(Color.WHITE);
    }

    @Override
    public void update(float delta) {
        //position.add(new Vector2(velocity).scl(delta)); // not optimal
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }
}
