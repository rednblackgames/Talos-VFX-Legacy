package games.rednblack.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.runtime.modules.NinePatchModule;

public class NinePatchModuleWrapper extends ModuleWrapper<NinePatchModule> {

    ValueWidget leftSplit;
    ValueWidget rightSplit;
    ValueWidget topSplit;
    ValueWidget bottomSplit;

    @Override
    protected float reportPrefWidth() {
        return 200;
    }

    @Override
    protected void configureSlots () {
        addInputSlot("input",  NinePatchModule.INPUT);
        addOutputSlot("output", NinePatchModule.OUTPUT);

        leftSplit = new ValueWidget();
        leftSplit.init(getSkin());
        leftSplit.setLabel("left split");
        leftSplit.setRange(0, 999);
        leftSplit.setStep(1);
        leftWrapper.add(leftSplit).left().expandX().growX().pad(3).row();

        rightSplit = new ValueWidget();
        rightSplit.init(getSkin());
        rightSplit.setLabel("right split");
        rightSplit.setRange(0, 999);
        rightSplit.setStep(1);
        leftWrapper.add(rightSplit).right().expandX().growX().pad(3).row();

        topSplit = new ValueWidget();
        topSplit.init(getSkin());
        topSplit.setLabel("top split");
        topSplit.setRange(0, 999);
        topSplit.setStep(1);
        leftWrapper.add(topSplit).left().expandX().growX().pad(3).row();

        bottomSplit = new ValueWidget();
        bottomSplit.init(getSkin());
        bottomSplit.setLabel("bottom split");
        bottomSplit.setRange(0, 999);
        bottomSplit.setStep(1);
        leftWrapper.add(bottomSplit).right().expandX().growX().pad(3);

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                updateValues();
            }
        };

        leftSplit.addListener(changeListener);
        rightSplit.addListener(changeListener);
        topSplit.addListener(changeListener);
        bottomSplit.addListener(changeListener);
    }

    @Override
    public void setModule(NinePatchModule module) {
        super.setModule(module);
        setData(module.getSplits());
    }

    private void updateValues() {
        module.setSplits(Math.round(leftSplit.getValue()), Math.round(rightSplit.getValue()),
                Math.round(topSplit.getValue()), Math.round(bottomSplit.getValue()));
        module.resetPatch();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setData(module.getSplits());
    }

    public void setData(int[] splits) {
        leftSplit.setValue(splits[0]);
        rightSplit.setValue(splits[1]);
        topSplit.setValue(splits[2]);
        bottomSplit.setValue(splits[3]);
        updateValues();
    }
}
