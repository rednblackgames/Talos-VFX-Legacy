package games.rednblack.talos.editor.notifications.events;

import games.rednblack.talos.editor.notifications.Notifications;

public class ProjectSavedEvent implements Notifications.Event {

    String projectName;

    public ProjectSavedEvent set(String projectName) {
        this.projectName = projectName;

        return this;
    }

    @Override
    public void reset () {
        projectName = null;
    }
}
