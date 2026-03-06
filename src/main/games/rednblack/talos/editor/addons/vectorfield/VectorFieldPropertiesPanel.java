package games.rednblack.talos.editor.addons.vectorfield;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import games.rednblack.talos.editor.addons.bvb.PropertyPanelContainer;
import games.rednblack.talos.editor.widgets.propertyWidgets.*;

public class VectorFieldPropertiesPanel extends PropertyPanelContainer {

    private final VectorFieldWorkspace workspace;

    private final GridProvider gridProvider;
    private final DisplayProvider displayProvider;
    private final BrushProvider brushProvider;
    private final RandomizeProvider randomizeProvider;
    private final ActionsProvider actionsProvider;

    public VectorFieldPropertiesPanel (Skin skin, VectorFieldWorkspace workspace) {
        super(skin);
        this.workspace = workspace;

        gridProvider = new GridProvider();
        displayProvider = new DisplayProvider();
        brushProvider = new BrushProvider();
        randomizeProvider = new RandomizeProvider();
        actionsProvider = new ActionsProvider();

        showPanel(gridProvider);
        showPanel(displayProvider);
        showPanel(brushProvider);
        showPanel(randomizeProvider);
        showPanel(actionsProvider);
    }

    public void syncFromWorkspace () {
        updateValues();
    }

    // --- Grid section ---
    private class GridProvider implements IPropertyProvider {
        @Override
        public Array<PropertyWidget> getListOfProperties () {
            Array<PropertyWidget> list = new Array<>();

            IntPropertyWidget gridXWidget = new IntPropertyWidget("Grid X", () -> workspace.getGridX(), value -> {
                int clamped = Math.max(2, Math.min(128, value));
                workspace.initField(clamped, workspace.getGridY());
            });
            list.add(gridXWidget);

            IntPropertyWidget gridYWidget = new IntPropertyWidget("Grid Y", () -> workspace.getGridY(), value -> {
                int clamped = Math.max(2, Math.min(128, value));
                workspace.initField(workspace.getGridX(), clamped);
            });
            list.add(gridYWidget);

            FloatPropertyWidget fieldScaleWidget = new FloatPropertyWidget("Field Scale", () -> workspace.getFieldScale(), value -> workspace.setFieldScale(value));
            fieldScaleWidget.configureFromValues(0.1f, 10f, 0.1f);
            list.add(fieldScaleWidget);

            return list;
        }

        @Override
        public String getPropertyBoxTitle () { return "Grid"; }

        @Override
        public int getPriority () { return 0; }

        @Override
        public Class<? extends IPropertyProvider> getType () { return GridProvider.class; }
    }

    // --- Display section ---
    private class DisplayProvider implements IPropertyProvider {
        @Override
        public Array<PropertyWidget> getListOfProperties () {
            Array<PropertyWidget> list = new Array<>();

            FloatPropertyWidget arrowScaleWidget = new FloatPropertyWidget("Arrow Scale", () -> workspace.getArrowScale(), value -> workspace.setArrowScale(value));
            arrowScaleWidget.configureFromValues(0.1f, 2f, 0.05f);
            list.add(arrowScaleWidget);

            CheckboxWidget colorByDirWidget = new CheckboxWidget("Color by Dir", () -> workspace.isColorByDirection(), value -> workspace.setColorByDirection(value));
            list.add(colorByDirWidget);

            return list;
        }

        @Override
        public String getPropertyBoxTitle () { return "Display"; }

        @Override
        public int getPriority () { return 1; }

        @Override
        public Class<? extends IPropertyProvider> getType () { return DisplayProvider.class; }
    }

    // --- Brush section ---
    private class BrushProvider implements IPropertyProvider {
        @Override
        public Array<PropertyWidget> getListOfProperties () {
            Array<PropertyWidget> list = new Array<>();

            Array<String> modeOptions = new Array<>();
            for (VectorFieldWorkspace.BrushMode mode : VectorFieldWorkspace.BrushMode.values()) {
                modeOptions.add(mode.name());
            }
            SelectBoxWidget brushModeWidget = new SelectBoxWidget("Mode", () -> workspace.getBrushMode().name(),
                    value -> workspace.setBrushMode(VectorFieldWorkspace.BrushMode.valueOf(value)),
                    () -> modeOptions);
            list.add(brushModeWidget);

            FloatPropertyWidget brushRadiusWidget = new FloatPropertyWidget("Radius", () -> workspace.getBrushRadius(), value -> workspace.setBrushRadius(value));
            brushRadiusWidget.configureFromValues(0.5f, 10f, 0.5f);
            list.add(brushRadiusWidget);

            FloatPropertyWidget brushStrengthWidget = new FloatPropertyWidget("Strength", () -> workspace.getBrushStrength(), value -> workspace.setBrushStrength(value));
            brushStrengthWidget.configureFromValues(0.01f, 1f, 0.01f);
            list.add(brushStrengthWidget);

            FloatPropertyWidget brushAngleWidget = new FloatPropertyWidget("Angle", () -> workspace.getBrushAngle(), value -> workspace.setBrushAngle(value));
            brushAngleWidget.configureFromValues(0f, 360f, 1f);
            list.add(brushAngleWidget);

            return list;
        }

        @Override
        public String getPropertyBoxTitle () { return "Brush"; }

        @Override
        public int getPriority () { return 2; }

        @Override
        public Class<? extends IPropertyProvider> getType () { return BrushProvider.class; }
    }

    // --- Randomize section ---
    private class RandomizeProvider implements IPropertyProvider {
        private long seed = 42;
        private float magnitude = 0.8f;
        private float frequency = 0.5f;

        @Override
        public Array<PropertyWidget> getListOfProperties () {
            Array<PropertyWidget> list = new Array<>();

            IntPropertyWidget seedWidget = new IntPropertyWidget("Seed", () -> (int) seed, value -> seed = value);
            list.add(seedWidget);

            FloatPropertyWidget magnitudeWidget = new FloatPropertyWidget("Magnitude", () -> magnitude, value -> magnitude = value);
            magnitudeWidget.configureFromValues(0.01f, 1f, 0.01f);
            list.add(magnitudeWidget);

            FloatPropertyWidget frequencyWidget = new FloatPropertyWidget("Frequency", () -> frequency, value -> frequency = value);
            frequencyWidget.configureFromValues(0.1f, 5f, 0.1f);
            list.add(frequencyWidget);

            ButtonPropertyWidget<Void> randomizeBtn = new ButtonPropertyWidget<>("Randomize", widget -> workspace.randomizeField(seed, magnitude, frequency));
            list.add(randomizeBtn);

            return list;
        }

        @Override
        public String getPropertyBoxTitle () { return "Randomize"; }

        @Override
        public int getPriority () { return 3; }

        @Override
        public Class<? extends IPropertyProvider> getType () { return RandomizeProvider.class; }
    }

    // --- Actions section ---
    private class ActionsProvider implements IPropertyProvider {
        @Override
        public Array<PropertyWidget> getListOfProperties () {
            Array<PropertyWidget> list = new Array<>();

            ButtonPropertyWidget<Void> clearBtn = new ButtonPropertyWidget<>("Clear Field", widget -> workspace.clearField());
            list.add(clearBtn);

            return list;
        }

        @Override
        public String getPropertyBoxTitle () { return "Actions"; }

        @Override
        public int getPriority () { return 4; }

        @Override
        public Class<? extends IPropertyProvider> getType () { return ActionsProvider.class; }
    }
}
