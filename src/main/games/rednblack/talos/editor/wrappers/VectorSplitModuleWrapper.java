package games.rednblack.talos.editor.wrappers;

import games.rednblack.talos.editor.widgets.ui.DragPoint;
import games.rednblack.talos.runtime.modules.VectorSplitModule;

public class VectorSplitModuleWrapper extends ModuleWrapper<VectorSplitModule> implements IDragPointProvider {


    @Override
    public DragPoint[] fetchDragPoints() {
        return null;
    }

    @Override
    public void dragPointChanged(DragPoint point) {

    }

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
