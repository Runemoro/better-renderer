package renderer.plugin;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwAttachWin32Window;
import static org.lwjgl.system.jawt.JAWTFunctions.*;

public class JawtContextWindows extends JawtContext {
    private final long window;

    protected JawtContextWindows(Component component) {
        super(component);

        try (Lock ignored = lock()) {
            JAWTDrawingSurfaceInfo info = JAWT_DrawingSurface_GetDrawingSurfaceInfo(drawingSurface, drawingSurface.GetDrawingSurfaceInfo());
            JAWTWin32DrawingSurfaceInfo winInfo = JAWTWin32DrawingSurfaceInfo.create(info.platformInfo());
            window = glfwAttachWin32Window(winInfo.hwnd(), 0);
            glfwMakeContextCurrent(window);
            GL.createCapabilities();
        }
    }

    @Override
    public void update() {
        try (Lock ignored = lock()) {
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    @Override
    public void attach() {
        glfwMakeContextCurrent(window);
    }

    @Override
    public void detach() {
        glfwMakeContextCurrent(0);
    }

    @Override
    public int width() {
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
        return framebufferWidth[0];
    }

    @Override
    public int height() {
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
        return framebufferHeight[0];
    }
}
