package games.rednblack.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.talos.runtime.Particle;
import games.rednblack.talos.runtime.ParticleDrawable;
import games.rednblack.talos.runtime.render.ParticleMaterial;

/**
 * @deprecated Use any {@link ParticleDrawable} with a {@link ParticleMaterial} instead.
 * This class is kept for backward compatibility with existing .tls effect files.
 */
@Deprecated
public class ShadedDrawable extends ParticleDrawable {

    private Texture texture;
    private TextureRegion region;

    public ShadedDrawable() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texture = new Texture(pixmap);
        region = new TextureRegion(texture);
        pixmap.dispose();

        material = new ParticleMaterial();
        material.setMainRegion(region);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
        if (material == null || !material.isValid()) return;

        float rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        ShaderProgram prev = material.bind(batch, particle.alpha * particle.life);

        batch.setColor(color);
        batch.draw(texture, x - width * particle.pivot.x, y - height * particle.pivot.y, width * particle.pivot.x, height * particle.pivot.y, width, height, 1f, 1f, rotation, 0, 0, 1, 1, false, false);

        if (prev != null) {
            material.unbind(batch, prev);
        }
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setCurrentParticle(Particle particle) {

    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }

    public void setShader(String fragCode) {
        material.setShader(fragCode);
    }

    public void setTextures(ObjectMap<String, TextureRegion> textureMap) {
        material.setTextures(textureMap);
    }

    /**
     * @deprecated Use {@link ParticleMaterial#bind(Batch, float)} directly.
     */
    @Deprecated
    public ShaderProgram getShaderProgram(Batch batch, Color color, float alpha, float life) {
        if (!material.isValid()) return null;
        return material.bind(batch, alpha * life);
    }

    /**
     * @deprecated Access {@link #getMaterial()} and call
     * {@link ParticleMaterial#bind(Batch, float)} instead.
     */
    @Deprecated
    public ShaderProgram processShaderData(ShaderProgram shaderProgram, float time) {
        // Legacy callers: material.bind() now handles this
        return shaderProgram;
    }
}
