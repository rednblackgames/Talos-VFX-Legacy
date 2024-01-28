package games.rednblack.talos.editor.utils.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class GridLine implements Pool.Poolable {
    public Vector2 startCoordinate;
    public Vector2 endCoordinate;
    public final Color color = new Color();
    public float thickness;

    public GridLine () {

    }

    public GridLine (Vector2 startCoordinate, Vector2 endCoordinate, Color color, float thickness) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.color.set(color);
        this.thickness = thickness;
    }

    public GridLine set(Vector2 startCoordinate, Vector2 endCoordinate, Color color, float thickness) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.color.set(color);
        this.thickness = thickness;
        return this;
    }

    @Override
    public void reset() {
        Pools.free(startCoordinate);
        Pools.free(endCoordinate);
        color.set(0);
        thickness = 0;
    }
}
