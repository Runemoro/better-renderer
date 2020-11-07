package renderer.gl;

import it.unimi.dsi.fastutil.ints.*;
import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Window {
    private long handle = 0;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private float scaleX;
    private float scaleY;
    private boolean focused;
    private boolean iconified;
    private boolean maximized;
    private final List<ButtonEvent> buttonEvents = new ArrayList<>();
    private final IntList charactersTyped = new IntArrayList();
    private final List<String> droppedFiles = new ArrayList<>();
    private Runnable refreshAction = null;
    private final List<Vector2d> scrolls = new ArrayList<>();

    private final Int2IntMap hints = new Int2IntArrayMap();
    private final Int2ObjectMap<String> stringHints = new Int2ObjectArrayMap<>();
    private String title = null;
    private Monitor monitor = null;
    private final Window share = null;

    Window() {
    }

    public void create() {
        glfwDefaultWindowHints();

        for (Int2IntMap.Entry hint : hints.int2IntEntrySet()) {
            glfwWindowHint(hint.getIntKey(), hint.getIntValue());
        }

        for (Int2ObjectMap.Entry<String> hint : stringHints.int2ObjectEntrySet()) {
            glfwWindowHintString(hint.getIntKey(), hint.getValue());
        }

        handle = glfwCreateWindow(width, height, title, monitor == null ? 0 : monitor.handle, share == null ? 0 : share.handle);

        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetWindowPos(handle, x, y);
        this.x = x[0];
        this.y = y[0];

        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        glfwGetFramebufferSize(handle, framebufferWidth, framebufferHeight);
        this.framebufferWidth = framebufferWidth[0];
        this.framebufferHeight = framebufferHeight[0];

        float[] scaleX = new float[1];
        float[] scaleY = new float[1];
        glfwGetWindowContentScale(handle, scaleX, scaleY);
        this.scaleX = scaleX[0];
        this.scaleY = scaleY[0];

        focused = glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE;
        iconified = glfwGetWindowAttrib(handle, GLFW_ICONIFIED) == GLFW_TRUE;
        maximized = glfwGetWindowAttrib(handle, GLFW_MAXIMIZED) == GLFW_TRUE;

        glfwSetWindowPosCallback(handle, (w, x_, y_) -> {
            this.x = x_;
            this.y = y_;
        });

        glfwSetWindowSizeCallback(handle, (w, width_, height_) -> {
            width = width_;
            height = height_;
        });

        glfwSetWindowRefreshCallback(handle, w -> refresh());

        glfwSetWindowFocusCallback(handle, (w, focused) -> this.focused = focused);
        glfwSetWindowIconifyCallback(handle, (w, iconified) -> this.iconified = iconified);
        glfwSetWindowMaximizeCallback(handle, (window, maximized) -> this.maximized = maximized);

        glfwSetFramebufferSizeCallback(handle, (window, framebufferWidth_, framebufferHeight_) -> {
            this.framebufferWidth = framebufferWidth_;
            this.framebufferHeight = framebufferHeight_;
        });

        glfwSetWindowContentScaleCallback(handle, (window, scaleX_, scaleY_) -> {
            this.scaleX = scaleX_;
            this.scaleY = scaleY_;
        });

        glfwSetMouseButtonCallback(handle, (w, button, action, mods) -> buttonEvents.add(new ButtonEvent(Button.Device.MOUSE, button, -1, action, mods)));
        glfwSetScrollCallback(handle, (w, scrollX, scrollY) -> scrolls.add(new Vector2d(scrollX, scrollY)));
        glfwSetKeyCallback(handle, (w, key, scancode, action, mods) -> buttonEvents.add(new ButtonEvent(Button.Device.KEYBOARD, key, scancode, action, mods)));
        glfwSetCharCallback(handle, (w, codepoint) -> charactersTyped.add(codepoint));

        glfwSetDropCallback(handle, (w, count, names) -> {
            for (int i = 0; i < count; i++) {
                droppedFiles.add(GLFWDropCallback.getName(names, i));
            }
        });

        makeCurrentContext();
        GL.createCapabilities();
        glfwSwapInterval(1);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void setPosition(int x, int y) {
        glfwSetWindowPos(handle, x, y);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        if (handle != 0) {
            glfwSetWindowSize(handle, width, height);
        }
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    public float scaleX() {
        return scaleX;
    }

    public float scaleY() {
        return scaleY;
    }

    public boolean visible() {
        return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE;
    }

    public void show() {
        glfwShowWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void setShouldClose(boolean shouldClose) {
        glfwSetWindowShouldClose(handle, shouldClose);
    }

    public void destroy() {
        glfwDestroyWindow(handle);
    }

    public boolean focused() {
        return focused;
    }

    public void focus() {
        glfwFocusWindow(handle);
    }

    public boolean iconified() {
        return iconified;
    }

    public void iconify() {
        glfwIconifyWindow(handle);
    }

    public boolean maximized() {
        return maximized;
    }

    public void maximize() {
        glfwMaximizeWindow(handle);
    }

    public void requestAttention() {
        glfwRequestWindowAttention(handle);
    }

    public float opacity() {
        return glfwGetWindowOpacity(handle);
    }

    public void setOpacity(float opacity) {
        glfwSetWindowOpacity(handle, opacity);
    }

    public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        glfwSetWindowSizeLimits(handle, minWidth, minHeight, maxWidth, maxHeight);
    }

    public void acceptButtonEvents(Consumer<ButtonEvent> consumer) {
        buttonEvents.forEach(consumer);
        buttonEvents.clear();
    }

    public void acceptCharactersTyped(IntConsumer consumer) {
        charactersTyped.forEach(consumer);
        charactersTyped.clear();
    }

    public void acceptDroppedFiles(Consumer<String> consumer) {
        droppedFiles.forEach(consumer);
        droppedFiles.clear();
    }

    public void acceptScrolls(Consumer<Vector2d> consumer) {
        scrolls.forEach(consumer);
        scrolls.clear();
    }

    public void makeCurrentContext() {
        glfwMakeContextCurrent(handle);
    }

    public String getClipboardString() {
        return glfwGetClipboardString(handle);
    }

    public void setClipboardString(String string) {
        glfwSetClipboardString(handle, string);
    }

    public boolean buttonDown(Button button) {
        if (button.device == Button.Device.KEYBOARD) {
            return glfwGetKey(handle, button.glfwCode) == GLFW_PRESS;
        }

        if (button.device == Button.Device.MOUSE) {
            return glfwGetMouseButton(handle, button.glfwCode) == GLFW_PRESS;
        }

        throw new AssertionError();
    }

    public void setRefreshAction(Runnable refreshAction) {
        this.refreshAction = refreshAction;
    }

    public void refresh() {
        if (refreshAction != null) {
            refreshAction.run();
        }
    }

    public void setIcon(byte[] bytes) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer components = stack.mallocInt(1);

            ByteBuffer image = stbi_load_from_memory(buffer, width, height, components, 4);

            if (components.get() != 4) {
                throw new IllegalArgumentException("Icon not RGBA");
            }

            GLFWImage.Buffer icons = GLFWImage.create(1);
            icons.put(GLFWImage.create().set(width.get(), height.get(), image));
            icons.position(0);
            glfwSetWindowIcon(handle, icons);

            stbi_image_free(image);
        }
    }

    public double mouseX() {
        DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(handle, mouseX, mouseY);
        return mouseX.get(0);
    }

    public double mouseY() {
        DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(handle, mouseX, mouseY);
        return mouseY.get(0);
    }

    public CursorMode cursorMode() {
        if (glfwGetInputMode(handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL) {
            return CursorMode.NORMAL;
        }

        if (glfwGetInputMode(handle, GLFW_CURSOR) == GLFW_CURSOR_HIDDEN) {
            return CursorMode.HIDDEN;
        }

        if (glfwGetInputMode(handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            return CursorMode.DISABLED;
        }

        throw new AssertionError();
    }

    public void setCursorMode(CursorMode cursorMode) {
        switch (cursorMode) {
            case NORMAL:
                glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                break;
            case HIDDEN:
                glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
                break;
            case DISABLED:
                glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                break;
            default:
                throw new AssertionError();
        }
    }

    public boolean stickyButtons(Button.Device device) {
        if (device == Button.Device.KEYBOARD) {
            return glfwGetInputMode(handle, GLFW_STICKY_KEYS) == GLFW_TRUE;
        } else if (device == Button.Device.MOUSE) {
            return glfwGetInputMode(handle, GLFW_STICKY_MOUSE_BUTTONS) == GLFW_TRUE;
        } else {
            return false;
        }
    }

    public void setStickyButtons(Button.Device device, boolean stickyButtons) {
        if (device == Button.Device.KEYBOARD) {
            glfwSetInputMode(handle, GLFW_STICKY_KEYS, stickyButtons ? GLFW_TRUE : GLFW_FALSE);
        } else if (device == Button.Device.MOUSE) {
            glfwSetInputMode(handle, GLFW_STICKY_MOUSE_BUTTONS, stickyButtons ? GLFW_TRUE : GLFW_FALSE);
        } else {
            throw new UnsupportedOperationException(device + " doesn't support sticky buttons");
        }
    }

    public boolean rawMouseMotion() {
        return glfwGetInputMode(handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
    }

    public void setRawMouseMotion(boolean rawMouseMotion) {
        glfwSetInputMode(handle, GLFW_RAW_MOUSE_MOTION, rawMouseMotion ? GLFW_TRUE : GLFW_FALSE);
    }

    public boolean modifierKeyLocking() {
        return glfwGetInputMode(handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
    }

    public void setModifierKeyLocking(boolean modifierKeyLocking) {
        glfwSetInputMode(handle, GLFW_LOCK_KEY_MODS, modifierKeyLocking ? GLFW_TRUE : GLFW_FALSE);
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public void setTitle(String title) {
        this.title = title;

        if (handle != 0) {
            glfwSetWindowTitle(handle, title);
        }
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void setResizable(boolean resizable) {
        setHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setVisible(boolean visible) {
        setHint(GLFW_VISIBLE, visible ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setDecorated(boolean decorated) {
        setHint(GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setFocused(boolean focused) {
        setHint(GLFW_FOCUSED, focused ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setAutoIconify(boolean autoIconify) {
        setHint(GLFW_AUTO_ICONIFY, autoIconify ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setFloating(boolean floating) {
        setHint(GLFW_FLOATING, floating ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setMaximized(boolean maximized) {
        setHint(GLFW_MAXIMIZED, maximized ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setCenterCursor(boolean centerCursor) {
        setHint(GLFW_CENTER_CURSOR, centerCursor ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setTransparentFramebuffer(boolean transparentFramebuffer) {
        setHint(GLFW_TRANSPARENT_FRAMEBUFFER, transparentFramebuffer ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setFocusOnShow(boolean focusOnShow) {
        setHint(GLFW_FOCUS_ON_SHOW, focusOnShow ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setScaleToMonitor(boolean scaleToMonitor) {
        setHint(GLFW_SCALE_TO_MONITOR, scaleToMonitor ? GLFW_TRUE : GLFW_FALSE);
    }

    public void setColorBits(int red, int green, int blue, int alpha) {
        setHint(GLFW_RED_BITS, red);
        setHint(GLFW_GREEN_BITS, red);
        setHint(GLFW_BLUE_BITS, red);
        setHint(GLFW_ALPHA_BITS, red);
    }

    public void setDepthBits(int bits) {
        setHint(GLFW_DEPTH_BITS, bits);
    }

    public void setStencilBits(int bits) {
        setHint(GLFW_STENCIL_BITS, bits);
    }

    public void setAccumColorBits(int red, int green, int blue, int alpha) {
        setHint(GLFW_ACCUM_RED_BITS, red);
        setHint(GLFW_ACCUM_GREEN_BITS, red);
        setHint(GLFW_ACCUM_BLUE_BITS, red);
        setHint(GLFW_ACCUM_ALPHA_BITS, red);
    }

    public void setSamples(int samples) {
        setHint(GLFW_SAMPLES, samples);
    }

    public void setNoClientApi() {
        setHint(GLFW_CLIENT_API, GLFW_NO_API);
    }

    private void setHint(int hint, int value) {
        hints.put(hint, value);

        if (handle != 0) {
            throw new IllegalStateException("can't change hints after creation");
        }
    }
}
