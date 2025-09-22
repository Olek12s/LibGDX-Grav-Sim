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
        //Main.getInstance().getUpdatables().add(this);
        //Main.getInstance().getRenderables().add(this);
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.color = color;
        this.acceleration = new Vector2();
        this.lastAcceleration = new Vector2();
    }

    /*
    @Override
    public void render() {
        float zoom = Main.getInstance().getCameraController().getCamera().zoom;
        Resources.batch.setColor(color);
        Resources.batch.draw(texture, position.x - zoom/2f, position.y - zoom/2f, zoom, zoom);
        //Resources.batch.setColor(Color.WHITE);
    }
    */

    @Override
    public void render() {
        float zoom = Main.getInstance().getCameraController().getCamera().zoom;

        float speed = velocity.len();
        float minIntensity = 0.1f;


        float normalizedSpeed = QuadTree.avgSpeed/4 > 0 ? speed / QuadTree.avgSpeed/4 : 1f;
        float intensity = (float)Math.sqrt(Math.min(normalizedSpeed, 1f));


        float maxDelta = 0.8f;
        float finalIntensity = minIntensity + Math.min(intensity * (1f - minIntensity), maxDelta);

        Color dynamicColor = new Color(0f, 0.9f, 0.9f, finalIntensity);

        Resources.batch.setColor(dynamicColor);
        Resources.batch.draw(texture, position.x - zoom / 2f, position.y - zoom / 2f, zoom, zoom);
    }

    @Override
    public void update(float delta) {
        euler(delta);   //  <-- euler method, updating bodies moved to the QuadTree class
    }

    private void euler(float delta) {
        velocity.x += acceleration.x * delta;
        velocity.y += acceleration.y * delta;
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public void leapFrogVStep(float halfDt) {
        velocity.x += acceleration.x * halfDt;
        velocity.y += acceleration.y * halfDt;
    }

    public void leapFrogPStep(float dt) {
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
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
