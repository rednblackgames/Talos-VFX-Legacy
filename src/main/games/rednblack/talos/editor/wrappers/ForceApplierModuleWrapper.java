package games.rednblack.talos.editor.wrappers;

import games.rednblack.talos.runtime.modules.ForceApplierModule;

public class ForceApplierModuleWrapper extends ModuleWrapper<ForceApplierModule> {


    @Override
    protected void configureSlots() {
        addInputSlot("sum forces: ", ForceApplierModule.SUM_FORCES);

        addOutputSlot("angle: ", ForceApplierModule.ANGLE);
        addOutputSlot("velocity", ForceApplierModule.VELOCITY);
    }


    @Override
    protected float reportPrefWidth () {
        return 210;
    }
}
