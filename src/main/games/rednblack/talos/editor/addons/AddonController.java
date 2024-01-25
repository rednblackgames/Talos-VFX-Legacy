package games.rednblack.talos.editor.addons;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuBar;
import games.rednblack.talos.editor.addons.bvb.BvBAddon;
import games.rednblack.talos.editor.addons.shader.ShaderAddon;
import games.rednblack.talos.editor.dialogs.SettingsDialog;

public class AddonController {

    Array<IAddon> activeAddons = new Array();

    public AddonController() {
        registerAddon(new BvBAddon());
        registerAddon(new ShaderAddon());
    }

    private void registerAddon(IAddon addon) {
        activeAddons.add(addon);
    }

    public void initAll() {
        for(IAddon addon: activeAddons) {
            addon.init();
        }
    }

    public IAddon projectFileDrop(FileHandle handle) {
        for(IAddon addon: activeAddons) {
            boolean accepted = addon.projectFileDrop(handle);
            if(accepted) return addon;
        }

        return null;
    }

    public void announceLocalSettings(SettingsDialog settingsDialog) {
        for(IAddon addon: activeAddons) {
            addon.announceLocalSettings(settingsDialog);
        }
    }

    public void buildMenu(MenuBar menuBar) {
        for(IAddon addon: activeAddons) {
            addon.buildMenu(menuBar);
        }
    }

    public IAddon getAddon(Class addonClass) {
        for(IAddon addon: activeAddons) {
            if (addon.getClass() == addonClass) {
                return addon;
            }
        }

        return null;
    }

    public void dispose () {
        for(IAddon addon: activeAddons) {
            addon.dispose();
        }
    }
}
