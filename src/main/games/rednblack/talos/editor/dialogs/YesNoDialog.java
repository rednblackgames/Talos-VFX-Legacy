package games.rednblack.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

public class YesNoDialog extends Dialogs.ConfirmDialog<Runnable> {

	public YesNoDialog(String title, String message, Runnable yes, Runnable no) {
		super(title, message, new String[]{"Yes", "No"}, new Runnable[]{yes, no}, new ConfirmDialogListener<Runnable>() {
			@Override
			public void result(Runnable result) {
				Gdx.app.postRunnable(result);
			}
		});

		getTitleLabel().setAlignment(Align.center);
		padTop(40);
		padBottom(12);
		padLeft(24);
		padRight(24);
		getContentTable().pad(16, 8, 16, 8);
		getButtonsTable().pad(8, 0, 0, 0);
		getButtonsTable().defaults().minWidth(90).pad(4);
	}
}
