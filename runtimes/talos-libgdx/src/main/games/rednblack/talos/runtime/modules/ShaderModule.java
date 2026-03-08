package games.rednblack.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.talos.runtime.ParticleDrawable;
import games.rednblack.talos.runtime.ParticleEmitterDescriptor;
import games.rednblack.talos.runtime.assets.AssetProvider;
import games.rednblack.talos.runtime.render.ParticleMaterial;
import games.rednblack.talos.runtime.render.drawables.ShadedDrawable;
import games.rednblack.talos.runtime.utils.ShaderDescriptor;
import games.rednblack.talos.runtime.values.DrawableValue;
import games.rednblack.talos.runtime.values.NumericalValue;

/**
 * Node-graph module that wraps a {@link ParticleMaterial}.
 *
 * When a shader source is loaded, uniforms are parsed and
 * corresponding input slots are created dynamically.
 * At runtime, connected graph values flow into the material's
 * uniform map before each particle draw.
 *
 * Accepts an optional Drawable input: if connected, the material is
 * stamped onto the incoming drawable and that drawable is output.
 * If no drawable is connected, a default ShadedDrawable (1x1 white quad)
 * is used as the output carrier.
 */
public class ShaderModule extends AbstractModule {

    public static final int DRAWABLE = 1;
    public static final int OUTPUT = 0;

    /** Dynamic input slots start at this index to leave room for fixed slots. */
    private static final int DYNAMIC_SLOT_BASE = 10;

    private DrawableValue inputDrawable;
    private DrawableValue outputValue;
    private ParticleMaterial material;
    private ShadedDrawable defaultCarrier;
    public String shaderFileName;
    public ShaderDescriptor shaderDescriptor;

    /** Maps slot index -> uniform name */
    private final ObjectMap<Integer, String> slotToUniform = new ObjectMap<>();
    /** Maps slot index -> component count (1=float, 2=vec2, 3=vec3, 4=vec4) */
    private final ObjectMap<Integer, Integer> slotToComponents = new ObjectMap<>();
    /** Holds the NumericalValue for each dynamic input slot */
    private final ObjectMap<Integer, NumericalValue> dynamicInputs = new ObjectMap<>();

    @Override
    protected void defineSlots() {
        inputDrawable = (DrawableValue) createInputSlot(DRAWABLE, new DrawableValue());
        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());

        material = new ParticleMaterial();

        // Default carrier: a 1x1 white quad rendered with the shader.
        // Used when no drawable is connected to the input.
        defaultCarrier = new ShadedDrawable();
        defaultCarrier.setMaterial(material);
        outputValue.setDrawable(defaultCarrier);
    }

    @Override
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);
        if (shaderFileName != null && !shaderFileName.isEmpty()) {
            AssetProvider assets = graph.getEffectDescriptor().getAssetProvider();
            ShaderDescriptor desc = assets.findAsset(shaderFileName, ShaderDescriptor.class);
            loadShader(desc, shaderFileName);
        }
    }

    /**
     * Called when the user selects a shader file in the editor.
     * Parses uniforms and creates dynamic input ports.
     */
    public void loadShader(ShaderDescriptor descriptor, String fileName) {
        this.shaderFileName = fileName;
        this.shaderDescriptor = descriptor;
        if (descriptor == null) return;

        material.setShader(descriptor.getFragCode());

        // Clear previous dynamic slots
        for (Integer slotId : dynamicInputs.keys()) {
            inputSlots.remove(slotId);
        }
        slotToUniform.clear();
        slotToComponents.clear();
        dynamicInputs.clear();

        // Parse uniforms and create input ports
        int slotIndex = DYNAMIC_SLOT_BASE;
        ObjectMap<String, TextureRegion> texMap = new ObjectMap<>();

        for (ObjectMap.Entry<String, ShaderDescriptor.UniformData> entry
                : descriptor.getUniformMap()) {
            ShaderDescriptor.UniformData data = entry.value;

            if (data.type == ShaderDescriptor.Type.TEXTURE) {
                // Texture uniforms: resolve and bind directly
                if (graph != null) {
                    TextureRegion region = graph.getEffectDescriptor()
                            .getAssetProvider().findAsset(data.payload, TextureRegion.class);
                    texMap.put(entry.key, region);
                }
            } else {
                // Numeric uniforms: create a dynamic input slot
                int components = componentCount(data.type);
                NumericalValue inputVal = createInputSlot(slotIndex);
                slotToUniform.put(slotIndex, entry.key);
                slotToComponents.put(slotIndex, components);
                dynamicInputs.put(slotIndex, inputVal);
                slotIndex++;
            }
        }

        material.setTextures(texMap);
    }

    private int componentCount(ShaderDescriptor.Type type) {
        switch (type) {
            case VEC2: return 2;
            case VEC3: return 3;
            case VEC4: return 4;
            default:   return 1;
        }
    }

    /**
     * Called once per particle evaluation.
     * Reads connected graph values and writes them into the material's uniform map.
     * If a drawable input is connected, stamps the material onto it and outputs that drawable.
     */
    @Override
    public void processValues() {
        // Write dynamic uniforms into the material
        material.clearUniforms();

        for (ObjectMap.Entry<Integer, NumericalValue> entry : dynamicInputs) {
            int slotId = entry.key;
            NumericalValue val = entry.value;
            String uniformName = slotToUniform.get(slotId);

            if (val.isEmpty()) continue;

            int comp = slotToComponents.get(slotId);
            switch (comp) {
                case 1:
                    material.setUniform(uniformName, val.getFloat());
                    break;
                case 2:
                    material.setUniform(uniformName, val.get(0), val.get(1));
                    break;
                case 3:
                    material.setUniform(uniformName, val.get(0), val.get(1), val.get(2));
                    break;
                case 4:
                    material.setUniform(uniformName,
                            val.get(0), val.get(1), val.get(2), val.get(3));
                    break;
            }
        }

        // Decide which drawable to output
        if (!inputDrawable.isEmpty() && inputDrawable.getDrawable() != null) {
            // Stamp our material onto the incoming drawable and pass it through
            ParticleDrawable incoming = inputDrawable.getDrawable();
            incoming.setMaterial(material);
            outputValue.setDrawable(incoming);
        } else {
            // No drawable input: output the default 1x1 white quad carrier
            defaultCarrier.setMaterial(material);
            outputValue.setDrawable(defaultCarrier);
        }
    }

    /** Returns the uniform name for a given dynamic slot, used by editor widgets. */
    public String getUniformNameForSlot(int slotId) {
        return slotToUniform.get(slotId);
    }

    /** Returns the component count for a given dynamic slot. */
    public int getComponentCountForSlot(int slotId) {
        Integer count = slotToComponents.get(slotId);
        return count != null ? count : 1;
    }

    public ParticleMaterial getMaterial() {
        return material;
    }

    public ShaderDescriptor getShaderDescriptor() {
        return shaderDescriptor;
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("shaderFile", shaderFileName);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        shaderFileName = jsonData.getString("shaderFile", "");
    }
}
