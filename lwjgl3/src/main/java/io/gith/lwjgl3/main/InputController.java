package io.gith.lwjgl3.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class InputController extends InputAdapter
{
    private float scrollX = 0;
    private float scrollY = 0;

    private int lastX = -1;
    private int lastY = -1;
    private int deltaX = 0;
    private int deltaY = 0;

    public float getScrollX() {return scrollX;}
    public float getScrollY() {return scrollY;}
    public int getDeltaX() { return deltaX; }
    public int getDeltaY() { return deltaY; }

    public void resetScroll() { // TODO: remove resetScroll or invoke it at the end of update queue
        scrollX = 0;
        scrollY = 0;
    }

    public void resetDrag() {   // TODO: remove resetDrag or invoke it at the end of update queue
        deltaX = 0;
        deltaY = 0;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        scrollX += amountX;
        scrollY += amountY;
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            if (lastX != -1 && lastY != -1) {
                deltaX = x - lastX;
                deltaY = y - lastY;
            }
            lastX = x;
            lastY = y;
        } else {
            lastX = -1;
            lastY = -1;
            deltaX = 0;
            deltaY = 0;
        }

        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            lastX = screenX;
            lastY = screenY;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            lastX = -1;
            lastY = -1;
            deltaX = 0;
            deltaY = 0;
        }
        return true;
    }
}
