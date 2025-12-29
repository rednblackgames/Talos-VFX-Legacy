package games.rednblack.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.UIStage;
import games.rednblack.talos.editor.project.ProjectController;

public class MainMenu extends Table {

    UIStage stage;
    private MenuItem saveProject;
    private MenuItem export;
    private MenuItem exportAs;
    private MenuItem saveAsProject;
    private Menu partcileMenu;
    private MenuItem removeSelectedModules;
    private MenuItem createModule;
    private MenuItem groupSelectedModules;
    private MenuItem ungroupSelectedModules;
    private PopupMenu openRecentPopup;

    public MainMenu(UIStage stage) {
        setSkin(stage.getSkin());
        this.stage = stage;

        setBackground(stage.getSkin().getDrawable("button-main-menu"));
    }

    public void build() {
        clearChildren();

        MenuBar menuBar = new MenuBar();
        Menu projectMenu = new Menu("File");
        menuBar.addMenu(projectMenu);
        partcileMenu = new Menu("Particle");
        menuBar.addMenu(partcileMenu);

        TalosMain.Instance().Addons().buildMenu(menuBar);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.addItem(about);
        menuBar.addMenu(helpMenu);

        about.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Dialogs.showOKDialog(stage.getStage(), "About Talos 1.5.4", "Talos is a an open source node based FX and Shader editor");
            }
        });

        createModule = new MenuItem("Create Module");
        PopupMenu createPopup = stage.createModuleListPopup();
        createModule.setSubMenu(createPopup);
        removeSelectedModules = new MenuItem("Remove Selected").setShortcut(Input.Keys.DEL);
        groupSelectedModules = new MenuItem("Group Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.G);
        ungroupSelectedModules = new MenuItem("Ungroup Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.U);
        final MenuItem newProject = new MenuItem("New Particle", icon("ic-file-new"));
        final MenuItem openProject = new MenuItem("Open Particle", icon("ic-folder"));
        partcileMenu.addItem(newProject);
        partcileMenu.addItem(openProject);
        partcileMenu.addItem(createModule);
        partcileMenu.addItem(removeSelectedModules);
        partcileMenu.addItem(groupSelectedModules);

        MenuItem openRecent = new MenuItem("Open Recent", icon("ic-folder-recent"));
        saveProject = new MenuItem("Save", icon("ic-save"));
        export = new MenuItem("Export" , icon("ic-download"));
        exportAs = new MenuItem("Export As");
        MenuItem examples = new MenuItem("Examples");

        openRecentPopup = new PopupMenu();
        openRecent.setSubMenu(openRecentPopup);

        MenuItem legacy = new MenuItem("libGDX Particles");
        PopupMenu legacyPopup = new PopupMenu();
        MenuItem legacyImportItem = new MenuItem("Import");
        MenuItem legacyBatchImportItem = new MenuItem("Batch Convert");
        legacyPopup.addItem(legacyImportItem);
        legacyPopup.addItem(legacyBatchImportItem);
        legacy.setSubMenu(legacyPopup);
        partcileMenu.addSeparator();
        partcileMenu.addItem(legacy);

        MenuItem settings = new MenuItem("Preferences");

        PopupMenu examplesPopup = new PopupMenu();
        examples.setSubMenu(examplesPopup);
        stage.initExampleList(examplesPopup);
        saveAsProject = new MenuItem("Save As", icon("ic-save-aster"));
        MenuItem exitApp = new MenuItem("Exit");

        projectMenu.addItem(openRecent);
        projectMenu.addItem(saveProject);
        projectMenu.addItem(saveAsProject);
        projectMenu.addItem(export);
        projectMenu.addItem(exportAs);
        projectMenu.addSeparator();
        projectMenu.addItem(examples);
        projectMenu.addSeparator();
        projectMenu.addItem(settings);
        projectMenu.addSeparator();
        projectMenu.addItem(exitApp);

        removeSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.deleteSelectedWrappers();
            }
        });

        groupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupFromSelectedWrappers();
            }
        });

        ungroupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.ungroupSelectedWrappers();
            }
        });

        newProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.newProjectAction();
            }
        });

        openProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.openProjectAction();
            }
        });

        saveProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!saveProject.isDisabled()) stage.saveProjectAction();
            }
        });

        export.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.exportAction();
            }
        });

        exportAs.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.exportAsAction();
            }
        });

        saveAsProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.saveAsProjectAction();
            }
        });

        legacyImportItem.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.legacyImportAction();
            }
        });

        legacyBatchImportItem.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.legacyBatchConvertAction();
            }
        });

        exitApp.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.exit();
            }
        });
        settings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.getStage().addActor(stage.settingsDialog.fadeIn());
            }
        });

        add(menuBar.getTable()).left().grow();


        // adding key listeners for menu items
        stage.getStage().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!newProject.isDisabled()) {
                        TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
                    }
                }
                if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!openProject.isDisabled()) {
                        stage.openProjectAction();
                    }
                }
                if(keycode == Input.Keys.S && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!saveProject.isDisabled()) {
                        stage.saveProjectAction();
                    }
                }
                if(keycode == Input.Keys.E && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!saveProject.isDisabled()) {
                        stage.exportAction();
                    }
                }

                return super.keyDown(event, keycode);
            }
        });

        TalosMain.Instance().ProjectController().updateRecentsList();
    }

    public void disableTalosSpecific() {
        disableItem(removeSelectedModules);
        disableItem(createModule);
        disableItem(groupSelectedModules);
        disableItem(ungroupSelectedModules);
    }

    public void disableItem(MenuItem item) {
        item.setDisabled(true);
    }

    public void enableItem(MenuItem item) {
        item.setDisabled(false);
    }

    public void restore() {
        enableItem(removeSelectedModules);
        enableItem(createModule);
        enableItem(groupSelectedModules);
        enableItem(ungroupSelectedModules);
    }

    public void updateRecentsList(Array<String> list) {
        openRecentPopup.clear();

        for(String path: list) {
            final FileHandle handle = Gdx.files.absolute(path);
            if(!handle.exists()) continue;
            String name = handle.name();
            MenuItem item = new MenuItem(name);
            item.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if(handle.extension().equals("tls")) {
                        TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
                        TalosMain.Instance().ProjectController().loadProject(handle);
                    } else {
                        TalosMain.Instance().Addons().projectFileDrop(handle);
                    }
                }
            });
            openRecentPopup.addItem(item);
        }
    }

    private Image icon(String name) {
        return new Image(getSkin().getDrawable(name));
    }
}
