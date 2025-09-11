package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleManager implements Renderable, Updatable {

    private ArrayList<Body> particles;
    private final ExecutorService executor;
    private final int numThreads;

    public ParticleManager() {
        this.numThreads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.particles = new ArrayList<>();
        //CircleParticle particle = new CircleParticle(new Vector2(400, 300), new Vector2((float) 5f, 0), Color.CYAN);
        //particles.add(particle);

        int n = 1_000_000;
        //int n = 500;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            Body p = new Body(new Vector2(
                random.nextInt(800),
                random.nextInt(600)),
                new Vector2(random.nextFloat(20f) - 10f,
                    random.nextFloat(20f) - 10f),
                0,
                Color.CYAN);

            particles.add(p);
        }
    }


    @Override
    public void render() {
       for (Body particle : particles) {
           particle.render();
       }
    }

    @Override
    public void update(float delta) {
        for (Body particle : particles) {
            particle.update(delta);
        }
    }
}
