package allyfade;

import arc.Core;
import arc.graphics.Camera;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Drawc;
import mindustry.gen.Unit;

/**
 * Pure, allocation-free alpha calculation.
 *
 * <p>All distances are world units. Since the camera width/height are already
 * expressed in world units, percentages of the smaller camera side stay
 * proportional to the screen at every resolution and zoom level.</p>
 */
public final class FadeCalculator {

    private FadeCalculator() {
    }

    public static float alphaFor(Drawc drawable) {
        return drawable instanceof Unit unit ? alphaFor(unit) : 1f;
    }

    public static float alphaFor(Unit unit) {
        if (!FadeSettings.enabled) return 1f;
        if (Vars.player == null || Vars.state == null || !Vars.state.isGame()) return 1f;
        if (unit.type == null || !unit.type.flying) return 1f;
        if (unit.team != Vars.player.team()) return 1f;
        if (FadeSettings.keepPlayerOpaque && unit == Vars.player.unit()) return 1f;

        Camera camera = Core.camera;
        if (camera == null) return 1f;

        float minSide = Math.min(camera.width, camera.height);
        if (!(minSide > 0f)) return 1f;

        float distance = Mathf.dst(unit.x, unit.y, camera.position.x, camera.position.y);
        if (FadeSettings.sizeAware) {
            // Large sprites start fading while their edge, not their center,
            // is still approaching the central zone.
            distance = Math.max(0f, distance - visualRadius(unit));
        }

        float inner = minSide * FadeSettings.centralZone;
        float width = minSide * FadeSettings.transitionZone;

        if (distance >= inner + width) return 1f;
        if (distance <= inner) return FadeSettings.minOpacity;
        if (!(width > 0f)) return 1f;

        float t = (distance - inner) / width;
        t = curve(Mathf.clamp(t));

        return FadeSettings.minOpacity + (1f - FadeSettings.minOpacity) * t;
    }

    /**
     * 0% -> linear, 50% -> smoothstep (the default), 100% -> smootherstep.
     * The 50% value is exactly the classic smoothstep required by the design.
     */
    private static float curve(float t) {
        float smooth = t * t * (3f - 2f * t);
        float s = FadeSettings.smoothness;

        if (s <= 0.5f) {
            return t + (smooth - t) * (s * 2f);
        }

        float smoother = t * t * t * (t * (t * 6f - 15f) + 10f);
        return smooth + (smoother - smooth) * ((s - 0.5f) * 2f);
    }

    /** Approximate world-space radius of the unit sprite. */
    private static float visualRadius(Unit unit) {
        float radius = unit.type.hitSize * 0.5f;
        TextureRegion region = unit.type.region;

        if (region != null && region.found()) {
            float spriteSize = Math.max(region.width, region.height) * region.scale;
            radius = Math.max(radius, spriteSize * 0.5f);
        }

        return radius;
    }
}
