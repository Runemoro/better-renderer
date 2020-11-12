package renderer.gl;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {
    final long handle;

    public Monitor(long handle) {
        this.handle = handle;
    }

    public static Monitor primaryMonitor() {
        return new Monitor(glfwGetPrimaryMonitor());
    }

    public int width() {
        return glfwGetVideoMode(handle).width();
    }

    public int height() {
        return glfwGetVideoMode(handle).height();
    }
}
