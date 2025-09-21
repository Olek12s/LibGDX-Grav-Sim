package io.gith.lwjgl3.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import imgui.internal.ImGui;
import io.gith.lwjgl3.quadTree.Body;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.ArrayList;

public class BodyCreator implements Updatable {
    private QuadTree quadTree;


    public BodyCreator(QuadTree quadTree) {
         Main.getInstance().getUpdatables().add(this);
         this.quadTree = quadTree;
    }

    @Override
    public void update(float dt) {
        CameraController cameraController = Main.getInstance().getCameraController();

        if (ImGui.getIO().getWantCaptureMouse()) return;    // ignore click if ImGui already captured it

        // LEFT CLICK
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
        {
            Vector2 worldPos = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());

            int bodyCount = Gui.bodyCount.get();
            int gridSize = (int) Math.ceil(Math.sqrt(bodyCount));

            int placed = 0;
            for (int y = 0; y < gridSize && placed < bodyCount; y++) {
                for (int x = 0; x < gridSize && placed < bodyCount; x++) {
                    float offsetX = (x - gridSize / 2f);
                    float offsetY = (y - gridSize / 2f);

                    Vector2 position = new Vector2(worldPos.x + offsetX, worldPos.y + offsetY);
                    Vector2 velocity = new Vector2(0, 0);
                    addBodyAt(position, velocity, Gui.massVal.get(), Color.LIME);
                    placed++;
                }
            }
        }

        // MIDDLE CLICK
        if (Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE)) {
            Vector2 worldPos = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
            addGalaxy(worldPos, 30_000, 100_000_000f);
        }
    }

    private void addBodyAt(Vector2 position, Vector2 velocity, float mass, Color color) {
        Body body = new Body(position, velocity, mass, Color.LIME);
        quadTree.addNewBody(body);
    }

    private void addGalaxy(Vector2 center, int starCount, float totalMass) {
        Body core = new Body(new Vector2(center), new Vector2(0, 0), totalMass, Color.YELLOW);
        quadTree.addNewBody(core);


        float starMass = 1f;
        java.util.Random rng = new java.util.Random();

        for (int i = 0; i < starCount; i++) {

            float r = (float) (rng.nextFloat() * 200 + 50); // promień 50–250 (dopasuj)
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            Vector2 pos = new Vector2(
                center.x + (float)Math.cos(angle) * r,
                center.y + (float)Math.sin(angle) * r
            );


            float vMag = (float)Math.sqrt(Units.G * totalMass / r);

            Vector2 vel = new Vector2(-(float)Math.sin(angle), (float)Math.cos(angle)).scl(vMag);

            addBodyAt(pos, vel, starMass, Color.WHITE);
        }
    }

}
