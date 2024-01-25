package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;

public class NotificationMessageHandler {

    public static void sendEventToSocket (Notifications.Event event) {
        try {
            StringWriter stringWriter = new StringWriter();
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.setWriter(stringWriter);
            json.getWriter().object();

            json.writeValue("eventType", event.getEventType());

            json.writeObjectStart("patch");
            collectDataForEvent(event, json);
            event.getMainData(json);
            json.writeObjectEnd();
            String s = stringWriter + "}";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Json collectDataForEvent (Notifications.Event event, Json json) {
        return event.getAdditionalData(json);
    }
}
