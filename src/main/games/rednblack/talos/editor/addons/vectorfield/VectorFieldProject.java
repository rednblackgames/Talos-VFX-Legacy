package games.rednblack.talos.editor.addons.vectorfield;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import games.rednblack.talos.editor.project.IProject;

import java.io.StringWriter;

public class VectorFieldProject implements IProject {

    private final VectorFieldAddon addon;

    public VectorFieldProject (VectorFieldAddon addon) {
        this.addon = addon;
    }

    @Override
    public void loadProject (FileHandle projectFileHandle, String data, boolean fromMemory) {
        Json json = new Json();
        JsonValue root = new JsonReader().parse(data);

        addon.getWorkspace().read(json, root.get("workspace"));
    }

    @Override
    public String getProjectString (boolean toMemory) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        StringWriter writer = new StringWriter();
        json.setWriter(new JsonWriter(writer));

        json.writeObjectStart();
        json.writeValue("workspace", addon.getWorkspace());
        json.writeObjectEnd();

        return writer.toString();
    }

    @Override
    public void resetToNew () {
        addon.getWorkspace().initField(16, 16);
    }

    @Override
    public String getExtension () {
        return ".tvf";
    }

    @Override
    public String getExportExtension () {
        return ".fga";
    }

    @Override
    public String getProjectNameTemplate () {
        return "VectorField";
    }

    @Override
    public void initUIContent () {
        addon.initUIContent();
    }

    @Override
    public FileHandle findFileInDefaultPaths (String fileName) {
        return null;
    }

    @Override
    public Array<String> getSavedResourcePaths () {
        return null;
    }

    @Override
    public String exportProject () {
        return addon.getWorkspace().exportFGA();
    }

    @Override
    public String getProjectTypeName () {
        return "Vector Field";
    }

    @Override
    public boolean requiresWorkspaceLocation () {
        return false;
    }

    @Override
    public void createWorkspaceEnvironment (String path, String name) {
    }
}
