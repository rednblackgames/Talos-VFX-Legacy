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
import games.rednblack.talos.editor.widgets.NoiseImage;
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.*;

public class NoiseModuleWrapper extends ModuleWrapper<NoiseModule> {

    NoiseImage noiseImage;
    ValueWidget frequencyWidget;

    @Override
    protected float reportPrefWidth() {
        return 165;
    }


    @Override
    protected void configureSlots() {

        addInputSlot("X: (0 to 1)", NoiseModule.X);
        addInputSlot("Y: (0 to 1)", NoiseModule.Y);

        addOutputSlot("output", NoiseModule.OUTPUT);

        frequencyWidget = new ValueWidget();
        frequencyWidget.init(getSkin());
        frequencyWidget.setLabel("Frequency");
        frequencyWidget.setRange(0.5f, 20f);
        frequencyWidget.setStep(0.1f);
        frequencyWidget.setValue(20f);
        leftWrapper.add(frequencyWidget).growX().padRight(2f).padBottom(5f).row();

        noiseImage = new NoiseImage(getSkin());
        leftWrapper.add(noiseImage).expandX().fillX().growX().height(100).padRight(3).padBottom(3);

        frequencyWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float frequency = frequencyWidget.getValue();
                noiseImage.setFrequency(frequency);
                module.setFrequency(frequency);
            }
        });

        rightWrapper.add().expandY();
    }

    @Override
    public void setModule(NoiseModule module) {
        super.setModule(module);
        noiseImage.setFrequency(module.getFrequency());
        frequencyWidget.setValue(module.getFrequency());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        frequencyWidget.setValue(module.getFrequency());
    }


    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == NoiseModule.X) return InputModule.class;
        if(slot.getIndex() == NoiseModule.Y) return InputModule.class;

        return null;
    }

}
