package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.Constants;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

/** Message-only bottle modals (identity chosen via buttons). */
public final class BottleModals {
    private BottleModals() {
    }

    /** New bottle, anonymous. Modal id: sendMIB-na */
    public static Modal newAnonymous() {
        return messageOnly("sendMIB-na", "Send a bottle (anonymous)");
    }

    /** New bottle, signed. Modal id: sendMIB-ns */
    public static Modal newSigned() {
        return messageOnly("sendMIB-ns", "Send a bottle (signed)");
    }

    /** Reply anonymous. Modal id: sendMIB-ra-{bottleId} */
    public static Modal replyAnonymous(String bottleId) {
        return messageOnly("sendMIB-ra-" + bottleId, "Add page (anonymous)");
    }

    /** Reply signed. Modal id: sendMIB-rs-{bottleId} */
    public static Modal replySigned(String bottleId) {
        return messageOnly("sendMIB-rs-" + bottleId, "Add page (signed)");
    }

    private static Modal messageOnly(String modalId, String title) {
        TextInput message = TextInput.create("message", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write something another Callerphone user can discover later")
                .setMinLength(Constants.MIB_MIN_PAGE_LENGTH)
                .setMaxLength(Constants.MIB_MAX_PAGE_LENGTH)
                .build();
        return Modal.create(modalId, title)
                .addComponents(Label.of("Message", message))
                .build();
    }
}
