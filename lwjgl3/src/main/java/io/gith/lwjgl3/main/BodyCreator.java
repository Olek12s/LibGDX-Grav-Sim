package io.gith.lwjgl3.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import imgui.ImDrawList;
import imgui.internal.ImGui;
import io.gith.lwjgl3.quadTree.Body;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.Random;

public class BodyCreator implements Updatable, Renderable {
    private QuadTree quadTree;

    private boolean draggingLeft = false;
    private boolean draggingMiddle = false;
    private Vector2 dragStart;
    private Vector2 dragEnd;
    private float dragScale;
    private final ShapeRenderer shapeRenderer;


    public BodyCreator(QuadTree quadTree) {
        Main.getInstance().getUpdatables().add(this);
        Main.getInstance().getRenderables().add(this);
        this.quadTree = quadTree;
        this.shapeRenderer = new ShapeRenderer();
        this.dragScale = 0.001f;
    }

    @Override
    public void update(float dt) {
        CameraController cameraController = Main.getInstance().getCameraController();

        if (ImGui.getIO().getWantCaptureMouse()) return;    // ignore click if ImGui already captured it

        // Drag beginning
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            draggingLeft = true;
            dragStart = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
            dragEnd = new Vector2(dragStart);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE)) {
            draggingMiddle = true;
            dragStart = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
            dragEnd = new Vector2(dragStart);
        }

        // drag update
        if (draggingLeft || draggingMiddle) {
            dragEnd = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        }

        // drag end - left
        if (draggingLeft && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector2 velocity = new Vector2(dragStart).sub(dragEnd).scl(dragScale); // scale
            spawnBodies(dragStart, velocity);
            draggingLeft = false;
        }

        // drag end - middle
        if (draggingMiddle && !Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            Vector2 velocity = new Vector2(dragStart).sub(dragEnd).scl(dragScale);
            addGalaxy(dragStart, Gui.bodyCountGalaxy.get(), Gui.galaxyCenterMass.get(), Gui.starMass.get(), velocity);
            draggingMiddle = false;
        }
    }

    private void spawnBodies(Vector2 worldPos, Vector2 velocity) {
        int bodyCount = Gui.bodyCount.get();
        int gridSize = (int) Math.ceil(Math.sqrt(bodyCount));

        int placed = 0;
        for (int y = 0; y < gridSize && placed < bodyCount; y++) {
            for (int x = 0; x < gridSize && placed < bodyCount; x++) {
                float offsetX = (x - gridSize / 2f);
                float offsetY = (y - gridSize / 2f);
                Vector2 position = new Vector2(worldPos.x + offsetX, worldPos.y + offsetY);

                addBodyAt(position, new Vector2(velocity), Gui.massVal.get(), Color.LIME);
                placed++;
            }
        }
    }

    private void addBodyAt(Vector2 position, Vector2 velocity, float mass, Color color) {
        Body body = new Body(position, velocity, mass, Color.LIME);
        quadTree.addNewBody(body);
    }

    private void addGalaxy(Vector2 center, int starCount, float coreMass, float starMass, Vector2 initialVelocity) {
        Random rng = new Random();

        Body core = new Body(new Vector2(center), new Vector2(initialVelocity), coreMass, Color.PINK);
        quadTree.addNewBody(core);

        float maxRadius = 100f + (float) Math.sqrt(starCount) * 10f;
        float innerRadius = maxRadius * 0.1f;

        for (int i = 0; i < starCount; i++) {
            float r = (float) (Math.sqrt(rng.nextFloat()) * (maxRadius - innerRadius) + innerRadius);
            float angle = (float) (rng.nextFloat() * Math.PI * 2);

            Vector2 pos = new Vector2(
                center.x + (float) Math.cos(angle) * r,
                center.y + (float) Math.sin(angle) * r
            );

            float velMul = 1.3f;
            float vMag = (float) Math.sqrt(Units.G * Units.GMultiplier * coreMass / Math.max(r, 1f));
            Vector2 vel = new Vector2(-(float) Math.sin(angle)*velMul, (float) Math.cos(angle)*velMul).scl(vMag);

            vel.add(initialVelocity);
            addBodyAt(pos, vel, starMass, Color.WHITE);
        }
    }


    @Override
    public void render() {
        if (draggingLeft || draggingMiddle) {
            shapeRenderer.setProjectionMatrix(Main.getInstance().getCameraController().getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.line(dragStart, dragEnd);
            shapeRenderer.end();
        }
    }
}
