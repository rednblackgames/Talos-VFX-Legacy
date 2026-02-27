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
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.editor.widgets.ui.DragPoint;
import games.rednblack.talos.editor.widgets.ui.PreviewWidget;
import games.rednblack.talos.runtime.modules.Vector2Module;

public class Vector2ModuleWrapper extends ModuleWrapper<Vector2Module> implements IDragPointProvider {

	private ValueWidget xWidget;
	private ValueWidget yWidget;

	private DragPoint dragPoint;

	@Override
	public void setModule(Vector2Module module) {
		super.setModule(module);
		xWidget.setValue(module.getDefaultX());
		yWidget.setValue(module.getDefaultY());
	}

	@Override
	protected void configureSlots () {

		xWidget = addInputSlotWithValueWidget("X", 0);
		xWidget.setRange(-9999, 9999);
		xWidget.setStep(0.01f);

		yWidget = addInputSlotWithValueWidget("Y", 1);
		yWidget.setRange(-9999, 9999);
		yWidget.setStep(0.01f);

		dragPoint = new DragPoint(0, 0);

		xWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				float x = xWidget.getValue();
				module.setX(x);
				dragPoint.set(x, dragPoint.position.y);
			}
		});

		yWidget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				float y = yWidget.getValue();
				module.setY(y);
				dragPoint.set(dragPoint.position.x, y);
			}
		});


		addOutputSlot("position", 0);
	}

	@Override
	protected void wrapperSelected() {
		PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
		previewWidget.registerForDragPoints(this);
	}

	@Override
	protected void wrapperDeselected() {
		PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
		previewWidget.unregisterDragPoints(this);
	}

	@Override
	protected float reportPrefWidth () {
		return 210;
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		xWidget.setValue(module.getDefaultX());
		yWidget.setValue(module.getDefaultY());
		dragPoint.set(module.getDefaultX(), module.getDefaultY());
	}

	@Override
	public DragPoint[] fetchDragPoints() {
		return new DragPoint[]{dragPoint};
	}

	@Override
	public void dragPointChanged(DragPoint point) {
		module.setX(point.position.x);
		module.setY(point.position.y);
		xWidget.setValue(module.getDefaultX());
		yWidget.setValue(module.getDefaultY());
	}
}
