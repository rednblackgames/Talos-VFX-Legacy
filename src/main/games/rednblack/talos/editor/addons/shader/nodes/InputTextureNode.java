package games.rednblack.talos.editor.addons.shader.nodes;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.runtime.shaders.ShaderBuilder;
import games.rednblack.talos.editor.notifications.FileActorBinder;
import games.rednblack.talos.editor.notifications.Notifications;
import games.rednblack.talos.editor.notifications.events.NodeDataModifiedEvent;

/**
 * Shader graph node that samples the particle drawable's texture (u_texture).
 *
 * Unlike {@link SampleTextureNode}, which declares its own texture uniform,
 * this node samples the built-in u_texture that LibGDX's SpriteBatch binds
 * to texture unit 0. When the compiled shader is used in the particle graph's
 * ShaderModule, u_texture will be whatever TextureRegion is connected to
 * the ShaderModule's Drawable input.
 *
 * In the shader editor, a test texture can be dragged onto this node's preview
 * box. The test texture is bound to unit 0 (replacing the default white.png)
 * so the shader preview shows realistic results.
 */
public class InputTextureNode extends AbstractShaderNode {

    public final String INPUT_UV_OVERRIDE = "overrideUV";

    public final String OUTPUT_RGBA = "outputRGBA";
    public final String OUTPUT_R = "outputR";
    public final String OUTPUT_G = "outputG";
    public final String OUTPUT_B = "outputB";
    public final String OUTPUT_A = "outputA";

    private Texture testTexture;
    private String testTexturePath;

    @Override
    protected String getPreviewOutputName() {
        return OUTPUT_RGBA;
    }

    @Override
    protected void inputStateChanged(boolean isInputDynamic) {
        showShaderBox();
    }

    @Override
    protected boolean isInputDynamic() {
        return true;
    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        FileActorBinder.register(shaderBox, "png");

        shaderBox.addListener(new FileActorBinder.FileEventListener() {
            @Override
            public void onFileSet(FileHandle fileHandle) {
                try {
                    testTexture = new Texture(fileHandle);
                    testTexturePath = fileHandle.path();
                    shaderBox.setBaseTexture(testTexture);
                    updatePreview();
                    Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(InputTextureNode.this));
                } catch (Exception e) {
                    // ignore invalid files
                }
            }
        });
    }

    @Override
    protected void readProperties(JsonValue properties) {
        testTexturePath = properties.getString("testTexture", "");

        if (testTexturePath != null && !testTexturePath.isEmpty()) {
            FileHandle fileHandle = TalosMain.Instance().ProjectController().findFile(testTexturePath);
            if (fileHandle != null) {
                FileActorBinder.FileEvent fileEvent = TalosMain.POOLS.obtain(FileActorBinder.FileEvent.class);
                fileEvent.setFileHandle(fileHandle);
                shaderBox.fire(fileEvent);
            }
        }
    }

    @Override
    protected void writeProperties(Json json) {
        json.writeValue("testTexture", testTexturePath);
    }

    @Override
    public void prepareDeclarations(ShaderBuilder shaderBuilder) {
        // u_texture is already declared in the default template — nothing to add
    }

    @Override
    public String writeOutputCode(String slotId) {
        String uvSample = getExpression(INPUT_UV_OVERRIDE, "v_texCoords");
        String sample = "texture2D(u_texture, " + uvSample + ")";

        if (slotId.equals(OUTPUT_RGBA)) {
            return sample;
        } else if (slotId.equals(OUTPUT_R)) {
            return sample + ".r";
        } else if (slotId.equals(OUTPUT_G)) {
            return sample + ".g";
        } else if (slotId.equals(OUTPUT_B)) {
            return sample + ".b";
        } else if (slotId.equals(OUTPUT_A)) {
            return sample + ".a";
        }

        return sample;
    }

    @Override
    protected String getPreviewLine(String expression) {
        ShaderBuilder.Type outputType = getVarType(getPreviewOutputName());
        expression = castTypes(expression, outputType, ShaderBuilder.Type.VEC4, CAST_STRATEGY_REPEAT);
        return "return " + expression;
    }
}
