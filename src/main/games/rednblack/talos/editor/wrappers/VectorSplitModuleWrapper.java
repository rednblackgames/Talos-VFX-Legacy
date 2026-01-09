package games.rednblack.talos.editor.wrappers;

import games.rednblack.talos.runtime.modules.VectorSplitModule;

public class VectorSplitModuleWrapper extends ModuleWrapper<VectorSplitModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("input", VectorSplitModule.INPUT);

        addOutputSlot("x", VectorSplitModule.X_OUT);
        addOutputSlot("y", VectorSplitModule.Y_OUT);
        addOutputSlot("z", VectorSplitModule.Z_OUT);
    }


    @Override
    protected float reportPrefWidth () {
        return 180;
    }
}
