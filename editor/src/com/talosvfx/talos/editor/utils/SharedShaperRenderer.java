package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ObjectMap;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SharedShaperRenderer {
    private static SharedShaperRenderer instance = null;

    public static SharedShaperRenderer getInstance() {
        if (instance == null) instance = new SharedShaperRenderer();
        return instance;
    }

    private final ObjectMap<Batch, ShapeDrawer> shapeDrawers = new ObjectMap<>();

    private SharedShaperRenderer() {
    }

    public ShapeDrawer getShapeDrawer(Batch batch) {
        if (shapeDrawers.get(batch) == null) {
            TextureRegion region = new TextureRegion(new Texture(Gdx.files.internal("white.png"))); //TODO: not cool
            shapeDrawers.put(batch, new ShapeDrawer(batch, region));
        }
        return shapeDrawers.get(batch);
    }

    public void dispose() {

    }
}
