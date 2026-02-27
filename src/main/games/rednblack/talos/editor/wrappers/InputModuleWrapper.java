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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.SelectWidget;
import games.rednblack.talos.runtime.modules.InputModule;
import games.rednblack.talos.runtime.ScopePayload;

public class InputModuleWrapper extends ModuleWrapper<InputModule> {

    IntMap<String> map;

    SelectWidget selectWidget;

    public InputModuleWrapper() {
        super();
    }

    @Override
    public void setModule(InputModule module) {
        super.setModule(module);
        setKey(module.getInput());
    }

    @Override
    protected float reportPrefWidth() {
        return 280;
    }

    @Override
    protected void configureSlots() {
        map = new IntMap<>();
        map.put(ScopePayload.EMITTER_ALPHA, "Emitter.alpha - Duration");
        map.put(ScopePayload.PARTICLE_ALPHA, "Particle.alpha - Lifetime");
        map.put(ScopePayload.EMITTER_ALPHA_AT_P_INIT, "Duration at particle init");
        map.put(ScopePayload.SECONDARY_SEED, "Primary Seed");
        map.put(ScopePayload.PARTICLE_SEED, "Secondary Seed");
        map.put(ScopePayload.PARTICLE_POSITION, "Particle position");
        map.put(ScopePayload.TOTAL_TIME, "Global Time");

        Array<String> displayNames = new Array<>();
        Array<String> values = new Array<>();
        for (IntMap.Entry<String> entry : map) {
            displayNames.add(entry.value);
            values.add(String.valueOf(entry.key));
        }

        selectWidget = new SelectWidget();
        selectWidget.init(getSkin());
        selectWidget.setItems(displayNames, values);

        leftWrapper.add(selectWidget).left().expandX().padBottom(4).padLeft(5).padRight(10).growX().row();

        addOutputSlot("output", 0);

        selectWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedValue = selectWidget.getValue();
                int key = Integer.parseInt(selectedValue);
                module.setInput(key);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setKey(module.getInput());
    }

    public void setKey(int key) {
        selectWidget.setSelected(String.valueOf(key));
        module.setInput(key);
    }
}
