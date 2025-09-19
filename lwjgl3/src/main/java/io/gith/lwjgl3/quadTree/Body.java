package io.gith.lwjgl3.quadTree;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.gith.lwjgl3.main.*;

public class Body implements Renderable, Updatable
{
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;
    private Vector2 lastAcceleration;
    private float mass;

    private Color color;
    private static Texture texture;

    public Vector2 getPosition() {return position;}
    public Vector2 getVelocity() {return velocity;}
    public Vector2 getAcceleration() {return acceleration;}
    public Vector2 getLastAcceleration() {return lastAcceleration;}
    public void setLastAcceleration(Vector2 acc) {this.lastAcceleration.set(acc);}
    public void setAcceleration(Vector2 acceleration) {this.acceleration = acceleration;}
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

    public Body(Vector2 position, Vector2 velocity, float mass, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.color = color;
        this.acceleration = new Vector2();
        this.lastAcceleration = new Vector2();
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
        velocity.x += acceleration.x * delta;
        velocity.y += acceleration.y * delta;
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    @Override
    public String toString() {
        return String.format(
            "Body{position=(%.15f, %.15f),\n velocity=(%.15f, %.15f),\n acceleration=(%.15f, %.15f),\n mass=%.1f, color=%s}",
            position.x, position.y,
            velocity.x, velocity.y,
            acceleration.x, acceleration.y,
            mass,
            color
        );
    }

}
