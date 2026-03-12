/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package games.rednblack.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.runtime.modules.StaticValueModule;
import games.rednblack.talos.runtime.values.NumericalValue;

public class StaticValueModuleWrapper extends ModuleWrapper<StaticValueModule> {

    private ValueWidget valueWidget;

    public StaticValueModuleWrapper() {
        super();
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        valueWidget.setFlavour(module.getOutputValue().getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            module.getOutputValue().setFlavour(NumericalValue.Flavour.REGULAR);
            valueWidget.setFlavour(NumericalValue.Flavour.REGULAR);
            valueWidget.setLabel("Number");
        }
    }

    @Override
    public void setModule(StaticValueModule module) {
        super.setModule(module);
        valueWidget.setValue(module.getStaticValue());
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        valueWidget = addContentWidget(new ValueWidget());
        valueWidget.setLabel("Number");
        valueWidget.setRange(-9999, 9999);
        valueWidget.setStep(0.01f);
        valueWidget.setValue(0);

        addOutputSlot("output", 0);

        valueWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = valueWidget.getValue();
                module.setStaticValue(value);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        valueWidget.setValue(module.getStaticValue());
    }

    public void setValue(int val) {
        valueWidget.setValue(val);
        module.setStaticValue(val);
    }
}
