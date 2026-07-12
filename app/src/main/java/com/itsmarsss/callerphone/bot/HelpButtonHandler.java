package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

/**
 * Plan patterns §28 — category navigation edits one help message.
 */
public final class HelpButtonHandler implements IButtonInteraction {
    private final Help help = new Help();

    @Override
    public void runClick(ButtonInteraction e) {
        HelpComponentIds.Parsed parsed = HelpComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            e.reply(ExperienceRenderer.toMessage(HelpPresenter.directory(false))).setEphemeral(true).queue();
            return;
        }
        boolean admin = com.itsmarsss.database.categories.Users.isModerator(e.getUser().getId());
        switch (parsed.action()) {
            case HelpComponentIds.ACTION_HOME -> e.editMessage(ExperienceRenderer.toEdit(
                    HelpPresenter.directory(admin)
            )).queue();
            case HelpComponentIds.ACTION_CAT -> {
                String term = parsed.opaqueId() == null || parsed.opaqueId().isBlank()
                        ? "commands"
                        : parsed.opaqueId();
                var embed = help.help(term, admin);
                String title = embed.getTitle() == null ? "Help" : embed.getTitle();
                String desc = embed.getDescription() == null ? "" : embed.getDescription();
                StringBuilder body = new StringBuilder(desc);
                if (embed.getFields() != null) {
                    for (var field : embed.getFields()) {
                        if (field.getName() != null && field.getValue() != null) {
                            body.append("\n\n**").append(field.getName()).append("**\n")
                                    .append(field.getValue());
                        }
                    }
                }
                e.editMessage(ExperienceRenderer.toEdit(HelpPresenter.category(title, body.toString())))
                        .queue();
            }
            default -> e.reply(ExperienceRenderer.toMessage(HelpPresenter.directory(false))).setEphemeral(true).queue();
        }
    }

    @Override
    public String getID() {
        return HelpComponentIds.PREFIX;
    }
}
