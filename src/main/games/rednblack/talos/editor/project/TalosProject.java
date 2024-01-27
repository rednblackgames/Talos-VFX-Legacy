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

package games.rednblack.talos.editor.project;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.editor.ParticleEmitterWrapper;
import games.rednblack.talos.editor.LegacyImporter;
import games.rednblack.talos.editor.assets.TalosAssetProvider;
import games.rednblack.talos.editor.data.ModuleWrapperGroup;
import games.rednblack.talos.editor.dialogs.SettingsDialog;
import games.rednblack.talos.editor.serialization.*;
import games.rednblack.talos.editor.utils.FileUtils;
import games.rednblack.talos.editor.widgets.ui.ModuleBoardWidget;
import games.rednblack.talos.editor.wrappers.ModuleWrapper;
import games.rednblack.talos.runtime.ParticleEmitterDescriptor;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.ParticleEffectDescriptor;
import games.rednblack.talos.runtime.modules.*;
import games.rednblack.talos.runtime.serialization.ConnectionData;
import games.rednblack.talos.runtime.serialization.ExportData;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Comparator;

import static games.rednblack.talos.editor.serialization.ProjectSerializer.readTalosTLSProject;

public class TalosProject implements IProject {

	private final Comparator<ParticleEmitterWrapper> emitterComparator;
	private ProjectData projectData;
	private ProjectSerializer projectSerializer;
	private Array<ParticleEmitterWrapper> activeWrappers = new Array<>();
	private ParticleEffectInstance particleEffect;
	private ParticleEffectDescriptor particleEffectDescriptor;
	private ParticleEmitterWrapper currentEmitterWrapper;
	private LegacyImporter importer;
	private TalosAssetProvider projectAssetProvider;
	private MetaData readMetaData;

	public TalosProject() {
		projectAssetProvider = new TalosAssetProvider();

		// provide some global default values
		for(int i = 0; i < 10; i++) {
			TalosMain.Instance().globalScope.setDynamicValue(i, new Vector3(1f,1f, 1f));
		}

		projectSerializer = new ProjectSerializer();
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffectDescriptor.setAssetProvider(projectAssetProvider);
		particleEffect = new ParticleEffectInstance(particleEffectDescriptor);
		particleEffect.setScope(TalosMain.Instance().globalScope);
		particleEffect.loopable = true;

		importer = new LegacyImporter(TalosMain.Instance().NodeStage());

		emitterComparator = new Comparator<ParticleEmitterWrapper>() {
			@Override
			public int compare(ParticleEmitterWrapper o1, ParticleEmitterWrapper o2) {
				return (int)( 10f * (o1.getPosition() - o2.getPosition()));
			}
		};
	}


	public void loadProject (FileHandle projectFileHandle, String data, boolean fromMemory) {
		TalosMain.Instance().UIStage().PreviewWidget().getGLProfiler().reset();

		cleanData();

		projectSerializer.prereadhack(data);
		projectData = readTalosTLSProject(data);
		readMetaData = projectData.metaData;

		ParticleEmitterWrapper firstEmitter = null;

		for(EmitterData emitterData: projectData.getEmitters()) {
			IntMap<ModuleWrapper> map = new IntMap<>();

			ParticleEmitterWrapper emitterWrapper = loadEmitter(emitterData.name, emitterData.sortPosition);

			TalosMain.Instance().NodeStage().moduleBoardWidget.loadEmitterToBoard(emitterWrapper, emitterData);

			final ParticleEmitterDescriptor graph = emitterWrapper.getGraph();
			for (ModuleWrapper module : emitterData.modules) {
				map.put(module.getId(), module);

				graph.addModule(module.getModule());
				module.getModule().setModuleGraph(graph);
			}

			particleEffectDescriptor.setEffectReference(particleEffect); // important
			particleEffectDescriptor.addEmitter(graph);
			particleEffect.init();
			// configure emitter visibility
			emitterWrapper.isMuted = emitterData.isMuted;
			particleEffect.getEmitter(emitterWrapper.getGraph()).setVisible(!emitterData.isMuted);
			// time to load groups here
			for(GroupData group: emitterData.groups) {
				ObjectSet<ModuleWrapper> childWrappers = new ObjectSet<>();
				for(Integer id: group.modules) {
					if(map.get(id) != null) {
						childWrappers.add(map.get(id));
					}
				}
				ModuleWrapperGroup moduleWrapperGroup = TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupForWrappers(childWrappers);
				Color clr = new Color();
				Color.abgr8888ToColor(clr, group.color);
				moduleWrapperGroup.setData(group.text, clr);
			}
		}

		sortEmitters();

		if(activeWrappers.size > 0) {
			firstEmitter = activeWrappers.first();
		}

		if(firstEmitter != null) {
			TalosMain.Instance().TalosProject().setCurrentEmitterWrapper(firstEmitter);
			TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(firstEmitter);
		}

		TalosMain.Instance().UIStage().setEmitters(activeWrappers);
		TalosMain.Instance().NodeStage().getStage().setKeyboardFocus(null);
	}

	public void sortEmitters() {
		activeWrappers.sort(emitterComparator);

		// fix for older projects
		if (activeWrappers.size > 1 &&
				activeWrappers.get(0).getEmitter().getSortPosition() == 0 &&
				activeWrappers.get(1).getEmitter().getSortPosition() == 0) {
			activeWrappers.reverse();
		}

		// re-normalize position numbers
		int index = 0;
		for(ParticleEmitterWrapper wrapper: activeWrappers) {
			wrapper.getEmitter().setSortPosition(index++);
			wrapper.setPosition(wrapper.getEmitter().getSortPosition());
		}
		particleEffect.sortEmitters();
	}

	public String getProjectString (boolean toMemory) {
		projectData.setFrom(TalosMain.Instance().NodeStage().moduleBoardWidget);
		String data = projectSerializer.write(projectData);

		return data;
	}

	public void resetToNew(){
		cleanData();
		projectData = new ProjectData();
		currentEmitterWrapper = loadEmitter("default_emitter", 0);
		TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);

		TalosMain.Instance().UIStage().setEmitters(activeWrappers);
	}

	@Override
	public String getExtension() {
		return ".tls";
	}

	@Override
	public String getExportExtension() {
		return ".p";
	}

	@Override
	public String getProjectNameTemplate() {
		return "effect";
	}

	@Override
	public void initUIContent() {

	}

	@Override
	public Array<String> getSavedResourcePaths () {
		return readMetaData.getResourcePathStrings();
	}

	@Override
	public FileHandle findFileInDefaultPaths(String fileName) {
		String path = TalosMain.Instance().Prefs().getString(SettingsDialog.ASSET_PATH);

		FileHandle handle = FileUtils.findFileRecursive(path, fileName, 10);

		return handle;
	}


	public void exportProject(FileHandle handle) {
		ExportData exportData = new ExportData();
		setToExportData(exportData, TalosMain.Instance().NodeStage().moduleBoardWidget);
		handle.writeString(projectSerializer.writeTalosPExport(exportData), false);
	}

	@Override
	public String exportProject() {
		ExportData exportData = new ExportData();
		setToExportData(exportData, TalosMain.Instance().NodeStage().moduleBoardWidget);
		return projectSerializer.writeTalosPExport(exportData);
	}

	private void cleanData() {
		TalosMain.Instance().UIStage().PreviewWidget().resetToDefaults();

		TalosMain.Instance().NodeStage().moduleBoardWidget.clearAll();
		activeWrappers.clear();
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffectDescriptor.setAssetProvider(projectAssetProvider);
		particleEffect = new ParticleEffectInstance(particleEffectDescriptor);
		particleEffect.setScope(TalosMain.Instance().globalScope);
		particleEffect.loopable = true;
	}

	public ParticleEffectInstance getParticleEffect () {
		return particleEffect;
	}

	private ParticleEmitterWrapper initEmitter (String emitterName) {
		ParticleEmitterWrapper emitterWrapper = new ParticleEmitterWrapper();
		emitterWrapper.setName(emitterName);

		ParticleEmitterDescriptor moduleGraph = TalosMain.Instance().TalosProject().particleEffectDescriptor.createEmitterDescriptor();
		emitterWrapper.setModuleGraph(moduleGraph);

		//particleEffect.addAdvancedEmitter(moduleGraph);
		particleEffect.addEmitter(moduleGraph);

		return emitterWrapper;
	}


	public ParticleEmitterWrapper createNewEmitter (String emitterName, float sortPosition) {
		ParticleEmitterWrapper emitterWrapper = initEmitter(emitterName);
		activeWrappers.add(emitterWrapper);
		currentEmitterWrapper = emitterWrapper;

		emitterWrapper.setPosition(sortPosition);
		sortEmitters();

		TalosMain.Instance().ProjectController().setDirty();

		TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);

		return emitterWrapper;
	}

	public ParticleEmitterWrapper loadEmitter(String emitterName, int sortPosition) {
		ParticleEmitterWrapper emitterWrapper = initEmitter(emitterName);
		activeWrappers.add(emitterWrapper);
		currentEmitterWrapper = emitterWrapper;

		emitterWrapper.getEmitter().setSortPosition(sortPosition);
		emitterWrapper.setPosition(sortPosition);

		TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);

		return emitterWrapper;
	}


	public void addEmitter (ParticleEmitterWrapper emitterWrapper) {
		activeWrappers.add(emitterWrapper);
	}

	public void removeEmitter (ParticleEmitterWrapper wrapper) {
		particleEffect.removeEmitterForEmitterDescriptor(wrapper.getEmitter());
		particleEffectDescriptor.removeEmitter(wrapper.getEmitter());

		activeWrappers.removeValue(wrapper, true);
		if (activeWrappers.size > 0) {
			currentEmitterWrapper = activeWrappers.peek();
		} else {
			currentEmitterWrapper = null;
		}
		TalosMain.Instance().NodeStage().onEmitterRemoved(wrapper);
		TalosMain.Instance().UIStage().setEmitters(activeWrappers);

	}

	public void setCurrentEmitterWrapper (ParticleEmitterWrapper emitterWrapper) {
		this.currentEmitterWrapper = emitterWrapper;
	}

	public ParticleEmitterWrapper getCurrentEmitterWrapper () {
		return currentEmitterWrapper;
	}

	public ParticleEmitterDescriptor getCurrentModuleGraph () {
		return currentEmitterWrapper.getGraph();
	}

	public TalosAssetProvider getProjectAssetProvider () {
		return projectAssetProvider;
	}

	public String getLocalPath() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
					.toURI()).getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void importFromLegacyFormat(FileHandle fileHandle) {
		cleanData();
		importer.read(fileHandle);
	}

	private void setToExportData (ExportData data, ModuleBoardWidget moduleBoardWidget) {
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

		for (ParticleEmitterWrapper key : moduleWrappers.keys()) {
			final ExportData.EmitterExportData emitterData = new ExportData.EmitterExportData();
			emitterData.name = key.getName();
			for (ModuleWrapper wrapper : moduleWrappers.get(key)) {
				emitterData.modules.add(wrapper.getModule());

				if (wrapper.getModule() instanceof TextureModule) {
					TextureModule textureModule = (TextureModule)wrapper.getModule();
					String name = textureModule.regionName;
					if (name == null)
						name = "fire";

					if (!data.metadata.resources.contains(name, false)) {
						data.metadata.resources.add(name);
					}
				}
				if (wrapper.getModule() instanceof PolylineModule) {
					PolylineModule module = (PolylineModule)wrapper.getModule();
					String name = module.regionName;
					if (name == null)
						name = "fire";

					if (!data.metadata.resources.contains(name, false)) {
						data.metadata.resources.add(name);
					}
				}
				if (wrapper.getModule() instanceof VectorFieldModule) {
					VectorFieldModule vectorFieldModule = (VectorFieldModule) wrapper.getModule();
					String fgaFileName = vectorFieldModule.fgaFileName;

					if (fgaFileName == null) {
						return;
					}
					fgaFileName = fgaFileName + ".fga";
					if (!data.metadata.resources.contains(fgaFileName, false)) {
						data.metadata.resources.add(fgaFileName);
					}
				}
				if (wrapper.getModule() instanceof ShadedSpriteModule) {
					ShadedSpriteModule shadedSpriteModule = (ShadedSpriteModule) wrapper.getModule();
					String shaderName = shadedSpriteModule.shdrFileName;

					if (shaderName == null) {
						continue;
					}

					if (!data.metadata.resources.contains(shaderName, false)) {
						data.metadata.resources.add(shaderName);
					}
				}
			}

			final Array<ModuleBoardWidget.NodeConnection> nodeConns = nodeConnections.get(key);
			if (nodeConns != null) {
				for (ModuleBoardWidget.NodeConnection nodeConn : nodeConns) {
					emitterData.connections.add(new ConnectionData(nodeConn.fromModule.getModule().getIndex(), nodeConn.toModule.getModule().getIndex(), nodeConn.fromSlot, nodeConn.toSlot));
				}
			}

			data.emitters.add(emitterData);
		}

	}

	public Array<ParticleEmitterWrapper> getActiveWrappers() {
		return activeWrappers;
	}

	public float estimateTotalEffectDuration() {
		Array<ParticleEmitterWrapper> activeWrappers = getActiveWrappers();

		if (particleEffectDescriptor.isContinuous()) {
			float maxWindow = 0;
			for (ParticleEmitterWrapper wrapper : activeWrappers) {
				if(wrapper.getEmitter().getEmitterModule() != null) {
					float duration = wrapper.getEmitter().getEmitterModule().getDuration();

					float totalWaitTime = duration;

					if (maxWindow < totalWaitTime) {
						maxWindow = totalWaitTime;
					}
				}
			}

			return maxWindow;
		} else {
			float furthestPoint = 0;
			for (ParticleEmitterWrapper wrapper : activeWrappers) {
				if(wrapper.getEmitter().getEmitterModule() != null && wrapper.getEmitter().getParticleModule() != null) {
					float delay = wrapper.getEmitter().getEmitterModule().getDelay();
					float duration = wrapper.getEmitter().getEmitterModule().getDuration();
					float life = wrapper.getEmitter().getParticleModule().getLife();

					float point = delay + duration + life;

					if (furthestPoint < point) {
						furthestPoint = point;
					}
				}
			}

			return furthestPoint;
		}
	}

	@Override
	public String getProjectTypeName () {
		return "Talos";
	}

	@Override
	public boolean requiresWorkspaceLocation () {
		return false;
	}

    @Override
    public void createWorkspaceEnvironment (String path, String name) {

    }
}
