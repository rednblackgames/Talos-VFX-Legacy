package games.rednblack.talos.runtime.bvb;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.*;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.render.ParticleRenderer;

public class SkeletonContainer {
    public String skeletonName;
    public final Array<BoundEffect> boundEffects = new Array<>();

    public transient Skeleton skeleton;
    public transient AnimationState animationState;

    //Skin -> Animation -> BoundEffect
    private  transient final ObjectMap<String, ObjectMap<String, Array<BoundEffect>>> boundEffectsMap = new ObjectMap<>();
    private transient BVBSkeletonRenderer skeletonRenderer;

    public SkeletonContainer() {

    }

    public SkeletonContainer(SkeletonContainer container) {
        skeletonName = container.skeletonName;
        boundEffects.addAll(container.boundEffects);
    }


    public void setSkeleton(Skeleton skeleton, AnimationState animationState, BVBParticleEffectPoolProvider poolProvider) {
        this.skeleton = skeleton;
        for (BoundEffect effect : boundEffects) {
            ObjectMap<String, Array<BoundEffect>> animationMap = boundEffectsMap.get(effect.skin);
            if (animationMap == null) {
                animationMap = new ObjectMap<>();
                boundEffectsMap.put(effect.skin, animationMap);
            }

            Array<BoundEffect> effects = animationMap.get(effect.animation);
            if (effects == null) {
                effects = new Array<>();
                animationMap.put(effect.animation, effects);
            }
            effect.setParticleEffectPool(poolProvider.getPool(effect.data.effectName));
            effect.setParent(this);
            effects.add(effect);
        }

        this.animationState = animationState;
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                super.event(entry, event);
                for(BoundEffect boundEffect : boundEffects) {
                    String startEvent = boundEffect.getStartEvent();
                    String completeEvent = boundEffect.getCompleteEvent();
                    if(startEvent.equals(event.getData().getName())) {
                        boundEffect.startInstance();
                    }
                    if(completeEvent.equals(event.getData().getName())) {
                        boundEffect.completeInstance();
                    }
                }
            }

            @Override
            public void start(AnimationState.TrackEntry entry) {
                super.start(entry);
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                /**
                 * A loop has been done, so stopping and starting
                 */
                for(BoundEffect boundEffect: boundEffects) {
                    String completeEventName = boundEffect.getCompleteEvent();
                    if(completeEventName.isEmpty()) {
                        boundEffect.completeInstance();
                    }
                    String startEventName = boundEffect.getStartEvent();
                    if(startEventName.isEmpty()) {
                        boundEffect.startInstance();
                    }
                }

                super.complete(entry);
            }
        });
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void setSkeletonRenderer(BVBSkeletonRenderer skeletonRenderer) {
        this.skeletonRenderer = skeletonRenderer;
    }

    public Bone getBoneByName(String boneName) {
        return skeleton.findBone(boneName);
    }

    public float getBonePosX(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getWorldX();
        }

        return 0;
    }

    public float getBonePosY(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getWorldY();
        }

        return 0;
    }

    public void update(float delta) {
        for(BoundEffect boundEffect: boundEffects) {
            boundEffect.update(delta);
        }
    }

    public void draw(ParticleRenderer particleRenderer) {
        drawVFXBefore(particleRenderer);
        //Draw Spine and nested
        drawSkeletonAndVFXNested(particleRenderer);
        drawVFX(particleRenderer);
    }

    private void drawVFXBefore(ParticleRenderer particleRenderer) {
        if (skeleton == null) return;

        for(BoundEffect effect: boundEffects) {
            if(effect.isNested() || !effect.isBehind()) continue;
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                particleRenderer.render(particleEffectInstance);
            }
        }
    }

    protected void drawSkeletonAndVFXNested(ParticleRenderer particleRenderer) {
        if (skeletonRenderer != null) {
            skeletonRenderer.draw(particleRenderer, this, skeleton);
        }
    }

    private void drawVFX(ParticleRenderer particleRenderer) {
        if (skeleton == null) return;

        for(BoundEffect effect: boundEffects) {
            if(effect.isNested() || effect.isBehind()) continue;
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                particleRenderer.render(particleEffectInstance);
            }
        }
    }

    /**
     * Find the bound effect, the skeleton instance is passed because particle may be bound to a SkeletonAttachment
     *
     * @param skeleton
     * @param slot
     * @return
     */
    public BoundEffect findEffect (Skeleton skeleton, Slot slot) {
        final String boneName = slot.getBone().getData().getName();

        final Skin skin = skeleton.getSkin();
        final ObjectMap<String, Array<BoundEffect>> entries = boundEffectsMap.get(skin.getName());

        if (entries == null) return null;

        for (Array<BoundEffect> value : entries.values()) {
            for (int i = 0; i < value.size; i++) {
                final BoundEffect boundEffect = value.get(i);
                if (boundEffect.getPositionAttachment().getBoneName().equals(boneName)) {
                    return boundEffect;
                }
            }
        }
        return null;
    }
}
