package games.rednblack.talos.editor.addons.bvb;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class SkeletonAttachmentLoader extends AtlasAttachmentLoader {

    protected TextureAtlas atlasRef;

    public SkeletonAttachmentLoader(TextureAtlas atlas) {
        super(atlas);
        atlasRef = atlas;
    }


    private String stripPath (String path) {
        String[] split = path.split("/");
        if (split.length > 1) {
            return split[split.length - 1];
        }

        return path;
    }

    @Override
    public RegionAttachment newRegionAttachment (Skin skin, String name, String path, @Null Sequence sequence) {
        TextureAtlas.AtlasRegion region = atlasRef.findRegion(path);
        if(region == null) {
            return super.newRegionAttachment(skin, name, stripPath(path), sequence);
        } else {
            return super.newRegionAttachment(skin, name, path, sequence);
        }
    }

    @Override
    public MeshAttachment newMeshAttachment (Skin skin, String name, String path, @Null Sequence sequence) {
        TextureAtlas.AtlasRegion region = atlasRef.findRegion(path);
        if(region == null) {
            return super.newMeshAttachment(skin, name, stripPath(path), sequence);
        } else {
            return super.newMeshAttachment(skin, name, path, sequence);
        }
    }

    @Override
    public BoundingBoxAttachment newBoundingBoxAttachment (Skin skin, String name) {
        return super.newBoundingBoxAttachment(skin, name);
    }

    @Override
    public ClippingAttachment newClippingAttachment (Skin skin, String name) {
        return super.newClippingAttachment(skin, name);
    }

    @Override
    public PathAttachment newPathAttachment (Skin skin, String name) {
        return super.newPathAttachment(skin, name);
    }

    @Override
    public PointAttachment newPointAttachment (Skin skin, String name) {
        return super.newPointAttachment(skin, name);
    }
}
