package io.gith.lwjgl3;

import com.badlogic.gdx.InputAdapter;

public class InputController extends InputAdapter
{
    private float scrollX = 0;
    private float scrollY = 0;

    public float getScrollX() {return scrollX;}
    public float getScrollY() {return scrollY;}

    public void resetScroll() {
        scrollX = 0;
        scrollY = 0;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        scrollX += amountX;
        scrollY += amountY;
        return true;
    }
}
