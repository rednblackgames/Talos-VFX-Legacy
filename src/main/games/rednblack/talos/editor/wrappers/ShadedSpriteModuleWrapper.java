package games.rednblack.talos.editor.wrappers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import games.rednblack.talos.editor.utils.MsdfFonts;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.talos.editor.notifications.FileActorBinder;
import games.rednblack.talos.runtime.modules.ShadedSpriteModule;
import games.rednblack.talos.runtime.utils.ShaderDescriptor;
import games.rednblack.talos.runtime.utils.VectorField;

public class ShadedSpriteModuleWrapper extends ModuleWrapper<ShadedSpriteModule> {

    private TextraLabel dropLabel;
    private String shaderFileName;

    @Override
    protected void configureSlots () {
        addOutputSlot("output", ShadedSpriteModule.OUTPUT);

        dropLabel = MsdfFonts.label("drop .shdr file here");
        dropLabel.setAlignment(Align.center);
        dropLabel.setWrap(true);
        contentWrapper.add(dropLabel).padTop(10f).padBottom(10f).size(180, 50).left().expand();

        FileActorBinder.register(this, "shdr");
        addListener(new FileActorBinder.FileEventListener() {

            @Override
            public void onFileSet (FileHandle fileHandle) {
                try {
                    String shaderFilePath = fileHandle.path();
                    shaderFileName = fileHandle.name();
                    setShaderLabel(shaderFileName);

                    setShaderDescriptor(new ShaderDescriptor(fileHandle), shaderFileName);

                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void setModule(ShadedSpriteModule module) {
        super.setModule(module);
        shaderFileName = module.shdrFileName;
        setShaderLabel(shaderFileName);
    }

    private void setShaderLabel(String shaderFileName) {
        dropLabel.setText("SHDR File: " + shaderFileName);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        if(module.shdrFileName != null && !module.shdrFileName.isEmpty()) {
            setShaderLabel(module.shdrFileName);
        }
    }

    private void setShaderDescriptor(ShaderDescriptor shaderDescriptor, String fileName) {
        module.setShaderData(shaderDescriptor, fileName);
        setShaderLabel(fileName);
    }
}
