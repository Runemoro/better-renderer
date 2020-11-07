package renderer;

import net.runelite.client.RuneLite;
import renderer.gl.*;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import renderer.cache.CacheSystem;
import renderer.gl.GlProgram;
import renderer.renderer.ChunkRenderScheduler;
import renderer.renderer.WorldRenderer;
import renderer.util.Util;
import renderer.world.World;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glGetError;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public class StandaloneRendererTest {
    private static final String XTEA_LOCATION = "https://gist.githubusercontent.com/Runemoro/d68a388aeb35ad432adf8af027eae832/raw/xtea.json";
    private static final double FOV = 0.5;
    public static final double CROSSHAIR_SIZE = 50;
    public static final double CROSSHAIR_THICKNESS = 5;
    public static final int CHUNK_SIZE = 8;
    public int viewDistance = 150;
    public int viewDistanceDynamic = 25;
    private static final Vector3d FOG_COLOR = new Vector3d(0.8, 0.9, 0.95);

    private Window window;

    private final Vector3d position = new Vector3d(3223, 3425, 20);
    private final Quaterniond rotation = new Quaterniond();
    private static final double lookSpeed = 1.7;
    private static final double rollSpeed = 0.1;
    private static double moveSpeed = 20;

    private final List<Button> buttonsPressed = new ArrayList<>();
    private boolean mouseLocked = false;
    private double lastMouseX;
    private double lastMouseY;
    private long lastInputTime;

    private GlProgram program;
    private GraphicsSystem gs;
    private final World world = new World();
    private final ChunkRenderScheduler chunkRenderer = new ChunkRenderScheduler(world);

    public void run() {
        gs = GraphicsSystem.instance();

        window = gs.window();
        window.setSamples(4);
        window.setSize(960, 720);
        window.setMaximized(true);
        window.setTitle("Renderer");
        window.setRefreshAction(this::draw);
        window.create();
        window.show();

        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        try {
            program = new GlProgram(
                    new String(Util.readAllBytes(StandaloneRendererTest.class.getResourceAsStream("/shaders/vertex-shader.glsl"))),
                    new String(Util.readAllBytes(StandaloneRendererTest.class.getResourceAsStream("/shaders/fragment-shader.glsl")))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long time = System.nanoTime();
        int frames = 0;
        while (!window.shouldClose()) {
            if (frames++ == 100) {
                frames = 0;
                System.out.println(1 / ((double) (System.nanoTime() - time) / 1000000000 / 100) + " fps");
                time = System.nanoTime();
            }

            draw();
            handleInput();
        }

        window.destroy();
        gs.terminate();
        System.exit(0);
    }

    private void draw() {
        int width = window.framebufferWidth();
        int height = window.framebufferHeight();
        glViewport(0, 0, width, height);
        glClearColor((float) FOG_COLOR.x, (float) FOG_COLOR.y, (float) FOG_COLOR.z, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawCrosshair(width, height);

        world.updateRoofs((int) position.x, (int) position.y, 0, 5);
        chunkRenderer.setRoofsRemoved(world.roofsRemoved, 0);

        Matrix4d projection = new Matrix4d()
                .perspective(FOV * Math.PI, (double) width / height, 50 / 128., Double.POSITIVE_INFINITY);

        Matrix4d transform = new Matrix4d()
                .rotate(rotation)
                .translate(-position.x, -position.y, -position.z);

        Vector3d light = new Vector3d(position.x - 50, position.y - 50, 200);

        program.enable(
                transform.get(new float[16]),
                projection.get(new float[16]),
                new float[]{(float) light.x, (float) light.y, (float) light.z},
                viewDistance - CHUNK_SIZE,
                FOG_COLOR,
                new float[]{(float) position.x, (float) position.y, (float) position.z},
                0.7f
        );

        List<WorldRenderer> chunks = new ArrayList<>();

        for (int dx = -viewDistance; dx <= viewDistance; dx += CHUNK_SIZE) {
            int w = (int) Math.sqrt(viewDistance * viewDistance - dx * dx);

            for (int dy = -w; dy <= w; dy += CHUNK_SIZE) {
                int x = (int) (position.x + dx) / CHUNK_SIZE;
                int y = (int) (position.y + dy) / CHUNK_SIZE;


                WorldRenderer chunk = chunkRenderer.get(x, y);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        for (WorldRenderer chunk : chunks) {
            program.render(chunk.opaqueBuffer.buffer());
        }

        for (WorldRenderer chunk : chunks) {
            program.render(chunk.translucentBuffer.buffer());
        }

        program.disable();
        window.swapBuffers();
        checkError();
    }

    private void drawCrosshair(int width, int height) {
        glColor4d(0.25, 0.25, 0.25, 1);
        glBegin(GL_QUADS);
        glVertex2d(-CROSSHAIR_SIZE / 2 / width, -CROSSHAIR_THICKNESS / 2 / height);
        glVertex2d(CROSSHAIR_SIZE / 2 / width, -CROSSHAIR_THICKNESS / 2 / height);
        glVertex2d(CROSSHAIR_SIZE / 2 / width, CROSSHAIR_THICKNESS / 2 / height);
        glVertex2d(-CROSSHAIR_SIZE / 2 / width, CROSSHAIR_THICKNESS / 2 / height);

        glVertex2d(CROSSHAIR_THICKNESS / 2 / width, -CROSSHAIR_SIZE / 2 / height);
        glVertex2d(CROSSHAIR_THICKNESS / 2 / width, CROSSHAIR_SIZE / 2 / height);
        glVertex2d(-CROSSHAIR_THICKNESS / 2 / width, CROSSHAIR_SIZE / 2 / height);
        glVertex2d(-CROSSHAIR_THICKNESS / 2 / width, -CROSSHAIR_SIZE / 2 / height);
        glEnd();
    }

    private void handleInput() {
        gs.pollEvents();
        double time = Math.min((System.nanoTime() - lastInputTime) / 1000000000., 0.2);
        lastInputTime = System.nanoTime();

        window.acceptScrolls(v -> {
            if (buttonsPressed.contains(Button.LEFT_ALT)) {
                moveSpeed += v.y / 5;
                if (moveSpeed < 0.1) {
                    moveSpeed = 0.1;
                }
            } else {
                rotation.rotateLocalZ(v.y * rollSpeed);
            }
        });

        window.acceptButtonEvents(event -> {
            if (event.action == ButtonEvent.Action.PRESS) {
                buttonsPressed.add(event.button);
            }

            if (event.action == ButtonEvent.Action.RELEASE) {
                buttonsPressed.remove(event.button);

                if (event.button == Button.LEFT_MOUSE) {
                    if (!mouseLocked) {
                        lockMouse();
                    }
                }

                if (event.button == Button.ESCAPE) {
                    unlockMouse();
                }

                if (event.button == Button.C) {
                    chunkRenderer.clear();
                }
            }
        });

        if (mouseLocked) {
            rotation.rotateLocalX((window.mouseY() - lastMouseY) * lookSpeed / window.height())
                    .rotateLocalY((window.mouseX() - lastMouseX) * lookSpeed / window.height());

            lastMouseX = window.mouseX();
            lastMouseY = window.mouseY();
        }

        double speedMultiplier = 1;
        if (buttonsPressed.contains(Button.LEFT_SHIFT)) speedMultiplier *= 5;
        if (buttonsPressed.contains(Button.LEFT_CONTROL)) speedMultiplier /= 5;

        if (buttonsPressed.contains(Button.Z)) {
            rotation.rotateLocalZ(-rollSpeed * speedMultiplier * time);
        }

        if (buttonsPressed.contains(Button.X)) {
            rotation.rotateLocalZ(rollSpeed * speedMultiplier * time);
        }

        if (buttonsPressed.contains(Button.S)) {
            move(0, 0, -moveSpeed * speedMultiplier * time);
        }

        if (buttonsPressed.contains(Button.W)) {
            move(0, 0, moveSpeed * speedMultiplier * time);
        }

        if (buttonsPressed.contains(Button.A)) {
            move(-moveSpeed * speedMultiplier * time, 0, 0);
        }

        if (buttonsPressed.contains(Button.D)) {
            move(moveSpeed * speedMultiplier * time, 0, 0);
        }

        if (buttonsPressed.contains(Button.Q)) {
            move(0, -moveSpeed * speedMultiplier * time, 0);
        }

        if (buttonsPressed.contains(Button.E)) {
            move(0, moveSpeed * speedMultiplier * time, 0);
        }
    }

    private void lockMouse() {
        if (!mouseLocked) {
            mouseLocked = true;
            window.setCursorMode(CursorMode.DISABLED);
            lastMouseX = window.mouseX();
            lastMouseY = window.mouseY();
        }
    }

    private void unlockMouse() {
        if (mouseLocked) {
            window.setCursorMode(CursorMode.NORMAL);
        }

        mouseLocked = false;
    }

    public void move(double right, double up, double forward) {
        position.add(rotation.transformInverse(new Vector3d(right, up, -forward)));
    }

    public static void checkError() {
        int err = glGetError();

        if (err != 0) {
            throw new IllegalStateException("0x" + Integer.toHexString(err));
        }
    }

    public static void main(String[] args) throws Exception {
        Path xteaPath = RuneLite.RUNELITE_DIR.toPath().resolve("better-renderer/xtea.json");
        Files.createDirectories(xteaPath.getParent());
        Files.write(xteaPath, Util.readAllBytes(new URL(XTEA_LOCATION).openStream()));
        CacheSystem.CACHE.init();
        new StandaloneRendererTest().run();
    }
}
