package games.rednblack.talos.editor.notifications.events;

import games.rednblack.talos.editor.nodes.NodeBoard;
import games.rednblack.talos.editor.notifications.Notifications;

public class NodeConnectionCreatedEvent implements Notifications.Event {

    private NodeBoard.NodeConnection connection;

    @Override
    public void reset () {
        connection = null;
    }

    public NodeConnectionCreatedEvent set(NodeBoard.NodeConnection connection) {
        this.connection = connection;

        return this;
    }

    public NodeBoard.NodeConnection getConnection() {
        return connection;
    }
}
