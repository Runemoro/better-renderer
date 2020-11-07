package renderer.gl;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.EnumMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public enum Button {
    SPACE(Device.KEYBOARD, GLFW_KEY_SPACE),
    APOSTROPHE(Device.KEYBOARD, GLFW_KEY_APOSTROPHE),
    COMMA(Device.KEYBOARD, GLFW_KEY_COMMA),
    MINUS(Device.KEYBOARD, GLFW_KEY_MINUS),
    PERIOD(Device.KEYBOARD, GLFW_KEY_PERIOD),
    SLASH(Device.KEYBOARD, GLFW_KEY_SLASH),
    NUMBER_0(Device.KEYBOARD, GLFW_KEY_0),
    NUMBER_1(Device.KEYBOARD, GLFW_KEY_1),
    NUMBER_2(Device.KEYBOARD, GLFW_KEY_2),
    NUMBER_3(Device.KEYBOARD, GLFW_KEY_3),
    NUMBER_4(Device.KEYBOARD, GLFW_KEY_4),
    NUMBER_5(Device.KEYBOARD, GLFW_KEY_5),
    NUMBER_6(Device.KEYBOARD, GLFW_KEY_6),
    NUMBER_7(Device.KEYBOARD, GLFW_KEY_7),
    NUMBER_8(Device.KEYBOARD, GLFW_KEY_8),
    NUMBER_9(Device.KEYBOARD, GLFW_KEY_9),
    SEMICOLON(Device.KEYBOARD, GLFW_KEY_SEMICOLON),
    EQUAL(Device.KEYBOARD, GLFW_KEY_EQUAL),
    A(Device.KEYBOARD, GLFW_KEY_A),
    B(Device.KEYBOARD, GLFW_KEY_B),
    C(Device.KEYBOARD, GLFW_KEY_C),
    D(Device.KEYBOARD, GLFW_KEY_D),
    E(Device.KEYBOARD, GLFW_KEY_E),
    F(Device.KEYBOARD, GLFW_KEY_F),
    G(Device.KEYBOARD, GLFW_KEY_G),
    H(Device.KEYBOARD, GLFW_KEY_H),
    I(Device.KEYBOARD, GLFW_KEY_I),
    J(Device.KEYBOARD, GLFW_KEY_J),
    K(Device.KEYBOARD, GLFW_KEY_K),
    L(Device.KEYBOARD, GLFW_KEY_L),
    M(Device.KEYBOARD, GLFW_KEY_M),
    N(Device.KEYBOARD, GLFW_KEY_N),
    O(Device.KEYBOARD, GLFW_KEY_O),
    P(Device.KEYBOARD, GLFW_KEY_P),
    Q(Device.KEYBOARD, GLFW_KEY_Q),
    R(Device.KEYBOARD, GLFW_KEY_R),
    S(Device.KEYBOARD, GLFW_KEY_S),
    T(Device.KEYBOARD, GLFW_KEY_T),
    U(Device.KEYBOARD, GLFW_KEY_U),
    V(Device.KEYBOARD, GLFW_KEY_V),
    W(Device.KEYBOARD, GLFW_KEY_W),
    X(Device.KEYBOARD, GLFW_KEY_X),
    Y(Device.KEYBOARD, GLFW_KEY_Y),
    Z(Device.KEYBOARD, GLFW_KEY_Z),
    LEFT_BRACKET(Device.KEYBOARD, GLFW_KEY_LEFT_BRACKET),
    BACKSLASH(Device.KEYBOARD, GLFW_KEY_BACKSLASH),
    RIGHT_BRACKET(Device.KEYBOARD, GLFW_KEY_RIGHT_BRACKET),
    GRAVE_ACCENT(Device.KEYBOARD, GLFW_KEY_GRAVE_ACCENT),

    WORLD_1(Device.KEYBOARD, GLFW_KEY_WORLD_1),
    WORLD_2(Device.KEYBOARD, GLFW_KEY_WORLD_2),
    ESCAPE(Device.KEYBOARD, GLFW_KEY_ESCAPE),
    ENTER(Device.KEYBOARD, GLFW_KEY_ENTER),
    TAB(Device.KEYBOARD, GLFW_KEY_TAB),
    BACKSPACE(Device.KEYBOARD, GLFW_KEY_BACKSPACE),
    INSERT(Device.KEYBOARD, GLFW_KEY_INSERT),
    DELETE(Device.KEYBOARD, GLFW_KEY_DELETE),
    RIGHT(Device.KEYBOARD, GLFW_KEY_RIGHT),
    LEFT(Device.KEYBOARD, GLFW_KEY_LEFT),
    DOWN(Device.KEYBOARD, GLFW_KEY_DOWN),
    UP(Device.KEYBOARD, GLFW_KEY_UP),
    PAGE_UP(Device.KEYBOARD, GLFW_KEY_PAGE_UP),
    PAGE_DOWN(Device.KEYBOARD, GLFW_KEY_PAGE_DOWN),
    HOME(Device.KEYBOARD, GLFW_KEY_HOME),
    END(Device.KEYBOARD, GLFW_KEY_END),
    CAPS_LOCK(Device.KEYBOARD, GLFW_KEY_CAPS_LOCK),
    SCROLL_LOCK(Device.KEYBOARD, GLFW_KEY_SCROLL_LOCK),
    NUM_LOCK(Device.KEYBOARD, GLFW_KEY_NUM_LOCK),
    PRINT_SCREEN(Device.KEYBOARD, GLFW_KEY_PRINT_SCREEN),
    PAUSE(Device.KEYBOARD, GLFW_KEY_PAUSE),

    F1(Device.KEYBOARD, GLFW_KEY_F1),
    F2(Device.KEYBOARD, GLFW_KEY_F2),
    F3(Device.KEYBOARD, GLFW_KEY_F3),
    F4(Device.KEYBOARD, GLFW_KEY_F4),
    F5(Device.KEYBOARD, GLFW_KEY_F5),
    F6(Device.KEYBOARD, GLFW_KEY_F6),
    F7(Device.KEYBOARD, GLFW_KEY_F7),
    F8(Device.KEYBOARD, GLFW_KEY_F8),
    F9(Device.KEYBOARD, GLFW_KEY_F9),
    F10(Device.KEYBOARD, GLFW_KEY_F10),
    F11(Device.KEYBOARD, GLFW_KEY_F11),
    F12(Device.KEYBOARD, GLFW_KEY_F12),
    F13(Device.KEYBOARD, GLFW_KEY_F13),
    F14(Device.KEYBOARD, GLFW_KEY_F14),
    F15(Device.KEYBOARD, GLFW_KEY_F15),
    F16(Device.KEYBOARD, GLFW_KEY_F16),
    F17(Device.KEYBOARD, GLFW_KEY_F17),
    F18(Device.KEYBOARD, GLFW_KEY_F18),
    F19(Device.KEYBOARD, GLFW_KEY_F19),
    F20(Device.KEYBOARD, GLFW_KEY_F20),
    F21(Device.KEYBOARD, GLFW_KEY_F21),
    F22(Device.KEYBOARD, GLFW_KEY_F22),
    F23(Device.KEYBOARD, GLFW_KEY_F23),
    F24(Device.KEYBOARD, GLFW_KEY_F24),
    F25(Device.KEYBOARD, GLFW_KEY_F25),
    NUMBERPAD_0(Device.KEYBOARD, GLFW_KEY_KP_0),
    NUMBERPAD_1(Device.KEYBOARD, GLFW_KEY_KP_1),
    NUMBERPAD_2(Device.KEYBOARD, GLFW_KEY_KP_2),
    NUMBERPAD_3(Device.KEYBOARD, GLFW_KEY_KP_3),
    NUMBERPAD_4(Device.KEYBOARD, GLFW_KEY_KP_4),
    NUMBERPAD_5(Device.KEYBOARD, GLFW_KEY_KP_5),
    NUMBERPAD_6(Device.KEYBOARD, GLFW_KEY_KP_6),
    NUMBERPAD_7(Device.KEYBOARD, GLFW_KEY_KP_7),
    NUMBERPAD_8(Device.KEYBOARD, GLFW_KEY_KP_8),
    NUMBERPAD_9(Device.KEYBOARD, GLFW_KEY_KP_9),
    NUMBERPAD_DECIMAL(Device.KEYBOARD, GLFW_KEY_KP_DECIMAL),
    NUMBERPAD_DIVIDE(Device.KEYBOARD, GLFW_KEY_KP_DIVIDE),
    NUMBERPAD_MULTIPLY(Device.KEYBOARD, GLFW_KEY_KP_MULTIPLY),
    NUMBERPAD_SUBTRACT(Device.KEYBOARD, GLFW_KEY_KP_SUBTRACT),
    NUMBERPAD_ADD(Device.KEYBOARD, GLFW_KEY_KP_ADD),
    NUMBERPAD_ENTER(Device.KEYBOARD, GLFW_KEY_KP_ENTER),
    NUMBERPAD_EQUAL(Device.KEYBOARD, GLFW_KEY_KP_EQUAL),
    LEFT_SHIFT(Device.KEYBOARD, GLFW_KEY_LEFT_SHIFT),
    LEFT_CONTROL(Device.KEYBOARD, GLFW_KEY_LEFT_CONTROL),
    LEFT_ALT(Device.KEYBOARD, GLFW_KEY_LEFT_ALT),
    LEFT_SUPER(Device.KEYBOARD, GLFW_KEY_LEFT_SUPER),
    RIGHT_SHIFT(Device.KEYBOARD, GLFW_KEY_RIGHT_SHIFT),
    RIGHT_CONTROL(Device.KEYBOARD, GLFW_KEY_RIGHT_CONTROL),
    RIGHT_ALT(Device.KEYBOARD, GLFW_KEY_RIGHT_ALT),
    RIGHT_SUPER(Device.KEYBOARD, GLFW_KEY_RIGHT_SUPER),
    MENU(Device.KEYBOARD, GLFW_KEY_MENU),
    LAST(Device.KEYBOARD, GLFW_KEY_LAST),
    KEYBOARD_UNKNOWN(Device.KEYBOARD, -1),

    MOUSE_1(Device.MOUSE, GLFW_MOUSE_BUTTON_1),
    MOUSE_2(Device.MOUSE, GLFW_MOUSE_BUTTON_2),
    MOUSE_3(Device.MOUSE, GLFW_MOUSE_BUTTON_3),
    MOUSE_4(Device.MOUSE, GLFW_MOUSE_BUTTON_4),
    MOUSE_5(Device.MOUSE, GLFW_MOUSE_BUTTON_5),
    MOUSE_6(Device.MOUSE, GLFW_MOUSE_BUTTON_6),
    MOUSE_7(Device.MOUSE, GLFW_MOUSE_BUTTON_7),
    MOUSE_8(Device.MOUSE, GLFW_MOUSE_BUTTON_8),
    MOUSE_UNKNOWN(Device.MOUSE, -1);

    private static final Map<Device, Int2ObjectMap<Button>> BUTTONS = new EnumMap<>(Device.class);
    public static final Button LEFT_MOUSE = MOUSE_1;
    public static final Button RIGHT_MOUSE = MOUSE_2;
    public static final Button MIDDLE_MOUSE = MOUSE_3;

    public final Device device;
    final int glfwCode;

    Button(Device device, int glfwCode) {
        this.device = device;
        this.glfwCode = glfwCode;
    }

    static Button fromGlfwCode(Device type, int glfwCode) {
        Button key = BUTTONS.computeIfAbsent(type, t -> new Int2ObjectOpenHashMap<>()).get(glfwCode);

        if (key == null) {
            throw new IllegalArgumentException("unknown GLFW code");
        }

        return key;
    }

    static {
        for (Button key : values()) {
            BUTTONS.computeIfAbsent(key.device, t -> new Int2ObjectOpenHashMap<>()).put(key.glfwCode, key);
        }
    }

    public enum Device {
        KEYBOARD,
        MOUSE
    }
}
