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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.SelectWidget;
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.*;
import games.rednblack.talos.runtime.utils.InterpolationMappings;

public class InterpolationModuleWrapper extends ModuleWrapper<InterpolationModule> {

    SelectWidget selectWidget;

    public InterpolationModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        Array<String> interps = new Array<>();
        InterpolationMappings.getAvailableInterpolations(interps);

        selectWidget = new SelectWidget();
        selectWidget.init(getSkin());
        selectWidget.setItems(interps, interps);

        leftWrapper.add(selectWidget).left().expandX().padBottom(4).padLeft(5).padRight(10).growX().row();

        selectWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectWidget.getValue();
                Interpolation interp = InterpolationMappings.getInterpolationForName(selectedString);
                module.setInterpolation(interp);
            }
        });
    }

    @Override
    public void setModule(InterpolationModule module) {
        super.setModule(module);
        selectWidget.setSelected(InterpolationMappings.getNameForInterpolation(module.getInterpolation()));
    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == InterpolationModule.ALPHA) return InputModule.class;
        return null;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectWidget.setSelected(InterpolationMappings.getNameForInterpolation(module.getInterpolation()));
    }

}
