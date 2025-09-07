package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class ParticleManager implements Renderable, Updatable {

    private ArrayList<CircleParticle> particles;

    public ParticleManager() {
        this.particles = new ArrayList<>();

        //CircleParticle particle = new CircleParticle(new Vector2(400, 300), new Vector2((float) 5f, 0), Color.CYAN);
        //particles.add(particle);

        //int n = 1_000_000;
        int n = 500;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            CircleParticle p = new CircleParticle(new Vector2(
                random.nextInt(800),
                random.nextInt(600)),
                new Vector2(random.nextFloat(20f) - 10f,
                    random.nextFloat(20f) - 10f),
                Color.CYAN);

            particles.add(p);
        }
    }


    @Override
    public void render() {
       for (CircleParticle particle : particles) {
           particle.render();
       }
    }

    @Override
    public void update(float delta) {
        for (CircleParticle particle : particles) {
            particle.update(delta);
        }
    }
}
