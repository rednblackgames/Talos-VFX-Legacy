package games.rednblack.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import games.rednblack.talos.editor.utils.SharedShapeDrawer;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CircularPort extends Widget {

    private static final float DEFAULT_SIZE = 15f;
    private static final float BORDER_WIDTH = 1.5f;

    private final Color fillColor = new Color();
    private final Color borderColor = new Color();

    public CircularPort(Color fill, Color border) {
        fillColor.set(fill);
        borderColor.set(border);
        setSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public void setFillColor(Color color) {
        fillColor.set(color);
    }

    public void setBorderColor(Color color) {
        borderColor.set(color);
    }

    @Override
    public float getPrefWidth() {
        return DEFAULT_SIZE;
    }

    @Override
    public float getPrefHeight() {
        return DEFAULT_SIZE;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        ShapeDrawer shapeDrawer = SharedShapeDrawer.getInstance().getShapeDrawer(batch);

        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;

        Color bc = batch.getColor();

        shapeDrawer.setColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a * parentAlpha);
        shapeDrawer.filledCircle(cx, cy, radius);

        shapeDrawer.setColor(borderColor.r, borderColor.g, borderColor.b, borderColor.a * parentAlpha);
        shapeDrawer.circle(cx, cy, radius, BORDER_WIDTH, JoinType.SMOOTH);

        shapeDrawer.setColor(bc);
    }
}
