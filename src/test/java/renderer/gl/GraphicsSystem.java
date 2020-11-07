package renderer.gl;

import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;

public class GraphicsSystem {
    private static GraphicsSystem instance;

    private GraphicsSystem() {
        glfwSetErrorCallback(GLFWErrorCallback.createThrow());
        glfwInit();
    }

    public void swapInterval(int interval) {
        glfwSwapInterval(interval);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void terminate() {
        glfwTerminate();
        instance = null;
    }

    public Window window() {
        return new Window();
    }

    public static GraphicsSystem instance() {
        if (instance == null) {
            instance = new GraphicsSystem();
        }

        return instance;
    }
}
