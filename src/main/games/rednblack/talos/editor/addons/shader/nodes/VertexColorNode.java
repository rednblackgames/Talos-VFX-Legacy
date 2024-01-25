package games.rednblack.talos.editor.addons.shader.nodes;

import games.rednblack.talos.runtime.shaders.ShaderBuilder;

public class VertexColorNode extends AbstractShaderNode {

    public final String OUTPUT = "outputValue";

    @Override
    protected void inputStateChanged (boolean isInputDynamic) {

    }

    @Override
    protected boolean isInputDynamic () {
        return false;
    }

    @Override
    public void prepareDeclarations (ShaderBuilder shaderBuilder) {

    }

    @Override
    public String writeOutputCode (String slotId) {
        return "v_color";
    }
}
