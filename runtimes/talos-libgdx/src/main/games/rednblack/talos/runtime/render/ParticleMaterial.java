package games.rednblack.talos.runtime.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.talos.runtime.utils.DefaultShaders;

/**
 * Encapsulates surface shading: shader program, texture bindings, and
 * dynamic uniform values fed from the node graph.
 *
 * Drawables delegate to this before issuing geometry draw calls.
 */
public class ParticleMaterial {

    private ShaderProgram shaderProgram;
    private TextureRegion mainRegion;
    private ObjectMap<String, TextureRegion> textureMap;
    private final ObjectMap<String, float[]> uniformValues = new ObjectMap<>();

    // ── Shader lifecycle ──────────────────────────────────────────────

    public void setShader(String fragCode) {
        if (fragCode == null) return;
        ShaderProgram.pedantic = false;
        shaderProgram = new ShaderProgram(
                DefaultShaders.DEFAULT_VERTEX_SHADER, fragCode);
        if (!shaderProgram.isCompiled()) {
            Gdx.app.log("ParticleMaterial", shaderProgram.getLog());
        }
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public boolean isValid() {
        return shaderProgram != null && shaderProgram.isCompiled();
    }

    // ── Texture bindings ──────────────────────────────────────────────

    public void setMainRegion(TextureRegion region) {
        this.mainRegion = region;
    }

    public TextureRegion getMainRegion() {
        return mainRegion;
    }

    public void setTextures(ObjectMap<String, TextureRegion> map) {
        this.textureMap = map;
    }

    public ObjectMap<String, TextureRegion> getTextures() {
        return textureMap;
    }

    // ── Dynamic uniforms (set per-particle from the node graph) ───────

    public void setUniform(String name, float value) {
        uniformValues.put(name, new float[]{value});
    }

    public void setUniform(String name, float x, float y) {
        uniformValues.put(name, new float[]{x, y});
    }

    public void setUniform(String name, float x, float y, float z) {
        uniformValues.put(name, new float[]{x, y, z});
    }

    public void setUniform(String name, float x, float y, float z, float w) {
        uniformValues.put(name, new float[]{x, y, z, w});
    }

    public void clearUniforms() {
        uniformValues.clear();
    }

    // ── Bind / Unbind ─────────────────────────────────────────────────

    /**
     * Prepares the batch for shaded drawing. Call this BEFORE geometry draw calls.
     * Returns the previous shader so the caller can restore it after drawing.
     *
     * @param batch the batch to configure
     * @param time  typically particle.alpha * particle.life
     * @return the previous ShaderProgram (may be null)
     */
    public ShaderProgram bind(Batch batch, float time) {
        if (!isValid()) return null;

        ShaderProgram prev = batch.getShader();
        batch.setShader(shaderProgram);

        // Built-in uniform
        shaderProgram.setUniformf("u_time", time);

        // Dynamic uniforms from node graph
        for (ObjectMap.Entry<String, float[]> entry : uniformValues) {
            float[] v = entry.value;
            switch (v.length) {
                case 1: shaderProgram.setUniformf(entry.key, v[0]); break;
                case 2: shaderProgram.setUniformf(entry.key, v[0], v[1]); break;
                case 3: shaderProgram.setUniformf(entry.key, v[0], v[1], v[2]); break;
                case 4: shaderProgram.setUniformf(entry.key, v[0], v[1], v[2], v[3]); break;
            }
        }

        // Bind extra textures (sampler2D uniforms)
        if (textureMap != null) {
            int bind = 1;
            for (ObjectMap.Entry<String, TextureRegion> e : textureMap) {
                if (e.value == null) continue;
                e.value.getTexture().bind(bind);
                shaderProgram.setUniformi(e.key, bind);
                shaderProgram.setUniformf(e.key + "regionUV",
                        e.value.getU(), e.value.getV(),
                        e.value.getU2(), e.value.getV2());
                bind++;
            }
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }

        return prev;
    }

    /**
     * Restores the previous shader after geometry was drawn.
     */
    public void unbind(Batch batch, ShaderProgram prev) {
        batch.setShader(prev);
    }
}
