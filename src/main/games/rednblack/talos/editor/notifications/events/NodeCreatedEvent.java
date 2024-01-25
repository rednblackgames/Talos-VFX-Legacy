package games.rednblack.talos.editor.notifications.events;

import games.rednblack.talos.editor.nodes.NodeWidget;
import games.rednblack.talos.editor.notifications.Notifications;

public class NodeCreatedEvent implements Notifications.Event {

    NodeWidget node;

    public NodeCreatedEvent set(NodeWidget node) {
        this.node = node;

        return this;
    }

    @Override
    public void reset () {
        node = null;
    }
}
