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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.nodes.widgets.SelectWidget;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.runtime.Expression;
import games.rednblack.talos.runtime.modules.MathModule;
import games.rednblack.talos.runtime.utils.MathExpressionMappings;
import games.rednblack.talos.runtime.values.NumericalValue;

public class MathModuleWrapper extends ModuleWrapper<MathModule> {

    private ValueWidget aField;
    private ValueWidget bField;

    private SelectWidget selectWidget;

    public MathModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        module.a.setFlavour(module.output.getFlavour());
        module.b.setFlavour(module.output.getFlavour());
        aField.setFlavour(module.output.getFlavour());
        bField.setFlavour(module.output.getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            module.a.setFlavour(NumericalValue.Flavour.REGULAR);
            module.b.setFlavour(NumericalValue.Flavour.REGULAR);
            aField.setFlavour(NumericalValue.Flavour.REGULAR);
            bField.setFlavour(NumericalValue.Flavour.REGULAR);
        }
    }

    @Override
    public void setModule(MathModule module) {
        super.setModule(module);
        aField.setValue(module.getDefaultA());
        bField.setValue(module.getDefaultB());
    }

    @Override
    protected void configureSlots() {
        Array<String> mathsExpressions = new Array<>();
        MathExpressionMappings.getAvailableMathExpressions(mathsExpressions);

        selectWidget = new SelectWidget();
        selectWidget.init(getSkin());
        selectWidget.setItems(mathsExpressions, mathsExpressions);

        aField = addInputSlotWithValueWidget("A: ", MathModule.A);
        aField.setRange(-9999, 9999);
        leftWrapper.add(selectWidget).left().expandX().pad(5).padLeft(17).growX().row();
        bField = addInputSlotWithValueWidget("B: ", MathModule.B);
        bField.setRange(-9999, 9999);

        aField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.setA(aField.getValue());
            }
        });

        bField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                module.setB(bField.getValue());
            }
        });


        addOutputSlot("result", 0);

        selectWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedValue = selectWidget.getValue();
                Expression expression = MathExpressionMappings.getMathExpressionForName(selectedValue);

                module.setExpression(expression);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectWidget.setSelected(MathExpressionMappings.getNameForMathExpression(module.getExpression()));

        aField.setValue(module.getDefaultA());
        bField.setValue(module.getDefaultB());
    }
}
