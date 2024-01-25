package games.rednblack.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import games.rednblack.talos.editor.widgets.ui.common.RoundedFlatButton;

public class ButtonWidget extends AbstractWidget<Object> {

    private Label label;

    @Override
    public void init(Skin skin) {
        super.init(skin);

        label = new Label("", skin);
        RoundedFlatButton button = new RoundedFlatButton(skin, label);
        content.add(button).growX();
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        label.setText(element.getText());
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {

    }

    @Override
    public void write(Json json, String name) {

    }
}
