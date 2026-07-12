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
        LINK,
        /** Discord Premium Apps SKU button; {@code componentId} is the SKU snowflake string. */
        PREMIUM
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

    /** Link button: {@code componentId} is the URL. */
    public static ActionSpec link(String url, String label) {
        return new ActionSpec(url, label, Style.LINK, false, false);
    }

    /** Premium Apps SKU button; Discord supplies the label. {@code skuId} is the SKU snowflake. */
    public static ActionSpec premiumSku(String skuId) {
        return new ActionSpec(skuId, "", Style.PREMIUM, false, false);
    }

    public ActionSpec asDisabled() {
        return new ActionSpec(componentId, label, style, primary, true);
    }
}
