package bot.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** A script overlay: draw-only, above the game, managed by RuneLite's overlay
 * service. Subclasses implement {@link #paint} and register the instance. */
public abstract class ScriptOverlay extends Overlay {
    private final String title;

    protected ScriptOverlay(String title) {
        this.title = title;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public final String title() {
        return title;
    }

    protected abstract void paint(Graphics2D g);

    @Override
    public final Dimension render(Graphics2D graphics) {
        paint(graphics);
        return null;
    }
}
