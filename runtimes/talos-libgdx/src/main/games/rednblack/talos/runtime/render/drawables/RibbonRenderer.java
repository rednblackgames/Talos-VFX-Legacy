package games.rednblack.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import games.rednblack.talos.runtime.Particle;
import games.rednblack.talos.runtime.ParticleDrawable;

public class RibbonRenderer extends ParticleDrawable {

    Particle particleRef;
    /** Polyline of particleRef, resolved once per particle instead of on every setPointData call. */
    Polyline currentPolyline;
    int interpolationPointCount;

    PointMemoryAccumulator accumulator;

    TextureRegionDrawable textureRegionDrawable;
    TextureRegion ribbonRegion;

    public Pool<Polyline> polylinePool = new Pool<Polyline>() {
        @Override
        protected Polyline newObject() {
            return new Polyline();
        }
    };
    ObjectMap<Particle, Polyline> polylineMap = new ObjectMap<>();

    private Color tmpColor = new Color();

    public RibbonRenderer() {

        textureRegionDrawable = new TextureRegionDrawable();
        accumulator = new PointMemoryAccumulator();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
        if(interpolationPointCount < 1) return;
        if(ribbonRegion == null && material == null) return;

        PointMemoryAccumulator.AccData data = accumulator.update(particleRef, x, y);

        Polyline polyline = polyline();

        accumulator.setDrawLocations(data, polyline.getPoints());

        ShaderProgram prevShader = null;
        TextureRegion drawRegion = ribbonRegion;

        if (material != null && material.isValid()) {
            float time = particleRef.alpha * particleRef.life;
            prevShader = material.bind(batch, time);
            if (material.getMainRegion() != null) {
                drawRegion = material.getMainRegion();
            }
        }

        polyline.draw(batch, drawRegion, material != null && material.isValid() ? material.getShaderProgram() : null);

        if (material != null && prevShader != null) {
            material.unbind(batch, prevShader);
        }
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
        float rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        draw(batch, x, y, width, height, rotation, particle.pivot.x, particle.pivot.y);
        textureRegionDrawable.draw(batch, particle, color);
    }

    private Polyline polyline() {
        if(currentPolyline == null) {
            currentPolyline = polylinePool.obtain();
            currentPolyline.initPoints(interpolationPointCount, particleRef.getX(), particleRef.getY());
            polylineMap.put(particleRef, currentPolyline);
        }

        return currentPolyline;
    }

    public TextureRegionDrawable getHeadDrawable() {
        return textureRegionDrawable;
    }

    @Override
    public float getAspectRatio() {
        return textureRegionDrawable.getAspectRatio();
    }

    @Override
    public void setCurrentParticle (Particle particle) {
        if(particle == particleRef) return;

        this.particleRef = particle;
        this.currentPolyline = polylineMap.get(particle);
    }

    @Override
    public TextureRegion getTextureRegion() {
        return ribbonRegion;
    }

    public void setRegions(TextureRegion mainRegion, TextureRegion ribbonRegion) {
        textureRegionDrawable.setRegion((Sprite) mainRegion);

        this.ribbonRegion = ribbonRegion;
    }

    public void setPointData(int pointIndex,float thickness, Color color) {
        Polyline polyline = polyline();
        polyline.setPointData(pointIndex, 0, 0, thickness, color);
    }

    public void adjustPointData() {
        float pointAlpha = accumulator.getPointAlpha(particleRef);
        Polyline polyline = polyline();
        for(int i = 1; i < polyline.points.size; i++) {
            float topThickness = polyline.points.get(i).thickness;
            float bottomThickness = polyline.points.get(i-1).thickness;
            Color topColor = polyline.points.get(i).color;
            Color bottomColor = polyline.points.get(i).color;

            tmpColor.set(topColor.r+(bottomColor.r-topColor.r)*pointAlpha,
                         topColor.g+(bottomColor.g-topColor.g)*pointAlpha,
                         topColor.b+(bottomColor.b-topColor.b)*pointAlpha,
                         topColor.a+(bottomColor.a-topColor.a)*pointAlpha);

            //polyline.setPointData(i, 0, 0, topThickness+(bottomThickness-topThickness)*pointAlpha, tmpColor);
        }
    }

    public void setConfig(int detail, float memoryDuration) {
        if(detail < 2) detail = 2;
        interpolationPointCount = detail - 2;
        accumulator.init(detail, memoryDuration);
        // reset all existing items from the pool
        polylinePool.freeAll(polylineMap.values().toArray());
        polylineMap.clear();
        currentPolyline = null;
    }

    @Override
    public void notifyCreate(Particle particle) {

    }

    @Override
    public void notifyDispose(Particle particle) {
       accumulator.clean(particle);
       Polyline polyline = polylineMap.get(particle);
       if(polyline != null) {
           polylineMap.remove(particle);
           polylinePool.free(polyline);
       }
       if(particle == particleRef) {
           currentPolyline = null;
       }
    }

    /**
     * @deprecated Use {@link #setMaterial(games.rednblack.talos.runtime.render.ParticleMaterial)} instead.
     */
    @Deprecated
    public void setShadedDrawable(ShadedDrawable drawable) {
        if (drawable != null && drawable.getMaterial() != null) {
            setMaterial(drawable.getMaterial());
        }
    }

    public class PointMemoryAccumulator {

        int pointCount;
        float memoryDuration;

        private Pool<AccData> dataPool;
        private ObjectMap<Particle, AccData> dataMap = new ObjectMap<>();

        Vector2 tmpVec = new Vector2();

        class AccData implements Pool.Poolable {
            Vector2 leadPoint = new Vector2();
            float leadLife = 0;
            Array<Vector2> points = new Array<>();
            int pointCount = 0;

            public AccData(int pointMaxCount) {
                for(int i = 0; i < pointMaxCount - 1; i++) {
                    points.add(new Vector2());
                }
            }

            @Override
            public void reset() {
                leadPoint.set(0, 0);
                leadLife = 0;
                for(int i = 0; i < pointCount - 1; i++) {
                    points.get(i).set(0, 0);
                }
                pointCount = 0;
            }
        }

        public PointMemoryAccumulator() {

        }

        public void init(final int pointCount, float memoryDuration) {
            this.memoryDuration = memoryDuration;
            this.pointCount = pointCount;
            if(dataPool != null) {
                dataPool.clear();
            }

            dataPool = new Pool<AccData>() {
                @Override
                protected AccData newObject() {
                    return new AccData(pointCount);
                }
            };

            dataMap.clear();
        }

        public void clean(Particle particle) {
            AccData accData = dataMap.get(particle);
            if(accData != null) {
                dataPool.free(accData);
                dataMap.remove(particle);
            }
        }

        private AccData obtainData(Particle particle) {
            AccData accData = dataMap.get(particle);
            if(accData == null) {
                accData = dataPool.obtain();
                dataMap.put(particle, accData);
            }

            return accData;
        }

        public AccData update(Particle particle, float x, float y) {
            AccData data = obtainData(particle);

            float delta = Gdx.graphics.getDeltaTime();

            if(delta > 1f/60f) delta = 1f/60f;

            data.leadPoint.set(x, y);

            float interval = getInterval();
            if(interval <= 0) {
                data.leadLife = 0;
                return data;
            }

            data.leadLife = data.leadLife + delta;

            // a single frame can be longer than the sampling interval, so drain every matured one of them,
            // otherwise leadLife grows unbounded and the drawn points get extrapolated instead of interpolated
            int iterations = 0;
            while(data.leadLife > interval && iterations < pointCount) { // adding new point data
                Array<Vector2> points = data.points;
                int currPointCount = data.pointCount;

                if(currPointCount < pointCount - 1) {
                    currPointCount++;
                }
                data.pointCount = currPointCount;

                // now shift
                for(int i  = currPointCount - 1; i > 0; i--) {
                    points.get(i).set(points.get(i-1));
                }
                points.get(0).set(data.leadPoint); // set the value of lead point

                data.leadLife = data.leadLife - interval;
                iterations++;
            }

            if(data.leadLife > interval) data.leadLife = interval;

            return data;
        }

        private float getInterval() {
            if(pointCount <= 0) return 0;
            return memoryDuration/pointCount;
        }

        public void setDrawLocations(AccData data, Array<Polyline.PointData> points) {
            if(points != null && points.size == pointCount) {
                points.get(0).position.set(data.leadPoint);

                if(data.pointCount == 0) {
                    for(int i = 0; i < points.size; i++) {
                        points.get(i).color.a = 0;
                        points.get(i).position.set(data.leadPoint);
                    }

                    return;
                }

                float progress = getProgress(data);

                for(int i = 0; i < points.size-1; i++) {

                    if(i < data.pointCount) {
                        Vector2 top = data.points.get(i);
                        Vector2 bottom = i > 0 ? data.points.get(i - 1) : data.leadPoint;

                        tmpVec.set(bottom).sub(top).scl(progress).add(top);
                        points.get(i + 1).position.set(tmpVec);
                    } else {
                        points.get(i + 1).position.set(data.points.get(data.pointCount - 1));
                    }
                }
            }
        }

        public float getPointAlpha(Particle particle) {
            return getProgress(dataMap.get(particle));
        }

        /** How far the trail has travelled inside the current sampling interval, always within [0, 1]. */
        private float getProgress(AccData data) {
            float interval = getInterval();
            if(data == null || interval <= 0) return 0;

            float progress = data.leadLife/interval;
            if(progress < 0) return 0;
            if(progress > 1f) return 1f;

            return progress;
        }
    }
}
