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
import imgui.type.ImFloat;
import imgui.type.ImInt;
import io.gith.lwjgl3.quadTree.Body;
import io.gith.lwjgl3.quadTree.QuadTree;

import java.util.concurrent.Executors;

public class Gui implements Renderable {

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static InputProcessor tmpProcessor;

    private static int[] threadVal = { Math.min(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors()) };
    private static float[] tmpTheta = {(float)QuadTree.theta};
    private static float[] tmpEps = {(float)QuadTree.epsilon};
    private static int[] ssVal = { Main.SS };
    private static float[] predictionRateVal = { (float)QuadTree.accPredictionRate };
    private static ImBoolean predictions = new ImBoolean(QuadTree.predictionsOn);
    private static ImBoolean renderTree = new ImBoolean(QuadTree.renderOn);
    public static ImFloat massVal = new ImFloat(1);
    public static ImInt bodyCount = new ImInt(1);
    public static ImInt bodyCountGalaxy = new ImInt(30000);
    public static ImFloat galaxyCenterMass = new ImFloat(999_000_000_000f);
    public static ImFloat starMass = new ImFloat(0.001f);
    public static int[] integrationMode = {1};  // 0 - Euler    // 1 - Leapfrog


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


        float minMultiplier = 1f;
        float maxMultiplier = 100f;
        float[] tmpGMultiplier = { Units.GMultiplier };

        if (ImGui.sliderFloat("G force multiplier", tmpGMultiplier, minMultiplier, maxMultiplier)) {
            Units.GMultiplier = tmpGMultiplier[0];
        }




        // Slider Theta
        if (ImGui.sliderFloat("Theta", tmpTheta, .000f, 5.0f)) {
            QuadTree.theta = tmpTheta[0];
        }

        // Slider Epsilon
        if (ImGui.sliderFloat("Epsilon", tmpEps, .001f, 50.0f)) {
            QuadTree.epsilon = tmpEps[0];
        }


        // SS
        if (ImGui.sliderInt("Sim speed", ssVal, 1, 5000)) {
            int val = ssVal[0];
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

        // Allow rendering quads
        if (ImGui.checkbox("Allow rendering quads", renderTree)) {
            QuadTree.renderOn = renderTree.get();
        }

        ImGui.text("Integration:");
        if (ImGui.radioButton("Euler", integrationMode[0] == 0)) {
            integrationMode[0] = 0;
        }
        if (ImGui.radioButton("Leapfrog", integrationMode[0] == 1)) {
            integrationMode[0] = 1;
        }

        // Remove Bodies Button
        if (ImGui.button("Remove bodies")) {
            Main.getInstance().getQuadTree().getBodies().clear();
            Main.getInstance().getQuadTree().erase();
        }
        ImGui.text("");

        // Mass
        if (ImGui.inputFloat("Mass", massVal)) {

            float minVal = 0.001f;
            float maxVal = 999_000_000_000f;
            float val = massVal.get();
            val = Math.max(minVal, Math.min(val, maxVal));
            massVal.set(val);
        }


        // Body count
        if (ImGui.inputInt("Bodies per click", bodyCount)) {
            int val = bodyCount.get();
            val = Math.max(1, Math.min(val, 50000));
            // action
        }

        // Body count galaxy
        if (ImGui.inputInt("Galaxy bodies", bodyCountGalaxy)) {
            int val = bodyCountGalaxy.get();
            val = Math.max(1, Math.min(val, 1000000));
            // action
        }

        // Galaxy central mass
        if (ImGui.inputFloat("Galaxy center mass", galaxyCenterMass)) {
            float val = galaxyCenterMass.get();
            val = Math.max(1, Math.min(val, 999_000_000_000f));
            // action
        }

        // Galaxy star mass
        if (ImGui.inputFloat("Galaxy star mass", starMass)) {
            float val = starMass.get();
            val = Math.max(0.001f, Math.min(val, 1000000));
            // action
        }

        ImGui.text("Current bodies: " + Main.getInstance().getQuadTree().getBodies().size());
        ImGui.text(String.format("FPS: %.1f | UPS: %.1f", Main.currentFPS, Main.currentUPS));
        ImGui.text(String.format("Zoom: %.2f", Main.getInstance().getCameraController().getCamera().zoom));


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
