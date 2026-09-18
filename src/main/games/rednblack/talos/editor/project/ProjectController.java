package games.rednblack.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.dialogs.NewProjectDialog;
import games.rednblack.talos.editor.widgets.ui.FileTab;
import games.rednblack.talos.editor.widgets.ui.TalosTabbedPane;

import java.io.File;
import java.util.Comparator;

/**
 * Owns the notion of "what is currently being edited".
 *
 * Every project type is a singleton holding the live model, so only one project can be alive at a time:
 * switching tab means serializing the outgoing tab into {@link FileTab} and deserializing the incoming one
 * back into the live model. All per tab state (content, bound file, undo history, export path) lives on the
 * tab itself, so tabs can never trade content with each other, no matter their name or their type.
 */
public class ProjectController {

    public FileTab currentTab;
    private boolean loading = false;

    IProject currentProject;

    public static final int MAX_RECENTS = 10;

    public static TalosProject TLS = new TalosProject();

    public ProjectController() {
        currentProject = TLS;
    }

    private TalosTabbedPane tabbedPane() {
        return TalosMain.Instance().UIStage().tabbedPane;
    }

    public void loadProject (FileHandle projectFileHandle) {
        loadProject(projectFileHandle, false);
    }

    /**
     * @param anonymous true when the content must not stay bound to the file it came from, as for the
     *                  examples shipped with the editor: the tab is filled but saving will ask where to
     */
    public void loadProject (FileHandle projectFileHandle, boolean anonymous) {
        try {
            if (!projectFileHandle.exists()) {
                //error handle
                return;
            }

            if (!anonymous) {
                FileTab alreadyOpen = findTabBoundTo(projectFileHandle.path());
                if (alreadyOpen != null) {
                    // opening it twice would fork its content into two tabs that then overwrite each other
                    tabbedPane().switchTab(alreadyOpen);
                    return;
                }
            }

            FileTab prevTab = currentTab;
            boolean removingUnworthy = prevTab != null
                    && prevTab.getProjectType() == currentProject && prevTab.isUnworthy();

            cacheCurrentTab();

            FileTab tab = new FileTab(projectFileHandle, currentProject);
            tab.setCachedData(projectFileHandle.readString(), false);
            if (anonymous) {
                tab.setUnworthy();
            } else {
                tab.setBoundPath(projectFileHandle.path());
                tab.setSuggestedDir(projectFileHandle.parent().path());
            }

            TalosMain.Instance().FileTracker().addTab(tab);
            // adding the tab selects it, and selecting it is what actually loads the content, see loadFromTab
            tabbedPane().add(tab);

            if (!anonymous) {
                reportProjectFileInterraction(projectFileHandle);

                TalosMain.Instance().Prefs().putString("lastOpen" + currentProject.getExtension(), projectFileHandle.parent().path());
                TalosMain.Instance().Prefs().flush();
            }

            if (removingUnworthy) {
                safeRemoveTab(prevTab);
            }
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    /**
     * Stores the live model into the active tab. Called before anything can replace what is on screen, so
     * that leaving a tab, even an anonymous one that was never saved, can not lose its content.
     */
    private void cacheCurrentTab() {
        if (currentTab == null) return;

        try {
            currentTab.setCachedData(currentTab.getProjectType().getProjectString(true), true);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    private FileTab findTabBoundTo(String path) {
        if (path == null) return null;

        for (Tab tab : tabbedPane().getTabs()) {
            FileTab fileTab = (FileTab) tab;
            if (path.equals(fileTab.getBoundPath())) {
                return fileTab;
            }
        }

        return null;
    }

    private void getProjectFromString(String string, boolean fromMemory) {
        try {
            loading = true;
            currentProject.loadProject(null, string, fromMemory);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        } finally {
            loading = false;
        }
    }

    public void loadFromExportedP(FileHandle fileHandle) {
        try {
            setProject(TLS);
            // an exported .p is not a project file, so the tab stays anonymous and saving will ask for a .tls
            createNewProjectTab(TLS, getUniqueFilename(fileHandle.nameWithoutExtension() + TLS.getExtension()));
            if (currentTab != null) {
                currentTab.setSuggestedDir(fileHandle.parent().path());
            }
            loading = true;
            TLS.loadFromExportP(fileHandle);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        } finally {
            loading = false;
        }

        setDirty();
    }

    public void saveProject (FileHandle destination) {
        saveProject(currentTab, destination);
    }

    /**
     * Writes the content of the given tab, activating it first if it is not the one being edited: the live
     * model only ever holds the active tab, so saving anything else would write the wrong project.
     */
    public void saveProject (FileTab tab, FileHandle destination) {
        if (tab == null) return;

        if (tab != currentTab) {
            if (!tabbedPane().getTabs().contains(tab, true)) return; // closed while we were away
            tabbedPane().switchTab(tab);
            if (tab != currentTab) return;
        }

        try {
            String data = currentProject.getProjectString(false);
            destination.writeString(data, false);

            reportProjectFileInterraction(destination);

            TalosMain.Instance().Prefs().putString("lastSave" + currentProject.getExtension(), destination.parent().path());
            TalosMain.Instance().Prefs().flush();

            tab.setDirty(false);
            tab.setWorthy();
            tab.setBoundPath(destination.path());
            tab.setSuggestedDir(destination.parent().path());
            tab.setCachedData(data, false);
            tab.setProjectFileHandle(destination);
            tabbedPane().updateTabTitle(tab);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    public void saveProject() {
        if(isBoundToFile()) {
            FileHandle handle = Gdx.files.absolute(currentTab.getBoundPath());
            saveProject(handle);
        }
    }

    /**
     * Saves a tab that is not necessarily the active one, then runs the given action. Saving always writes
     * the content of the requested tab, so closing a dirty background tab can not write the wrong project.
     * The action does not run if the user cancels the destination chooser.
     */
    public void saveTabThen(FileTab tab, Runnable onSaved) {
        if (tab == null) {
            if (onSaved != null) onSaved.run();
            return;
        }

        if (tab != currentTab) {
            tabbedPane().switchTab(tab);
        }

        if (tab.isBoundToFile()) {
            saveProject(tab, Gdx.files.absolute(tab.getBoundPath()));
            if (onSaved != null) onSaved.run();
        } else {
            TalosMain.Instance().UIStage().saveAsProjectAction(onSaved);
        }
    }

    public boolean hasUnsavedChanges() {
        return getUnsavedTabCount() > 0;
    }

    public int getUnsavedTabCount() {
        if (TalosMain.Instance().UIStage() == null || tabbedPane() == null) return 0;

        int count = 0;
        for (Tab tab : tabbedPane().getTabs()) {
            if (tab.isDirty()) count++;
        }

        return count;
    }

    /**
     * Saves every tab holding unsaved changes, one after the other, then runs the given action. Projects
     * that were never saved ask for a destination in turn, and backing out of one stops the whole chain,
     * so nothing is written and the action never runs.
     */
    public void saveAllDirtyTabsThen(Runnable onAllSaved) {
        Array<FileTab> dirtyTabs = new Array<>();
        if (tabbedPane() != null) {
            for (Tab tab : tabbedPane().getTabs()) {
                if (tab.isDirty()) {
                    dirtyTabs.add((FileTab) tab);
                }
            }
        }

        saveDirtyTabsFrom(dirtyTabs, 0, onAllSaved);
    }

    private void saveDirtyTabsFrom(Array<FileTab> dirtyTabs, int index, Runnable onAllSaved) {
        if (index >= dirtyTabs.size) {
            if (onAllSaved != null) onAllSaved.run();
            return;
        }

        saveTabThen(dirtyTabs.get(index), () -> saveDirtyTabsFrom(dirtyTabs, index + 1, onAllSaved));
    }

    public void newProject (IProject project) {
        if(project.requiresWorkspaceLocation()) {
            String fileName = getNewFilename(project);
            String projectName = fileName.substring(0, fileName.indexOf("."));
            NewProjectDialog.show(project.getProjectTypeName(), projectName, new NewProjectDialog.NewProjectListener() {
                @Override
                public void create (String path, String name) {
                    createNewProjectTab(project, fileName);
                    project.createWorkspaceEnvironment(path, name);
                }
            });
        } else {
            String fileName = getNewFilename(project);
            createNewProjectTab(project, fileName);
        }
    }

    public void createNewProjectTab(IProject project, String fileName) {
        FileTab prevTab = currentTab;
        boolean removingUnworthy = prevTab != null
                && prevTab.getProjectType() == project && prevTab.isUnworthy();

        cacheCurrentTab();

        final FileTab tab = new FileTab(Gdx.files.local(fileName), project);
        tab.setUnworthy(); // all new projects are unworthy, and will only become worthy when worked on

        TalosMain.Instance().FileTracker().addTab(tab);
        // adding the tab selects it, and selecting it is what resets the project to a new one, see loadFromTab
        tabbedPane().add(tab);

        if(removingUnworthy) {
            safeRemoveTab(prevTab);
        }
    }

    /**
     * removes tab without listener crap
     */
    public void safeRemoveTab(FileTab tab) {
        FileTab tmp = currentTab;
        tabbedPane().remove(tab, true);
        if (tmp != tab) {
            currentTab = tmp;
        }
    }

    public String getNewFilename(IProject project) {
        int index = 1;
        String name = project.getProjectNameTemplate() + index + project.getExtension();
        while (isNameTaken(name)) {
            index++;
            name = project.getProjectNameTemplate() + index + project.getExtension();
        }

        return name;
    }

    private String getUniqueFilename(String preferred) {
        if (!isNameTaken(preferred)) return preferred;

        int dot = preferred.lastIndexOf(".");
        String base = dot > 0 ? preferred.substring(0, dot) : preferred;
        String extension = dot > 0 ? preferred.substring(dot) : "";

        int index = 2;
        while (isNameTaken(base + index + extension)) {
            index++;
        }

        return base + index + extension;
    }

    private boolean isNameTaken(String fileName) {
        for (Tab tab : tabbedPane().getTabs()) {
            if (((FileTab) tab).getFileName().equals(fileName)) {
                return true;
            }
        }

        return false;
    }

    public boolean isBoundToFile() {
        return currentTab != null && currentTab.isBoundToFile();
    }

    public void unbindFromFile() {
        if (currentTab != null) {
            currentTab.setBoundPath(null);
        }
    }

    public String getCurrentProjectPath () {
        return currentTab == null ? null : currentTab.getBoundPath();
    }

    public void setDirty() {
        if(!loading && currentTab != null) {
            currentTab.setDirty(true);
            currentTab.setWorthy();

            try {
                // also add this as snapshot
                currentTab.getSnapshotTracker().addSnapshot(getProjectString(true));
            } catch (Exception e) {
                TalosMain.Instance().reportException(e);
            }
        }
    }

    private String getProjectString(boolean toMemory) {
        return currentProject.getProjectString(toMemory);
    }

    /**
     * Makes the given tab the one being edited. This is the only place that fills the live model, and it
     * always fills it: from the tab's own cache, from the file it is bound to, or with an empty project.
     * It never leaves on screen what the previous tab was showing.
     */
    public void loadFromTab(FileTab tab) {
        if (tab == null || tab == currentTab) return;

        cacheCurrentTab();

        currentTab = tab; // trackers and dirty reporting need to know what current tab is before loading
        currentProject = tab.getProjectType();

        boolean loaded = false;
        loading = true;
        try {
            String data = tab.getCachedData();
            boolean fromMemory = tab.isCachedFromMemory();

            if (data == null && tab.isBoundToFile()) {
                FileHandle handle = Gdx.files.absolute(tab.getBoundPath());
                if (handle.exists()) {
                    data = handle.readString();
                    fromMemory = false;
                }
            }

            if (data != null) {
                currentProject.loadProject(tab.getProjectFileHandle(), data, fromMemory);
                loaded = true;
            } else {
                currentProject.resetToNew();
            }

            if (tab.getSnapshotTracker().isEmpty()) {
                tab.getSnapshotTracker().reset(currentProject.getProjectString(true));
            }
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        } finally {
            loading = false;
        }

        if (loaded) {
            try {
                TalosMain.Instance().FileTracker().addSavedResourcePathsFor(tab, currentProject.getSavedResourcePaths());
            } catch (Exception e) {
                TalosMain.Instance().reportException(e);
            }
        }

        if (currentProject == TLS) {
            TalosMain.Instance().UIStage().swapToTalosContent();
        } else {
            currentProject.initUIContent();
        }
        TalosMain.Instance().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void removeTab(FileTab tab) {
        TalosMain.Instance().FileTracker().removeTab(tab);
        if(tab == currentTab) {
            currentTab = null;
        }
    }

    public void setProject(IProject project) {
        currentProject = project;
        if(project.equals(TLS)) {
            TalosMain.Instance().UIStage().swapToTalosContent();
        }
    }

    public IProject getProject() {
        return currentProject;
    }

    public FileHandle findFile(String path) {
        return findFile(Gdx.files.absolute(path));
    }

    public FileHandle findFile(FileHandle initialFile) {
        String fileName = initialFile.name();

        // local is priority, then the path, then the default lookup
        // do we currently have project loaded?
        String currentProjectPath = getCurrentProjectPath();
        if(currentProjectPath != null) {
            // we can look for local file then
            FileHandle currentProjectHandle = Gdx.files.absolute(currentProjectPath);
            if(currentProjectHandle.exists()) {
                String localPath = currentProjectHandle.parent().path() + File.separator + fileName;
                FileHandle localTry = Gdx.files.absolute(localPath);
                if(localTry.exists()) {
                    return localTry;
                }
            }
        }

        //Maybe the absolute path was a better ideas
        if(initialFile.exists()) return initialFile;

        //oh crap it's nowhere to be found, default path to the rescue!
        FileHandle lastHopeHandle = currentProject.findFileInDefaultPaths(fileName);
        if(lastHopeHandle != null && lastHopeHandle.exists()) {
            return lastHopeHandle;
        }

        // well we did all we could. seppuku is imminent
        return null;
    }

    public void exportProject(FileHandle fileHandle) {
        if (currentTab != null) {
            currentTab.setExportPath(fileHandle.path());
        }

        String data = currentProject.exportProject();
        fileHandle.writeString(data, false);

        TalosMain.Instance().Prefs().putString("lastExport"+currentProject.getExtension(), fileHandle.parent().path());
        TalosMain.Instance().Prefs().flush();
    }

    public String getCurrentExportNameSuggestion() {
        if(currentTab != null) {
            String projectName = currentTab.getFileName();
            String exportExt = currentProject.getExportExtension();
            return projectName.substring(0, projectName.lastIndexOf(".")) + exportExt;
        }
        return "";
    }

    public String getLastDir(String action, IProject projectType) {
        String path = TalosMain.Instance().Prefs().getString("last" + action + projectType.getExtension());
        FileHandle handle = Gdx.files.absolute(path);
        if(handle.exists()) {
            return handle.path();
        }

        return "";
    }

    public String getExportPath() {
        return currentTab == null ? null : currentTab.getExportPath();
    }

    public void undo() {
        if (currentTab == null) return;

        SnapshotTracker snapshotTracker = currentTab.getSnapshotTracker();
        boolean changed = snapshotTracker.moveBack();
        if (changed) {
            getProjectFromString(snapshotTracker.getCurrentSnapshot(), true);
        }
    }

    public void redo() {
        if (currentTab == null) return;

        SnapshotTracker snapshotTracker = currentTab.getSnapshotTracker();
        boolean changed = snapshotTracker.moveForward();
        if (changed) {
            getProjectFromString(snapshotTracker.getCurrentSnapshot(), true);
        }
    }

    public void closeCurrentTab() {
        if (currentTab != null) {
            tabbedPane().remove(currentTab, true);
        }
    }

    public static class RecentsEntry {
        String path;
        long time;

        public RecentsEntry() {

        }

        public RecentsEntry(String path, long time) {
            this.path = path;
            this.time = time;
        }

        @Override
        public boolean equals(Object obj) {
            return path.equals(((RecentsEntry)obj).path);
        }
    }

    Comparator<RecentsEntry> recentsEntryComparator = new Comparator<RecentsEntry>() {
        @Override
        public int compare(RecentsEntry o1, RecentsEntry o2) {
            return Long.compare(o2.time, o1.time);
        }
    };

    public void reportProjectFileInterraction(FileHandle handle) {
        Preferences prefs = TalosMain.Instance().Prefs();
        String data = prefs.getString("recents");
        Array<RecentsEntry> list = new Array<>();
        //read
        Json json = new Json();
        try {
            if (data != null && !data.isEmpty()) {
                list = json.fromJson(list.getClass(), data);
            }
        } catch( Exception e) {

        }
        RecentsEntry newEntry = new RecentsEntry(handle.path(), TimeUtils.millis());
        list.removeValue(newEntry, false);
        list.add(newEntry);
        //sort
        list.sort(recentsEntryComparator);
        list.truncate(MAX_RECENTS);
        //write
        String result = json.toJson(list);
        prefs.putString("recents", result);
        prefs.flush();
        updateRecentsList();
    }

    public Array<String> updateRecentsList() {
        Preferences prefs = TalosMain.Instance().Prefs();
        String data = prefs.getString("recents");
        Array<String> list = new Array<>();
        //read

        try {
            Json json = new Json();
            if (data != null && !data.isEmpty()) {
                Array<RecentsEntry> rList = new Array<>();
                rList = json.fromJson(rList.getClass(), data);
                rList.sort(recentsEntryComparator);
                rList.truncate(MAX_RECENTS);
                for (RecentsEntry entry : rList) {
                    list.add(entry.path);
                }
            }

            TalosMain.Instance().UIStage().Menu().updateRecentsList(list);
        } catch (Exception e) {

        }

        return list;
    }
}
