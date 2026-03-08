package games.rednblack.talos.editor.dialogs;

import games.rednblack.talos.TalosMain;

public class ErrorReporting {

    public boolean enabled = true;

    public void reportException(Throwable e) {
        e.printStackTrace();
        if (enabled) {
            TalosDialog.showError(
                    TalosMain.Instance().UIStage().getStage(),
                    "Talos encountered an unexpected error.",
                    e
            );
        }
    }
}
