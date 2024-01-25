package games.rednblack.talos.editor.notifications.events;

import games.rednblack.talos.editor.nodes.NodeWidget;
import games.rednblack.talos.editor.notifications.Notifications;

public class NodeRemovedEvent implements Notifications.Event {

    private NodeWidget node;

    @Override
    public void reset () {
        node = null;
    }

    public NodeRemovedEvent set (NodeWidget node) {
        this.node = node;
        return this;
    }

    public NodeWidget getNode() {
        return node;
    }
}
