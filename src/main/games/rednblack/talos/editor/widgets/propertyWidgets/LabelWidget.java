package games.rednblack.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import games.rednblack.talos.TalosMain;

import java.util.function.Supplier;

public class LabelWidget extends PropertyWidget<String> {

	private Label propertyValue;

	public LabelWidget() {
		super();
	}

	public LabelWidget(String name, Supplier<String> supplier) {
		super(name, supplier, null);
	}

	@Override
	public Actor getSubWidget() {
		propertyValue = new Label("", TalosMain.Instance().getSkin());
		propertyValue.setEllipsis(true);
		propertyValue.setAlignment(Align.right);

		return propertyValue;
	}

	@Override
	public void updateWidget(String value) {
		if(value == null) {
			propertyValue.setText("-");
		} else {
			propertyValue.setText(value);
		}
	}
}
