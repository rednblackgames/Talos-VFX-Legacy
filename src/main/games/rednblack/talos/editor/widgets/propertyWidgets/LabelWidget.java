package games.rednblack.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.editor.utils.MsdfFonts;

import java.util.function.Supplier;

public class LabelWidget extends PropertyWidget<String> {

	private TextraLabel propertyValue;

	public LabelWidget() {
		super();
	}

	public LabelWidget(String name, Supplier<String> supplier) {
		super(name, supplier, null);
	}

	@Override
	public Actor getSubWidget() {
		propertyValue = MsdfFonts.label("");
		propertyValue.setEllipsis("...");
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
