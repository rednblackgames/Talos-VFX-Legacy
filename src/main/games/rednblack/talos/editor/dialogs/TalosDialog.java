package games.rednblack.talos.editor.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import games.rednblack.talos.editor.utils.MsdfFonts;

/**
 * Base dialog class providing consistent styling for all Talos dialogs.
 * Subclasses build their UI inside {@link #buildContent(Table)}.
 *
 * Also provides static factory methods for common dialog patterns:
 * {@link #showInfo}, {@link #showConfirm}, {@link #showError}.
 */
public abstract class TalosDialog extends VisWindow {

    private static final float CONTENT_PAD = 20f;
    private static final float CONTENT_PAD_TOP = 15f;
    private static final float CONTENT_PAD_BOTTOM = 10f;
    private static final float ROW_SPACING = 10f;
    private static final float LABEL_WIDTH = 200f;
    private static final float BUTTON_MIN_WIDTH = 90f;
    private static final float BUTTON_PAD = 6f;

    protected Table contentTable;
    private Table buttonBar;

    public TalosDialog(String title) {
        this(title, 480f);
    }

    public TalosDialog(String title, float minWidth) {
        super(title);

        setCenterOnAdd(true);
        setModal(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        getTitleLabel().setAlignment(Align.center);
        padTop(40);

        // Main content area with consistent padding
        contentTable = new Table();
        contentTable.top().left();
        contentTable.defaults().left();
        add(contentTable).pad(CONTENT_PAD_TOP, CONTENT_PAD, CONTENT_PAD_BOTTOM, CONTENT_PAD).grow().minWidth(minWidth - CONTENT_PAD * 2);
        row();

        // Separator line above button bar
        Drawable sepDrawable = getSkin().getDrawable("separator-menu");
        if (sepDrawable != null) {
            Table sep = new Table();
            sep.setBackground(sepDrawable);
            add(sep).growX().height(1).padLeft(10).padRight(10).padTop(4).padBottom(4);
            row();
        }

        // Button bar
        buttonBar = new Table();
        buttonBar.defaults().minWidth(BUTTON_MIN_WIDTH).pad(BUTTON_PAD);
        add(buttonBar).right().padRight(CONTENT_PAD).padBottom(14).padTop(4);
        row();
    }

    /**
     * Must be called at the end of subclass constructors to trigger content building.
     */
    protected void buildAndFinalize() {
        buildContent(contentTable);
        buildButtons(buttonBar);
        pack();
        invalidate();
        centerWindow();
    }

    /**
     * Subclasses build their main content here.
     */
    protected abstract void buildContent(Table content);

    /**
     * Subclasses add their action buttons here. Default adds nothing.
     */
    protected void buildButtons(Table buttons) {
    }

    /**
     * Creates a form row: label on the left, field on the right.
     * Adds the row to contentTable with proper spacing.
     */
    protected Table addFormRow(String label, Actor field) {
        return addFormRow(contentTable, label, field);
    }

    /**
     * Creates a form row inside a specific table.
     */
    protected Table addFormRow(Table target, String label, Actor field) {
        Table rowTable = new Table();
        rowTable.add(MsdfFonts.label(label)).width(LABEL_WIDTH).left().padRight(12);
        rowTable.add(field).growX().left().minWidth(200);
        target.add(rowTable).growX().padTop(ROW_SPACING);
        target.row();
        return rowTable;
    }

    /**
     * Creates a form row with a browse button: label, text field, and browse button.
     */
    protected Table addBrowseRow(String label, TextField field, ClickListener browseAction) {
        Table fieldGroup = new Table();
        fieldGroup.add(field).growX().minWidth(200);
        TextButton browseBtn = new TextButton("Browse", getSkin());
        fieldGroup.add(browseBtn).padLeft(8).minWidth(80);
        browseBtn.addListener(browseAction);
        return addFormRow(label, fieldGroup);
    }

    /**
     * Adds a styled action button to the button bar.
     */
    protected TextButton addButton(String text, ClickListener listener) {
        return addButton(text, "default", listener);
    }

    /**
     * Adds a styled action button with a specific skin style.
     */
    protected TextButton addButton(String text, String style, ClickListener listener) {
        TextButton button = new TextButton(text, getSkin(), style);
        button.addListener(listener);
        buttonBar.add(button);
        return button;
    }

    // -----------------------------------------------------------------------
    //  Static factory methods for common one-shot dialogs
    // -----------------------------------------------------------------------

    /**
     * Shows a styled informational dialog with an OK button.
     */
    public static void showInfo(Stage stage, String title, String message) {
        QuickDialog dialog = new QuickDialog(title, message, 420f);
        dialog.addButton("OK", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
            }
        });
        dialog.pack();
        dialog.invalidate();
        dialog.centerWindow();
        stage.addActor(dialog.fadeIn());
    }

    /**
     * Shows a styled confirmation dialog with Yes / No buttons.
     */
    public static void showConfirm(Stage stage, String title, String message, Runnable onYes, Runnable onNo) {
        QuickDialog dialog = new QuickDialog(title, message, 460f);
        dialog.addButton("Yes", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
                if (onYes != null) onYes.run();
            }
        });
        dialog.addButton("No", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
                if (onNo != null) onNo.run();
            }
        });
        dialog.pack();
        dialog.invalidate();
        dialog.centerWindow();
        stage.addActor(dialog.fadeIn());
    }

    /**
     * Shows a styled save-confirm dialog with Save / Don't Save / Cancel buttons.
     * Used for unsaved changes prompts.
     */
    public static void showSaveConfirm(Stage stage, String title, String message,
                                        Runnable onSave, Runnable onDontSave, Runnable onCancel) {
        QuickDialog dialog = new QuickDialog(title, message, 480f);
        dialog.addButton("Save", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
                if (onSave != null) onSave.run();
            }
        });
        dialog.addButton("Don't Save", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
                if (onDontSave != null) onDontSave.run();
            }
        });
        dialog.addButton("Cancel", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
                if (onCancel != null) onCancel.run();
            }
        });
        dialog.pack();
        dialog.invalidate();
        dialog.centerWindow();
        stage.addActor(dialog.fadeIn());
    }

    /**
     * Shows a styled error dialog with the error message and optional expandable stack trace.
     */
    public static void showError(Stage stage, String message, Throwable exception) {
        QuickDialog dialog = new QuickDialog("Error", null, 520f);

        TextraLabel msgLabel = MsdfFonts.label(message);
        msgLabel.setWrap(true);
        dialog.contentTable.add(msgLabel).growX().padBottom(10);
        dialog.contentTable.row();

        if (exception != null) {
            TextraLabel detailHeader = MsdfFonts.smallLabel("Details:");
            detailHeader.setColor(new Color(0.63f, 0.63f, 0.63f, 1f));
            dialog.contentTable.add(detailHeader).left().padTop(6).padBottom(4);
            dialog.contentTable.row();

            StringBuilder sb = new StringBuilder();
            sb.append(exception.toString()).append("\n");
            for (StackTraceElement el : exception.getStackTrace()) {
                sb.append("  at ").append(el.toString()).append("\n");
            }

            TextraLabel traceLabel = MsdfFonts.smallLabel(sb.toString());
            traceLabel.setWrap(true);
            Table traceContainer = new Table();
            traceContainer.add(traceLabel).growX().top().left();

            ScrollPane scrollPane = new ScrollPane(traceContainer, dialog.getSkin(), "text-output");
            dialog.contentTable.add(scrollPane).grow().minHeight(120).maxHeight(200);
            dialog.contentTable.row();
        }

        dialog.addButton("OK", "orange", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.close();
            }
        });
        dialog.pack();
        dialog.invalidate();
        dialog.centerWindow();
        stage.addActor(dialog.fadeIn());
    }

    /**
     * Internal helper class for one-shot dialogs created via static factory methods.
     */
    private static class QuickDialog extends TalosDialog {
        private final String messageText;

        QuickDialog(String title, String messageText, float minWidth) {
            super(title, minWidth);
            this.messageText = messageText;
            buildContent(contentTable);
        }

        @Override
        protected void buildContent(Table content) {
            if (messageText != null && !messageText.isEmpty()) {
                TextraLabel label = MsdfFonts.label(messageText);
                label.setWrap(true);
                content.add(label).growX().padBottom(6);
                content.row();
            }
        }
    }
}
