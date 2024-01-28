/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package games.rednblack.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneListener;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.addons.IAddon;
import games.rednblack.talos.editor.dialogs.BatchConvertDialog;
import games.rednblack.talos.editor.dialogs.NewProjectDialog;
import games.rednblack.talos.editor.dialogs.SettingsDialog;
import games.rednblack.talos.editor.dialogs.YesNoDialog;
import games.rednblack.talos.editor.notifications.Notifications;
import games.rednblack.talos.editor.notifications.events.AssetFileDroppedEvent;
import games.rednblack.talos.editor.project.IProject;
import games.rednblack.talos.editor.project.ProjectController;
import games.rednblack.talos.editor.project.TalosProject;
import games.rednblack.talos.editor.widgets.ui.*;
import games.rednblack.talos.editor.wrappers.WrapperRegistry;
import games.rednblack.talos.runtime.ParticleEmitterDescriptor;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class UIStage {

	private final Stage stage;
	private final Skin skin;
	private final DragAndDrop dragAndDrop;

	Table fullScreenTable;

	private EmitterList emitterList;
	public PreviewWidget previewWidget;

	FileChooser fileChooser;
	BatchConvertDialog batchConvertDialog;
	public SettingsDialog settingsDialog;
	public NewProjectDialog newProjectDialog;

	ColorPicker colorPicker;

	ModuleListPopup moduleListPopup;

	public TabbedPane tabbedPane;

	private VisSplitPane bottomPane;
	private Table leftTable;
	private Table rightTable;
	private Table bottomTable;

	private Table bottomContainer;
	private MainMenu mainMenu;
	private VisSplitPane horizontalPane;
	private VisSplitPane verticalPane;
	private Table mainLayout;
	private Table customLayout;


	public UIStage (Skin skin) {
		this.stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
		this.skin = skin;
		this.dragAndDrop = new DragAndDrop();
	}

	public void init () {
		fullScreenTable = new Table();
		fullScreenTable.setFillParent(true);

		stage.addActor(fullScreenTable);

		defaults();
		constructMenu();
		constructTabPane();

		constructSplitPanes();

		initFileChoosers();

		batchConvertDialog = new BatchConvertDialog();
		settingsDialog = new SettingsDialog();
		newProjectDialog = new NewProjectDialog();

		FileHandle list = Gdx.files.internal("modules.xml");
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element root = xmlReader.parse(list);
		WrapperRegistry.map.clear();
		moduleListPopup = new ModuleListPopup(root);

		colorPicker = new ColorPicker();
		colorPicker.padTop(32);
		colorPicker.padLeft(16);
		colorPicker.setHeight(330);
		colorPicker.setWidth(430);
		colorPicker.padRight(26);
	}

	public Stage getStage () {
		return stage;
	}

	private void defaults () {
		fullScreenTable.top().left();
	}

	public void fileDrop (String[] paths, float x, float y) {
		// let's see what this can mean by extension
		for(String path: paths) {
			FileHandle handle = Gdx.files.absolute(path);
			if(handle.exists()) {
				String extension = handle.extension();
				if(extension.equals("tls")) {
					// load project file
					TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
					TalosMain.Instance().ProjectController().loadProject(handle);
				} else {
					// notify talos first
					if(TalosMain.Instance().ProjectController().getProject() == ProjectController.TLS) {
						if (handle.extension().equals("p")) {
							TalosMain.Instance().ProjectController().loadFromExportedP(handle);
						} else {
							AssetFileDroppedEvent event = Notifications.obtainEvent(AssetFileDroppedEvent.class);
							event.setFileHandle(handle);
							event.setScreenPos(Gdx.input.getX(), Gdx.input.getY());
							Notifications.fireEvent(event);
						}
					}
					// ask addons if they are interested
					IAddon addon = TalosMain.Instance().Addons().projectFileDrop(handle);
					if (addon != null) {
						continue;
					}
				}
			}
		}

		if(previewWidget.getStage() != null) {
			previewWidget.fileDrop(x, y, paths);
		}
	}

	private void constructMenu () {
		mainMenu = new MainMenu(this);
		mainMenu.build();
		fullScreenTable.add(mainMenu).growX();
	}


	private void constructTabPane() {
		tabbedPane = new TabbedPane();
		fullScreenTable.row();
		fullScreenTable.add(tabbedPane.getTable()).left().expandX().fillX().growX();

		tabbedPane.addListener(new TabbedPaneListener() {
			@Override
			public void switchedTab(Tab tab) {
				TalosMain.Instance().ProjectController().loadFromTab((FileTab) tab);
			}

			@Override
			public void removedTab(Tab tab) {
				TalosMain.Instance().ProjectController().removeTab((FileTab) tab);
				if(tabbedPane.getTabs().size == 0) {
					TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
				}
			}

			@Override
			public void removedAllTabs() {

			}
		});
	}

	public PopupMenu createModuleListPopup() {
		OrthographicCamera cam = (OrthographicCamera) TalosMain.Instance().NodeStage().getStage().getCamera();
		Vector2 location = new Vector2(cam.position.x, cam.position.y);

		PopupMenu menu = new PopupMenu();
		Array<Class> temp = new Array<>();
		for (Class registeredModule : ParticleEmitterDescriptor.getRegisteredModules()) {
			temp.add(registeredModule);
		}
		temp.sort(new Comparator<Class>() {
			@Override
			public int compare (Class o1, Class o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});
		for(final Class clazz : temp) {
			String className = clazz.getSimpleName();
			MenuItem menuItem = new MenuItem(className);
			menu.addItem(menuItem);

			final Vector2 finalLocation = location;
			menuItem.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					TalosMain.Instance().NodeStage().moduleBoardWidget.createModule( clazz, finalLocation.x, finalLocation.y);
				}
			});
		}

		return menu;
	}

	public void createModuleListAdvancedPopup(Vector2 location) {
		moduleListPopup.showPopup(stage, location);
	}

	private void initFileChoosers() {
		fileChooser = new FileChooser(FileChooser.Mode.SAVE);
		fileChooser.setBackground(skin.getDrawable("window-noborder"));
	}


	public void newProjectAction() {
		TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
	}

	public void openProjectAction(final IProject projectType) {
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Open", projectType);
		if(defaultLocation.equals("")) {
			TalosMain.Instance().ProjectController().getLastDir("Save", projectType);
		}
		fileChooser.setDirectory(defaultLocation);

		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(projectType.getExtension());
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				TalosMain.Instance().ProjectController().setProject(projectType);
				TalosMain.Instance().ProjectController().loadProject(Gdx.files.absolute(path));
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}

	public void openProjectAction() {
		openProjectAction(ProjectController.TLS);
	}

	public void saveProjectAction() {
		if(!TalosMain.Instance().ProjectController().isBoundToFile()) {
			saveAsProjectAction();
		} else {
			TalosMain.Instance().ProjectController().saveProject();
		}
	}
	public void exportAction() {
		String path = TalosMain.Instance().ProjectController().getExportPath();
		if(path == null || path.isEmpty()) {
			exportAsAction();
		} else {
			TalosMain.Instance().ProjectController().exportProject(Gdx.files.absolute(path));
		}
	}

	public void exportAsAction() {
		IProject projectType = TalosMain.Instance().ProjectController().getProject();
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Export", projectType);
		fileChooser.setDirectory(defaultLocation);

		final String ext = projectType.getExportExtension();

		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(ext)) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ext;
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().ProjectController().exportProject(handle);
			}
		});

		fileChooser.setDefaultFileName(TalosMain.Instance().ProjectController().getCurrentExportNameSuggestion());

		stage.addActor(fileChooser.fadeIn());
	}

	public void saveAsProjectAction() {
		IProject projectType = TalosMain.Instance().ProjectController().getProject();
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Save", projectType);
		fileChooser.setDirectory(defaultLocation);

		final String ext = projectType.getExtension();
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(ext)) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ext;
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().ProjectController().saveProject(handle);
			}
		});

		fileChooser.setDefaultFileName(TalosMain.Instance().ProjectController().currentTab.getFileName());

		stage.addActor(fileChooser.fadeIn());
	}

	public void openDialog(VisWindow dialog) {
		stage.addActor(dialog.fadeIn());
	}


	public void legacyImportAction() {
		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileChooser.DefaultFileFilter(fileChooser));
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				TalosMain.Instance().TalosProject().importFromLegacyFormat(file.get(0));
				TalosMain.Instance().ProjectController().unbindFromFile();
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}

	public void legacyBatchConvertAction() {
		// show dialog for batch convert
		stage.addActor((batchConvertDialog.fadeIn()));
	}


	private void constructSplitPanes () {

		Table layoutContainer = new Table();
		mainLayout = new Table();
		customLayout = new Table();
		customLayout.setVisible(false);
		Stack stack = new Stack(mainLayout, customLayout);
		layoutContainer.add(stack).grow();

		previewWidget = new PreviewWidget();

		emitterList = new EmitterList(skin);

		Table midTable = new Table();
		bottomTable = new Table();
		bottomTable.setSkin(skin);
		bottomTable.setBackground(skin.getDrawable("button-main-menu"));

		bottomContainer = new Table();
		Table libraryContainer = new Table();

		libraryContainer.addListener(new ClickListener(0) { //Quick hack for library container intercepting touch as its an empty table currently
			@Override
			public void clicked (InputEvent event, float x, float y) {
			}
		});
		libraryContainer.addListener(new ClickListener(1) { //Quick hack for library container intercepting touch as its an empty table currently
			@Override
			public void clicked (InputEvent event, float x, float y) {
			}
		});
		libraryContainer.setTouchable(Touchable.enabled);
		bottomPane = new VisSplitPane(bottomContainer, libraryContainer, false);
		bottomPane.setSplitAmount(1f); // remove this line when the bottom-right panel content will be implemented (which is the library container)
		bottomContainer.add(emitterList).grow().expand().fill();
		bottomTable.add(bottomPane).expand().grow();

		verticalPane = new VisSplitPane(midTable, bottomTable, true);
		verticalPane.setMaxSplitAmount(0.70f);
		verticalPane.setMinSplitAmount(0.3f);
		verticalPane.setSplitAmount(0.7f);

		leftTable = new Table();
		leftTable.setSkin(skin);
		leftTable.add(previewWidget).grow();
		rightTable = new Table(); rightTable.setSkin(skin);
		rightTable.add().grow();
		horizontalPane = new VisSplitPane(leftTable, rightTable, false);
		midTable.add(horizontalPane).expand().grow().fill();
		horizontalPane.setMaxSplitAmount(0.8f);
		horizontalPane.setMinSplitAmount(0.2f);
		horizontalPane.setSplitAmount(0.3f);

		fullScreenTable.row();
		fullScreenTable.add(layoutContainer).grow();

		mainLayout.add(verticalPane).grow();
	}

	public void showCustomLayout(Table table) {
		mainLayout.setVisible(false);
		customLayout.setVisible(true);

		customLayout.clearChildren();
		customLayout.add(table).grow();
	}

	public void hideCustomLayout() {
		mainLayout.setVisible(true);
		customLayout.setVisible(false);
	}

	public void swapToAddonContent(Table left, Table right, Table bottom) {
		leftTable.clearChildren();
		rightTable.clearChildren();
		bottomTable.clearChildren();
		TalosMain.Instance().disableNodeStage();

		leftTable.add(left).grow();
		rightTable.add(right).grow();
		bottomTable.add(bottom).expand().grow();

		if(left == null && right == null && bottom != null) {
			horizontalPane.setVisible(false);
		}

		if(left == null && right == null && bottom == null) {
			horizontalPane.setVisible(false);
			verticalPane.setVisible(false);
		}

	}

	public void swapToTalosContent() {
		hideCustomLayout();
		verticalPane.setVisible(true);
		horizontalPane.setVisible(true);

		leftTable.clearChildren();
		rightTable.clearChildren();
		bottomTable.clearChildren();

		leftTable.add(previewWidget).grow();
		bottomTable.add(bottomPane).expand().grow();
		TalosMain.Instance().enableNodeStage();

		TalosMain.Instance().UIStage().getStage().setKeyboardFocus(rightTable);
		hideCustomLayout();

		mainMenu.restore();
	}


	public void initExampleList (PopupMenu examples) {
		FileHandle list = Gdx.files.internal("samples/list.xml");
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element root = xmlReader.parse(list);
		Array<XmlReader.Element> samples = root.getChildrenByName("sample");
		for (XmlReader.Element sample : samples) {
			String name = sample.getAttribute("name");
			final String fileName = sample.getAttribute("file");
			MenuItem item = new MenuItem(name);
			examples.addItem(item);

			item.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					//openProject(fileName);
					TalosMain.Instance().ProjectController().lastDirTrackingDisable();
					TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
					TalosMain.Instance().ProjectController().loadProject(Gdx.files.internal("samples/" + fileName));
					TalosMain.Instance().ProjectController().lastDirTrackingEnable();
					TalosMain.Instance().ProjectController().unbindFromFile();
				}
			});
		}
	}

	public EmitterList Timeline() {
		return emitterList;
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void setEmitters (Array<ParticleEmitterWrapper> emitterWrappers) {
		emitterList.setEmitters(emitterWrappers);
	}

	public void showColorPicker(ColorPickerListener listener) {
		colorPicker.setListener(listener);
		TalosMain.Instance().UIStage().getStage().addActor(colorPicker.fadeIn());
	}

	public void showColorPicker(Color color, ColorPickerListener listener) {
		colorPicker.setColor(color);
		colorPicker.setListener(listener);
		TalosMain.Instance().UIStage().getStage().addActor(colorPicker.fadeIn());
	}

	public PreviewWidget PreviewWidget() {
		return previewWidget;
	}

	public MainMenu Menu() {
		return mainMenu;
	}

	public Skin getSkin() {
		return skin;
	}

	public void showSaveFileChooser(String extension, FileChooserAdapter listener) {
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(extension);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(listener);

		stage.addActor(fileChooser.fadeIn());
	}

	public void showFileChooser(String extension, FileChooserAdapter listener) {
		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(extension);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(listener);

		stage.addActor(fileChooser.fadeIn());
	}

	public void setFileChooserVisibility(boolean visible) {
		fileChooser.setVisible(visible);
	}

	public void showYesNoDialog (String title, String message, Runnable yes, Runnable no) {
		YesNoDialog yesNoDialog = new YesNoDialog(title, message, yes, no);
		stage.addActor(yesNoDialog.fadeIn());
	}
}
