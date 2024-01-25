package games.rednblack.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.widgets.ui.common.ColorLibrary;

public class ColorWidget extends AbstractWidget<Color> {

    private Color color = new Color();
    private Table colorButton;

    @Override
    public void init(Skin skin) {
        super.init(skin);

        Label label = new Label("Color", skin);

        colorButton = new Table();
        colorButton.setBackground(skin.newDrawable(ColorLibrary.SHAPE_SQUIRCLE));

        content.add(label).left().expandX().height(32);
        content.add(colorButton).right().expandX().height(32).width(96);
        color.set(Color.CORAL);
        colorButton.setColor(color);

        colorButton.setTouchable(Touchable.enabled);
        colorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        color.set(newColor);
                        colorButton.setColor(newColor);

                        fireChangedEvent();
                    }
                });
            }
        });
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {

    }

    @Override
    public Color getValue () {
        return color;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        color = json.readValue(Color.class, jsonValue);
        colorButton.setColor(color);
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, color);
    }
}
