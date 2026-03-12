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

package games.rednblack.talos.editor.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.editor.utils.MsdfFonts;
import games.rednblack.talos.editor.utils.SharedShapeDrawer;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class RotatorWidget extends Actor {

    private Skin skin;

    private float value;

    private ChangeListener listener;

    private boolean normalize = false;

    private TextraLabel label;

    private boolean isActive = false;

    private static final Color BG_COLOR = new Color(0.15f, 0.15f, 0.15f, 1f);
    private static final Color RING_COLOR = new Color(0.35f, 0.35f, 0.35f, 1f);
    private static final Color RING_ACTIVE_COLOR = new Color(0.5f, 0.6f, 0.9f, 1f);
    private static final Color NEEDLE_COLOR = new Color(0.7f, 0.7f, 0.7f, 1f);
    private static final Color NEEDLE_ACTIVE_COLOR = new Color(0.6f, 0.7f, 1f, 1f);
    private static final Color TICK_COLOR = new Color(0.3f, 0.3f, 0.3f, 1f);

    CharArray stringBuilder;

    public RotatorWidget(Skin skin) {
        this.skin = skin;

        label = MsdfFonts.smallLabel("");

        stringBuilder = new CharArray();

        addListener(new ClickListener() {

            Vector2 tmp = new Vector2();
            float prevAngle;

            public void applyAngle(float x, float y) {
                float newAngle = tmp.set(x - getWidth() / 2f, y - getHeight() / 2f).angle();
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    float step = 15f;
                    float offset = newAngle % step;
                    if(offset < step/2f) {
                        newAngle -= offset;
                    } else {
                        newAngle += (step - offset);
                    }

                    if(prevAngle == 360 && newAngle == 0) newAngle = 360;
                    if(prevAngle == 0 && newAngle == 360) newAngle = 0;
                }

                if(prevAngle >= 0 && prevAngle <= 90 && newAngle > 270 && newAngle <= 360) {
                    newAngle = 0;
                }
                if(prevAngle <= 360 && prevAngle >= 270 && newAngle > 0 && newAngle <= 90) {
                    newAngle = 360;
                }

                value = (int)newAngle;

                if(listener != null) {
                    listener.changed(new ChangeListener.ChangeEvent(), RotatorWidget.this);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                prevAngle = value;
                applyAngle(x, y);
                isActive = true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                prevAngle = value;
                applyAngle(x, y);
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                isActive = false;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer shapeDrawer = SharedShapeDrawer.getInstance().getShapeDrawer(batch);
        Color bc = batch.getColor();

        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - 2f;

        // Background circle
        shapeDrawer.setColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a * parentAlpha);
        shapeDrawer.filledCircle(cx, cy, radius);

        // Tick marks at 0, 90, 180, 270
        shapeDrawer.setColor(TICK_COLOR.r, TICK_COLOR.g, TICK_COLOR.b, TICK_COLOR.a * parentAlpha);
        float tickInner = radius * 0.7f;
        float tickOuter = radius * 0.9f;
        for (int deg = 0; deg < 360; deg += 90) {
            float rad = deg * MathUtils.degreesToRadians;
            float cos = MathUtils.cos(rad);
            float sin = MathUtils.sin(rad);
            shapeDrawer.line(
                    cx + cos * tickInner, cy + sin * tickInner,
                    cx + cos * tickOuter, cy + sin * tickOuter,
                    1f
            );
        }

        // Outer ring
        Color ringColor = isActive ? RING_ACTIVE_COLOR : RING_COLOR;
        shapeDrawer.setColor(ringColor.r, ringColor.g, ringColor.b, ringColor.a * parentAlpha);
        shapeDrawer.polygon(cx, cy, 50, radius, radius, 0, 2f, JoinType.SMOOTH);

        // Needle
        float needleRad = value * MathUtils.degreesToRadians;
        float needleCos = MathUtils.cos(needleRad);
        float needleSin = MathUtils.sin(needleRad);
        Color needleColor = isActive ? NEEDLE_ACTIVE_COLOR : NEEDLE_COLOR;
        shapeDrawer.setColor(needleColor.r, needleColor.g, needleColor.b, needleColor.a * parentAlpha);
        shapeDrawer.line(cx, cy, cx + needleCos * (radius * 0.85f), cy + needleSin * (radius * 0.85f), 2f);

        // Needle tip dot
        shapeDrawer.filledCircle(cx + needleCos * (radius * 0.85f), cy + needleSin * (radius * 0.85f), 3f);

        // Center dot
        shapeDrawer.setColor(ringColor.r, ringColor.g, ringColor.b, ringColor.a * parentAlpha);
        shapeDrawer.filledCircle(cx, cy, 3f);

        shapeDrawer.setColor(bc);

        // Label above
        int intVal = (int) value;
        stringBuilder.clear();
        stringBuilder.append(intVal);

        label.setText(stringBuilder.toString());
        label.setPosition(cx - label.getPrefWidth() / 2f, cy + 20 - label.getPrefHeight() / 2f);
        label.draw(batch, parentAlpha);
    }

    public Skin getSkin() {
        return skin;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getValue() {
        if(normalize) {
            return value/360f;
        } else {
            return value;
        }
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }
}
