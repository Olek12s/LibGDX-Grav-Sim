package io.gith.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import io.gith.lwjgl3.quadTree.Body;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.ArrayList;
import java.util.Random;

public class Main extends ApplicationAdapter {
    private static Main instance;

    private ArrayList<Renderable> renderables;
    private ArrayList<Updatable> updatables;
    public static int MAX_UPS = 30;   // logic updates per second
    public static int MAX_FPS = 30;   // rendering frames per second
    private static int SS = 1;
    private float logicInterval = 1f / MAX_UPS;  // seconds per logic update
    private float accumulator = 0; // acc λt
    private long lastRenderTime = 0; // to limit FPS

    private int frames = 0;
    private int updates = 0;
    private float fpsUpsTimer = 0f;

    private CameraController cameraController;
    private InputController inputController;
    private ArrayList<Body> particles;
    private QuadTree quadTree;


    public static Main getInstance() {return instance;}
    public InputController getInputController() {return inputController;}
    public CameraController getCameraController() {return cameraController;}
    public ArrayList<Body> getParticles() {return particles;}
    public ArrayList<Renderable> getRenderables() {return renderables;}
    public ArrayList<Updatable> getUpdatables() {return updatables;}

    public void create() {
        instance = this;
        particles = new ArrayList<>();
        renderables = new ArrayList<>();
        updatables = new ArrayList<>();
        cameraController = new CameraController();
        inputController = new InputController();
        Resources.batch = new SpriteBatch();
        Gdx.input.setInputProcessor(inputController);


        updatables.add(cameraController);

        galaxy(200000, 800f, 500_000_000f);
        quadTree = new QuadTree(particles);
    }

    public void galaxy(int n, float radius, float centralMass) {
        Body central = new Body(new Vector2(0, 0), new Vector2(0, 0), centralMass, Color.YELLOW);
        particles.add(central);
        renderables.add(central);
        updatables.add(central);

        Body central2 = new Body(new Vector2(1110, 541), new Vector2(0, 0), centralMass, Color.YELLOW);
        particles.add(central2);
        renderables.add(central2);
        updatables.add(central2);

        Random r = new Random();
        int arms = 8;
        float spiralTightness = 0.5f;
        float angleJitter = 0.2f;

        for (int i = 0; i < n; i++) {
            float dist = (float)(Math.pow(r.nextFloat(), 2) * radius);

            int arm = r.nextInt(arms);
            float angle = (float) (spiralTightness * Math.log(dist + 1) + (2 * Math.PI / arms) * arm);
            angle += r.nextGaussian() * angleJitter;

            float x = (float) Math.cos(angle) * dist;
            float y = (float) Math.sin(angle) * dist;

            float speed = (float) Math.sqrt(QuadTree.G * centralMass / (dist + QuadTree.epsilon));
            float vx = (float) (-Math.sin(angle) * speed);
            float vy = (float) ( Math.cos(angle) * speed);

            Body b = new Body(
                new Vector2(x, y),
                new Vector2(vx/4, vy/4),
                Math.max(r.nextInt(100), 1),
                Color.LIGHT_GRAY
            );

            particles.add(b);
            renderables.add(b);
            updatables.add(b);
        }
    }





    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;
        fpsUpsTimer += delta;

        int maxUpdatesPerFrame = MAX_UPS / MAX_FPS;
        int updatesThisFrame = 0;

        while (accumulator >= logicInterval && updatesThisFrame < maxUpdatesPerFrame) {
            for (Updatable u : updatables) {
                u.update(logicInterval * SS);
            }
            accumulator -= logicInterval;
            updates++;
            updatesThisFrame++;
        }

        if (updatesThisFrame == maxUpdatesPerFrame) {
            accumulator = 0;
        }

        if (MAX_FPS > 0) {
            long now = System.nanoTime();
            long minFrameTime = 1_000_000_000L / MAX_FPS;
            if (lastRenderTime > 0) {
                long frameDuration = now - lastRenderTime;
                if (frameDuration < minFrameTime) {
                    try {
                        Thread.sleep((minFrameTime - frameDuration) / 1_000_000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            lastRenderTime = System.nanoTime();
        }
        draw();
        frames++;

        if (fpsUpsTimer >= 1f) {
            System.out.println("FPS: " + frames + " | UPS: " + updates);
            frames = 0;
            updates = 0;
            fpsUpsTimer -= 1f;
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        cameraController.getCamera().update();
        Resources.batch.setProjectionMatrix(cameraController.getCamera().combined);

        Resources.batch.begin();
        for (Renderable r : renderables) {
            r.render();
        }
        Resources.batch.end();
    }

    public void resize (int width, int height) {
        cameraController.getViewport().update(width, height, true);
    }

    public void pause () {
    }

    public void resume () {
    }

    public void dispose () {
    }
}
