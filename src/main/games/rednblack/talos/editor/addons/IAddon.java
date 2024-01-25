package games.rednblack.talos.editor.addons;

import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.widget.MenuBar;
import games.rednblack.talos.editor.dialogs.SettingsDialog;
import games.rednblack.talos.editor.project.IProject;

public interface IAddon {
    void init();

    void initUIContent();

    boolean projectFileDrop(FileHandle handle);

    IProject getProjectType();

    void announceLocalSettings(SettingsDialog settingsDialog);

    void buildMenu(MenuBar menuBar);

    void dispose();
}
