package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class CircleParticle implements Renderable, Updatable
{
    private Vector2 position;
    private Vector2 velocity;
    private float mass;
    private Color color;
    private static Texture texture;

    public Vector2 getPosition() {return position;}
    public Vector2 getVelocity() {return velocity;}
    public float getMass() {return mass;}

    public Color getColor() {return color;}
    public static Texture getTexture() {return texture;}

    /* pixel
    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.drawPixel(0, 0);
        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
    }
    */

    // circle
    static {
        int radius = 16;
        int size = radius * 2;

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        pixmap.setColor(1, 1, 1, 1);
        pixmap.fillCircle(radius, radius, radius);

        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
    }


    public CircleParticle(Vector2 position, Vector2 velocity, float Mass, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.color = color;
    }

    @Override
    public void render() {
        Resources.batch.setColor(color);
        Resources.batch.draw(texture, position.x - 1/2, position.y - 1/2, 1, 1);
        //Resources.batch.setColor(Color.WHITE);
    }

    @Override
    public void update(float delta) {
        //position.add(new Vector2(velocity).scl(delta)); // not optimal
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }
}
