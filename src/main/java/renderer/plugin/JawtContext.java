package renderer.plugin;

import org.lwjgl.system.Platform;
import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;

import java.awt.*;

import static jogamp.nativewindow.jawt.JAWTFactory.JAWT_VERSION_1_4;
import static org.lwjgl.system.jawt.JAWTFunctions.*;

public abstract class JawtContext implements AutoCloseable {
    protected final JAWT jawt;
    protected final JAWTDrawingSurface drawingSurface;

    protected JawtContext(Component component) {
        jawt = JAWT.calloc();
        jawt.version(JAWT_VERSION_1_4);
        JAWT_GetAWT(jawt);
        drawingSurface = JAWT_GetDrawingSurface(component, jawt.GetDrawingSurface());
    }

    public static JawtContext create(Component component) {
        switch (Platform.get()) {
            case WINDOWS:
                return new JawtContextWindows(component);
            case LINUX:
                return new JawtContextLinux(component);
            case MACOSX:
                throw new UnsupportedOperationException();
            default:
                throw new AssertionError();
        }
    }

    public void close() {
        JAWT_FreeDrawingSurface(drawingSurface, jawt.FreeDrawingSurface());
        jawt.free();
    }

    public Lock lock() {
        JAWT_DrawingSurface_Lock(drawingSurface, drawingSurface.Lock());
        return () -> JAWT_DrawingSurface_Unlock(drawingSurface, drawingSurface.Unlock());
    }

    public abstract void attach();

    public abstract void update();

    public abstract int width();

    public abstract int height();

    public abstract void detach();

    public interface Lock extends AutoCloseable {
        void close();
    }
}
