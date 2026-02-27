package games.rednblack.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.runtime.modules.RibbonModule;

public class RibbonModuleWrapper extends ModuleWrapper<RibbonModule> {

    private ValueWidget memoryDuration;
    private ValueWidget detailCount;

    @Override
    public void setModule(RibbonModule module) {
        super.setModule(module);
        detailCount.setValue(module.getDetailCount());
        memoryDuration.setValue(module.getMemoryDuration());
    }

    @Override
    protected void configureSlots() {
        addInputSlot("main texture",  RibbonModule.MAIN_REGION);
        addInputSlot("ribbon texture",  RibbonModule.RIBBON_REGION);

        addInputSlot("thickness",  RibbonModule.THICKNESS);
        addInputSlot("transparency",  RibbonModule.TRANSPARENCY);
        addInputSlot("color",  RibbonModule.COLOR);

        addOutputSlot("output", RibbonModule.OUTPUT);

        detailCount = new ValueWidget();
        detailCount.init(getSkin());
        detailCount.setLabel("detail count:");
        detailCount.setRange(1, 200);
        detailCount.setStep(1);
        detailCount.setValue(20);
        leftWrapper.add(detailCount).pad(3).left().expandX().growX().row();

        detailCount.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setDetailCount(Math.round(detailCount.getValue()));
            }
        });

        memoryDuration = new ValueWidget();
        memoryDuration.init(getSkin());
        memoryDuration.setLabel("memory:");
        memoryDuration.setRange(0, 100);
        memoryDuration.setStep(0.01f);
        memoryDuration.setValue(1);
        leftWrapper.add(memoryDuration).left().expandX().growX().pad(3);

        memoryDuration.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setMemoryDuration(memoryDuration.getValue());
            }
        });
    }


    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        detailCount.setValue(module.getDetailCount());
        memoryDuration.setValue(module.getMemoryDuration());
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }


    @Override
    protected float reportPrefWidth() {
        return 180;
    }
}
