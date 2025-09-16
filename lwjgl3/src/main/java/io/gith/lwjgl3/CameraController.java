package io.gith.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController implements Updatable
{
    private Viewport viewport;
    private OrthographicCamera camera;

    public Viewport getViewport() {return viewport;}
    public OrthographicCamera getCamera() {return camera;}

    public CameraController()
    {
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();
    }

    @Override
    public void update(float delta) {
        camera.update();
        cameraZoom();
        cameraDrag();
    }

    private void cameraZoom() {
        InputController input = Main.getInstance().getInputController();
        float scrollY = input.getScrollY();
        if (scrollY != 0) {
            camera.zoom += scrollY * 0.4f;

            if (camera.zoom < 0.1f) camera.zoom = 0.1f;
            if (camera.zoom > 50f) camera.zoom = 50f;

            input.resetScroll();
        }
    }
    private void cameraDrag() {
        InputController input = Main.getInstance().getInputController();
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            camera.position.x -= input.getDeltaX() * camera.zoom*8;
            camera.position.y += input.getDeltaY() * camera.zoom*8;

            input.resetDrag();
        }
    }
}
