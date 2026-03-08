package games.rednblack.talos.editor.addons.shader.dialogs;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import games.rednblack.talos.editor.addons.shader.workspace.ShaderNodeStage;
import games.rednblack.talos.editor.dialogs.TalosDialog;

public class ExportSequenceDialog extends TalosDialog {

    private final ShaderNodeStage nodeStage;
    FileChooser fileChooser;
    private TextField fileName;
    private TextField inputPathField;
    private TextField widthField;
    private TextField heightField;
    private TextField durationField;
    private TextField fpsField;

    public ExportSequenceDialog(ShaderNodeStage nodeStage) {
        super("Export Sequence", 520);
        this.nodeStage = nodeStage;

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        buildAndFinalize();
    }

    @Override
    protected void buildContent(Table content) {
        fileName = new TextField("sequence", getSkin());
        addFormRow("Name Prefix", fileName);

        inputPathField = new TextField("", getSkin());
        addBrowseRow("Output Directory", inputPathField, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fileChooser.setMode(FileChooser.Mode.OPEN);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> file) {
                        inputPathField.setText(file.get(0).path());
                    }
                });

                getStage().addActor(fileChooser.fadeIn());
            }
        });

        widthField = new TextField("256", getSkin());
        addFormRow("Width", widthField);

        heightField = new TextField("256", getSkin());
        addFormRow("Height", heightField);

        durationField = new TextField("1", getSkin());
        addFormRow("Duration", durationField);

        fpsField = new TextField("40", getSkin());
        addFormRow("FPS", fpsField);
    }

    @Override
    protected void buildButtons(Table buttons) {
        addButton("Export", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
            }
        });
    }

    private void save() {
        nodeStage.exportSequence(
                fileName.getText(),
                inputPathField.getText(),
                Integer.parseInt(widthField.getText()),
                Integer.parseInt(heightField.getText()),
                Float.parseFloat(durationField.getText()),
                Integer.parseInt(fpsField.getText())
        );
        close();
    }
}
