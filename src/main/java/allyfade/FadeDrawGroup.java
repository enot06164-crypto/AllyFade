package allyfade;

import arc.func.Cons;
import mindustry.entities.EntityGroup;
import mindustry.entities.EntityIndexer;
import mindustry.gen.Drawc;

/**
 * The entity group used by the renderer for every Drawc entity.
 *
 * <p>Renderer.draw() calls Groups.draw.draw(Drawc::draw). This subclass keeps
 * the original viewport culling from EntityGroup and additionally scopes the
 * fade to a single entity: status effects, UnitType.draw() and every nested
 * weapon/engine/shield/payload draw happen between setFade(alpha) and the
 * restoration in the finally block.</p>
 *
 * <p>The indexer is required: Mindustry removes entities by their stored group
 * index. Without it, a swapped entity keeps a stale index and can stay in the
 * array after being freed.</p>
 *
 * <p>The wrapper Cons is a field, so no lambda is allocated per frame or per
 * entity.</p>
 */
public final class FadeDrawGroup extends EntityGroup<Drawc> {

    private final Cons<Drawc> perEntity = this::drawOne;
    private Cons<Drawc> delegate;

    public FadeDrawGroup(EntityIndexer indexer) {
        super(Drawc.class, false, false, indexer);
    }

    @Override
    public void draw(Cons<Drawc> cons) {
        FadeSettings.refresh();

        if (!FadeSettings.enabled) {
            super.draw(cons);
            return;
        }

        delegate = cons;
        super.draw(perEntity);
    }

    private void drawOne(Drawc drawable) {
        float alpha = FadeCalculator.alphaFor(drawable);

        if (alpha >= 0.999f) {
            delegate.get(drawable);
            return;
        }

        FadeBatch.setFade(alpha);
        try {
            delegate.get(drawable);
        } finally {
            FadeBatch.setFade(1f);
        }
    }
}
