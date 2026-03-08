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

package games.rednblack.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraLabel;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.utils.MsdfFonts;

import java.io.File;

public class BatchConvertDialog extends TalosDialog {

    FileChooser fileChooser;
    TextField inputPathField;
    TextField outputPathField;
    TextField inputFilterField;
    List<TextraLabel> logArea;

    String outputPath;
    Array<String> fileList = new Array<>();
    Array<TextraLabel> logItems = new Array<>();

    ScrollPane scrollPane;

    boolean isConverting = false;

    public BatchConvertDialog() {
        super("Batch Convert Legacy Effects", 700);
        setResizable(true);

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        buildAndFinalize();
        setSize(700, 450);
        centerWindow();
    }

    @Override
    protected void buildContent(Table content) {
        // Input folder row with extension filter
        inputPathField = new TextField("", getSkin());
        inputFilterField = new TextField("p", getSkin());

        Table inputFieldGroup = new Table();
        inputFieldGroup.add(inputPathField).growX().minWidth(200);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputFieldGroup.add(browseInputBtn).padLeft(8).minWidth(80);
        inputFieldGroup.add(MsdfFonts.label("  Ext:")).padLeft(12);
        inputFieldGroup.add(inputFilterField).width(50).padLeft(6);
        addFormRow("Input Folder", inputFieldGroup);

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showFolderSelect(inputPathField);
            }
        });

        // Output folder row
        outputPathField = new TextField("", getSkin());
        addBrowseRow("Output Folder", outputPathField, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showFolderSelect(outputPathField);
            }
        });

        // Log area
        content.add(MsdfFonts.smallLabel("Conversion Log:")).left().padTop(16).padBottom(4);
        content.row();

        logArea = new List<>(getSkin());
        scrollPane = new ScrollPane(logArea, getSkin(), "text-output");
        content.add(scrollPane).grow().minHeight(150).padTop(4);
        content.row();
    }

    @Override
    protected void buildButtons(Table buttons) {
        addButton("Convert", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startConversion();
            }
        });
    }

    private void showFolderSelect(final TextField pathField) {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> file) {
                pathField.setText(file.get(0).path());
            }
        });

        getStage().addActor(fileChooser.fadeIn());
    }

    private void startConversion() {
        String inputPath = inputPathField.getText();
        outputPath = outputPathField.getText();
        String extension = inputFilterField.getText();

        fileList.clear();

        FileHandle input = Gdx.files.absolute(inputPath);

        if (input.isDirectory() && input.exists()) {
            traverseFolder(input, fileList, extension, 0);
        }

        isConverting = true;
        logArea.clearItems();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (fileList.size == 0) isConverting = false;

        if (isConverting) {
            String path = fileList.pop();
            FileHandle fileHandle = Gdx.files.absolute(path);
            convertOne(fileHandle);
        }
    }

    private void convertOne(FileHandle fileHandle) {
        String subPath;

        if (inputPathField.getText().length() == fileHandle.parent().path().length()) {
            subPath = File.separator;
        } else {
            subPath = fileHandle.parent().path().substring(inputPathField.getText().length() + 1) + File.separator;
        }
        String projectPath = outputPath + File.separator + "projects" + File.separator + subPath + fileHandle.nameWithoutExtension() + ".tls";
        String runtimePath = outputPath + File.separator + "runtime" + File.separator + subPath + fileHandle.nameWithoutExtension() + ".p";

        FileHandle projectDestination = Gdx.files.absolute(projectPath);
        FileHandle exportDestination = Gdx.files.absolute(runtimePath);

        String result = "ok";
        try {
            TalosMain.Instance().TalosProject().importFromLegacyFormat(fileHandle);
            TalosMain.Instance().ProjectController().saveProject(projectDestination);
            TalosMain.Instance().TalosProject().exportProject(exportDestination);
        } catch (Exception e) {
            result = "nok";
        }

        String text = "converting: " + fileHandle.name() + "        " + result + "\n";

        logItems.add(MsdfFonts.label(text));
        logArea.setItems(logItems);
        TextraLabel lbl = logArea.getItems().get(logArea.getItems().size - 1);
        logArea.setSelected(lbl);
        scrollPane.layout();
        scrollPane.scrollTo(0, 0, 0, 0);
    }

    private void traverseFolder(FileHandle folder, Array<String> fileList, String extension, int depth) {
        for (FileHandle file : folder.list()) {
            if (file.isDirectory() && depth < 10) {
                traverseFolder(file, fileList, extension, depth + 1);
            }
            if (file.extension().equals(extension)) {
                fileList.add(file.path());
            }
        }
    }
}
