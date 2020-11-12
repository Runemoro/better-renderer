package renderer.plugin;

import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.awt.PlatformGLCanvas;
import org.lwjgl.opengl.awt.PlatformMacOSXGLCanvas;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.macosx.ObjCRuntime;

import java.awt.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.CGL.*;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.system.JNI.invokePPP;
import static org.lwjgl.system.JNI.invokePPPP;
import static org.lwjgl.system.jawt.JAWTFunctions.*;
import static org.lwjgl.system.macosx.ObjCRuntime.objc_getClass;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_getUid;

public class PlatformMacOSXGLCanvas2 extends PlatformMacOSXGLCanvas {
    private static final int NSOpenGLPFAAllRenderers = 1;    /* choose from all available renderers          */
    private static final int NSOpenGLPFATripleBuffer = 3;    /* choose a triple buffered pixel format        */
    private static final int NSOpenGLPFADoubleBuffer = 5;    /* choose a double buffered pixel format        */
    private static final int NSOpenGLPFAAuxBuffers = 7;    /* number of aux buffers                        */
    private static final int NSOpenGLPFAColorSize = 8;    /* number of color buffer bits                  */
    private static final int NSOpenGLPFAAlphaSize = 11;    /* number of alpha component bits               */
    private static final int NSOpenGLPFADepthSize = 12;    /* number of depth buffer bits                  */
    private static final int NSOpenGLPFAStencilSize = 13;    /* number of stencil buffer bits                */
    private static final int NSOpenGLPFAAccumSize = 14;    /* number of accum buffer bits                  */
    private static final int NSOpenGLPFAMinimumPolicy = 51;    /* never choose smaller buffers than requested  */
    private static final int NSOpenGLPFAMaximumPolicy = 52;    /* choose largest buffers of type requested     */
    private static final int NSOpenGLPFASampleBuffers = 55;    /* number of multi sample buffers               */
    private static final int NSOpenGLPFASamples = 56;    /* number of samples per multi sample buffer    */
    private static final int NSOpenGLPFAAuxDepthStencil = 57;    /* each aux buffer has its own depth stencil    */
    private static final int NSOpenGLPFAColorFloat = 58;    /* color buffers store floating point pixels    */
    private static final int NSOpenGLPFAMultisample = 59;    /* choose multisampling                         */
    private static final int NSOpenGLPFASupersample = 60;    /* choose supersampling                         */
    private static final int NSOpenGLPFASampleAlpha = 61;    /* request alpha filtering                      */
    private static final int NSOpenGLPFARendererID = 70;    /* request renderer by ID                       */
    private static final int NSOpenGLPFANoRecovery = 72;    /* disable all failure recovery systems         */
    private static final int NSOpenGLPFAAccelerated = 73;    /* choose a hardware accelerated renderer       */
    private static final int NSOpenGLPFAClosestPolicy = 74;    /* choose the closest color buffer to request   */
    private static final int NSOpenGLPFABackingStore = 76;    /* back buffer contents are valid after swap    */
    private static final int NSOpenGLPFAScreenMask = 84;    /* bit mask of supported physical screens       */
    private static final int NSOpenGLPFAAllowOfflineRenderers = 96;  /* allow use of offline renderers               */
    private static final int NSOpenGLPFAAcceleratedCompute = 97;    /* choose a hardware accelerated compute device */
    private static final int NSOpenGLPFAOpenGLProfile = 99;    /* specify an OpenGL Profile to use             */
    private static final int NSOpenGLPFAVirtualScreenCount = 128;    /* number of virtual screens in this format     */

    private static final int NSOpenGLPFAStereo = 6;
    private static final int NSOpenGLPFAOffScreen = 53;
    private static final int NSOpenGLPFAFullScreen = 54;
    private static final int NSOpenGLPFASingleRenderer = 71;
    private static final int NSOpenGLPFARobust = 75;
    private static final int NSOpenGLPFAMPSafe = 78;
    private static final int NSOpenGLPFAWindow = 80;
    private static final int NSOpenGLPFAMultiScreen = 81;
    private static final int NSOpenGLPFACompliant = 83;
    private static final int NSOpenGLPFAPixelBuffer = 90;
    private static final int NSOpenGLPFARemotePixelBuffer = 91;

    private static final int NSOpenGLProfileVersion3_2Core = 0x3200;
    private static final int NSOpenGLProfileVersionLegacy = 0x1000;
    private static final int NSOpenGLProfileVersion4_1Core = 0x4100;

    private static final long objc_msgSend;
    private static final long CATransaction;
    private static final long NSOpenGLPixelFormat;

    static {
        objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
        CATransaction = objc_getClass("CATransaction");
        NSOpenGLPixelFormat = objc_getClass("NSOpenGLPixelFormat");
    }

    public JAWTDrawingSurface ds;
    private long view;
    private int width;
    private int height;

    // core animation flush
    private static void caFlush() {
        invokePPP(CATransaction, sel_getUid("flush"), objc_msgSend);
    }

    @Override
    public long create(Canvas canvas, GLData attribs, GLData effective) throws AWTException {
        ds = JAWT_GetDrawingSurface(canvas, awt.GetDrawingSurface());
        JAWTDrawingSurface ds = JAWT_GetDrawingSurface(canvas, awt.GetDrawingSurface());
        try {
            int lock = JAWT_DrawingSurface_Lock(ds, ds.Lock());
            if ((lock & JAWT_LOCK_ERROR) != 0)
                throw new AWTException("JAWT_DrawingSurface_Lock() failed");
            try {
                JAWTDrawingSurfaceInfo dsi = JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds, ds.GetDrawingSurfaceInfo());
                try {
                    width = dsi.bounds().width();
                    height = dsi.bounds().height();
                    long pixelFormat = invokePPP(NSOpenGLPixelFormat, sel_getUid("alloc"), objc_msgSend);

                    //TODO: we don't really need 100
                    ByteBuffer attribsArray = ByteBuffer.allocateDirect(4 * 100).order(ByteOrder.nativeOrder());
                    attribsArray.putInt(NSOpenGLPFAAccelerated);
                    attribsArray.putInt(NSOpenGLPFAClosestPolicy);
                    if (attribs.stereo) {
                        attribsArray.putInt(NSOpenGLPFAStereo);
                    }
                    if (attribs.doubleBuffer) {
                        //doesn't work currently
                        //attribsArray.putInt(NSOpenGLPFADoubleBuffer);
                    }

                    if (attribs.pixelFormatFloat) {
                        attribsArray.putInt(NSOpenGLPFAColorFloat);
                    }

                    attribsArray.putInt(NSOpenGLPFAAccumSize);
                    attribsArray.putInt(attribs.accumRedSize + attribs.accumGreenSize + attribs.accumBlueSize + attribs.accumAlphaSize);

                    int colorBits = attribs.redSize +
                            attribs.greenSize +
                            attribs.blueSize;

                    // macOS needs non-zero color size, so set reasonable values
                    if (colorBits == 0)
                        colorBits = 24;
                    else if (colorBits < 15)
                        colorBits = 15;

                    attribsArray.putInt(NSOpenGLPFAColorSize);
                    attribsArray.putInt(colorBits);

                    attribsArray.putInt(NSOpenGLPFAAlphaSize);
                    attribsArray.putInt(attribs.alphaSize);

                    attribsArray.putInt(NSOpenGLPFADepthSize);
                    attribsArray.putInt(attribs.depthSize);

                    attribsArray.putInt(NSOpenGLPFAStencilSize);
                    attribsArray.putInt(attribs.stencilSize);

                    if (attribs.samples == 0) {
                        attribsArray.putInt(NSOpenGLPFASampleBuffers);
                        attribsArray.putInt(0);
                    } else {
                        attribsArray.putInt(NSOpenGLPFASampleBuffers);
                        attribsArray.putInt(1);
                        attribsArray.putInt(NSOpenGLPFASamples);
                        attribsArray.putInt(attribs.samples);
                    }

                    if (attribs.profile == GLData.Profile.CORE) {
                        attribsArray.putInt(NSOpenGLPFAOpenGLProfile);
                        attribsArray.putInt(NSOpenGLProfileVersion3_2Core);
                    }
                    if (attribs.profile == GLData.Profile.COMPATIBILITY) {
                        attribsArray.putInt(NSOpenGLPFAOpenGLProfile);
                        attribsArray.putInt(NSOpenGLProfileVersionLegacy);
                    } else {
                        if (attribs.majorVersion >= 4) {
                            attribsArray.putInt(NSOpenGLPFAOpenGLProfile);
                            attribsArray.putInt(NSOpenGLProfileVersion4_1Core);
                        } else if (attribs.majorVersion >= 3) {
                            attribsArray.putInt(NSOpenGLPFAOpenGLProfile);
                            attribsArray.putInt(NSOpenGLProfileVersion3_2Core);
                        } else {
                            attribsArray.putInt(NSOpenGLPFAOpenGLProfile);
                            attribsArray.putInt(NSOpenGLProfileVersionLegacy);
                        }
                    }

                    // 0 Terminated
                    attribsArray.putInt(0);

                    attribsArray.rewind();

                    pixelFormat = invokePPPP(pixelFormat, sel_getUid("initWithAttributes:"), MemoryUtil.memAddress(attribsArray), objc_msgSend);

                    try {
                        Method createView = PlatformMacOSXGLCanvas.class.getDeclaredMethod("createView", long.class, long.class, int.class, int.class, int.class, int.class);
                        createView.setAccessible(true);
                        createView.invoke(this, dsi.platformInfo(), pixelFormat, dsi.bounds().x(), dsi.bounds().y(), width, height);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }

                    caFlush();
                    long openGLContext = invokePPP(view, sel_getUid("openGLContext"), objc_msgSend);
                    return invokePPP(openGLContext, sel_getUid("CGLContextObj"), objc_msgSend);
                } finally {
                    JAWT_DrawingSurface_FreeDrawingSurfaceInfo(dsi, ds.FreeDrawingSurfaceInfo());
                }
            } finally {
                JAWT_DrawingSurface_Unlock(ds, ds.Unlock());
            }
        } finally {
            JAWT_FreeDrawingSurface(ds, awt.FreeDrawingSurface());
        }
    }
}