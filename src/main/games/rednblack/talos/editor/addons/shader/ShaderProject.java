package games.rednblack.talos.editor.addons.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import games.rednblack.talos.editor.addons.shader.workspace.ShaderNodeStage;
import games.rednblack.talos.editor.nodes.DynamicNodeStage;
import games.rednblack.talos.editor.project.IProject;

public class ShaderProject implements IProject {

    ShaderAddon shaderAddon;

    public ShaderProject(ShaderAddon shaderAddon) {
        this.shaderAddon = shaderAddon;
    }

    @Override
    public void loadProject (FileHandle projectFileHandle, String data, boolean fromMemory) {
        Json json = new Json();
        ProjectData projectData = json.fromJson(ProjectData.class, data);
        projectData.loadStage(shaderAddon.nodeStage);
    }

    @Override
    public String getProjectString (boolean toMemory) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        ProjectData projectData = new ProjectData();
        projectData.nodeStage = shaderAddon.nodeStage;

        String data = json.prettyPrint(projectData);

        return data;
    }

    @Override
    public void resetToNew () {
        shaderAddon.nodeStage.reset();
    }

    @Override
    public String getExtension () {
        return ".tsh";
    }

    @Override
    public String getExportExtension () {
        return ".shdr";
    }

    @Override
    public String getProjectNameTemplate () {
        return "Shader";
    }

    @Override
    public void initUIContent () {
        shaderAddon.initUIContent();
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
        return ((ShaderNodeStage)(shaderAddon.nodeStage)).getShaderData();
    }

    public void getNodeStage () {

    }

    private static class ProjectData implements Json.Serializable {

        public ProjectMetadata projectMetadata;
        public DynamicNodeStage nodeStage;

        private JsonValue nodeStageData;

        @Override
        public void write (Json json) {
            json.writeValue("metadata", projectMetadata);
            json.writeValue("nodes", nodeStage);
        }

        @Override
        public void read (Json json, JsonValue jsonValue) {
            projectMetadata = new ProjectMetadata();
            projectMetadata.read(json, jsonValue.get("metadata"));
            nodeStageData = jsonValue.get("nodes");
        }

        public void loadStage (DynamicNodeStage nodeStage) {
            Json json = new Json();
            nodeStage.read(json, nodeStageData);
        }
    }

    private static class ProjectMetadata implements Json.Serializable {

        @Override
        public void write (Json json) {

        }

        @Override
        public void read (Json json, JsonValue jsonValue) {

        }
    }

    @Override
    public String getProjectTypeName () {
        return "Shader Graph";
    }

    @Override
    public boolean requiresWorkspaceLocation () {
        return false;
    }

    @Override
    public void createWorkspaceEnvironment (String path, String name) {

    }
}
