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
        int n = 100000;
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            Body body = (new Body(
                new Vector2(r.nextInt(50), r.nextInt(50)),
                new Vector2(r.nextFloat() * 10f - 5f, r.nextFloat() * 10f - 5f),
                r.nextInt(Math.max(1, 50)),
                //new Vector2(r.nextInt(12), r.nextInt(12)),
                //new Vector2(0,0),
                //r.nextInt(50),
                new Color(0.7f,0.7f,0.7f,0.7f)));

            particles.add(body);
            updatables.add(body);
            renderables.add(body);
        }

        /*
        for (int i = 0; i < 5; i++) {
            Body b1 = (new Body(new Vector2(0,0), new Vector2(0,0),1,Color.WHITE));
            Body b2 = (new Body(new Vector2(2f,0), new Vector2(0,0),1,Color.WHITE));
            particles.add(b1);
            updatables.add(b1);
            renderables.add(b1);
        }
        */
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;
        fpsUpsTimer += delta;

        while (accumulator >= logicInterval) {
            for (Updatable u : updatables) {
                u.update(logicInterval);
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
        long start = System.nanoTime();
        for (Body b : particles) {
            //quadTree.insertBody(0, b.getPosition(), b.getMass());
            quadTree.insertBody(0, b);
        }
        quadTree.updateMassDirstribution();
        long end = System.nanoTime();
        long durationUs = (end - start) / 1_000;
        long durationMs = (end - start) / 1_000_000;
        System.out.println(durationUs + " us");
        System.out.println(durationMs + " ms");
        System.out.println("Nodes: " + quadTree.getNodes().size());

        quadTree.renderVisualization();
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
