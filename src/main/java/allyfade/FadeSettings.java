package allyfade;

import arc.Core;
import arc.math.Mathf;

/**
 * Settings keys, defaults and a once-per-frame snapshot.
 *
 * <p>Reading Core.settings once per frame keeps the per-unit path free of map
 * lookups and also makes the standard "Reset to Defaults" button take effect
 * immediately, because the snapshot is re-read from disk on every frame.</p>
 */
public final class FadeSettings {

    public static final String KEY_ENABLED = "allyfade-enabled";
    public static final String KEY_OPACITY = "allyfade-opacity";
    public static final String KEY_CENTRAL_ZONE = "allyfade-central-zone";
    public static final String KEY_TRANSITION_ZONE = "allyfade-transition-zone";
    public static final String KEY_SMOOTHNESS = "allyfade-smoothness";
    public static final String KEY_SIZE_AWARE = "allyfade-size-aware";
    public static final String KEY_KEEP_PLAYER_OPAQUE = "allyfade-keep-player-opaque";

    public static final boolean DEF_ENABLED = true;
    public static final int DEF_OPACITY = 25;
    public static final int DEF_CENTRAL_ZONE = 4;
    public static final int DEF_TRANSITION_ZONE = 16;
    public static final int DEF_SMOOTHNESS = 50;
    public static final boolean DEF_SIZE_AWARE = true;
    public static final boolean DEF_KEEP_PLAYER_OPAQUE = true;

    // Defaults that are valid before the first refresh.
    public static boolean enabled = DEF_ENABLED;
    public static float minOpacity = DEF_OPACITY / 100f;
    public static float centralZone = DEF_CENTRAL_ZONE / 100f;
    public static float transitionZone = DEF_TRANSITION_ZONE / 100f;
    public static float smoothness = DEF_SMOOTHNESS / 100f;
    public static boolean sizeAware = DEF_SIZE_AWARE;
    public static boolean keepPlayerOpaque = DEF_KEEP_PLAYER_OPAQUE;

    static boolean uiInstalled = false;

    private FadeSettings() {
    }

    public static void registerDefaults() {
        Core.settings.defaults(
                KEY_ENABLED, DEF_ENABLED,
                KEY_OPACITY, DEF_OPACITY,
                KEY_CENTRAL_ZONE, DEF_CENTRAL_ZONE,
                KEY_TRANSITION_ZONE, DEF_TRANSITION_ZONE,
                KEY_SMOOTHNESS, DEF_SMOOTHNESS,
                KEY_SIZE_AWARE, DEF_SIZE_AWARE,
                KEY_KEEP_PLAYER_OPAQUE, DEF_KEEP_PLAYER_OPAQUE
        );
        refresh();
    }

    /** Called once per rendered frame, before the draw group is iterated. */
    public static void refresh() {
        enabled = Core.settings.getBool(KEY_ENABLED, DEF_ENABLED);
        minOpacity = Mathf.clamp(Core.settings.getInt(KEY_OPACITY, DEF_OPACITY) / 100f);
        centralZone = Mathf.clamp(Core.settings.getInt(KEY_CENTRAL_ZONE, DEF_CENTRAL_ZONE) / 100f);
        transitionZone = Mathf.clamp(Core.settings.getInt(KEY_TRANSITION_ZONE, DEF_TRANSITION_ZONE) / 100f);
        smoothness = Mathf.clamp(Core.settings.getInt(KEY_SMOOTHNESS, DEF_SMOOTHNESS) / 100f);
        sizeAware = Core.settings.getBool(KEY_SIZE_AWARE, DEF_SIZE_AWARE);
        keepPlayerOpaque = Core.settings.getBool(KEY_KEEP_PLAYER_OPAQUE, DEF_KEEP_PLAYER_OPAQUE);
    }
}
