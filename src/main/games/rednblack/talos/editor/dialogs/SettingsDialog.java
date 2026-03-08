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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import games.rednblack.talos.TalosMain;

public class SettingsDialog extends TalosDialog {

    FileChooser fileChooser;

    public static final String ASSET_PATH = "assetPath";

    private ObjectMap<String, TextField> textFieldMap;

    public SettingsDialog() {
        super("Talos Preferences", 560);

        textFieldMap = new ObjectMap<>();

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        buildAndFinalize();

        for (String key : textFieldMap.keys()) {
            textFieldMap.get(key).setText(TalosMain.Instance().Prefs().getString(key));
        }
    }

    public void addPathSetting(String name, final String id) {
        TextField inputPathField = new TextField("", getSkin());
        inputPathField.setDisabled(true);
        textFieldMap.put(id, inputPathField);

        addBrowseRow(name, inputPathField, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showFolderSelect(id);
            }
        });
    }

    @Override
    protected void buildContent(Table content) {
        addPathSetting("Particle Assets Path", ASSET_PATH);
        TalosMain.Instance().Addons().announceLocalSettings(this);
    }

    @Override
    protected void buildButtons(Table buttons) {
        addButton("Save", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
            }
        });
    }

    private void showFolderSelect(final String id) {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> file) {
                textFieldMap.get(id).setText(file.get(0).path());
            }
        });

        getStage().addActor(fileChooser.fadeIn());
    }

    private void save() {
        for (String key : textFieldMap.keys()) {
            TalosMain.Instance().Prefs().putString(key, textFieldMap.get(key).getText());
        }
        TalosMain.Instance().Prefs().flush();
        close();
    }
}
