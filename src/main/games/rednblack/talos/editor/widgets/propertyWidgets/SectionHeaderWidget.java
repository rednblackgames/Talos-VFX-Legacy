package games.rednblack.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.utils.MsdfFonts;
import games.rednblack.talos.editor.widgets.ui.common.ColorLibrary;

public class SectionHeaderWidget extends PropertyWidget<String> {

    private TextraLabel headerLabel;

    public SectionHeaderWidget () {
        super();
    }

    public SectionHeaderWidget (String title) {
        super(null, () -> title, value -> {});
    }

    @Override
    public Actor getSubWidget () {
        Table container = new Table();

        headerLabel = MsdfFonts.label("");
        headerLabel.setAlignment(Align.left);
        headerLabel.setColor(ColorLibrary.FONT_GRAY);

        Image separator = new Image(ColorLibrary.obtainBackground(
                TalosMain.Instance().getSkin(), ColorLibrary.BackgroundColor.DARK_GRAY));

        container.add(headerLabel).left().padTop(6).padBottom(2).growX().row();
        container.add(separator).growX().height(1).padBottom(2);

        return container;
    }

    @Override
    public void updateWidget (String value) {
        if (value != null) {
            headerLabel.setText(value);
        }
    }
}
