package games.rednblack.talos.editor.utils.grid;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.widgets.ui.ViewportWidget;

import static com.kotcrab.vis.ui.VisUI.getSkin;

public class RulerRenderer extends Group {

    public static final float RULER_SIZE = 20f;

    private GridPropertyProvider gridPropertyProvider;

    private Table yRulerTable;
    private Table xRulerTable;

    private ViewportWidget viewportWidget;

    public RulerRenderer (GridPropertyProvider gridRenderer, ViewportWidget widget) {
        this.gridPropertyProvider = gridRenderer;
        this.viewportWidget = widget;
        addRulers();
    }

    public void setGridPropertyProvider (GridPropertyProvider gridPropertyProvider) {
        this.gridPropertyProvider = gridPropertyProvider;
    }

    protected void addRulers () {
        Skin skin = TalosMain.Instance().getSkin();
        xRulerTable = new Table(skin);
        xRulerTable.background("panel_input_bg");
        addActor(xRulerTable);

        yRulerTable = new Table(skin);
        yRulerTable.background("panel_input_bg");
        addActor(yRulerTable);
    }

    private final ObjectMap<String, Array<Label>> labelCache = new ObjectMap<>();
    private final Array<Table> tableCache = new Array<>();

    private Label getOrCreateLabel(String text) {
        if (labelCache.containsKey(text)) {
            Array<Label> cache = labelCache.get(text);
            for (int i = 0; i < cache.size; i++) {
                if (!cache.get(i).hasParent()) return cache.get(i);
            }
            cache.add(new Label(text, getSkin()));
            return cache.get(cache.size -1);
        }
        Array<Label> cache = new Array<>();
        cache.add(new Label(text, getSkin()));
        labelCache.put(text, cache);
        return cache.get(0);
    }

    private Table getTable() {
        for (int i = 0; i < tableCache.size; i++) {
            if (!tableCache.get(i).hasParent()) {
                Table table = tableCache.get(i);
                table.clearChildren(true);
                return table;
            }
        }
        Table table = new Table();
        tableCache.add(table);
        return table;
    }

    public void configureRulers () {
        xRulerTable.clearChildren();
        xRulerTable.setWidth(viewportWidget.getWidth());
        xRulerTable.setY(viewportWidget.getHeight() - RULER_SIZE);
        xRulerTable.setHeight(RULER_SIZE);
        int minSpaceBetweenActors = 40;
        int xSkipCount = 0;

        float xStart = 0;
        float previousX = viewportWidget.getLocalFromWorld(xStart, 0).x;

        for (float gap = 0; gap <= minSpaceBetweenActors; xSkipCount++){
            xStart += gridPropertyProvider.getUnitX();
            float x = viewportWidget.getLocalFromWorld(xStart, 0).x;
            gap += x - previousX;
            previousX = x;
        }

        xStart = 0;
        while (xStart <= gridPropertyProvider.getGridEndX()) {
            String coordText;
            int testInt = (int)xStart;
            float tmp = xStart - testInt;
            coordText = tmp > 0 ? "" + xStart : "" + testInt;
            Label coordinateLabel = getOrCreateLabel(coordText);
            float x = viewportWidget.getLocalFromWorld(xStart, 0).x - coordinateLabel.getWidth() / 2f;
            coordinateLabel.setX(x);
            xRulerTable.addActor(coordinateLabel);
            xStart += xSkipCount * gridPropertyProvider.getUnitX();
        }

        xStart = -(xSkipCount * gridPropertyProvider.getUnitX());
        while (xStart >= gridPropertyProvider.getGridStartX()) {
            String coordText;
            int testInt = (int)xStart;
            float tmp = xStart - testInt;
            coordText = tmp < 0 ? "" + xStart : "" + testInt;
            Label coordinateLabel = getOrCreateLabel(coordText);
            float x = viewportWidget.getLocalFromWorld(xStart, 0).x - coordinateLabel.getWidth() / 2f;
            coordinateLabel.setX(x);
            xRulerTable.addActor(coordinateLabel);
            xStart -= xSkipCount * gridPropertyProvider.getUnitX();;
        }

        int ySkipCount = 0;

        float yStart = 0;
        float previousY = viewportWidget.getLocalFromWorld(0, yStart).y;

        for (float gap = 0; gap <= minSpaceBetweenActors; ySkipCount++){
            yStart += gridPropertyProvider.getUnitY();
            float y = viewportWidget.getLocalFromWorld(0, yStart).y;
            gap += y - previousY;
            previousY = y;
        }

        yStart = 0;

        yRulerTable.setTransform(true);
        yRulerTable.clearChildren();
        yRulerTable.setHeight(viewportWidget.getHeight());

        while (yStart <= gridPropertyProvider.getGridEndY()) {
            String coordText;
            int testInt = (int)yStart;
            float tmp = yStart - testInt;
            coordText = tmp > 0 ? "" + yStart : "" + testInt;

            Table wrapperTable = getTable();
            wrapperTable.setTransform(true);
            wrapperTable.setRotation(90);
            Label coordinateLabel = getOrCreateLabel(coordText);
            float height = coordinateLabel.getHeight();
            float y = viewportWidget.getLocalFromWorld(0, yStart).y - height / 2f;
            wrapperTable.setY(y);
            coordinateLabel.setAlignment(Align.center);
            coordinateLabel.setOrigin(Align.center);

            float width = coordinateLabel.getWidth();
            wrapperTable.setSize(width, height);
            wrapperTable.setX((RULER_SIZE - width) / 2f);
            wrapperTable.setOrigin(width / 2f, height / 2f);


            wrapperTable.addActor(coordinateLabel);

            yRulerTable.addActor(wrapperTable);
            yStart += ySkipCount * gridPropertyProvider.getUnitY();
        }

        yStart = - ySkipCount * gridPropertyProvider.getUnitY();
        while (yStart >= gridPropertyProvider.getGridStartY()) {
            String coordText;
            int testInt = (int)yStart;
            float tmp = yStart - testInt;
            coordText = tmp < 0 ? "" + yStart : "" + testInt;

            Table wrapperTable = getTable();
            wrapperTable.setTransform(true);
            wrapperTable.setRotation(90);
            Label coordinateLabel = getOrCreateLabel(coordText);
            float height = coordinateLabel.getHeight();
            float y = viewportWidget.getLocalFromWorld(0, yStart).y - height / 2f;
            wrapperTable.setY(y);
            coordinateLabel.setAlignment(Align.center);
            coordinateLabel.setOrigin(Align.center);

            float width = coordinateLabel.getWidth();
            wrapperTable.setSize(width, height);
            wrapperTable.setX((RULER_SIZE - width) / 2f);
            wrapperTable.setOrigin(width / 2f, height / 2f);

            wrapperTable.addActor(coordinateLabel);

            yRulerTable.addActor(wrapperTable);
            yStart -= ySkipCount * gridPropertyProvider.getUnitY();
        }

        yRulerTable.setWidth(RULER_SIZE);
    }

}
