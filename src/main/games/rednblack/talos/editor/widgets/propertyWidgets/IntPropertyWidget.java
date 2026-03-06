package games.rednblack.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.utils.MsdfFonts;
import games.rednblack.talos.editor.nodes.widgets.ValueWidget;

import java.util.function.Supplier;

public class IntPropertyWidget extends PropertyWidget<Integer> {

    private ValueWidget valueWidget;
    private TextraLabel title;

    public IntPropertyWidget() {
        super();
    }

    public IntPropertyWidget(String name, Supplier<Integer> supplier, ValueChanged<Integer> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    protected void build (String name) {
        listener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                try {
                    if (event.getTarget() == valueWidget) {
                        callValueChanged(Math.round(valueWidget.getValue()));
                    }
                } catch (NumberFormatException e) {
                    callValueChanged(0);
                }
            }
        };

        valueWidget = new ValueWidget();
        valueWidget.init(TalosMain.Instance().getSkin());
        valueWidget.setRange(-9999, 9999);
        valueWidget.setStep(1f);
        valueWidget.setValue(0);
        valueWidget.setLabel("");

        title = MsdfFonts.label(name);
        title.setAlignment(Align.left);

        add(title).minWidth(70);
        add(valueWidget).growX().maxWidth(200).right().expand();

        valueWidget.addListener(listener);
    }

    @Override
    public void updateWidget(Integer value) {
        valueWidget.removeListener(listener);
        if (value == null) {
            valueWidget.setNone();
        } else {
            valueWidget.setValue(value);
        }
        valueWidget.addListener(listener);
    }

    public void setValue(int value) {
        valueWidget.setValue(value);
        this.value = value;
    }

    public void configureFromValues (int min, int max) {
        valueWidget.setRange(min, max);
    }

    @Override
    public PropertyWidget clone () {
        IntPropertyWidget clone = (IntPropertyWidget) super.clone();
        clone.title.setText(this.title.storedText);
        return clone;
    }
}
