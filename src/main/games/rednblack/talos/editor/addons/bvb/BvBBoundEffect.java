package games.rednblack.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.EventData;
import games.rednblack.talos.editor.utils.NumberUtils;
import games.rednblack.talos.editor.widgets.propertyWidgets.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.widgets.ui.timeline.TimelineItemDataProvider;
import games.rednblack.talos.runtime.*;
import games.rednblack.talos.runtime.bvb.AttachmentPoint;
import games.rednblack.talos.runtime.bvb.BoundEffect;

import java.util.function.Supplier;

public class BvBBoundEffect extends BoundEffect implements Json.Serializable, IPropertyProvider, TimelineItemDataProvider<BvBBoundEffect> {
    /**
     * Draw order of this effect
     */
    private int drawOrder;

    private float startTime = 0;

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    public BvBBoundEffect() {
        data = new BoundEffectData();
        data.valueAttachments = new Array<>();
    }

    public BvBBoundEffect(String name, ParticleEffectDescriptor descriptor, BvBSkeletonContainer container) {
        parent = container;
        data = new BoundEffectData();
        data.valueAttachments = new Array<>();
        data.effectName = name;
        this.setParticleEffectPool(new ParticleEffectInstancePool(descriptor));
    }

    public void setForever(boolean isForever) {
        /*
        if(isForever && !forever) {
            particleEffects.clear();
            ParticleEffectInstance instance = spawnEffect();
            instance.loopable = true; // this is evil
        }
        forever = isForever;*/
    }

    private ParticleEffectInstance spawnEffect() {
        ParticleEffectInstance instance = particleEffectInstancePool.obtain();
        instance.restart();
        instance.setScope(scopePayload);
        particleEffects.add(instance);

        return instance;
    }

    public void setBehind(boolean isBehind) {
        data.isBehind = isBehind;
    }

    public boolean isBehind() {
        return data.isBehind;
    }

    public boolean isNested () {
        return data.isNested;
    }

    public void removePositionAttachment() {
        data.positionAttachment = null;
    }

    public void setPositionAttachment(String bone) {
        data.positionAttachment = new AttachmentPoint();
        data.positionAttachment.setTypeAttached(bone, -1);
    }

    public Array<ParticleEffectInstance> getParticleEffects() {
        return particleEffects;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        PropertyWidget effectName = WidgetFactory.generate(data, "effectName", "effect name");
        PropertyWidget standalone = WidgetFactory.generate(data, "isStandalone", "standalone");
        PropertyWidget behind = WidgetFactory.generate(data, "isBehind", "is behind");
        PropertyWidget nested = WidgetFactory.generate(data, "isNested", "is nested");

        SelectBoxWidget startEventWidget = new SelectBoxWidget("Start Emitting", new Supplier<String>() {
            @Override
            public String get() {
                return data.startEvent;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                setStartEvent(value);
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return getEvents();
            }
        });

        SelectBoxWidget completeEventWidget = new SelectBoxWidget("Stop Emitting", new Supplier<String>() {
            @Override
            public String get() {
                return data.completeEvent;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                setCompleteEvent(value);
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return getEvents();
            }
        });

        AttachmentPointWidget position = new AttachmentPointWidget(new Supplier<AttachmentPoint>() {
            @Override
            public AttachmentPoint get() {
                return data.positionAttachment;
            }
        }, new Supplier<Array<Bone>>() {
            @Override
            public Array<Bone> get() {
                return parent.getSkeleton().getBones();
            }
        });

        LabelWidget offset = new LabelWidget("Offset", new Supplier<String>() {
            @Override
            public String get() {
                return "X: " + NumberUtils.roundToDecimalPlaces(data.positionAttachment.getWorldOffsetX(), 3) + ", Y: " + NumberUtils.roundToDecimalPlaces(data.positionAttachment.getWorldOffsetY(), 3);
            }
        });

        GlobalValuePointsWidget globalValues = new GlobalValuePointsWidget(new Supplier<Array<AttachmentPoint>>() {
            @Override
            public Array<AttachmentPoint> get() {
                return data.valueAttachments;
            }
        }, new Supplier<Array<Bone>>() {
            @Override
            public Array<Bone> get() {
                return parent.getSkeleton().getBones();
            }
        });

        properties.add(effectName);
        properties.add(standalone);
        properties.add(behind);
        properties.add(nested);
        properties.add(startEventWidget);
        properties.add(completeEventWidget);
        properties.add(position);
        properties.add(offset);
        properties.add(globalValues);

        return properties;
    }

    private void setStartEvent(String value) {
        data.startEvent = value;
    }

    private void setCompleteEvent(String value) {
        data.completeEvent = value;
    }

    protected Array<String> getEvents() {
        Array<EventData> events = parent.getSkeleton().getData().getEvents();
        Array<String> result = new Array<>();
        result.add("");
        for(EventData eventData: events) {
            result.add(eventData.getName());
        }
        return result;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Effect: " + data.effectName;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    public Array<AttachmentPoint> getAttachments() {
        return data.valueAttachments;
    }

    public void updateEffect(ParticleEffectDescriptor descriptor) {
        setParticleEffectPool(new ParticleEffectInstancePool(descriptor));
        if(forever) {
            particleEffects.clear();
            ParticleEffectInstance instance = spawnEffect();
            instance.loopable = true; // this is evil
        }
        // else this will get auto-spawned on next event call anyway.
    }

    public void setParent(BvBSkeletonContainer parent) {
        this.parent = parent;
    }

    @Override
    public void write(Json json) {
        json.writeValue("effectName", data.effectName);
        json.writeValue("isStandalone", data.isStandalone);
        json.writeValue("isBehind", data.isBehind);
        json.writeValue("isNested", data.isNested);
        json.writeValue("positionAttachment", data.positionAttachment);
        json.writeValue("valueAttachments", data.valueAttachments);
        json.writeValue("startEvent", data.startEvent);
        json.writeValue("completeEvent", data.completeEvent);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String effectName = jsonData.getString("effectName");
        BvBSkeletonContainer parent = ((BvBSkeletonContainer) this.parent);
        String effectPath = parent.getWorkspace().getPath(effectName + ".p");
        FileHandle effectHandle = TalosMain.Instance().ProjectController().findFile(effectPath);
        data.effectName = effectName;

        if(effectHandle == null || !effectHandle.exists()) {
           throw new GdxRuntimeException("Particle effect not found");
        }

        parent.getWorkspace().registerTalosAssets(effectHandle);

        //TODO: refactor this
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        descriptor.setAssetProvider(TalosMain.Instance().TalosProject().getProjectAssetProvider());
        descriptor.load(effectHandle);
        parent.getWorkspace().getVfxLibrary().put(data.effectName, descriptor);

        // track this file
        TalosMain.Instance().FileTracker().trackFile(effectHandle, parent.getWorkspace().bvb.particleTracker);

        setParticleEffectPool(new ParticleEffectInstancePool(descriptor));

        data.positionAttachment = json.readValue(AttachmentPoint.class, jsonData.get("positionAttachment"));
        JsonValue valueAttachmentsJson = jsonData.get("valueAttachments");
        for(JsonValue valueAttachmentJson: valueAttachmentsJson) {
            AttachmentPoint point = json.readValue(AttachmentPoint.class, valueAttachmentJson);
            data.valueAttachments.add(point);
        }

        setStartEvent(jsonData.getString("startEvent", ""));
        setCompleteEvent(jsonData.getString("completeEvent", ""));

        data.isStandalone = jsonData.getBoolean("isStandalone", false);
        data.isBehind = jsonData.getBoolean("isBehind");
        data.isNested = jsonData.getBoolean("isNested");

        //setForever(startEvent.equals("") && completeEvent.equals(""));
    }

    /**
     * Timeline Data goes here
     */

    @Override
    public Array<Button> registerSecondaryActionButtons () {
        return null;
    }

    @Override
    public Array<Button> registerMainActionButtons () {
        return null;
    }

    @Override
    public String getItemName () {
        return data.effectName;
    }

    @Override
    public BvBBoundEffect getIdentifier () {
        return this;
    }

    @Override
    public int getIndex () {
        return drawOrder;
    }

    @Override
    public boolean isFull () {
        if(particleEffectInstancePool.getParticleEffectDescriptor().emitterModuleGraphs.size == 0) return false;

        if (particleEffectInstancePool.getParticleEffectDescriptor().isContinuous()) {
            return true;
        }
        return false;
    }

    @Override
    public float getDurationOne () {
        if (particleEffectInstancePool.getParticleEffectDescriptor().isContinuous()) {
            return BvBWorkspace.getInstance().getSkeletonContainer().getCurrentAnimation().getDuration();
        } else {
            float maxDuration = 0;
            Array<ParticleEmitterDescriptor> emitterModuleGraphs = particleEffectInstancePool.getParticleEffectDescriptor().emitterModuleGraphs;
            if(particleEffectInstancePool.getParticleEffectDescriptor().getInstanceReference() == null) return 0;
            for (ParticleEmitterDescriptor descriptor : emitterModuleGraphs) {
                float duration = descriptor.getEmitterModule().getDuration();
                if (maxDuration < duration) {
                    maxDuration = duration;
                }
            }

            return maxDuration;
        }
    }

    @Override
    public float getDurationTwo () {
        if (particleEffectInstancePool.getParticleEffectDescriptor().isContinuous()) {
            return BvBWorkspace.getInstance().getSkeletonContainer().getCurrentAnimation().getDuration();
        } else {
            float maxLife = 0;
            Array<ParticleEmitterDescriptor> emitterModuleGraphs = particleEffectInstancePool.getParticleEffectDescriptor().emitterModuleGraphs;
            if(particleEffectInstancePool.getParticleEffectDescriptor().getInstanceReference() == null) return 0;
            for (ParticleEmitterDescriptor descriptor : emitterModuleGraphs) {

                float life = descriptor.getParticleModule().getLife();
                if (maxLife < life) {
                    maxLife = life;
                }
            }

            return maxLife;
        }
    }

    @Override
    public float getTimePosition () {
        return startTime;
    }

    @Override
    public boolean isItemVisible () {
        return true;
    }

    @Override
    public void setTimePosition (float time) {
        if (particleEffectInstancePool.getParticleEffectDescriptor().isContinuous()) {
            return;
        }

        startTime = time;
        if(startTime < 0) startTime = 0;
    }

    public void setDrawOrder (int drawOrder) {
        this.drawOrder = drawOrder;
    }

    public boolean isContinuous () {
        return particleEffectInstancePool.getParticleEffectDescriptor().isContinuous();
    }
}
