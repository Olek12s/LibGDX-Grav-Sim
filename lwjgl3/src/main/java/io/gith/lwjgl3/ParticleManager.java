package io.gith.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class ParticleManager implements Renderable, Updatable {

    private ArrayList<CircleParticle> particles;

    public ParticleManager() {
        this.particles = new ArrayList<>();

        CircleParticle particle = new CircleParticle(new Vector2(400, 300), new Vector2(0, 0), Color.CYAN);
        particles.add(particle);
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
