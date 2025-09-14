package io.gith.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.gith.lwjgl3.quadTree.Body;
import io.gith.lwjgl3.quadTree.Quad;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main extends ApplicationAdapter {
    private static Main instance;

    private ArrayList<Renderable> renderables;
    private ArrayList<Updatable> updatables;
    private static int MAX_UPS = 30;   // logic updates per second
    private static int MAX_FPS = 30;   // rendering frames per second
    private static int SS = 4;
    private float logicInterval;   // seconds per logic update
    private float accumulator = 0; // acc λt
    private long lastRenderTime = 0; // to limit FPS

    private int frames = 0;
    private int updates = 0;
    private float fpsUpsTimer = 0f;

    private CameraController cameraController;
    private InputController inputController;
    private ArrayList<Body> particles;
    private QuadTree quadTree;

    public static int getMaxUps() {return MAX_UPS;}
    public static int getMaxFps() {return MAX_FPS;}
    public static Main getInstance() {return instance;}
    public InputController getInputController() {return inputController;}
    public CameraController getCameraController() {return cameraController;}
    public ArrayList<Body> getParticles() {
        return particles;
    }
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
        logicInterval = 1f / MAX_UPS;


        quadTree = new QuadTree();
        //galaxy(15000, 500f, 1_000_00f);
        galaxy(15000, 500f, 1f);
        //b = new Body(new Vector2(50,64), new Vector2(0,0), 1000000f, new Color(Color.PINK));
        //quadTree.insertBody(0, b);
        //particles.add(b);
        //updatables.add(b);
        //renderables.add(b);
        int n = 0;
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            Body body = (new Body(
                new Vector2(r.nextInt(50), r.nextInt(50)),
                //new Vector2(r.nextFloat() * 10f - 5f, r.nextFloat() * 10f - 5f),
                //new Vector2(0,0),
                new Vector2(r.nextFloat() * 0.10f, r.nextFloat() * 0.10f),
                r.nextInt(500),
                //r.nextInt(Math.max(1, 2000)),

                //new Vector2(r.nextInt(12), r.nextInt(12)),
                //new Vector2(0,0),
                //r.nextInt(50),
                new Color(0.7f,0.7f,0.7f,0.7f)));

            particles.add(body);
            updatables.add(body);
            renderables.add(body);
        }
    }

    public void galaxy(int n, float radius, float centralMass) {
        Body central = new Body(
            new Vector2(0, 0),
            new Vector2(0, 0),
            centralMass,
            Color.YELLOW
        );
        particles.add(central);
        renderables.add(central);
        updatables.add(central);
        quadTree.insertBody(0, central);

        Random r = new Random();

        for (int i = 0; i < n; i++) {
            double angle = r.nextDouble() * Math.PI * 2;
            float dist = radius * (0.9f + r.nextFloat() * 0.2f);

            float x = (float) Math.cos(angle) * dist;
            float y = (float) Math.sin(angle) * dist;

            float speed = (float) Math.sqrt(QuadTree.G * centralMass / dist);
            float vx = (float) -Math.sin(angle) * speed;
            float vy = (float) Math.cos(angle) * speed;

            Body b = new Body(
                new Vector2(x, y),
                new Vector2(vx/2, vy/2),
                r.nextInt(100),
                Color.CYAN
            );

            particles.add(b);
            renderables.add(b);
            updatables.add(b);
            quadTree.insertBody(0, b);
        }
    }


    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;
        fpsUpsTimer += delta;

        while (accumulator >= logicInterval) {
            for (Updatable u : updatables) {
                u.update(logicInterval * SS);
            }
            accumulator -= logicInterval;
            updates++;
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


        quadTree.erase();
        for (Body b : particles) {
            quadTree.insertBody(0, b);
        }
        long start = System.nanoTime();
        quadTree.updateMassDirstribution();
        quadTree.updateGravitationalAcceleration();
        long end = System.nanoTime();
        long durationUs = (end - start) / 1_000;
        long durationMs = (end - start) / 1_000_000;
        System.out.println(durationUs + " us");
        System.out.println(durationMs + " ms");
        System.out.println("Nodes: " + quadTree.getNodes().size());

        //quadTree.renderVisualization();
        //quadTree.renderRootVisualization();



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
