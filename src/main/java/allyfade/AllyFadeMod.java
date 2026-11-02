package allyfade;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;

/**
 * Ally Fade entry point.
 *
 * <p>All work is client-side. The init method is called by Mindustry after the
 * renderer, UI and asset system have been created, but before any world exists,
 * which is exactly the point where the draw hooks can be installed safely.</p>
 */
public class AllyFadeMod extends Mod {

    @Override
    public void init() {
        FadeSettings.registerDefaults();

        try {
            installUi();
        } catch (Throwable error) {
            Log.err("[AllyFade] Could not add the settings category.", error);
        }

        if (Vars.headless) {
            Log.info("[AllyFade] Headless mode: rendering hooks were not installed.");
            return;
        }

        try {
            FadeRenderer.install();
        } catch (Throwable error) {
            Log.err("[AllyFade] Could not install the rendering hooks.", error);
        }
    }

    /**
     * Adds the Ally Fade page to the settings dialog. Kept as a small helper so
     * failures in one part of setup cannot prevent the other parts from running.
     */
    static void installUi() {
        if (FadeSettings.uiInstalled) return;

        SettingsMenuDialog settings = Vars.ui == null ? null : Vars.ui.settings;
        if (settings == null) {
            // Should not happen in a normal client startup, but registering the
            // event makes the mod resilient to a different startup order.
            Events.on(EventType.ClientLoadEvent.class, event -> installUi());
            return;
        }

        FadeSettings.uiInstalled = true;

        settings.addCategory("@allyfade.settings", Icon.eye, table -> {
            table.add(Core.bundle.get("allyfade.settings.description"))
                    .wrap().left().padBottom(10f).row();

            table.checkPref(FadeSettings.KEY_ENABLED, FadeSettings.DEF_ENABLED,
                    value -> FadeSettings.refresh());

            table.sliderPref(FadeSettings.KEY_OPACITY, FadeSettings.DEF_OPACITY,
                    0, 100, 1, value -> value + "%",
                    value -> FadeSettings.refresh());

            table.sliderPref(FadeSettings.KEY_CENTRAL_ZONE, FadeSettings.DEF_CENTRAL_ZONE,
                    0, 40, 1, value -> value + "%",
                    value -> FadeSettings.refresh());

            table.sliderPref(FadeSettings.KEY_TRANSITION_ZONE, FadeSettings.DEF_TRANSITION_ZONE,
                    0, 60, 1, value -> value + "%",
                    value -> FadeSettings.refresh());

            table.sliderPref(FadeSettings.KEY_SMOOTHNESS, FadeSettings.DEF_SMOOTHNESS,
                    0, 100, 1, value -> value + "%",
                    value -> FadeSettings.refresh());

            table.checkPref(FadeSettings.KEY_SIZE_AWARE, FadeSettings.DEF_SIZE_AWARE,
                    value -> FadeSettings.refresh());

            table.checkPref(FadeSettings.KEY_KEEP_PLAYER_OPAQUE, FadeSettings.DEF_KEEP_PLAYER_OPAQUE,
                    value -> FadeSettings.refresh());

            // SettingsTable.rebuild() automatically appends the standard
            // "Reset to Defaults" button for every setting registered above.
        });
    }
}
