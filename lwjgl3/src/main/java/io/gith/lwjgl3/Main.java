package io.gith.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

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

    private ParticleManager particleManager;
    private CameraController cameraController;
    private InputController inputController;

    public static int getMaxUps() {return MAX_UPS;}
    public static int getMaxFps() {return MAX_FPS;}
    public static Main getInstance() {return instance;}
    public InputController getInputController() {return inputController;}
    public ParticleManager getParticleManager() {return particleManager;}
    public CameraController getCameraController() {return cameraController;}

    public void create() {
        instance = this;

        renderables = new ArrayList<>();
        updatables = new ArrayList<>();
        particleManager = new ParticleManager();
        cameraController = new CameraController();
        inputController = new InputController();

        Resources.batch = new SpriteBatch();
        Gdx.input.setInputProcessor(inputController);

        renderables.add(particleManager);
        updatables.add(particleManager);
        updatables.add(cameraController);

        logicInterval = 1f / MAX_UPS;
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
        Quad quad = new Quad(new Vector2(50, 50), 20);
        ScreenUtils.clear(Color.BLACK);
        cameraController.getCamera().update();
        Resources.batch.setProjectionMatrix(cameraController.getCamera().combined);

        Resources.batch.begin();
        for (Renderable r : renderables) {
            r.render();
        }
        quad.render();
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
