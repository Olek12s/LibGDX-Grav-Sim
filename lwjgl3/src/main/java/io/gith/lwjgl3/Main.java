package io.gith.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    private ArrayList<Renderable> renderables;
    private ArrayList<Updatable> updatables;
    private int MAX_UPS = 60;   // logic updates per second
    private int MAX_FPS = 20;   // rendering frames per second
    private float logicInterval;   // seconds per logic update
    private float accumulator = 0; // acc λt
    private long lastRenderTime = 0; // to limit FPS

    public void create () {
        renderables = new ArrayList<>();
        updatables = new ArrayList<>();

        logicInterval = 1f / MAX_UPS;
    }

    public void render () {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;

        while (accumulator >= logicInterval) {
            for (Updatable u : updatables) {
                u.update(logicInterval);
            }
            accumulator -= logicInterval;
            System.out.println("LOGIC");
        }

        if (MAX_FPS > 0) {
            long now = System.nanoTime();
            long minFrameTime = 1_000_000_000L / MAX_FPS;
            if (lastRenderTime > 0) {
                long frameDuration = now - lastRenderTime;
                if (frameDuration < minFrameTime) {
                    try {
                        Thread.sleep((minFrameTime - frameDuration) / 1_000_000L);
                    } catch (InterruptedException ignored) {}
                }
            }
            lastRenderTime = System.nanoTime();
        }
        for (Renderable r : renderables) {
            r.render();
        }
        System.out.println("RENDER");
    }

    public void resize (int width, int height) {

    }

    public void pause () {
    }

    public void resume () {
    }

    public void dispose () {
    }
}
