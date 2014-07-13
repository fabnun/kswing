package com.michaelbaranov.microba.calendar;

import java.util.Locale;

/**
 * A very basic implementation of {@link CalendarResources}. Used by default by {@link CalendarPane} and {@link DatePicker} classes. The resources are loaded from 'DefaultCalendarResources.properties'
 * file.
 *
 * @author Michael Baranov
 *
 */
public class DefaultCalendarResources implements CalendarResources {

    public DefaultCalendarResources() {
    }

    @Override
    public String getResource(String key, Locale locale) {
        switch (key) {
            case "key.none":
                return "Borrar";
            case "key.today":
                return "Hoy";
        }
        return null;
    }

}
