package renderer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import renderer.plugin.BetterRendererPlugin;

public class PluginRendererTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(BetterRendererPlugin.class);
        RuneLite.main(args);
    }
}
