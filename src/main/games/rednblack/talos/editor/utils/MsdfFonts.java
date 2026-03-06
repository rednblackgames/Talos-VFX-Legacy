package games.rednblack.talos.editor.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TextraLabel;

public class MsdfFonts {
    private static MsdfFonts instance;

    private Font defaultFont;
    private Font smallFont;

    private MsdfFonts() {
    }

    public static MsdfFonts getInstance() {
        if (instance == null) instance = new MsdfFonts();
        return instance;
    }

    public void init() {
        defaultFont = KnownFonts.getOpenSans(Font.DistanceFieldType.MSDF).scaleHeightTo(20);
        smallFont = KnownFonts.getOpenSans(Font.DistanceFieldType.MSDF).scaleHeightTo(15);
    }

    public void replaceSkinFonts(Skin skin) {
        BitmapFont defaultBmf = KnownFonts.getBitmapFont(KnownFonts.OPEN_SANS);
        defaultBmf.getData().setScale(20f / defaultBmf.getData().lineHeight * defaultBmf.getData().scaleY);
        skin.add("default-font", defaultBmf, BitmapFont.class);

        BitmapFont smallBmf = KnownFonts.getBitmapFont(KnownFonts.OPEN_SANS);
        smallBmf.getData().setScale(12f / smallBmf.getData().lineHeight * smallBmf.getData().scaleY);
        skin.add("small-font", smallBmf, BitmapFont.class);
    }

    public Font getDefaultFont() {
        return defaultFont;
    }

    public Font getSmallFont() {
        return smallFont;
    }

    public static TextraLabel label(String text) {
        return new TextraLabel(text, getInstance().defaultFont);
    }

    public static TextraLabel label(String text, Color color) {
        return new TextraLabel(text, getInstance().defaultFont, color);
    }

    public static TextraLabel smallLabel(String text) {
        return new TextraLabel(text, getInstance().smallFont);
    }

    public static TextraLabel smallLabel(String text, Color color) {
        return new TextraLabel(text, getInstance().smallFont, color);
    }

    public void resize(int width, int height) {
        if (defaultFont != null)
            defaultFont.resizeDistanceField(width, height);
        if (smallFont != null)
            smallFont.resizeDistanceField(width, height);
    }

    public void dispose() {
        if (defaultFont != null) defaultFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}
