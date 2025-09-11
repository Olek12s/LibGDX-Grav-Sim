package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Body implements Renderable, Updatable
{
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;
    private float mass;

    private Color color;
    private static Texture texture;

    public Vector2 getPosition() {return position;}
    public Vector2 getVelocity() {return velocity;}
    public float getMass() {return mass;}
    public Color getColor() {return color;}
    public static Texture getTexture() {return texture;}

    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.drawPixel(0, 0);
        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
    }

    public Body(Vector2 position, Vector2 velocity, float Mass, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.color = color;
    }

    @Override
    public void render() {
        float zoom = Main.getInstance().getCameraController().getCamera().zoom;

        Resources.batch.setColor(color);
        Resources.batch.draw(texture, position.x - zoom/2f, position.y - zoom/2f, zoom, zoom);
        //Resources.batch.setColor(Color.WHITE);
    }

    @Override
    public void update(float delta) {
        //position.add(new Vector2(velocity).scl(delta)); // not optimal
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }
}
