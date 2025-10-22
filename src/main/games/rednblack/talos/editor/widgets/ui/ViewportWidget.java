/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package games.rednblack.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.utils.CameraController;
import games.rednblack.talos.editor.utils.CursorUtil;
import games.rednblack.talos.editor.utils.grid.GridPropertyProvider;
import games.rednblack.talos.editor.utils.grid.GridRenderer;
import games.rednblack.talos.editor.utils.grid.RulerRenderer;

public abstract class ViewportWidget extends Table {

	protected OrthographicCamera camera;

	protected Matrix4 emptyTransform = new Matrix4();
	private Matrix4 prevTransform = new Matrix4();
	private Matrix4 prevProjection = new Matrix4();

	public CameraController cameraController;

	protected float maxZoom = 0.01f;
	protected float minZoom = 200f;

	private float gridSize;
	private float worldWidth = 1f;

	private Vector3 tmp = new Vector3();
	private Vector2 vec2 = new Vector2();

	protected InputListener inputListener;
	protected boolean isInViewPort;
	protected boolean isDragging;
	private boolean inputListenersEnabled = true;

	protected boolean locked;

	protected GridPropertyProvider gridPropertyProvider;

	protected GridRenderer gridRenderer;
	protected RulerRenderer rulerRenderer;

	public ViewportWidget () {
		camera = new OrthographicCamera();
		camera.viewportWidth = 10;
		initializeGridPropertyProvider();
		gridRenderer = new GridRenderer(gridPropertyProvider, this);
		rulerRenderer = new RulerRenderer(gridPropertyProvider, this);

		setTouchable(Touchable.enabled);

		cameraController = new CameraController(camera);
		cameraController.setInvert(true);
		cameraController.setBoundsProvider(this);

		addPanListener();
	}

	protected void addPanListener () {
		addListener(new InputListener() {
			boolean canPan = false;

			@Override
			public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
				float currWidth = camera.viewportWidth * camera.zoom;
				float nextWidth = currWidth * (1f + amountY * 0.1f);
				float nextZoom = nextWidth / camera.viewportWidth;
				camera.zoom = nextZoom;

				camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
				camera.update();

				return true;
			}

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				canPan = canMoveAround();
				cameraController.touchDown((int)x, (int)y, pointer, button);
				return !event.isHandled();
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				isDragging = false;
				cameraController.touchUp((int)x, (int)y, pointer, button);
				canPan = false;
			}


			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				// can't move around disable dragging
				if (!canPan)
					return;

				isDragging = true;

				cameraController.touchDragged((int)x, (int)y, pointer);
			}

			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				isInViewPort = true;

				super.enter(event, x, y, pointer, fromActor);
				TalosMain.Instance().UIStage().getStage().setScrollFocus(ViewportWidget.this);
			}

			@Override
			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (pointer != -1)
					return; //Only care about exit/enter from mouse move
				TalosMain.Instance().UIStage().getStage().setScrollFocus(null);

				isInViewPort = false;
				super.exit(event, x, y, pointer, toActor);
			}
		});
	}

	Vector2 temp = new Vector2();

	private void enableClickListener () {
		if (inputListener == null)
			return;
		if (inputListenersEnabled)
			return;
		inputListenersEnabled = true;
		addListener(inputListener);
	}

	private void disableClickListener () {
		if (inputListener == null)
			return;
		if (!inputListenersEnabled)
			return;
		inputListenersEnabled = false;
		removeListener(inputListener);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		batch.end();

		localToScreenCoordinates(temp.set(0, 0));
		int x = (int)temp.x;
		int y = (int)temp.y;

		localToScreenCoordinates(temp.set(getWidth(), getHeight()));

		int x2 = (int)temp.x;
		int y2 = (int)temp.y;

		int ssWidth = x2 - x;
		int ssHeight = y - y2;

		HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float aspect = getWidth() / getHeight();

		camera.viewportHeight = camera.viewportWidth / aspect;

		camera.update();

		prevTransform.set(batch.getTransformMatrix());
		prevProjection.set(batch.getProjectionMatrix());

		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(emptyTransform);

		batch.begin();
		drawContent(batch, parentAlpha);

		HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

		batch.end();

		HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		batch.setProjectionMatrix(prevProjection);
		batch.setTransformMatrix(prevTransform);
		batch.begin();

//		Debug entity secltion
//		if (entityUnderMouse != null) {
//			batch.draw(entitySelectionBuffer.getFrameBuffer().getColorBufferTexture(), getX(), getY(), getWidth(), getHeight(), 0, 0, 1, 1);
//			System.out.println(entityUnderMouse.uuid.toString() + " " + this.getClass());
//		}

		rulerRenderer.configureRulers();

		super.draw(batch, parentAlpha);
	}

	private boolean rgbCompare (Color color, Color colourForEntityUUID) {
		int inR = (int)(color.r * 256);
		int inG = (int)(color.g * 256);
		int inB = (int)(color.b * 256);

		int testR = (int)(colourForEntityUUID.r * 256);
		int testG = (int)(colourForEntityUUID.g * 256);
		int testB = (int)(colourForEntityUUID.b * 256);

		if (inR != testR) return false;
		if (inG != testG) return false;
		if (inB != testB) return false;
		return true;
	}

	protected void drawGroup (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

	@Override
	public void act (float delta) {
		super.act(delta);

		if (isDragging) {
			CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.GRABBED);
			disableClickListener();
		} else {
			enableClickListener();
		}
	}

	// allow moving around if space bar is pressed and is in viewport or has dragged from viewport
	protected boolean canMoveAround() {
		return Gdx.input.isKeyPressed(Input.Keys.SPACE) && (isInViewPort || isDragging);
	}

	public abstract void drawContent (Batch batch, float parentAlpha);

	public OrthographicCamera getCamera () {
		return camera;
	}

	public float getCameraPosX () {
		return camera.position.x;
	}

	public float getCameraPosY () {
		return camera.position.y;
	}

	public float getCameraZoom () {
		return camera.zoom;
	}

	public void setCameraPos (float x, float y) {
		camera.position.set(x, y, 0);
	}

	public void setCameraZoom (float zoom) {
		camera.zoom = zoom;
	}

	public void setViewportWidth (float width) {
		camera.viewportWidth = width;
		camera.update();
	}

	protected void setWorldSize (float worldWidth) {
		this.worldWidth = worldWidth;
		updateNumbers();
	}

	private void updateNumbers () {
		camera.zoom = worldWidth / camera.viewportWidth;
		gridSize = worldWidth / 40f;
		float minWidth = gridSize * 4f;
		float maxWidth = worldWidth * 10f;
		minZoom = minWidth / camera.viewportWidth;
		maxZoom = maxWidth / camera.viewportWidth;
		camera.update();
	}

	protected void resetCamera () {
		camera.position.set(0, 0, 0);
		camera.zoom = worldWidth / camera.viewportWidth;
	}

	private void getViewportBounds (Rectangle out) {
		localToScreenCoordinates(temp.set(0, 0));
		int x = (int)temp.x;
		int y = (int)temp.y;

		localToScreenCoordinates(temp.set(getWidth(), getHeight()));

		int x2 = (int)temp.x;
		int y2 = (int)temp.y;

		int ssWidth = x2 - x;
		int ssHeight = y - y2;

		y = Gdx.graphics.getHeight() - y;

		out.set(x, y, ssWidth, ssHeight);
	}

	public Vector2 getLocalFromWorld (float x, float y) {
		getViewportBounds(Rectangle.tmp);
		camera.project(tmp.set(x, y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		tmp.y = Gdx.graphics.getHeight() - tmp.y;
		Vector2 v = TalosMain.POOLS.obtain(Vector2.class).set(tmp.x, tmp.y);
		Vector2 vector2 = screenToLocalCoordinates(v);
		vec2.set(vector2);
        TalosMain.POOLS.free(v);
		return vec2;
	}

	public Vector2 getWorldFromLocal (float x, float y) {
		Vector2 vector2 = localToScreenCoordinates(new Vector2(x, y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(tmp.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);

		vec2.set(tmp.x, tmp.y);

		return vec2;
	}

	protected Vector3 getWorldFromLocal (Vector3 vec) {
		Vector2 vector2 = localToScreenCoordinates(new Vector2(vec.x, vec.y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	public Vector3 getTouchToWorld (float x, float y) {
		Vector3 vec = new Vector3(x, y, 0);

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vec.x, vec.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	protected float pixelToWorld (float pixelSize) {
		tmp.set(0, 0, 0);
		camera.unproject(tmp);
		float baseline = tmp.x;

		tmp.set(pixelSize, 0, 0);
		camera.unproject(tmp);
		float pos = tmp.x;

		return Math.abs(pos - baseline) * (getStage().getWidth() / getWidth()); //TODO: I am sure there is a better way to do this
	}

	public float getWorldWidth () {
		return worldWidth;
	}

	public abstract void initializeGridPropertyProvider ();

	public static boolean isEnterPressed (int keycode) {
		switch (keycode) {
			case Input.Keys.ENTER:
			case Input.Keys.NUMPAD_ENTER:
				return true;
			default:
				return false;
		}
	}
}
