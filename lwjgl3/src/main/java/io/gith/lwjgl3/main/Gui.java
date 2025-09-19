package io.gith.lwjgl3.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGui;
import io.gith.lwjgl3.quadTree.QuadTree;

public class Gui implements Renderable
{
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static InputProcessor tmpProcessor;

    public Gui() {
        Main.getInstance().getRenderables().add(this);
        initImGui();
    }

    private static float x = 0.0f;
    @Override
    public void render()
    {
        startImGui();

        float[] tmp2 = {QuadTree.theta};
        if (ImGui.sliderFloat("X (0..10)", tmp2, 0.0f, 10.0f)) {
            x = tmp2[0];
        }
        QuadTree.theta = x;

        // podgląd aktualnej wartości
        ImGui.text("x = " + x);
        endImGui();
    }

    public static void initImGui() {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();
        io.getFonts().build();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");
    }

    public static void startImGui() {
        if (tmpProcessor != null) { // Restore the input processor after ImGui caught all inputs, see #end()
            Gdx.input.setInputProcessor(tmpProcessor);
            tmpProcessor = null;
        }

        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public static void endImGui() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        // If ImGui wants to capture the input, disable libGDX's input processor
        if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantCaptureMouse()) {
            tmpProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(null);
        }
    }

    public static void disposeImGui() {
        //imGuiGl3.dispose();
        imGuiGl3 = null;
        //imGuiGlfw.dispose();
        imGuiGlfw = null;
        ImGui.destroyContext();
    }
}
