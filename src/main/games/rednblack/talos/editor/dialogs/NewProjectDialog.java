package games.rednblack.talos.editor.dialogs;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import games.rednblack.talos.TalosMain;

public class NewProjectDialog extends TalosDialog {

    FileChooser fileChooser;
    private TextField projectNameField;
    private TextField parentPathField;
    private NewProjectListener listener;

    public NewProjectDialog() {
        super("New Project", 520);

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        buildAndFinalize();
    }

    public interface NewProjectListener {
        void create(String path, String name);
    }

    public static void show(String typeName, String projectName, NewProjectListener listener) {
        NewProjectDialog dialog = TalosMain.Instance().UIStage().newProjectDialog;
        dialog.setData(typeName, projectName, listener);
        TalosMain.Instance().UIStage().openDialog(dialog);
    }

    private void setData(String typeName, String projectName, NewProjectListener listener) {
        this.listener = listener;
        getTitleLabel().setText("New " + typeName + " Project");
        projectNameField.setText(projectName);
        parentPathField.setText(TalosMain.Instance().Prefs().getString("sceneEditorProjectsPath"));
    }

    @Override
    protected void buildContent(Table content) {
        projectNameField = new TextField("", getSkin());
        addFormRow("Project Name", projectNameField);

        parentPathField = new TextField("", getSkin());
        addBrowseRow("Project Directory", parentPathField, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showFolderSelect();
            }
        });
    }

    @Override
    protected void buildButtons(Table buttons) {
        addButton("Create", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String pathText = parentPathField.getText();
                FileHandle fileHandle = new FileHandle(pathText);
                if (!fileHandle.exists()) {
                    showFolderSelect();
                } else {
                    save();
                }
            }
        });
    }

    private void save() {
        if (listener != null) {
            listener.create(parentPathField.getText(), projectNameField.getText());
        }
        remove();
    }

    private void showFolderSelect() {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> file) {
                parentPathField.setText(file.get(0).path());
            }
        });

        getStage().addActor(fileChooser.fadeIn());
    }
}
