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

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.editor.widgets.TextureDropWidget;
import games.rednblack.talos.runtime.modules.FlipbookModule;
import games.rednblack.talos.runtime.modules.AbstractModule;

public class FlipbookModuleWrapper extends TextureDropModuleWrapper<FlipbookModule> {

    ValueWidget rows;
    ValueWidget cols;
    ValueWidget duration;

    @Override
    public void setModuleToDefaults () {
        module.regionName = "fire";
    }

    @Override
    protected void configureSlots() {
        super.configureSlots();
        dropWidget = new TextureDropWidget<AbstractModule>(defaultRegion, getSkin(), 100f);
        leftWrapper.add(dropWidget).growX().left().padBottom(3).row();

        addInputSlot("phase",  FlipbookModule.PHASE);

        rows = new ValueWidget();
        rows.init(getSkin());
        rows.setLabel("Rows");
        rows.setRange(1, 100);
        rows.setStep(1);
        rows.setValue(1);

        cols = new ValueWidget();
        cols.init(getSkin());
        cols.setLabel("Cols");
        cols.setRange(1, 100);
        cols.setStep(1);
        cols.setValue(1);

        duration = new ValueWidget();
        duration.init(getSkin());
        duration.setLabel("Duration");
        duration.setRange(0, 9999);
        duration.setStep(0.01f);
        duration.setValue(1f);

        leftWrapper.add(rows).pad(3).left().expandX().growX().row();
        leftWrapper.add(cols).pad(3).left().expandX().growX().row();
        leftWrapper.add(duration).pad(3).left().expandX().growX().row();

        rightWrapper.add().growY().row();
        addOutputSlot("output", FlipbookModule.OUTPUT);

        rows.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setRows(Math.round(rows.getValue()));
            }
        });

        duration.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.duration = duration.getValue();
            }
        });

        cols.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setCols(Math.round(cols.getValue()));
            }
        });
    }

    @Override
    public void setModuleRegion(String name, Sprite region) {
        module.setRegion(name, region);
    }

    @Override
    public void setModule(FlipbookModule module) {
        super.setModule(module);
        rows.setValue(module.getRows());
        cols.setValue(module.getCols());
        duration.setValue(module.duration);
        setTexture(module.regionName + ".png");
    }

    @Override
    protected float reportPrefWidth() {
        return 200;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        rows.setValue(module.getRows());
        cols.setValue(module.getCols());
        duration.setValue(module.duration);
    }
}
