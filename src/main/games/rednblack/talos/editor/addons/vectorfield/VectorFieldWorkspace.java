package games.rednblack.talos.editor.addons.vectorfield;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.utils.SharedShapeDrawer;
import games.rednblack.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import games.rednblack.talos.editor.widgets.ui.ViewportWidget;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class VectorFieldWorkspace extends ViewportWidget implements Json.Serializable {

    private int gridX = 16;
    private int gridY = 16;
    private float fieldScale = 1f;

    // field data: vectors stored as [x][y] with Vector2 direction+magnitude
    private Vector2[][] field;

    // brush
    public enum BrushMode {
        DIRECTIONAL, RADIAL_OUT, RADIAL_IN, VORTEX_CW, VORTEX_CCW, TURBULENCE, ERASE
    }

    private BrushMode brushMode = BrushMode.DIRECTIONAL;
    private float brushRadius = 2f;
    private float brushStrength = 1f;
    private float brushAngle = 0f; // for directional mode, degrees

    // visualization
    private float arrowScale = 0.8f;
    private boolean colorByDirection = true;

    // interaction state
    private boolean painting = false;
    private final Vector2 paintWorldPos = new Vector2();
    private final Vector2 lastPaintWorldPos = new Vector2();

    // temp vectors
    private final Vector2 tmp = new Vector2();
    private final Vector2 tmp2 = new Vector2();
    private final Vector3 tmp3 = new Vector3();
    private final Color tmpColor = new Color();

    public VectorFieldWorkspace () {
        setWorldSize(2f);
        initField(gridX, gridY);
    }

    private void markDirty () {
        TalosMain.Instance().ProjectController().setDirty();
    }

    public void initField (int x, int y) {
        gridX = x;
        gridY = y;
        field = new Vector2[gridX][gridY];
        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                field[i][j] = new Vector2();
            }
        }
        //markDirty();
    }

    @Override
    public void initializeGridPropertyProvider () {
        gridPropertyProvider = new DynamicGridPropertyProvider();
        gridPropertyProvider.getBackgroundColor().set(0.12f, 0.12f, 0.12f, 1f);
    }

    @Override
    protected void addPanListener () {
        super.addPanListener();

        addListener(new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT && !canMoveAround()) {
                    painting = true;
                    Vector2 world = getWorldFromLocal(x, y);
                    paintWorldPos.set(world);
                    lastPaintWorldPos.set(world);
                    applyBrush(world.x, world.y);
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (painting) {
                    Vector2 world = getWorldFromLocal(x, y);
                    paintWorldPos.set(world);
                    applyBrush(world.x, world.y);
                    lastPaintWorldPos.set(world);
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    painting = false;
                    markDirty();
                }
            }
        });
    }

    private void applyBrush (float worldX, float worldY) {
        float halfField = fieldScale * 0.5f;
        float cellW = fieldScale / gridX;
        float cellH = fieldScale / gridY;

        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                float cx = -halfField + (i + 0.5f) * cellW;
                float cy = -halfField + (j + 0.5f) * cellH;

                float dist = Vector2.dst(worldX, worldY, cx, cy);
                if (dist > brushRadius * cellW) continue;

                float falloff = 1f - (dist / (brushRadius * cellW));
                falloff = falloff * falloff; // quadratic falloff
                float strength = brushStrength * falloff;

                switch (brushMode) {
                    case DIRECTIONAL:
                        tmp.set(MathUtils.cosDeg(brushAngle), MathUtils.sinDeg(brushAngle)).scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case RADIAL_OUT:
                        tmp.set(cx - worldX, cy - worldY).nor().scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case RADIAL_IN:
                        tmp.set(worldX - cx, worldY - cy).nor().scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case VORTEX_CW:
                        tmp.set(cy - worldY, -(cx - worldX)).nor().scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case VORTEX_CCW:
                        tmp.set(-(cy - worldY), cx - worldX).nor().scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case TURBULENCE:
                        float noiseX = (float) (Math.sin(cx * 7.3f + cy * 3.1f) * Math.cos(cy * 5.7f));
                        float noiseY = (float) (Math.cos(cx * 4.1f + cy * 6.3f) * Math.sin(cx * 2.9f));
                        tmp.set(noiseX, noiseY).nor().scl(strength);
                        field[i][j].add(tmp);
                        break;
                    case ERASE:
                        field[i][j].scl(1f - strength);
                        break;
                }

                // clamp magnitude
                if (field[i][j].len() > 1f) {
                    field[i][j].nor();
                }
            }
        }
    }

    @Override
    public void drawContent (Batch batch, float parentAlpha) {
        ShapeDrawer shapeDrawer = SharedShapeDrawer.getInstance().getShapeDrawer(batch);

        gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
        ((DynamicGridPropertyProvider) gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);
        gridPropertyProvider.update(camera, parentAlpha);
        gridRenderer.drawGrid(batch, shapeDrawer);

        drawFieldBounds(shapeDrawer);
        drawArrows(shapeDrawer);

        if (painting) {
            drawBrushCursor(shapeDrawer);
        }
    }

    private void drawFieldBounds (ShapeDrawer shapeDrawer) {
        float halfField = fieldScale * 0.5f;
        float thickness = pixelToWorld(1.5f);

        tmpColor.set(0.4f, 0.6f, 0.8f, 0.5f);
        shapeDrawer.setColor(tmpColor);

        // draw field boundary rectangle
        shapeDrawer.line(-halfField, -halfField, halfField, -halfField, thickness);
        shapeDrawer.line(halfField, -halfField, halfField, halfField, thickness);
        shapeDrawer.line(halfField, halfField, -halfField, halfField, thickness);
        shapeDrawer.line(-halfField, halfField, -halfField, -halfField, thickness);
    }

    private void drawArrows (ShapeDrawer shapeDrawer) {
        float halfField = fieldScale * 0.5f;
        float cellW = fieldScale / gridX;
        float cellH = fieldScale / gridY;
        float maxArrowLen = Math.min(cellW, cellH) * arrowScale * 0.5f;
        float lineThickness = pixelToWorld(1.5f);
        float headSize = pixelToWorld(4f);

        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                Vector2 v = field[i][j];
                float mag = v.len();
                if (mag < 0.001f) continue;

                float cx = -halfField + (i + 0.5f) * cellW;
                float cy = -halfField + (j + 0.5f) * cellH;

                // arrow color
                if (colorByDirection) {
                    float hue = v.angleDeg();
                    if (hue < 0) hue += 360;
                    hsvToRgb(hue, 0.7f + 0.3f * mag, 0.6f + 0.4f * mag, tmpColor);
                } else {
                    tmpColor.set(0.3f + 0.7f * mag, 0.3f + 0.2f * mag, 0.3f, 1f);
                }
                tmpColor.a = 0.5f + 0.5f * mag;

                shapeDrawer.setColor(tmpColor);

                float arrowLen = maxArrowLen * mag;
                float dx = v.x / mag * arrowLen;
                float dy = v.y / mag * arrowLen;

                float startX = cx - dx * 0.5f;
                float startY = cy - dy * 0.5f;
                float endX = cx + dx * 0.5f;
                float endY = cy + dy * 0.5f;

                // arrow body
                shapeDrawer.line(startX, startY, endX, endY, lineThickness);

                // arrowhead
                float angle = MathUtils.atan2(dy, dx);
                float headAngle = 2.5f; // ~143 degrees
                float hx1 = endX - headSize * MathUtils.cos(angle - headAngle * 0.5f);
                float hy1 = endY - headSize * MathUtils.sin(angle - headAngle * 0.5f);
                float hx2 = endX - headSize * MathUtils.cos(angle + headAngle * 0.5f);
                float hy2 = endY - headSize * MathUtils.sin(angle + headAngle * 0.5f);

                shapeDrawer.filledTriangle(endX, endY, hx1, hy1, hx2, hy2);
            }
        }
    }

    private void drawBrushCursor (ShapeDrawer shapeDrawer) {
        float cellW = fieldScale / gridX;
        float radius = brushRadius * cellW;
        float thickness = pixelToWorld(1f);

        tmpColor.set(1f, 1f, 1f, 0.3f);
        shapeDrawer.setColor(tmpColor);
        shapeDrawer.circle(paintWorldPos.x, paintWorldPos.y, radius, thickness);
    }

    private static void hsvToRgb (float h, float s, float v, Color out) {
        float c = v * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        out.set(r + m, g + m, b + m, 1f);
    }

    // --- Export to FGA format ---

    public String exportFGA () {
        StringBuilder sb = new StringBuilder();
        sb.append(gridX).append(',');
        sb.append(gridY).append(',');
        sb.append(1); // zSize = 1 for 2D

        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                sb.append(',').append(field[i][j].x);
                sb.append(',').append(field[i][j].y);
                sb.append(',').append(0); // z = 0
            }
        }
        return sb.toString();
    }

    // --- Import from FGA format ---

    public void importFGA (String data) {
        String[] arr = data.split(",");
        int xSize = Integer.parseInt(arr[0].trim());
        int ySize = Integer.parseInt(arr[1].trim());
        // int zSize = Integer.parseInt(arr[2].trim()); // unused, we only care about z=0 slice

        initField(xSize, ySize);

        int index = 3;
        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                float vx = Float.parseFloat(arr[index++].trim());
                float vy = Float.parseFloat(arr[index++].trim());
                index++; // skip vz
                field[i][j].set(vx, vy);
            }
        }
        markDirty();
    }

    public void clearField () {
        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                field[i][j].set(0, 0);
            }
        }
        markDirty();
    }

    public void randomizeField (long seed, float magnitude, float frequency) {
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                // layered pseudo-noise using sin/cos with seed-derived offsets
                float ox = rng.nextFloat() * 1000f;
                float oy = rng.nextFloat() * 1000f;
                float fx = i * frequency + ox;
                float fy = j * frequency + oy;
                float vx = (float) (Math.sin(fx * 1.7f) * Math.cos(fy * 2.3f) + Math.sin(fy * 0.9f) * 0.5f);
                float vy = (float) (Math.cos(fx * 2.1f) * Math.sin(fy * 1.3f) + Math.cos(fx * 0.7f) * 0.5f);
                field[i][j].set(vx, vy).nor().scl(magnitude * (0.3f + 0.7f * rng.nextFloat()));
            }
        }
        markDirty();
    }

    // --- Json serialization for project files ---

    @Override
    public void write (Json json) {
        json.writeValue("gridX", gridX);
        json.writeValue("gridY", gridY);
        json.writeValue("fieldScale", fieldScale);
        json.writeValue("arrowScale", arrowScale);
        json.writeValue("colorByDirection", colorByDirection);
        json.writeValue("brushMode", brushMode.name());
        json.writeValue("brushRadius", brushRadius);
        json.writeValue("brushStrength", brushStrength);
        json.writeValue("brushAngle", brushAngle);

        // write field data as flat float array
        float[] fieldData = new float[gridX * gridY * 2];
        int idx = 0;
        for (int i = 0; i < gridX; i++) {
            for (int j = 0; j < gridY; j++) {
                fieldData[idx++] = field[i][j].x;
                fieldData[idx++] = field[i][j].y;
            }
        }
        json.writeValue("fieldData", fieldData);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        gridX = jsonData.getInt("gridX", 16);
        gridY = jsonData.getInt("gridY", 16);
        fieldScale = jsonData.getFloat("fieldScale", 1f);
        arrowScale = jsonData.getFloat("arrowScale", 0.8f);
        colorByDirection = jsonData.getBoolean("colorByDirection", true);
        brushRadius = jsonData.getFloat("brushRadius", 2f);
        brushStrength = jsonData.getFloat("brushStrength", 1f);
        brushAngle = jsonData.getFloat("brushAngle", 0f);

        String mode = jsonData.getString("brushMode", "DIRECTIONAL");
        try {
            brushMode = BrushMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            brushMode = BrushMode.DIRECTIONAL;
        }

        initField(gridX, gridY);

        JsonValue fieldDataVal = jsonData.get("fieldData");
        if (fieldDataVal != null) {
            float[] fieldData = fieldDataVal.asFloatArray();
            int idx = 0;
            for (int i = 0; i < gridX; i++) {
                for (int j = 0; j < gridY; j++) {
                    if (idx + 1 < fieldData.length) {
                        field[i][j].set(fieldData[idx], fieldData[idx + 1]);
                    }
                    idx += 2;
                }
            }
        }
    }

    // --- Getters / Setters ---

    public int getGridX () { return gridX; }
    public int getGridY () { return gridY; }
    public float getFieldScale () { return fieldScale; }
    public void setFieldScale (float scale) { this.fieldScale = scale; markDirty(); }
    public float getArrowScale () { return arrowScale; }
    public void setArrowScale (float scale) { this.arrowScale = scale; markDirty(); }
    public boolean isColorByDirection () { return colorByDirection; }
    public void setColorByDirection (boolean val) { this.colorByDirection = val; markDirty(); }
    public BrushMode getBrushMode () { return brushMode; }
    public void setBrushMode (BrushMode mode) { this.brushMode = mode; markDirty(); }
    public float getBrushRadius () { return brushRadius; }
    public void setBrushRadius (float radius) { this.brushRadius = radius; markDirty(); }
    public float getBrushStrength () { return brushStrength; }
    public void setBrushStrength (float strength) { this.brushStrength = strength; markDirty(); }
    public float getBrushAngle () { return brushAngle; }
    public void setBrushAngle (float angle) { this.brushAngle = angle; markDirty(); }
    public Vector2[][] getField () { return field; }
}
