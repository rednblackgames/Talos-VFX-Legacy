package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class SkeletonAttachmentLoader implements AttachmentLoader {
    public TextureAtlas atlas;

    public SkeletonAttachmentLoader () {
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    private void loadSequence (String name, String basePath, Sequence sequence) {
        TextureRegion[] regions = sequence.getRegions();
        for (int i = 0, n = regions.length; i < n; i++) {
            String path = sequence.getPath(basePath, i);
            regions[i] = atlas.findRegion(path);
            if (regions[i] == null) throw new RuntimeException("Region not found in atlas: " + path + " (sequence: " + name + ")");
        }
    }

    public RegionAttachment newRegionAttachment (Skin skin, String name, String path, @Null Sequence sequence) {
        if(atlas == null) return null;
        RegionAttachment attachment = new RegionAttachment(name);
        if (sequence != null)
            loadSequence(name, path, sequence);
        else {
            TextureAtlas.AtlasRegion region = atlas.findRegion(path);
            if (region == null) throw new RuntimeException("Region not found in atlas: " + path + " (region attachment: " + name + ")");
            attachment.setRegion(region);
        }
        return attachment;
    }

    public MeshAttachment newMeshAttachment (Skin skin, String name, String path, @Null Sequence sequence) {
        if(atlas == null) return null;
        MeshAttachment attachment = new MeshAttachment(name);
        if (sequence != null)
            loadSequence(name, path, sequence);
        else {
            TextureAtlas.AtlasRegion region = atlas.findRegion(path);
            if (region == null) throw new RuntimeException("Region not found in atlas: " + path + " (mesh attachment: " + name + ")");
            attachment.setRegion(region);
        }

        return attachment;
    }

    public BoundingBoxAttachment newBoundingBoxAttachment (Skin skin, String name) {
        return new BoundingBoxAttachment(name);
    }

    public ClippingAttachment newClippingAttachment (Skin skin, String name) {
        return new ClippingAttachment(name);
    }

    public PathAttachment newPathAttachment (Skin skin, String name) {
        return new PathAttachment(name);
    }

    public PointAttachment newPointAttachment (Skin skin, String name) {
        return new PointAttachment(name);
    }
}
