package games.rednblack.talos.editor.widgets.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.project.IProject;
import games.rednblack.talos.editor.project.SnapshotTracker;

/**
 * A tab owns everything that belongs to the project it shows: the serialized content while the tab sits in
 * background, the file it is bound to, its own undo history and its own export path.
 *
 * Tabs are compared by identity on purpose, so any number of anonymous projects can be open at the same
 * time, even of the same type and with the same name, without ever sharing state.
 */
public class FileTab extends Tab {

    public FileHandle projectFileHandle;
    private final IProject projectType;
    private boolean unworthy = false;

    /** absolute path of the file this tab is bound to, null while the project is anonymous */
    private String boundPath = null;

    /** serialized content of this tab, kept up to date whenever the tab is not the active one */
    private String cachedData = null;
    private boolean cachedFromMemory = false;

    /** directory to offer when this anonymous project gets saved for the first time */
    private String suggestedDir = null;

    private String exportPath = null;

    private final SnapshotTracker snapshotTracker = new SnapshotTracker();

    public FileTab(FileHandle projectFileHandle, IProject projectType) {
        super(true, true);
        this.projectFileHandle = projectFileHandle;
        this.projectType = projectType;
    }

    @Override
    public String getTabTitle() {
        return projectFileHandle.name();
    }

    @Override
    public boolean save() {
        if(isSavable()) {
            TalosMain.Instance().ProjectController().saveTabThen(this, null);
        }

        return false;
    }

    @Override
    public Table getContentTable() {
        return null;
    }

    public String getFileName() {
        return projectFileHandle.name();
    }

    public FileHandle getProjectFileHandle() {
        return projectFileHandle;
    }

    public void setProjectFileHandle (FileHandle projectFileHandle) {
        this.projectFileHandle = projectFileHandle;
    }

    public IProject getProjectType() {
        return projectType;
    }

    public void setUnworthy() {
        unworthy = true;
    }

    public void setWorthy() {
        unworthy = false;
    }

    public boolean isUnworthy() {
        return unworthy;
    }

    public String getBoundPath() {
        return boundPath;
    }

    public void setBoundPath(String boundPath) {
        this.boundPath = boundPath;
    }

    public boolean isBoundToFile() {
        return boundPath != null;
    }

    public String getCachedData() {
        return cachedData;
    }

    public boolean isCachedFromMemory() {
        return cachedFromMemory;
    }

    public void setCachedData(String cachedData, boolean fromMemory) {
        this.cachedData = cachedData;
        this.cachedFromMemory = fromMemory;
    }

    public String getSuggestedDir() {
        return suggestedDir;
    }

    public void setSuggestedDir(String suggestedDir) {
        this.suggestedDir = suggestedDir;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    public SnapshotTracker getSnapshotTracker() {
        return snapshotTracker;
    }
}
