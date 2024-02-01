package games.rednblack.talos.runtime.bvb;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Slot;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;
import games.rednblack.talos.runtime.ScopePayload;
import games.rednblack.talos.runtime.values.NumericalValue;

public class BoundEffect {
    public String skin, animation;
    public BoundEffectData data;
    protected final boolean forever = false;

    protected transient SkeletonContainer parent;

    protected transient ParticleEffectInstancePool particleEffectInstancePool;

    /**
     * even though this is one effect, many instances of it can be rendered at the same time
     * in cases when it starts more often then finishes
     */
    protected final Array<ParticleEffectInstance> particleEffects = new Array<>();
    protected final Array<ParticleEffectInstance> removeList = new Array<>();

    /**
     * each effect hsa it's own instance of scope payload, we want this global values local to effect type
     */
    protected final ScopePayload scopePayload = new ScopePayload();

    /**
     * System vars
     */
    protected final Vector2 tmpVec = new Vector2();
    protected final NumericalValue val = new NumericalValue();

    public static class BoundEffectData {
        public String effectName, startEvent = "", completeEvent = "";
        public boolean isStandalone, isBehind, isNested;
        public Array<AttachmentPoint> valueAttachments;
        public AttachmentPoint positionAttachment;
    }

    public void setParticleEffectPool(ParticleEffectInstancePool particleEffectInstancePool) {
        this.particleEffectInstancePool = particleEffectInstancePool;
    }

    public void setParent(SkeletonContainer parent) {
        this.parent = parent;
    }

    public boolean isBehind() {
        return data.isBehind;
    }

    public boolean isNested () {
        return data.isNested;
    }

    public AttachmentPoint getPositionAttachment() {
        return data.positionAttachment;
    }

    public String getStartEvent() {
        return data.startEvent;
    }

    public String getCompleteEvent() {
        return data.completeEvent;
    }

    public String getEffectName() {
        return data.effectName;
    }

    public Array<ParticleEffectInstance> getParticleEffects() {
        return particleEffects;
    }

    public void startInstance() {
        if(forever) return;

        if(data.isStandalone && !particleEffects.isEmpty()) return;

        ParticleEffectInstance instance = particleEffectInstancePool.obtain();
        instance.restart();
        instance.setScope(scopePayload);
        particleEffects.add(instance);
    }

    public void completeInstance() {
        if(forever) return;
        if(data.isStandalone && particleEffects.size == 1) return;

        for(ParticleEffectInstance instance: particleEffects) {
            instance.allowCompletion();
        }
    }

    public void resetInstances() {
        for (ParticleEffectInstance effect: particleEffects) {
            if (effect instanceof ParticleEffectInstancePool.PooledParticleEffectInstance)
                ((ParticleEffectInstancePool.PooledParticleEffectInstance) effect).free();
        }

        removeList.clear();
        particleEffects.clear();
    }

    public void update(float delta) {
        // value attachments
        for(AttachmentPoint attachmentPoint: data.valueAttachments) {
            if(attachmentPoint.isStatic()) {
                scopePayload.setDynamicValue(attachmentPoint.getSlotId(), attachmentPoint.getStaticValue());
            } else {
                Bone bone = parent.getBoneByName(attachmentPoint.getBoneName());
                attachmentPoint.setBoneScale(bone.getWorldScaleX());
                float rotation = bone.getWorldRotationX();
                Color color = Color.WHITE;
                for(Slot slot: parent.getSkeleton().getSlots()) {
                    if(slot.getBone().getData().getName().equals(bone.getData().getName())) {
                        //can be many
                        color = slot.getColor();
                        break;
                    }
                }

                tmpVec.set(attachmentPoint.getWorldOffsetX(), attachmentPoint.getWorldOffsetY());
                tmpVec.rotate(rotation);
                tmpVec.add(parent.getBonePosX(attachmentPoint.getBoneName()), parent.getBonePosY(attachmentPoint.getBoneName()));

                if (attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.POSITION) {
                    val.set(tmpVec.x, tmpVec.y);
                } else if (attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.ROTATION) {
                    val.set(rotation);
                } else if(attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.TRANSPARENCY) {
                    val.set(color.a);
                } else if(attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.COLOR) {
                    val.set(color.r, color.g, color.b);
                }

                scopePayload.setDynamicValue(attachmentPoint.getSlotId(), val);
            }
        }

        // update position for each instance and update effect itself
        removeList.clear();
        for(ParticleEffectInstance instance: particleEffects) {
            if(instance.isComplete()) {
                removeList.add(instance);
            }
            if (data.positionAttachment != null) {
                if(data.positionAttachment.isStatic()) {
                    instance.setPosition(data.positionAttachment.getStaticValue().get(0), data.positionAttachment.getStaticValue().get(1));
                } else {
                    Bone bone = parent.getBoneByName(data.positionAttachment.getBoneName());
                    data.positionAttachment.setBoneScale(bone.getWorldScaleX());

                    tmpVec.set(data.positionAttachment.getWorldOffsetX(), data.positionAttachment.getWorldOffsetY());
                    float rotation = bone.getWorldRotationX();
                    tmpVec.rotate(rotation);
                    tmpVec.add(parent.getBonePosX(data.positionAttachment.getBoneName()), parent.getBonePosY(data.positionAttachment.getBoneName()));
                    instance.setPosition(tmpVec.x, tmpVec.y);
                }

                instance.update(delta);
            }
        }

        for(ParticleEffectInstance instance: removeList) {
            if (instance instanceof ParticleEffectInstancePool.PooledParticleEffectInstance)
                ((ParticleEffectInstancePool.PooledParticleEffectInstance) instance).free();
            particleEffects.removeValue(instance, true);
        }
    }
}