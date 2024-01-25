package games.rednblack.talos.editor.wrappers;

import games.rednblack.talos.runtime.modules.RadToCartModule;

public class RadToCartModuleWrapper extends ModuleWrapper<RadToCartModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("angle: ", RadToCartModule.A);
        addInputSlot("velocity: ", RadToCartModule.L);

        addOutputSlot("XY", 0);
    }


    @Override
    protected float reportPrefWidth () {
        return 210;
    }
}
