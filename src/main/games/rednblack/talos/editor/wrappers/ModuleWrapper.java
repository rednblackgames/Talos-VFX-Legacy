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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.textra.TextraLabel;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.Tooltip;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.nodes.widgets.AbstractWidget;
import games.rednblack.talos.editor.nodes.widgets.CircularPort;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;
import games.rednblack.talos.editor.utils.MsdfFonts;
import games.rednblack.talos.editor.widgets.ui.DynamicTable;
import games.rednblack.talos.editor.widgets.ui.EditableLabel;
import games.rednblack.talos.editor.widgets.ui.ModuleBoardWidget;
import games.rednblack.talos.editor.widgets.ui.common.ColorLibrary;
import games.rednblack.talos.editor.widgets.ui.common.ColorLibrary.BackgroundColor;
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.AbstractModule;
import games.rednblack.talos.runtime.values.NumericalValue;

public abstract class ModuleWrapper<T extends AbstractModule> extends VisWindow implements Json.Serializable {

    protected T module;
    protected DynamicTable leftWrapper, rightWrapper, contentWrapper;
    protected Table content;
    protected Table outputRow;

    protected IntMap<Actor> inputSlotMap = new IntMap<>();
    protected IntMap<Actor> outputSlotMap = new IntMap<>();

    private ModuleBoardWidget moduleBoardWidget;

    private int hoveredSlot = -1;
    private boolean hoveredSlotIsInput = false;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();

    private IntMap<String> leftSlotNames = new IntMap<>();
    private IntMap<String> rightSlotNames = new IntMap<>();

    private int id;

    private boolean isSelected = false;

    private int lastAttachedTargetSlot;
    private ModuleWrapper lastAttachedWrapper;

    private EditableLabel titleLabel;
    private String titleOverride = "";
    protected Table headerTable;

    public void setSelectionState(boolean selected) {
        if(isSelected != selected) {
            if(selected) {
                headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", BackgroundColor.LIGHT_BLUE));
                wrapperSelected();
            } else {
                headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", BackgroundColor.RED));
                wrapperDeselected();
            }
        }
        isSelected = selected;
    }

    protected void wrapperSelected() {

    }

    protected void wrapperDeselected() {

    }

    /**
     * Called only when creating a new Module, not when deserializing
     */
    public void setModuleToDefaults () {

    }

    class SlotRowData {
        String title;
        int key;

        public SlotRowData(String title, int key) {
            this.title = title;
            this.key = key;
        }
    }

    public void setTitleText(String text) {
        titleLabel.setText(text);
    }

    public ModuleWrapper() {
        super("", "panel");

        setClip(false);

        // Strip VisWindow's default chrome
        clearChildren();
        padTop(0); padLeft(0); padRight(0); padBottom(0);

        Skin skin = getSkin();

        // --- NodeWidget-style visual structure ---
        Stack mainStack = new Stack();
        Table backgroundTable = new Table();
        Table contentTable = new Table();
        mainStack.add(backgroundTable);
        mainStack.add(contentTable);

        // Header (red, turns blue on selection)
        headerTable = new Table();
        headerTable.setBackground(ColorLibrary.obtainBackground(skin, "node-header", BackgroundColor.RED));

        // Body
        Table bodyTable = new Table();
        bodyTable.setBackground(ColorLibrary.obtainBackground(skin, "node-body", BackgroundColor.WHITE));

        // Shadow border on the whole widget
        setBackground(ColorLibrary.obtainBackground(skin, "node-shadow-border", BackgroundColor.WHITE));

        backgroundTable.add(headerTable).growX().height(32).row();
        backgroundTable.add(bodyTable).grow().row();

        // Editable title in header
        titleLabel = new EditableLabel("Module", skin);
        titleLabel.setListener(new EditableLabel.EditableLabelChangeListener() {
            @Override
            public void changed(String newText) {
                titleOverride = newText;
            }
        });
        headerTable.add(titleLabel).expandX().top().left().padLeft(12).height(15);

        // Content area with slot wrappers
        content = new Table();
        leftWrapper = new DynamicTable();
        leftWrapper.defaults().padBottom(4);
        leftWrapper.defaults().padTop(4);
        rightWrapper = new DynamicTable();
        rightWrapper.defaults().padBottom(4);
        rightWrapper.defaults().padTop(4);
        contentWrapper = new DynamicTable();
        outputRow = new Table();
        outputRow.defaults().padBottom(4);
        outputRow.defaults().padTop(4);

        Stack slotStack = new Stack();
        slotStack.add(leftWrapper);
        slotStack.add(rightWrapper);
        slotStack.add(contentWrapper);

        configureSlots();

        content.add(outputRow).padTop(8).padBottom(8).right().growX().row();
        content.add(slotStack).grow().fill().width(reportPrefWidth()).row();
        contentTable.add(content).padLeft(16).padRight(16).grow().top().padTop(32);
        contentTable.row();
        contentTable.add().height(15).row();

        add(mainStack).width(reportPrefWidth() + 30).pad(15);

        setModal(false);
        setMovable(false);
        setKeepWithinParent(false);
        setKeepWithinStage(false);

        invalidateHierarchy();
        pack();

        addCaptureListener(new InputListener() {

            Vector2 tmp = new Vector2();
            Vector2 prev = new Vector2();
            boolean isDraggingHeader = false;

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                prev.set(x, y);
                ModuleWrapper.this.localToStageCoordinates(prev);
                moduleBoardWidget.wrapperClicked(ModuleWrapper.this);
                // Only allow dragging from header area (top 47px: 15px pad + 32px header)
                isDraggingHeader = y >= getHeight() - 47;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!isDraggingHeader) return;
                tmp.set(x, y);
                ModuleWrapper.this.localToStageCoordinates(tmp);
                super.touchDragged(event, x, y, pointer);
                float deltaX = tmp.x - prev.x;
                float deltaY = tmp.y - prev.y;
                ModuleWrapper.this.moveBy(deltaX, deltaY);
                moduleBoardWidget.wrapperMovedBy(ModuleWrapper.this, deltaX, deltaY);

                prev.set(tmp);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                moduleBoardWidget.wrapperClickedUp(ModuleWrapper.this);
                isDraggingHeader = false;
            }
        });

        String moduleName = TalosMain.Instance().moduleNames.get(getClass());
        new Tooltip.Builder(moduleName).target(this).build();
    }

    protected abstract void configureSlots();

    protected float reportPrefWidth() {
        return 300;
    }

    protected CircularPort createCircularPort() {
        return new CircularPort(BackgroundColor.BROKEN_WHITE.getColor(), BackgroundColor.BROKEN_WHITE.getColor());
    }

    protected void addSeparator(boolean input) {
        if(input) {
            leftWrapper.addSeparator();
        } else {
            leftWrapper.addSeparator();
        }
    }

    protected TextraLabel getLabelFromCell(Cell cell) {
        for(Actor actor: ((Table)cell.getActor()).getChildren()) {
            if(actor instanceof TextraLabel) {
                return (TextraLabel) actor;
            }
        }

        return null;
    }

    protected void markLabelAsHilighted(final Actor label) {
        label.clearActions();
        label.setColor(Color.ORANGE);
        label.addAction(Actions.sequence(Actions.delay(1f), Actions.run(new Runnable() {
            @Override
            public void run() {
                label.setColor(Color.WHITE);
            }
        })));
    }

    protected Cell addInputSlot(String title, int key) {
        Table slotRow = new Table();
        CircularPort port = createCircularPort();
        TextraLabel label = new TextraLabel(title, MsdfFonts.getInstance().getSmallFont());
        slotRow.add(port).left().size(15).padLeft(-24);
        slotRow.add(label).left().padLeft(5).padRight(10);

        Cell cell = leftWrapper.addRow(slotRow, true);

        leftSlotNames.put(key, title);

        configureNodeActions(port, key, true);

        return cell;
    }

    protected Cell addOutputSlot(String title, int key) {
        Table slotRow = new Table();
        CircularPort port = createCircularPort();
        TextraLabel label = new TextraLabel(title, MsdfFonts.getInstance().getSmallFont());
        slotRow.add(label).right().padLeft(10).padRight(5);
        slotRow.add(port).right().size(15).padRight(-24);

        Cell cell = outputRow.add(slotRow).right().expandX();
        outputRow.row();

        rightSlotNames.put(key, title);

        configureNodeActions(port, key, false);

        return cell;
    }


    protected VisTextField addTextField(String title) {
        Table slotRow = new Table();
        VisTextField textField = new VisTextField(title);
        slotRow.add(textField).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        return textField;
    }

    protected VisSelectBox addSelectBox(Array<String> values) {
        Table slotRow = new Table();
        VisSelectBox selectBox = new VisSelectBox();

        selectBox.setItems(values);

        slotRow.add(selectBox).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        return selectBox;
    }

    protected VisSelectBox addSelectBox(IntMap.Values<String> values) {
        return addSelectBox(values.toArray());
    }

    protected void configureNodeActions(final Actor portActor, final int key, final boolean isInput) {

        if(isInput) {
            inputSlotMap.put(key, portActor);
        } else {
            outputSlotMap.put(key, portActor);
        }

        portActor.addListener(new ClickListener() {

            private Vector2 tmp = new Vector2();
            private Vector2 tmp2 = new Vector2();

            private ModuleWrapper currentWrapper;

            private boolean currentIsInput = false;

            private int currentSlot;

            private boolean dragged;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentIsInput = isInput;
                currentWrapper = ModuleWrapper.this;
                tmp.set(x, y);
                portActor.localToStageCoordinates(tmp);
                tmp2.set(portActor.getWidth()/2f, portActor.getHeight()/2f);
                portActor.localToStageCoordinates(tmp2);

                currentSlot = key;

                dragged = false;

                ModuleBoardWidget.NodeConnection connection = moduleBoardWidget.findConnection(ModuleWrapper.this, isInput, key);

                if(isInput && connection!= null) {
                    moduleBoardWidget.removeConnection(connection);
                    moduleBoardWidget.ccCurrentlyRemoving = true;

                    connection.fromModule.getOutputSlotPos(connection.fromSlot, tmp2);
                    currentIsInput = false;
                    currentWrapper = connection.fromModule;
                    currentSlot = connection.fromSlot;
                    moduleBoardWidget.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, false);
                } else {
                    // we are creating new connection
                    moduleBoardWidget.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, isInput);
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                tmp.set(x, y);
                portActor.localToStageCoordinates(tmp);
                moduleBoardWidget.updateActiveCurve(tmp.x, tmp.y);

                dragged = true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                moduleBoardWidget.connectNodeIfCan(currentWrapper, currentSlot, currentIsInput);
                moduleBoardWidget.ccCurrentlyRemoving = false;

                if(!dragged) {
                    // clicked
                    slotClicked(currentSlot, currentIsInput);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                hoveredSlot = key;
                hoveredSlotIsInput = isInput;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                hoveredSlot = -1;
            }
        });

    }

    public void slotClicked(int slotId, boolean isInput) {

        Slot slot = module.getInputSlot(slotId);
        if(!isInput) {
            slot = module.getOutputSlot(slotId);
        }

        if(slot == null) return;

        if(slot.isInput()) {
            Class<? extends AbstractModule> clazz = getSlotsPreferredModule(slot);

            if (clazz != null) {
                ModuleWrapper newWrapper = moduleBoardWidget.createModule(clazz, getX(), getY());

                //connecting
                moduleBoardWidget.makeConnection(newWrapper, this, 0, slotId);

                // now tricky positioning
                float offset = MathUtils.random(100, 300);
                newWrapper.getOutputSlotPos(0, tmp);
                getInputSlotPos(slotId, tmp2);
                tmp2.x -= offset;
                tmp2.sub(tmp);
                tmp2.add(newWrapper.getX(), newWrapper.getY()); // new target
                tmp.set(tmp2).add(offset, 0); // starting position
                newWrapper.setPosition(tmp.x, tmp.y);

                // now the animation
                float duration = 0.2f;
                newWrapper.addAction(Actions.fadeIn(duration));
                newWrapper.addAction(Actions.moveTo(tmp2.x, tmp2.y, duration, Interpolation.swingOut));
            }
        }
    }

    public Class<? extends AbstractModule> getSlotsPreferredModule(Slot slot) {
        return null;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void setModule(T module) {
        this.module = module;
        setTitleText(constructTitle());
    }

    protected String getOverrideTitle() {
        return null;
    }

    public String constructTitle() {

        String override = getOverrideTitle();
        if(override != null) {
            return override;
        }

        if(!titleOverride.equals("")) {
            return titleOverride;
        }

        String name = TalosMain.Instance().moduleNames.get(this.getClass());

        String title = name;

        if(lastAttachedWrapper != null) {
            title = lastAttachedWrapper.getLeftSlotName(lastAttachedTargetSlot);
        }

        return title;
    }

    public T getModule() {
        return module;
    }

    public void setBoard(ModuleBoardWidget moduleBoardWidget) {
        this.moduleBoardWidget = moduleBoardWidget;
    }

    public boolean findHoveredSlot(int[] result) {
        if(hoveredSlot >= 0) {
            result[0] = hoveredSlot;
            if(hoveredSlotIsInput) {
                result[1] = 0;
            } else {
                result[1] = 1;
            }

            return true;
        }

        result[0] = -1;
        result[1] = -1;
        return false;
    }

    public void getInputSlotPos(int slot, Vector2 tmp) {
        if(inputSlotMap.get(slot) == null) return;
        tmp.set(inputSlotMap.get(slot).getWidth()/2f, inputSlotMap.get(slot).getHeight()/2f);
        inputSlotMap.get(slot).localToStageCoordinates(tmp);
    }

    public void getOutputSlotPos(int slot, Vector2 tmp) {
        if(outputSlotMap.get(slot) == null) return;
        tmp.set(outputSlotMap.get(slot).getWidth()/2f, outputSlotMap.get(slot).getHeight()/2f);
        outputSlotMap.get(slot).localToStageCoordinates(tmp);
    }

    public void setSlotActive(int slotTo, boolean isInput) {
        Actor slot = isInput ? inputSlotMap.get(slotTo) : outputSlotMap.get(slotTo);
        if (slot == null) return;
        if (slot instanceof CircularPort) {
            ((CircularPort) slot).setFillColor(BackgroundColor.LIGHT_BLUE.getColor());
        }
    }

    public void setSlotInactive(int slotTo, boolean isInput) {
        Actor slot = isInput ? inputSlotMap.get(slotTo) : outputSlotMap.get(slotTo);
        if (slot == null) return;
        if (slot instanceof CircularPort) {
            ((CircularPort) slot).setFillColor(BackgroundColor.BROKEN_WHITE.getColor());
        }
        if (!isInput) {
            lastAttachedWrapper = null;
            setTitleText(constructTitle());
        }
    }

    protected VisTextField addInputSlotWithTextField(String title, int key) {
        return addInputSlotWithTextField(title, key, 60, false);
    }

    protected VisTextField addInputSlotWithTextField(String title, int key, float size) {
        return addInputSlotWithTextField(title, key, size, false);
    }

    protected VisTextArea addInputSlotWithTextArea (String title, int key) {
        Table slotRow = new Table();
        CircularPort port = createCircularPort();
        TextraLabel label = new TextraLabel(title, MsdfFonts.getInstance().getSmallFont());
        slotRow.add(port).left().size(15).padLeft(-24);
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        VisTextArea textArea = new VisTextArea();
        slotRow.add(textArea).width(60);

        contentWrapper.add(slotRow).left().expandX().pad(3);

        configureNodeActions(port, key, true);

        return textArea;
    }

    protected VisTextField addInputSlotWithTextField(String title, int key, float size, boolean grow) {
        Table slotRow = new Table();
        CircularPort port = createCircularPort();
        TextraLabel label = new TextraLabel(title, MsdfFonts.getInstance().getSmallFont());
        slotRow.add(port).left().size(15).padLeft(-24);
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        final VisTextField textField = new VisTextField();
        slotRow.add().fillX().expandX().growX();
        slotRow.add(textField).right().width(size);

        Cell cell = leftWrapper.add(slotRow).pad(3).expandX().left();
        if(grow) {
            cell.growX();
        }

        leftWrapper.row();

        textField.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(textField.getSelection().length() == 0) {
                    textField.selectAll();
                }
            }
        });

        configureNodeActions(port, key, true);

        return textField;
    }

    protected ValueWidget addInputSlotWithValueWidget(String label, int key) {
        Table slotRow = new Table();
        CircularPort port = createCircularPort();

        ValueWidget valueWidget = new ValueWidget();
        valueWidget.init(getSkin());
        valueWidget.setLabel(label);

        slotRow.add(port).left().size(15).padLeft(-24);
        slotRow.add(valueWidget).left().padLeft(5).growX();

        leftWrapper.add(slotRow).pad(3).expandX().left().growX().row();

        leftSlotNames.put(key, label);
        configureNodeActions(port, key, true);
        return valueWidget;
    }

    protected float floatFromText(String text) {
        float value = 0;
        try {
            if (text.length() > 0) {
                value = Float.parseFloat(text);
            }
        } catch (NumberFormatException e) {

        }

        return value;
    }

    protected float floatFromText(VisTextField textField) {
        float value = 0;
        try {
            if (textField.getText().length() > 0) {
                value = Float.parseFloat(textField.getText());
            }
        } catch (NumberFormatException e) {

        }

        return value;
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }



    public void fileDrop(String[] paths, float x, float y) {
        // do nothing
    }

    public void attachModuleToMyInput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {

    }

    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        // find the flavour
        Slot mySlotObject = getModule().getOutputSlot(mySlot);
        Slot toSlotObject = moduleWrapper.getModule().getInputSlot(targetSlot);
        if(mySlotObject == null || toSlotObject == null) return;
        if(mySlotObject.getValue() instanceof NumericalValue && toSlotObject.getValue() instanceof NumericalValue) {
            NumericalValue myValue = (NumericalValue) mySlotObject.getValue();
            NumericalValue toValue = (NumericalValue) toSlotObject.getValue();

            myValue.setFlavour(toValue.getFlavour());
        }

        // change the name
        lastAttachedTargetSlot = targetSlot;
        lastAttachedWrapper = moduleWrapper;
        setTitleText(constructTitle());
    }

    private String getLeftSlotName(int targetSlot) {
        return leftSlotNames.get(targetSlot);
    }

    protected <W extends AbstractWidget<?>> W addWidgetWithPort(W widget, int slotKey, boolean isInput) {
        widget.init(getSkin());
        CircularPort port = widget.addPort(isInput);
        configureNodeActions(port, slotKey, isInput);
        if (isInput) {
            leftSlotNames.put(slotKey, "");
        } else {
            rightSlotNames.put(slotKey, "");
        }
        contentWrapper.add(widget).growX().padTop(10).padBottom(10).row();
        return widget;
    }

    protected <W extends AbstractWidget<?>> W addContentWidget(W widget) {
        widget.init(getSkin());
        contentWrapper.add(widget).growX().padTop(10).padBottom(10).row();
        return widget;
    }

    @Override
    public void write (Json json) {
		json.writeValue("id", getId());
		if(!titleOverride.equals("")) {
            json.writeValue("titleOverride", titleOverride);
        }
		json.writeValue("x", getX());
		json.writeValue("y", getY());

		json.writeObjectStart("module", module.getClass(), module.getClass());
		json.writeValue("data", module, null);
		json.writeObjectEnd();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
		setId(jsonData.getInt("id"));
		setX(jsonData.getFloat("x"));
 		setY(jsonData.getFloat("y"));
 		titleOverride = jsonData.getString("titleOverride", "");

        module = (T)json.readValue(AbstractModule.class, jsonData.get("module").get("data"));
        //TODO: this has to be create through module graph to go with properr creation channels

        setModule(module);
    }

    protected void setDirty() {
        TalosMain.Instance().ProjectController().setDirty();
    }
}
