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
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.*;

public class EmitterModuleWrapper extends ModuleWrapper<EmitterModule> {

    ValueWidget delayWidget;
    ValueWidget durationWidget;
    ValueWidget emissionWidget;

    @Override
    protected float reportPrefWidth() {
        return 180;
    }


    @Override
    protected void configureSlots() {
        delayWidget = addInputSlotWithValueWidget("delay", EmitterModule.DELAY);
        delayWidget.setRange(0, 9999);
        delayWidget.setStep(0.01f);

        durationWidget = addInputSlotWithValueWidget("duration", EmitterModule.DURATION);
        durationWidget.setRange(0, 9999);
        durationWidget.setStep(0.01f);

        emissionWidget = addInputSlotWithValueWidget("emission", EmitterModule.RATE);
        emissionWidget.setRange(0, 9999);
        emissionWidget.setStep(0.01f);

        delayWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                 module.defaultDelay = delayWidget.getValue();
            }
        });

        durationWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.defaultDuration = durationWidget.getValue();
            }
        });

        emissionWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.defaultRate = emissionWidget.getValue();
            }
        });

        addInputSlot("config", EmitterModule.CONFIG).pad(3);
    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == EmitterModule.RATE) {
            return StaticValueModule.class;
        }
        if(slot.getIndex() == EmitterModule.CONFIG) {
            return EmConfigModule.class;
        }
        if(slot.getIndex() == EmitterModule.DURATION) {
            return StaticValueModule.class;
        }
        if(slot.getIndex() == EmitterModule.DELAY) {
            return StaticValueModule.class;
        }

        return null;
    }

    @Override
    public void setModule(EmitterModule module) {
        super.setModule(module);
        delayWidget.setValue(module.defaultDelay);
        durationWidget.setValue(module.defaultDuration);
        emissionWidget.setValue(module.defaultRate);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        delayWidget.setValue(module.defaultDelay);
        durationWidget.setValue(module.defaultDuration);
        emissionWidget.setValue(module.defaultRate);
    }

}
