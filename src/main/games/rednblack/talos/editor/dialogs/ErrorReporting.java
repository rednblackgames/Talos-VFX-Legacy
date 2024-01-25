package games.rednblack.talos.editor.dialogs;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.talos.TalosMain;

public class ErrorReporting {

    public boolean enabled = true;

    public void reportException(Throwable e) {
        e.printStackTrace();
        if(enabled) {
            Dialogs.showErrorDialog(TalosMain.Instance().UIStage().getStage(), "Talos just encountered an error, click details, then copy and send error developers if you dare", e);
        }
    }
}
