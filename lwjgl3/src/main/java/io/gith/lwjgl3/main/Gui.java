package io.gith.lwjgl3.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.concurrent.Executors;

public class Gui implements Renderable {

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static InputProcessor tmpProcessor;

    private int[] threadVal = { Math.min(Runtime.getRuntime().availableProcessors() - (Runtime.getRuntime().availableProcessors())/4, Runtime.getRuntime().availableProcessors()) };
    private float[] tmpTheta = {QuadTree.theta};
    private float[] tmpEps = {QuadTree.epsilon};
    private ImInt ssVal = new ImInt(Main.SS);
    private float[] predictionRateVal = { QuadTree.accPredictionRate };
    private ImBoolean predictions = new ImBoolean(QuadTree.predictionsOn);

    public Gui() {
        Main.getInstance().getRenderables().add(this);
        initImGui();
    }
    @Override
    public void render() {
        startImGui();

        float windowWidth = Gdx.graphics.getWidth() / 5f;

        ImGui.setNextWindowPos(Gdx.graphics.getWidth() - windowWidth, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(windowWidth, 0, ImGuiCond.Always);

        int windowFlags = ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize;

        ImGui.begin("Config", windowFlags);

        // Slider Theta
        if (ImGui.sliderFloat("Theta", tmpTheta, .0f, 5.0f)) {
            QuadTree.theta = tmpTheta[0];
        }

        // Slider Epsilon
        if (ImGui.sliderFloat("Epsilon", tmpEps, 0.0f, 500.0f)) {
            QuadTree.epsilon = tmpEps[0];
        }


        // SS
        if (ImGui.inputInt("Sim speed", ssVal)) {
            int val = ssVal.get();
            val = Math.max(1, Math.min(val, 50000));
            Main.SS = val;
        }

        // Threads
        int maxThreads = Runtime.getRuntime().availableProcessors();
        if (ImGui.sliderInt("Threads", threadVal, 1, maxThreads)) {
            int val = threadVal[0];
            QuadTree.threadNum = val;

            // close old pool
            if (QuadTree.executorService != null && !QuadTree.executorService.isShutdown()) {
                QuadTree.executorService.shutdownNow();
            }
            QuadTree.executorService = Executors.newFixedThreadPool(val);
        }

        // Prediction rate
        if (ImGui.sliderFloat("Prediction rate", predictionRateVal, 0, 1)) {
            float val = predictionRateVal[0];
            QuadTree.accPredictionRate = val;
        }

        // Allow predictions
        if (ImGui.checkbox("Allow predictions", predictions)) {
            QuadTree.predictionsOn = predictions.get();
        }

        // Remove Bodies Button
        if (ImGui.button("Remove bodies")) {
            Main.getInstance().getQuadTree().getBodies().clear();
            Main.getInstance().getQuadTree().erase();
        }

        ImGui.end();
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
        if (tmpProcessor != null) {
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

        if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantCaptureMouse()) {
            tmpProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(null);
        }
    }
    public static void disposeImGui() {
        imGuiGl3 = null;
        imGuiGlfw = null;
        ImGui.destroyContext();
    }
}
