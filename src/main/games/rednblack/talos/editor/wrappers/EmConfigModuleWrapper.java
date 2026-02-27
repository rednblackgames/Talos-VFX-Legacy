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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.CheckBoxWidget;
import games.rednblack.talos.runtime.modules.EmConfigModule;

public class EmConfigModuleWrapper extends ModuleWrapper<EmConfigModule> {

    CheckBoxWidget additiveBox;
    CheckBoxWidget blendAddBox;
    CheckBoxWidget attachedBox;
    CheckBoxWidget continuousBox;
    CheckBoxWidget alignedBox;
    CheckBoxWidget immortalBox;

    boolean lockListeners = false;

    @Override
    protected void configureSlots() {
        addOutputSlot("config", EmConfigModule.OUTPUT);

        additiveBox = new CheckBoxWidget();
        additiveBox.init(getSkin());
        additiveBox.setText("additive");

        blendAddBox = new CheckBoxWidget();
        blendAddBox.init(getSkin());
        blendAddBox.setText("blendadd");

        attachedBox = new CheckBoxWidget();
        attachedBox.init(getSkin());
        attachedBox.setText("attached");

        continuousBox = new CheckBoxWidget();
        continuousBox.init(getSkin());
        continuousBox.setText("continuous");

        alignedBox = new CheckBoxWidget();
        alignedBox.init(getSkin());
        alignedBox.setText("aligned");

        immortalBox = new CheckBoxWidget();
        immortalBox.init(getSkin());
        immortalBox.setText("immortal");

        Table form = new Table();
        form.add(additiveBox).left().growX().row();
        form.add(blendAddBox).left().growX().row();
        form.add(attachedBox).left().growX().row();
        form.add(continuousBox).left().growX().row();
        form.add(alignedBox).left().growX().row();
        form.add(immortalBox).left().growX().row();

        contentWrapper.add(form).left().growX();
        contentWrapper.add().expandX();

        rightWrapper.add().expandY();

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        };

        additiveBox.addListener(listener);
        blendAddBox.addListener(listener);
        attachedBox.addListener(listener);
        continuousBox.addListener(listener);
        alignedBox.addListener(listener);
        immortalBox.addListener(listener);
    }

    @Override
    public void setModule(EmConfigModule module) {
        super.setModule(module);
        fromDataToUI();
    }

    public void fromUIToData() {
        if(!lockListeners) {
            module.getUserValue().additive = additiveBox.getValue();
            module.getUserValue().isBlendAdd = blendAddBox.getValue();
            module.getUserValue().attached = attachedBox.getValue();
            module.getUserValue().continuous = continuousBox.getValue();
            module.getUserValue().aligned = alignedBox.getValue();
            module.getUserValue().immortal = immortalBox.getValue();
        }
    }

    public void fromDataToUI() {
        lockListeners = true;
        additiveBox.setChecked(module.getUserValue().additive);
        blendAddBox.setChecked(module.getUserValue().isBlendAdd);
        attachedBox.setChecked(module.getUserValue().attached);
        continuousBox.setChecked(module.getUserValue().continuous);
        alignedBox.setChecked(module.getUserValue().aligned);
        immortalBox.setChecked(module.getUserValue().immortal);
        lockListeners = false;
    }

    @Override
    protected float reportPrefWidth() {
        return 170;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        fromDataToUI();
    }

    public void setAttached(boolean attached) {
        attachedBox.setChecked(attached);
    }

    public void setContinuous(boolean attached) {
        continuousBox.setChecked(attached);
    }

    public void setBlendAdd(boolean blendAdd) {
        blendAddBox.setChecked(blendAdd);
    }

    public void setAdditive(boolean attached) {
        additiveBox.setChecked(attached);
    }

    public void setAligned(boolean attached) {
        alignedBox.setChecked(attached);
    }

    public void setImmortal(boolean immortal) {
        immortalBox.setChecked(immortal);
    }
}
