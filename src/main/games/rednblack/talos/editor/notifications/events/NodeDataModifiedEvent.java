package games.rednblack.talos.editor.notifications.events;

import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.talos.editor.addons.shader.nodes.ColorNode;
import games.rednblack.talos.editor.nodes.NodeWidget;
import games.rednblack.talos.editor.notifications.Notifications;

public class NodeDataModifiedEvent implements Notifications.Event {
    private NodeWidget node;

    @Override
    public void reset () {

    }

    public NodeDataModifiedEvent set (NodeWidget node) {
        this.node = node;
        return this;
    }

    public NodeWidget getNode() {
        return node;
    }
}
