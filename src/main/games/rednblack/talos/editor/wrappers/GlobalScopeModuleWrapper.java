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
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.nodes.widgets.SelectWidget;
import games.rednblack.talos.editor.widgets.ui.DragPoint;
import games.rednblack.talos.editor.widgets.ui.PreviewWidget;
import games.rednblack.talos.runtime.modules.GlobalScopeModule;
import games.rednblack.talos.runtime.values.NumericalValue;

public class GlobalScopeModuleWrapper extends ModuleWrapper<GlobalScopeModule> implements IDragPointProvider {

    SelectWidget selectWidget;

    DragPoint dragPoint;

    @Override
    protected void configureSlots() {
        dragPoint = new DragPoint(0, 0);

        Array<String> array = new Array<>();
        for(int i = 0; i < 10; i++) {
            array.add(i+"");
        }

        selectWidget = new SelectWidget();
        selectWidget.init(getSkin());
        selectWidget.setItems(array, array);

        leftWrapper.add(selectWidget).left().expandX().padBottom(4).padLeft(5).padRight(10).growX().row();

        selectWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFromSelectBox();
            }
        });

        addOutputSlot("output", GlobalScopeModule.OUTPUT);
    }

    @Override
    public void setModule(GlobalScopeModule module) {
        super.setModule(module);
        NumericalValue value = TalosMain.Instance().globalScope.getDynamicValue(module.getKey());
        dragPoint.set(value.get(0), value.get(1));
        selectWidget.setSelected(String.valueOf(module.getKey()));
    }

    @Override
    protected void wrapperSelected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.registerForDragPoints(this);
        updateFromSelectBox();
    }

    @Override
    protected void wrapperDeselected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.unregisterDragPoints(this);
    }

    private void updateFromSelectBox() {
        String selected = selectWidget.getValue();
        int key = Integer.parseInt(selected);
        module.setKey(key);
        NumericalValue value = TalosMain.Instance().globalScope.getDynamicValue(key);
        dragPoint.set(value.get(0), value.get(1));
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public DragPoint[] fetchDragPoints() {
        return new DragPoint[]{dragPoint};
    }

    @Override
    public void dragPointChanged(DragPoint point) {
        TalosMain.Instance().globalScope.setDynamicValue(module.getKey(), dragPoint.position);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectWidget.setSelected(module.getKey()+"");
    }
}
