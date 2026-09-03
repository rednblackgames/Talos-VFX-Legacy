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

package games.rednblack.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class Polyline implements Pool.Poolable {

    Vector2 leftTangent = new Vector2();
    Vector2 rightTanget = new Vector2();

    Vector2 leftPoint = new Vector2();
    Vector2 rightPoint = new Vector2();

    float rotation;

    Array<PointData> points = new Array<>();

    // inner workings
    private float[] vertices;
    private short[] indexes;

    private static final float EPSILON = 0.0001f;
    /** How wide the ribbon may get compared to how long it actually is. */
    private static final float MAX_THICKNESS_TO_LENGTH = 0.5f;

    private float thicknessCap = Float.MAX_VALUE;
    private float[] arcLengths;
    private float[] thicknesses;
    private float[] directionsX;
    private float[] directionsY;
    private float[] packedColors;
    private final Color profileColor = new Color();

    private final Vector2 tmp = new Vector2();
    private final Vector2 tmp2 = new Vector2();
    private final Vector2 tmp3 = new Vector2();
    private final Vector2 point0 = new Vector2();
    private final Vector2 point1 = new Vector2();
    private final Vector2 point2 = new Vector2();
    private final Vector2 point3 = new Vector2();

    Batch batch;

    @Override
    public void reset() {
        for(PointData pointData: points) {
            pointData.position.set(0, 0);
            pointData.offset.set(0, 0);
            pointData.thickness = 0;
        }
    }

    static class PointData {
        Vector2 position = new Vector2();
        Vector2 offset = new Vector2();
        float thickness;
        Color color = new Color();
    }

    public Polyline() {

    }

    public void set(float size, float rotation) {
        leftPoint.set(-size/2f, 0);
        rightPoint.set(size/2f, 0);
        this.rotation = rotation;
    }

    public void initPoints(int interpolationPoints, float x, float y) {
        if(points.size != interpolationPoints + 2) {
            points.clear();
            for(int i = 0; i < interpolationPoints + 2; i++) {
                points.add(new PointData());
            }
            initVertices();
        } // else we reuse them

        for(int i = 0; i < points.size; i++) {
            points.get(i).position.set(x, y);
        }

    }

    private void initVertices() {
        int attributeCount = 5;
        int pointCount = points.size;
        int vertexCount = (pointCount - 1) * 4;
        int trisCount = (pointCount - 1) * 2;

        if(vertices == null || vertices.length != vertexCount * attributeCount) {
            vertices = new float[vertexCount * attributeCount];
            indexes = new short[trisCount * 3];
        }

        if(arcLengths == null || arcLengths.length != pointCount) {
            arcLengths = new float[pointCount];
            thicknesses = new float[pointCount];
            directionsX = new float[pointCount];
            directionsY = new float[pointCount];
            packedColors = new float[pointCount];
        }
    }

    public void setPointData(int index, float offsetX, float offsetY, float thickness, Color color) {
        points.get(index).color.set(color);
        points.get(index).offset.set(offsetX, offsetY);
        points.get(index).thickness = thickness;
    }

    public Array<PointData> getPoints() {
        return points;
    }

    public void draw(Batch batch, TextureRegion region, ShaderProgram shaderProgram) {
        if(region == null) return;
        if(batch instanceof PolygonBatch) {
            PolygonBatch polygonSpriteBatch = (PolygonBatch) batch;
            this.batch = polygonSpriteBatch;

            updateProfile();

            for(int i = 0; i < points.size - 1; i++) {
                // extrude each point
                extrudePoint(region, i, 0);
                extrudePoint(region, i, 1);

                // creating indexes
                indexes[i * 6] =     (short) (i * 4);
                indexes[i * 6 + 1] = (short) (i * 4 + 1);
                indexes[i * 6 + 2] = (short) (i * 4 + 3);
                indexes[i * 6 + 3] = (short) (i * 4);
                indexes[i * 6 + 4] = (short) (i * 4 + 3);
                indexes[i * 6 + 5] = (short) (i * 4 + 2);

            }

            // do the actual drawing
            if(shaderProgram != null) {
                batch.setShader(shaderProgram);
            }
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);
        }
    }

    public void draw(Batch batch, TextureRegion region, float x, float y, ShaderProgram shaderProgram) {
        if(region == null) return;

        if(batch instanceof PolygonBatch) {
            PolygonBatch polygonSpriteBatch = (PolygonBatch) batch;
            this.batch = polygonSpriteBatch;

            updatePointPositions(x, y);
            updateProfile();

            for(int i = 0; i < points.size - 1; i++) {
                // extrude each point
                extrudePoint(region, i, 0);
                extrudePoint(region, i, 1);

                // creating indexes
                indexes[i * 6] =     (short) (i * 4);
                indexes[i * 6 + 1] = (short) (i * 4 + 1);
                indexes[i * 6 + 2] = (short) (i * 4 + 3);
                indexes[i * 6 + 3] = (short) (i * 4);
                indexes[i * 6 + 4] = (short) (i * 4 + 3);
                indexes[i * 6 + 5] = (short) (i * 4 + 2);

            }

            // do the actual drawing
            if(shaderProgram != null) {
                batch.setShader(shaderProgram);
            }
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);
        }
    }

    private void updatePointPositions(float x, float y) {
        for(int i = 0; i < points.size; i++) {
            float alpha = (float)i/(points.size-1);
            point0.set(leftPoint);
            point3.set(rightPoint);
            point1.set(leftPoint).add( leftTangent );
            point2.set(rightPoint).add(rightTanget );
            Bezier.cubic(points.get(i).position, alpha, point0, point1, point2, point3, tmp);

            // ad the offsets
            points.get(i).position.add(points.get(i).offset);

            // apply rotation while origin is at 0
            points.get(i).position.rotateDeg(rotation);

            //apply origin position
            points.get(i).position.add(x, y);
        }
    }

    /**
     * Thickness and color arrive indexed by point, but the points do not stay evenly spread: a trail that is
     * being pulled back into its anchor piles its first indices onto a single spot while the rest still covers
     * the old path. Reading the profile by index then hands a several units long quad the thickness of a point
     * that should sit at the very end of the ribbon, which draws as a thin needle out of the anchor. Measuring
     * the arc the polyline really covers lets the profile be sampled by distance instead.
     */
    private void updateProfile() {
        arcLengths[0] = 0;
        for(int i = 1; i < points.size; i++) {
            arcLengths[i] = arcLengths[i - 1] + points.get(i - 1).position.dst(points.get(i).position);
        }

        thicknessCap = arcLengths[points.size - 1] * MAX_THICKNESS_TO_LENGTH;

        for(int i = 0; i < points.size; i++) {
            float position = profilePosition(i);
            thicknesses[i] = thicknessAt(position);
            // The tint is constant over the whole draw, so each point is packed once instead of once per vertex.
            packedColors[i] = colorAt(position).mul(batch.getColor()).toFloatBits();
        }

        // Every point is shared by two quads, so its direction is worked out once here rather than twice
        // while extruding.
        for(int i = 0; i < points.size; i++) {
            directionAt(i, thicknesses[i], tmp3);
            directionsX[i] = tmp3.x;
            directionsY[i] = tmp3.y;
        }
    }

    /** Where the given point sits along the ribbon, as a fraction of the length actually covered. */
    private float profilePosition(int index) {
        float length = arcLengths[points.size - 1];
        if(length < EPSILON) return (float)index/(points.size - 1); // fully collapsed, nothing is drawn anyway

        return arcLengths[index]/length * (points.size - 1);
    }

    private float thicknessAt(float position) {
        int low = (int)position;
        int high = low + 1 < points.size ? low + 1 : low;
        float alpha = position - low;

        float thickness = points.get(low).thickness + (points.get(high).thickness - points.get(low).thickness) * alpha;

        return Math.min(thickness, thicknessCap);
    }

    private Color colorAt(float position) {
        int low = (int)position;
        int high = low + 1 < points.size ? low + 1 : low;
        float alpha = position - low;

        return profileColor.set(points.get(low).color).lerp(points.get(high).color, alpha);
    }

    private void extrudePoint(TextureRegion region, int index, int pos) {

        int i = index + pos;
        float v = (float)(i)/(points.size-1);

        float thickness = thicknesses[i];

        Vector2 position = points.get(i).position;

        // A zero length segment has to stay zero area. Extruding its two ends along even slightly different
        // directions turns it into a bow tie as wide as the ribbon, which is what piles up around the anchor
        // once the trail stops moving and every point collapses onto the same spot.
        int directionIndex = position.dst2(points.get(index).position) < EPSILON * EPSILON ? index : i;

        tmp3.set(directionsX[directionIndex], directionsY[directionIndex]);
        tmp3.rotate90(1).scl(thickness/2f); //Left hand side normal, half thickness

        tmp.set(position).add(tmp3);
        tmp2.set(position).sub(tmp3);

        if(i == points.size - 1) {
            packVertex(region, vertices, index * 4 + 1 + pos * 2, tmp.x, tmp.y, packedColors[i - 1], 0, v); // left extension vertex
            packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y, packedColors[i], 1, v); // right extension vertex
        } else {
            packVertex(region, vertices, index * 4 + 1 + pos * 2, tmp.x, tmp.y, packedColors[i], 0, v); // left extension vertex
            packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y, packedColors[i + 1], 1, v); // right extension vertex
        }
    }

    /**
     * Direction of the polyline at the given point, measured over a baseline of at least minLength.
     * Widening the neighbour window keeps the extrusion normal stable when consecutive points sit much
     * closer to each other than the ribbon is thick, which is what makes slow moving trails fold over
     * themselves. Coincident points are skipped by construction.
     */
    private Vector2 directionAt(int index, float minLength, Vector2 out) {
        Vector2 position = points.get(index).position;
        float halfLength = minLength/2f;
        float halfLength2 = halfLength * halfLength; // squared, so walking the window costs no square roots

        int prev = index;
        while(prev > 0 && position.dst2(points.get(prev).position) < halfLength2) prev--;

        int next = index;
        while(next < points.size - 1 && position.dst2(points.get(next).position) < halfLength2) next++;

        out.set(points.get(next).position).sub(points.get(prev).position);

        if(out.len2() < EPSILON * EPSILON) {
            // whole window is coincident, fall back to the overall direction of the polyline
            out.set(points.get(points.size - 1).position).sub(points.get(0).position);
        }

        if(out.len2() < EPSILON * EPSILON) {
            return out.set(0, 0); // fully degenerate, collapse the quad instead of extruding a random direction
        }

        return out.nor();
    }


    private void packVertex(TextureRegion region, float[] vertices, int index, float x, float y, float packedColor, float u, float v) {
        float insideOffset = 0.0f; // needed in case region has have some weird transparent edge. maybe.

        vertices[index * 5] = x;
        vertices[index * 5 + 1] = y;
        vertices[index * 5 + 2] = packedColor;
        vertices[index * 5 + 3] = region.getU() + u * (region.getU2() - region.getU() - insideOffset) + insideOffset;
        vertices[index * 5 + 4] = region.getV() + v * (region.getV2() - region.getV() - insideOffset) + insideOffset;


    }

}
