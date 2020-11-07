package renderer.util;

import org.joml.Vector3d;

import java.awt.*;

public class Colors {
    public static int hsl(int hsl) {
        float hue = (hsl >> 10 & 63) / 63f;
        float saturation = (hsl >> 7 & 7) / 7f;
        float lightness = (hsl & 127) / 127f;

        float brightness = lightness < 0.5 ? lightness * (1 + saturation) : lightness * (1 - saturation) + saturation;
        float adjustedSaturation = 2 * (1 - lightness / brightness);
        return Color.HSBtoRGB(hue, adjustedSaturation, brightness);
    }

    public static int pack(Vector3d add) {
        return ((int) (add.x * 255) << 16) | ((int) (add.y * 255) << 8) | ((int) (add.z * 255));
    }

    public static Vector3d unpack(int i) {
        if (i == -1) {
            return null;
        }

        return new Vector3d(
                (i >> 16 & 0xff) / 255.,
                (i >> 8 & 0xff) / 255.,
                (i & 0xff) / 255.
        );
    }
}
