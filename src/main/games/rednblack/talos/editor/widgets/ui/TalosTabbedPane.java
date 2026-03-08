package games.rednblack.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import games.rednblack.talos.editor.dialogs.TalosDialog;

/**
 * Custom TabbedPane that replaces the default vis-ui unsaved changes dialog
 * with a styled TalosDialog.
 */
public class TalosTabbedPane extends TabbedPane {

    public TalosTabbedPane() {
        super();
    }

    @Override
    public boolean remove(final Tab tab, boolean ignoreTabDirty) {
        if (ignoreTabDirty) {
            return super.remove(tab, true);
        }

        Stage stage = getTable().getStage();
        if (tab.isDirty() && stage != null) {
            TalosDialog.showSaveConfirm(stage,
                    "Unsaved Changes",
                    "Do you want to save changes before closing?",
                    () -> {
                        tab.save();
                        super.remove(tab, true);
                    },
                    () -> super.remove(tab, true),
                    null
            );
            return false;
        }

        return super.remove(tab, true);
    }
}
