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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends ApplicationAdapter {
    private static Main instance;

    private ArrayList<Renderable> renderables;
    private ArrayList<Updatable> updatables;
    private static int MAX_UPS = 30;   // logic updates per second
    private static int MAX_FPS = 30;   // rendering frames per second
    private static int SS = 1;
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

        // <200k make parallel
        galaxy(100_000, 4800f, 500_000_000f);



        long strt = System.nanoTime();
        int threadNum = Runtime.getRuntime().availableProcessors();
        //int threadNum = 1;
        int chunkSize = (int) Math.ceil((double)particles.size() / threadNum);
        CountDownLatch latch = new CountDownLatch(threadNum);
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        float[] localMinsX = new float[threadNum];
        float[] localMaxsX = new float[threadNum];
        float[] localMinsY = new float[threadNum];
        float[] localMaxsY = new float[threadNum];

        for (int i = 0; i < threadNum; i++) {
            int startIdx = i * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, particles.size());

            localMinsX[i] = Float.MAX_VALUE;
            localMaxsX[i] = -Float.MAX_VALUE;
            localMinsY[i] = Float.MAX_VALUE;
            localMaxsY[i] = -Float.MAX_VALUE;

            int finalI = i;
            executorService.execute(() -> {
                try
                {
                    for (int j = startIdx; j < endIdx; j++) {
                        Body particle = particles.get(j);
                        localMinsX[finalI] = Math.min(localMinsX[finalI], particle.getPosition().x);
                        localMaxsX[finalI] = Math.max(localMaxsX[finalI], particle.getPosition().x);
                        localMinsY[finalI] = Math.min(localMinsY[finalI], particle.getPosition().y);
                        localMaxsY[finalI] = Math.max(localMaxsY[finalI], particle.getPosition().y);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();  // block main thread till latch is not 0
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // merge results

        float xMin = Float.MAX_VALUE;
        float xMax = -Float.MAX_VALUE;
        float yMin = Float.MAX_VALUE;
        float yMax = -Float.MAX_VALUE;

        for (int i = 0; i < threadNum; i++) {
            xMin = Math.min(xMin, localMinsX[i]);
            xMax = Math.max(xMax, localMaxsX[i]);
            yMin = Math.min(yMin, localMinsY[i]);
            yMax = Math.max(yMax, localMaxsY[i]);
        }
        long end = System.nanoTime();
        System.out.println("Parallel: " + (end-strt) / 1_000_000);
        System.out.printf("xMin=%.2f, xMax=%.2f, yMin=%.2f, yMax=%.2f%n", xMin, xMax, yMin, yMax);


        strt = System.nanoTime();
        xMin = Float.MAX_VALUE;
        xMax = Float.MIN_VALUE;
        yMin = Float.MAX_VALUE;
        yMax = Float.MIN_VALUE;
        for (Body b : particles) {
            xMin = Math.min(xMin, b.getPosition().x);
            xMax = Math.max(xMax, b.getPosition().x);
            yMin = Math.min(yMin, b.getPosition().y);
            yMax = Math.max(yMax, b.getPosition().y);
        }
        end = System.nanoTime();
        System.out.println("Concurrent: " + (end-strt) / 1_000_000);
        System.out.printf("xMin=%.2f, xMax=%.2f, yMin=%.2f, yMax=%.2f%n", xMin, xMax, yMin, yMax);
        System.exit(1);
    }

    public void galaxy(int n, float radius, float centralMass) {
        Body central = new Body(new Vector2(0, 0), new Vector2(0, 0), centralMass, Color.YELLOW);
        particles.add(central);
        renderables.add(central);
        updatables.add(central);

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
                new Vector2(vx, vy),
                Math.max(r.nextInt(1), 1),
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

        quadTree = new QuadTree();
        for (Renderable r : renderables) {
                r.render();
        }

        long start = System.nanoTime();
        quadTree.erase();

        long t1 = System.nanoTime();



        for (Body b : particles) {
            quadTree.insertBody(0, b);
        }



        long t2 = System.nanoTime();
        quadTree.updateMassDirstribution();
        long t3 = System.nanoTime();
        quadTree.updateGravitationalAccelerationConcurrent(particles);
        long t4 = System.nanoTime();

        long eraseTime = t1 - start;
        long insertTime = t2 - t1;
        long massTime = t3 - t2;
        long gravityTime = t4 - t3;
        long totalTime = t4 - start;

        System.out.println("Nodes: " + quadTree.getNodes().size());
        System.out.println("erase: " + eraseTime / 1_000 + " us | " + eraseTime / 1_000_000 + " ms | " + ((float)eraseTime / totalTime)*100 + "%");
        System.out.println("insert: " + insertTime / 1_000 + " us | " + insertTime / 1_000_000 + " ms | " + ((float)insertTime / totalTime)*100 + "%");
        System.out.println("mass distribution: " + massTime / 1_000 + " us | " + massTime / 1_000_000 + " ms | " + ((float)massTime / totalTime)*100 + "%");
        System.out.println("gravitational acceleration: " + gravityTime / 1_000 + " us | " + gravityTime / 1_000_000 + " ms | " + ((float)gravityTime / totalTime)*100 + "%");
        System.out.println("TOTAL: " + totalTime / 1_000 + " us | " + totalTime / 1_000_000 + " ms | " + ((float)totalTime / totalTime)*100 + "%");
        System.out.println();

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
