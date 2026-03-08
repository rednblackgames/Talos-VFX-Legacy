package games.rednblack.talos.editor.wrappers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntMap;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.editor.utils.MsdfFonts;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.notifications.FileActorBinder;
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.ShaderModule;
import games.rednblack.talos.runtime.utils.ShaderDescriptor;

public class ShaderModuleWrapper extends ModuleWrapper<ShaderModule> {

    private TextraLabel dropLabel;
    private String shaderFileName;

    @Override
    protected void configureSlots() {
        addInputSlot("drawable", ShaderModule.DRAWABLE);

        addOutputSlot("output", ShaderModule.OUTPUT);

        dropLabel = MsdfFonts.label("drop .shdr file here");
        dropLabel.setAlignment(Align.center);
        dropLabel.setWrap(true);
        contentWrapper.add(dropLabel).padTop(10f).padBottom(10f).size(180, 50).left().expand();

        FileActorBinder.register(this, "shdr");
        addListener(new FileActorBinder.FileEventListener() {

            @Override
            public void onFileSet(FileHandle fileHandle) {
                try {
                    shaderFileName = fileHandle.name();
                    setShaderLabel(shaderFileName);
                    setShaderDescriptor(new ShaderDescriptor(fileHandle), shaderFileName);
                } catch (Exception e) {
                    // shader parse error
                }
            }
        });
    }

    @Override
    public void setModule(ShaderModule module) {
        super.setModule(module);
        shaderFileName = module.shaderFileName;
        setShaderLabel(shaderFileName);
        rebuildDynamicPorts();
    }

    private void setShaderLabel(String shaderFileName) {
        dropLabel.setText("SHDR: " + (shaderFileName != null ? shaderFileName : "none"));
    }

    private void setShaderDescriptor(ShaderDescriptor shaderDescriptor, String fileName) {
        module.loadShader(shaderDescriptor, fileName);
        setShaderLabel(fileName);
        rebuildDynamicPorts();
    }

    private void rebuildDynamicPorts() {
        // Remove previous dynamic input ports (slot index >= 10)
        removeDynamicInputSlots();

        // Add new ports for each dynamic uniform
        IntMap<Slot> inputSlots = module.getInputSlots();
        for (IntMap.Entry<Slot> entry : inputSlots) {
            if (entry.key >= 10) {
                String label = module.getUniformNameForSlot(entry.key);
                if (label != null) {
                    addInputSlot(label, entry.key);
                }
            }
        }

        invalidateHierarchy();
    }

    private void removeDynamicInputSlots() {
        // This removes UI elements for dynamic slots; base class handles the collection
        // The module itself clears and recreates its slots in loadShader()
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        if (module.shaderFileName != null && !module.shaderFileName.isEmpty()) {
            setShaderLabel(module.shaderFileName);
            rebuildDynamicPorts();
        }
    }
}
