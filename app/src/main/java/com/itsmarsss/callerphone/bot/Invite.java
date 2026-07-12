package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Plan §31: invite with clear link actions. */
public class Invite implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String invite = Callerphone.config.getBotInviteLink();
        String support = Callerphone.config.getSupportServer();
        String donate = Callerphone.config.getDonateLink();
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Bring Callerphone with you")
                .description(
                        "Add Callerphone to a server or join the community.\n\n"
                                + "**Add to a server** · [Invite](" + invite + ")\n"
                                + "**Community** · [Support server](" + support + ")\n"
                                + "**Support us** · [Donate](" + donate + ")"
                )
                .footer("Glad you're here")
                .actions(
                        ActionSpec.link(invite, "Invite bot"),
                        ActionSpec.link(support, "Support server"),
                        ActionSpec.link(donate, "Donate")
                )
                .build())).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`/invite` bot and support links";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Bot and support links")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
