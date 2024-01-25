package games.rednblack.talos.editor.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

/**
 * @author Eduard Peshtmaljyan
 */
public class ClippedNinePatchDrawable extends BaseDrawable {

    private ClippedNinePatch patch;

    public float maskScaleX = 1, maskScaleY = 1;

    public ClippedNinePatchDrawable (ClippedNinePatchDrawable drawable) {
        super(drawable);
        this.patch = drawable.patch;
    }

    public ClippedNinePatchDrawable (ClippedNinePatch patch) {
        this.patch = patch;
    }

    public ClippedNinePatchDrawable (TextureAtlas.AtlasRegion region) {
        int[] splits = region.findValue("split");
        this.patch = new ClippedNinePatch(region,
                splits[0],
                splits[1],
                splits[2],
                splits[3]);
    }

    public void setMaskScale (float clipScaleX, float clipScaleY) {
        this.maskScaleX = clipScaleX;
        this.maskScaleY = clipScaleY;
    }

    public void setColor (Color color) {
        patch.setColor(color);
    }

    @Override
    public void draw (Batch batch, float x, float y, float width, float height) {
        patch.setMaskScale(maskScaleX, maskScaleY);
        patch.draw(batch, x, y, width, height);
    }
}
