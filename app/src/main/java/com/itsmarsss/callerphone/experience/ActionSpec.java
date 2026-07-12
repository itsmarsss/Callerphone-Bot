package com.itsmarsss.callerphone.experience;

/**
 * Presenter-described CTA. Style and primacy are semantic; the renderer enforces
 * Discord row limits and button label length.
 */
public record ActionSpec(
        String componentId,
        String label,
        Style style,
        boolean primary,
        boolean disabled
) {
    public enum Style {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        LINK
    }

    public static ActionSpec primary(String componentId, String label) {
        return new ActionSpec(componentId, label, Style.PRIMARY, true, false);
    }

    public static ActionSpec secondary(String componentId, String label) {
        return new ActionSpec(componentId, label, Style.SECONDARY, false, false);
    }

    public static ActionSpec success(String componentId, String label) {
        return new ActionSpec(componentId, label, Style.SUCCESS, false, false);
    }

    public static ActionSpec danger(String componentId, String label) {
        return new ActionSpec(componentId, label, Style.DANGER, false, false);
    }

    public ActionSpec asDisabled() {
        return new ActionSpec(componentId, label, style, primary, true);
    }
}
