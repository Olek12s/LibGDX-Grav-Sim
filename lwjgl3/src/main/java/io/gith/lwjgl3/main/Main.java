package io.gith.lwjgl3.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
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
    private static int SS = 10000;
    private float logicInterval = 1f / MAX_UPS;  // seconds per logic update
    private float accumulator = 0; // acc λt
    private long lastRenderTime = 0; // to limit FPS

    private int frames = 0;
    private int updates = 0;
    private float fpsUpsTimer = 0f;

    private CameraController cameraController;
    private InputController inputController;
    private Gui gui;
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
        gui = new Gui();
        Resources.batch = new SpriteBatch();
        Gdx.input.setInputProcessor(inputController);


        updatables.add(cameraController);

        quadTree = new QuadTree(particles);
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
