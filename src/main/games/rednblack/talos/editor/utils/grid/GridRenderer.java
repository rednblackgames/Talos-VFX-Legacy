package games.rednblack.talos.editor.utils.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import games.rednblack.talos.editor.widgets.ui.ViewportWidget;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GridRenderer {

	public GridPropertyProvider gridPropertyProvider;

	public ViewportWidget widget;

	public GridRenderer(GridPropertyProvider propertyProvider, ViewportWidget widget) {
		this.gridPropertyProvider = propertyProvider;
		this.widget = widget;
	}

	public void setGridPropertyProvider (GridPropertyProvider gridPropertyProvider) {
		this.gridPropertyProvider = gridPropertyProvider;
	}

	public void drawGrid (Batch batch, ShapeDrawer shapeRenderer) {
		Color backgroundColor = gridPropertyProvider.getBackgroundColor();
		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f);

		for (GridLine gridLine : gridPropertyProvider.getGridLines()) {
			shapeRenderer.setColor(gridLine.color);
			Vector2 startCoordinate = gridLine.startCoordinate;
			Vector2 endCoordinate = gridLine.endCoordinate;
			drawLine(shapeRenderer, startCoordinate.x, startCoordinate.y, endCoordinate.x, endCoordinate.y, gridLine.thickness);
		}


		float gridSizeX = gridPropertyProvider.getUnitX();
		float gridSizeY = gridPropertyProvider.getUnitY();


		if (gridPropertyProvider.shouldHighlightCursorHover()) {
			int projX = getMouseCellX();
			int projY = getMouseCellY();

			shapeRenderer.filledRectangle(projX, projY, gridSizeX, gridSizeY);
		}

		if (gridPropertyProvider.shouldHighlightCursorSelect()) {
			if (Gdx.input.isTouched()) {
				int projX = getMouseCellX();
				int projY = getMouseCellY();

				shapeRenderer.filledRectangle(projX, projY, gridSizeX, gridSizeY);
			}
		}
	}

	private void drawLine (ShapeDrawer shapeRenderer, float x1, float y1, float x2, float y2, float thickness) {
		shapeRenderer.line(x1, y1, x2, y2, thickness);
	}

	public int getMouseCellX () {
		float gridSizeX = gridPropertyProvider.getUnitX();

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector3 projected = widget.getTouchToWorld(x, y);

		//Find the 'cell'
		float projX = projected.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);
		projX *= gridSizeX;

		return (int)projX;
	}



	public int getMouseCellY () {
		float gridSizeY = gridPropertyProvider.getUnitY();

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector3 projected = widget.getTouchToWorld(x, y);

		float projY = projected.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);
		projY *= gridSizeY;

		return (int)projY;
	}


	public Vector2 project (Vector2 position) {
		float gridSizeX = gridPropertyProvider.getUnitX();
		float gridSizeY = gridPropertyProvider.getUnitY();

		final Vector3 projected = widget.getTouchToWorld(position.x, position.y);

		// find cell x axis
		float projX = projected.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);

		// find cell y axis
		float projY = projected.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);

		position.x = (int) projX;
		position.y = (int) projY;

		return position;
	}

	public void toLocalCell (Vector3 vec) {
		float gridSizeX = gridPropertyProvider.getUnitX();
		float gridSizeY = gridPropertyProvider.getUnitY();

		//Find the 'cell'
		float projX = vec.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);
		projX *= gridSizeX;

		float projY = vec.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);
		projY *= gridSizeY;
		vec.set(projX, projY, vec.z);
	}
}
