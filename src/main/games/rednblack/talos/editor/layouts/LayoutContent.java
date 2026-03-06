package games.rednblack.talos.editor.layouts;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.editor.utils.MsdfFonts;

public class LayoutContent extends LayoutItem {


	public LayoutContent (Skin skin, LayoutGrid grid) {
		super(skin, grid);

		TextraLabel label = MsdfFonts.label("TestContent: " + MathUtils.random(10));

		Table innerContents = new Table();

		innerContents.top().left();
		innerContents.add(label);

		innerContents.setFillParent(true);
		addActor(innerContents);
	}

}
