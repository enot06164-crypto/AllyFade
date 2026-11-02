package allyfade;

import arc.Core;
import arc.graphics.g2d.Batch;
import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.entities.EntityIndexer;
import mindustry.gen.Drawc;
import mindustry.gen.EffectState;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Installs the two client-side hooks exactly once.
 *
 * <p>1. Core.batch is replaced with FadeBatch, the only place where Arc lets a
 *    mod intercept the packed color used for every sprite/fill vertex.</p>
 * <p>2. Groups.draw is replaced with FadeDrawGroup, which scopes the fade to one
 *    unit around the game's existing draw callback and reuses the normal
 *    camera culling.</p>
 *
 * <p>No game content, textures, gameplay state or unit types are modified.</p>
 */
public final class FadeRenderer {

    private static final ClassValue<Method> INDEX_SETTERS = new ClassValue<>() {
        @Override
        protected Method computeValue(Class<?> type) {
            try {
                Method method = type.getMethod("setIndex__draw", int.class);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException | SecurityException ignored) {
                return null;
            }
        }
    };

    private static boolean installed = false;

    private FadeRenderer() {
    }

    public static void install() {
        if (installed || Vars.headless) return;
        installed = true;

        installBatch();
        installDrawGroup();
    }

    private static void installBatch() {
        if (Core.batch instanceof FadeBatch) return;

        // The old batch is intentionally not disposed: another mod may still
        // hold a reference to it. It is released together with the process.
        Batch previous = Core.batch;
        FadeBatch batch = new FadeBatch();
        Core.batch = batch;

        Log.info("[AllyFade] Fading sprite batch installed (replaced @).",
                previous == null ? "null" : previous.getClass().getSimpleName());
    }

    private static void installDrawGroup() {
        if (Groups.draw instanceof FadeDrawGroup) return;

        EntityGroup<Drawc> previous = Groups.draw;
        EntityIndexer indexer = extractIndexer(previous);
        FadeDrawGroup group = new FadeDrawGroup(indexer);

        // A normal client starts this hook before any world exists. Copying is
        // a safety net for unusual load orders, and indices must be rewritten
        // so every copied entity can still remove itself correctly.
        if (previous != null) {
            for (Drawc drawable : previous.rawSeq()) {
                // A freed effect (effect == null) must never enter the group:
                // clipSize() would dereference it immediately.
                if (drawable instanceof EffectState effect && effect.effect() == null) {
                    continue;
                }

                int index = group.addIndex(drawable);
                indexer.change(drawable, index);
            }
        }

        Groups.draw = group;
        Log.info("[AllyFade] Per-unit draw hook installed.");
    }

    /**
     * Reuses the private callback created by Groups.init(). It updates
     * index__draw for every generated Drawc implementation. If reflection is
     * unavailable, the fallback handles the known types and the rest by name.
     */
    private static EntityIndexer extractIndexer(EntityGroup<Drawc> group) {
        if (group != null) {
            try {
                Field field = EntityGroup.class.getDeclaredField("indexer");
                field.setAccessible(true);
                EntityIndexer indexer = (EntityIndexer) field.get(group);
                if (indexer != null) return indexer;
            } catch (Throwable ignored) {
                // Fall through to the local implementation.
            }
        }

        return FadeRenderer::updateDrawIndex;
    }

    private static void updateDrawIndex(Entityc entity, int index) {
        if (entity instanceof Unit unit) {
            unit.setIndex__draw(index);
        } else if (entity instanceof Player player) {
            player.setIndex__draw(index);
        } else {
            Method setter = INDEX_SETTERS.get(entity.getClass());
            if (setter != null) {
                try {
                    setter.invoke(entity, index);
                } catch (ReflectiveOperationException ignored) {
                    // Nothing else can be done; EntityGroup.removeIndex() will
                    // fall back to an identity search for this entity.
                }
            }
        }
    }
}
