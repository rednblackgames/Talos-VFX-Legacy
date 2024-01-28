package games.rednblack.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import games.rednblack.talos.editor.utils.grid.GridLine;
import games.rednblack.talos.editor.utils.grid.GridPropertyProvider;

public class DynamicGridPropertyProvider implements GridPropertyProvider {

    public float gridUnit;
    public float gridXStart;
    public float gridYStart;
    public float gridXEnd;
    public float gridYEnd;
    public float distanceThatLinesShouldBe;
    float thickness;

    Color backgroundColor = Color.BROWN;

    private Array<GridLine> gridLines = new Array<>();
    private boolean highlightZero = true;

    @Override
    public Array<GridLine> getGridLines () {
        return gridLines;
    }

    @Override
    public Color getBackgroundColor () {
        return backgroundColor;
    }

    @Override
    public float getUnitX () {
        return gridUnit;
    }

    @Override
    public float getUnitY () {
        return gridUnit;
    }

    @Override
    public float getWorldHeight () {
        return -1;
    }

    @Override
    public float getWorldWidth () {
        return -1;
    }

    @Override
    public float getGridStartX () {
        return gridXStart;
    }

    @Override
    public float getGridEndX () {
        return gridXEnd;
    }

    @Override
    public float getGridStartY () {
        return gridYStart;
    }

    @Override
    public float getGridEndY () {
        return gridYEnd;
    }

    @Override
    public void setLineThickness (float thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean shouldHighlightCursorHover () {
        return false;
    }

    @Override
    public boolean shouldHighlightCursorSelect () {
        return false;
    }

    @Override
    public void setHighlightCursorHover (boolean shouldHighlight) {

    }

    @Override
    public void setHighlightCursorSelect (boolean shouldHighlight) {

    }

    @Override
    public boolean shouldHighlightZero() {
        return highlightZero;
    }

    Color gridMainLineColor = new Color();
    Color comingLinesColor = new Color();
    Color smallLinesColor = new Color();
    Color zeroColor = new Color();

    @Override
    public void update (OrthographicCamera camera, float parentAlpha) {
        Pools.freeAll(gridLines);
        gridLines.clear();

        float zeroAlpha = 0.2f;
        float mainLinesAlpha = 0.2f;
        float smallLinesAlpha = 0.1f;
        float linesToAppearAlpha = 0.01f;

        gridUnit = nextPowerOfTwo(distanceThatLinesShouldBe);

        float previousUnit = gridUnit / 2;
        linesToAppearAlpha = MathUtils.lerp(smallLinesAlpha, linesToAppearAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));
        smallLinesAlpha = MathUtils.lerp(mainLinesAlpha, smallLinesAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));

        int baseLineDivisor = 4;

        float visibleWidth = camera.viewportWidth * camera.zoom;
        float visibleHeight = camera.viewportHeight * camera.zoom;

        float cameraX = camera.position.x;
        float cameraY = camera.position.y;

        float visibleStartX = cameraX - visibleWidth / 2;
        float visibleStartY = cameraY - visibleHeight / 2;
        float visibleEndX = cameraX + visibleWidth / 2;
        float visibleEndY = cameraY + visibleHeight / 2;

        // configure colors
        gridMainLineColor.set(Color.GRAY);
        gridMainLineColor.a =  mainLinesAlpha * parentAlpha;

        comingLinesColor.set(Color.GRAY);
        comingLinesColor.a =  linesToAppearAlpha * parentAlpha;

        smallLinesColor.set(Color.GRAY);
        smallLinesColor.a =  smallLinesAlpha * parentAlpha;

        zeroColor.set(Color.CYAN);
        zeroColor.a = zeroAlpha;

        gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(visibleStartX, 0), Pools.obtain(Vector2.class).set(visibleEndX, 0), shouldHighlightZero() ? zeroColor : gridMainLineColor, thickness));
        gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(0, visibleStartY), Pools.obtain(Vector2.class).set(0, visibleEndY), shouldHighlightZero() ? zeroColor : gridMainLineColor, thickness));

        gridXStart = gridUnit * MathUtils.floor(visibleStartX / gridUnit) ;

        // creating vertical lines
        for (float i = gridXStart; i < visibleEndX; i += gridUnit) {

            for (int j = 0; j < baseLineDivisor; j++) {
                float smallUnitSize = gridUnit / baseLineDivisor;
                float x1 = i + j * smallUnitSize;

                for (int k = 0; k < baseLineDivisor; k++) {
                    float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;
                    gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(x1 + k * nextUnitSize, cameraY - visibleHeight / 2),
                            Pools.obtain(Vector2.class).set(x1 + k * nextUnitSize, cameraY + visibleHeight / 2), comingLinesColor, thickness));
                }

                gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(x1, cameraY - visibleHeight / 2),
                        Pools.obtain(Vector2.class).set(x1, cameraY + visibleHeight / 2), smallLinesColor, thickness));
            }

            gridXEnd = i;
            if (i == 0) continue;
            gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(i, cameraY - visibleHeight / 2),
                    Pools.obtain(Vector2.class).set(i, cameraY + visibleHeight / 2), gridMainLineColor, thickness));
        }

        gridYStart = gridUnit * MathUtils.floor(visibleStartY / gridUnit);
        // creating vertical lines
        for (float i = gridYStart; i < visibleEndY; i += gridUnit) {
            for (int j = 0; j < baseLineDivisor; j++) {
                float smallUnitSize = gridUnit / baseLineDivisor;
                float y1 = i + j * smallUnitSize;

                for (int k = 0; k < baseLineDivisor; k++) {
                    float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;
                    gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(cameraX - visibleWidth / 2, y1 + k * nextUnitSize),
                            Pools.obtain(Vector2.class).set(cameraX + visibleWidth / 2, y1 + k * nextUnitSize), comingLinesColor, thickness));
                }

                gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(cameraX - visibleWidth / 2, y1),
                        Pools.obtain(Vector2.class).set(cameraX + visibleWidth / 2, y1), smallLinesColor, thickness));
            }

            gridYEnd = i;
            if (i == 0) continue;
            gridLines.add(Pools.obtain(GridLine.class).set(Pools.obtain(Vector2.class).set(cameraX - visibleWidth / 2, i),
                    Pools.obtain(Vector2.class).set(cameraX + visibleWidth / 2, i), gridMainLineColor, thickness));
        }
    }

    private float nextPowerOfTwo (float value) {
        boolean negative = false;
        boolean smallerOne = false;
        if (value < 0) {
            negative = true;
            value *= -1;
        }

        if (value < 1 ) {
            value = 1 / value;
            smallerOne = true;
        }

        float unit = MathUtils.nextPowerOfTwo(MathUtils.ceil(value));
        if (smallerOne) {
            unit = 1 / unit;
            unit *= 2;
        }

        return unit;
    }

    public void hideZero() {
        highlightZero = false;
    }
}
