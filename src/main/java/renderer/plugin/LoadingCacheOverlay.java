package renderer.plugin;

import net.runelite.client.ui.overlay.Overlay;

import java.awt.*;

public class LoadingCacheOverlay extends Overlay {
    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setColor(new Color(255, 0, 0));
        graphics.drawString("Downloading game cache, this may take a few minutes", 0, 0);
        return null;
    }
}
