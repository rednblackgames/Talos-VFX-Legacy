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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.editor.utils.ScreenshotService;
import games.rednblack.talos.runtime.modules.ColorModule;


public class ColorModuleWrapper extends ModuleWrapper<ColorModule> {

    private Image colorBtn;

    private ColorPicker picker;

    ValueWidget rWidget;
    ValueWidget gWidget;
    ValueWidget bWidget;

    Color tmpClr = new Color();
    Vector2 vec = new Vector2();

    public ColorModuleWrapper () {

    }

    @Override
    protected void configureSlots() {
        picker = new ColorPicker(new ColorPickerAdapter() {
            @Override
            public void changed (Color newColor) {
                if(colorBtn != null) {
                    colorBtn.setColor(newColor);
                    rWidget.setValue(newColor.r * 255f);
                    gWidget.setValue(newColor.g * 255f);
                    bWidget.setValue(newColor.b * 255f);

                    module.setR(newColor.r);
                    module.setG(newColor.g);
                    module.setB(newColor.b);
                }
            }
        });

        // create color picker Btn
        colorBtn = new Image(getSkin().getDrawable("white"));
        leftWrapper.add(colorBtn).width(50).height(50).center().padBottom(3).row();

        rWidget = addInputSlotWithValueWidget("R", 0);
        rWidget.setRange(0, 255);
        rWidget.setStep(1);
        rWidget.setValue(255);

        gWidget = addInputSlotWithValueWidget("G", 1);
        gWidget.setRange(0, 255);
        gWidget.setStep(1);
        gWidget.setValue(0);

        bWidget = addInputSlotWithValueWidget("B", 2);
        bWidget.setRange(0, 255);
        bWidget.setStep(1);
        bWidget.setValue(0);

        rWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setR(rWidget.getValue() / 255f);
                update();
            }
        });

        gWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setG(gWidget.getValue() / 255f);
                update();
            }
        });

        bWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setB(bWidget.getValue() / 255f);
                update();
            }
        });

        addOutputSlot("position", 0);

        colorBtn.setColor(1f, 0, 0, 1f);

        colorBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().getStage().addActor(picker.fadeIn());
            }
        });

        picker.padTop(32);
        picker.padLeft(16);
        picker.setHeight(330);
        picker.setWidth(430);
        picker.padRight(26);
    }

    private void update() {
        colorBtn.setColor(module.getColor());
    }

    @Override
    protected float reportPrefWidth() {
        return 230;
    }

    @Override
    public void setModule(ColorModule module) {
        super.setModule(module);

        final Color color = module.getColor();
        tmpClr.set(color);
        colorBtn.setColor(tmpClr);
        rWidget.setValue(color.r * 255f);
        gWidget.setValue(color.g * 255f);
        bWidget.setValue(color.b * 255f);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        final Color color = module.getColor();
        tmpClr.set(color);

        colorBtn.setColor(tmpClr);
        rWidget.setValue(color.r * 255f);
        gWidget.setValue(color.g * 255f);
        bWidget.setValue(color.b * 255f);
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        ScreenshotService.testForPicker(picker);
    }
}
