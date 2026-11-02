package allyfade;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Batch;
import arc.graphics.g2d.SpriteBatch;
import arc.math.Mathf;

/**
 * A vanilla SpriteBatch whose packed vertex color can be faded.
 *
 * <p>Everything Mindustry draws (unit body, weapons, engine sprites and
 * particles that use Draw.color, shadows, shields, status effects, payloads,
 * parts) ultimately uses this single packed color as the vertex color. Scaling
 * its alpha here therefore affects the complete visual representation of the
 * current unit, including parts that reset Draw.color to white.</p>
 *
 * <p>getPackedColor() intentionally returns the unfaded value so nested
 * Draw.alpha()/Draw.color() calls compose with the user's color instead of
 * multiplying the fade twice.</p>
 */
public final class FadeBatch extends SpriteBatch {

    private static float fade = 1f;
    private float base = Color.toFloatBits(1f, 1f, 1f, 1f);

    public FadeBatch() {
        super();
        super.setPackedColor(base);
    }

    @Override
    protected void setPackedColor(float color) {
        base = color;
        super.setPackedColor(apply(color, fade));
    }

    @Override
    protected float getPackedColor() {
        return base;
    }

    /**
     * Sets the multiplier for all subsequent vertex colors and refreshes the
     * color that is currently active, so a unit that draws without setting a
     * color first is faded as well.
     */
    public static void setFade(float alpha) {
        float next = Mathf.clamp(alpha);
        if (next == fade) return;

        fade = next;
        Batch batch = Core.batch;
        if (batch instanceof FadeBatch fadeBatch) {
            fadeBatch.refreshColor();
        }
    }

    private void refreshColor() {
        super.setPackedColor(apply(base, fade));
    }

    private static float apply(float packed, float alpha) {
        if (alpha >= 0.999f) return packed;

        int abgr = Color.floatToIntColor(packed);
        int a = (int) (((abgr >>> 24) & 0xff) * alpha + 0.5f);
        return Color.intToFloatColor((abgr & 0x00ffffff) | (a << 24));
    }
}
