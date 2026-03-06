package games.rednblack.talos.editor.addons.vectorfield;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.addons.IAddon;
import games.rednblack.talos.editor.dialogs.SettingsDialog;
import games.rednblack.talos.editor.project.IProject;

public class VectorFieldAddon implements IAddon {

    public static VectorFieldProject VF_PROJECT;

    private VectorFieldWorkspace workspace;
    private VectorFieldPropertiesPanel propertiesPanel;

    @Override
    public void init () {
        VF_PROJECT = new VectorFieldProject(this);
        workspace = new VectorFieldWorkspace();
        propertiesPanel = new VectorFieldPropertiesPanel(TalosMain.Instance().UIStage().getSkin(), workspace);
    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(propertiesPanel, workspace, null);
        TalosMain.Instance().disableNodeStage();
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();
        TalosMain.Instance().UIStage().getStage().setKeyboardFocus(workspace);
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {
        if (handle.extension().equals("tvf")) {
            TalosMain.Instance().ProjectController().setProject(getProjectType());
            TalosMain.Instance().ProjectController().loadProject(handle);
            return true;
        }

        IProject currProjectType = TalosMain.Instance().ProjectController().getProject();

        if (currProjectType == VF_PROJECT) {
            if (handle.extension().equals("fga")) {
                String data = handle.readString();
                workspace.importFGA(data);
                propertiesPanel.syncFromWorkspace();
                return true;
            }
        }

        return false;
    }

    @Override
    public IProject getProjectType () {
        return VF_PROJECT;
    }

    @Override
    public void announceLocalSettings (SettingsDialog settingsDialog) {
    }

    @Override
    public void buildMenu (MenuBar menuBar) {
        Menu menu = new Menu("Vector Field");

        MenuItem newFile = new MenuItem("New Vector Field", icon("ic-file-new"));
        menu.addItem(newFile);
        MenuItem openFile = new MenuItem("Open Vector Field", icon("ic-folder"));
        menu.addItem(openFile);
        MenuItem importFga = new MenuItem("Import .fga");
        menu.addItem(importFga);
        MenuItem exportFga = new MenuItem("Export .fga");
        menu.addItem(exportFga);

        newFile.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().ProjectController().newProject(VF_PROJECT);
            }
        });

        openFile.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().openProjectAction(VF_PROJECT);
            }
        });

        importFga.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showFileChooser(".fga", new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        if (files.size > 0) {
                            // ensure we're in VF project mode
                            IProject currProject = TalosMain.Instance().ProjectController().getProject();
                            if (currProject != VF_PROJECT) {
                                TalosMain.Instance().ProjectController().newProject(VF_PROJECT);
                            }
                            String data = files.get(0).readString();
                            workspace.importFGA(data);
                            propertiesPanel.syncFromWorkspace();
                        }
                    }
                });
            }
        });

        exportFga.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showSaveFileChooser(".fga", new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        if (files.size > 0) {
                            String fgaData = workspace.exportFGA();
                            files.get(0).writeString(fgaData, false);
                        }
                    }
                });
            }
        });

        menuBar.addMenu(menu);
    }

    @Override
    public void dispose () {
    }

    public VectorFieldWorkspace getWorkspace () {
        return workspace;
    }

    public VectorFieldPropertiesPanel getPropertiesPanel () {
        return propertiesPanel;
    }

    private Image icon (String name) {
        return new Image(TalosMain.Instance().UIStage().getSkin().getDrawable(name));
    }
}
