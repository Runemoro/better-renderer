package renderer.gl;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static org.lwjgl.glfw.GLFW.*;

public class ButtonEvent {
    public final Button button;
    public final int scancode;
    public final Action action;
    public final boolean shift;
    public final boolean control;
    public final boolean alt;
    public final boolean superModifier;
    public final boolean capsLock;
    public final boolean numLock;

    ButtonEvent(Button.Device type, int button, int scancode, int action, int mods) {
        this.button = Button.fromGlfwCode(type, button);
        this.scancode = scancode;
        this.action = Action.fromGlfwCode(action);
        shift = (mods & GLFW_MOD_SHIFT) != 0;
        control = (mods & GLFW_MOD_CONTROL) != 0;
        alt = (mods & GLFW_MOD_ALT) != 0;
        superModifier = (mods & GLFW_MOD_SUPER) != 0;
        capsLock = (mods & GLFW_MOD_CAPS_LOCK) != 0;
        numLock = (mods & GLFW_MOD_NUM_LOCK) != 0;
    }

    public String keyName() {
        if (button.device != Button.Device.KEYBOARD) {
            return null;
        }

        return glfwGetKeyName(button.glfwCode, scancode);
    }

    public enum Action {
        PRESS(GLFW_PRESS),
        RELEASE(GLFW_RELEASE),
        REPEAT(GLFW_REPEAT);

        private static final Int2ObjectMap<Action> BY_GLFW_CODE = new Int2ObjectOpenHashMap<>();
        final int glfwCode;

        Action(int glfwCode) {
            this.glfwCode = glfwCode;
        }

        static Action fromGlfwCode(int glfwCode) {
            Action action = BY_GLFW_CODE.get(glfwCode);

            if (action == null) {
                throw new IllegalArgumentException("invalid GLFW code " + glfwCode);
            }

            return action;
        }

        static {
            for (Action action : Action.values()) {
                BY_GLFW_CODE.put(action.glfwCode, action);
            }
        }
    }
}
