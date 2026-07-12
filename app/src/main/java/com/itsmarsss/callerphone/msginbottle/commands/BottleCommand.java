package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.msginbottle.BottleListUi;
import com.itsmarsss.callerphone.msginbottle.BottlePresenter;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

/**
 * Plan §25–27: consolidated bottle surface.
 * Legacy /sendbottle, /findbottle, /viewbottle remain registered as aliases.
 */
public final class BottleCommand implements ISlashCommand, ICommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String sub = e.getSubcommandName();
        if (sub == null) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                    .title("Message in a bottle")
                    .description(
                            "Send something into the sea, find what others cast, "
                                    + "or reopen threads you're part of.\n\n"
                                    + "`/bottle threads` · your replies\n"
                                    + "`/bottle saved` · bookmarks"
                    )
                    .actions(
                            com.itsmarsss.callerphone.experience.ActionSpec.primary(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_SEND, "_"
                                    ),
                                    "Send a bottle"
                            ),
                            com.itsmarsss.callerphone.experience.ActionSpec.success(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                    ),
                                    "Find a bottle"
                            )
                    )
                    .build())).setEphemeral(true).queue();
            return;
        }
        switch (sub) {
            case "send" -> openSend(e);
            case "find" -> find(e);
            case "saved", "view" -> view(e);
            case "threads" -> threads(e);
            default -> e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
        }
    }

    private void openSend(SlashCommandInteractionEvent e) {
        if (e.getUser() == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        String userId = e.getUser().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(userId);
        if (elapsed < ToolSet.SENDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.SENDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.sendCooldown(minutes)))
                    .setEphemeral(true).queue();
            return;
        }
        e.reply(ExperienceRenderer.toMessage(BottlePresenter.identityPick())).setEphemeral(true).queue();
    }

    private void find(SlashCommandInteractionEvent e) {
        String userId = e.getUser().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(userId);
        if (elapsed < ToolSet.FINDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.FINDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.findCooldown(minutes)))
                    .setEphemeral(true).queue();
            return;
        }
        Bottle bottle = MessageInBottle.findBottle();
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.emptySea())).setEphemeral(true).queue();
            return;
        }
        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        Cooldown.setMIBFindCoolDown(userId);
        try {
            if (ApplicationContext.isReady()) {
                var analytics = ApplicationContext.get().analytics();
                analytics.track(userId, "bottle_find", bottle.getId());
                analytics.trackSurface(userId, "bottle", "find", bottle.getId());
            }
        } catch (Exception ignored) {
        }
        e.reply(message).setEphemeral(true).queue();
    }

    private void view(SlashCommandInteractionEvent e) {
        OptionMapping idOpt = e.getOption("id");
        if (idOpt != null && !idOpt.getAsString().trim().isEmpty()) {
            Bottle bottle = MIB.getBottle(idOpt.getAsString().trim());
            if (bottle == null) {
                e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                        .title("Not found")
                        .description("No bottle with that id.")
                        .build())).setEphemeral(true).queue();
                return;
            }
            MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
            if (message == null) {
                e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
                return;
            }
            e.reply(message).setEphemeral(true).queue();
            return;
        }

        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title("Saved bottles")
                    .description("Still starting up. Try again in a moment.")
                    .build())).setEphemeral(true).queue();
            return;
        }
        e.deferReply(true).queue();
        ApplicationContext ctx = ApplicationContext.get();
        ctx.dbExecutor().execute(() -> {
            List<String> ids = ctx.bottleSaves().listBottleIds(e.getUser().getId(), 25);
            List<Bottle> bottles = new ArrayList<>();
            for (String id : ids) {
                Bottle b = MIB.getBottle(id);
                if (b != null) {
                    bottles.add(b);
                }
            }
            e.getHook().sendMessage(BottleListUi.saved(bottles)).setEphemeral(true).queue();
        });
    }

    private void threads(SlashCommandInteractionEvent e) {
        e.deferReply(true).queue();
        String userId = e.getUser().getId();
        if (ApplicationContext.isReady()) {
            ApplicationContext.get().dbExecutor().execute(() -> {
                List<Bottle> bottles = MIB.findThreadsForUser(userId, 25);
                e.getHook().sendMessage(BottleListUi.threads(bottles)).setEphemeral(true).queue();
            });
        } else {
            List<Bottle> bottles = MIB.findThreadsForUser(userId, 25);
            e.getHook().sendMessage(BottleListUi.threads(bottles)).setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "`/bottle send` cast a bottle\n"
                + "`/bottle find` discover one\n"
                + "`/bottle saved` your bookmarks\n"
                + "`/bottle threads` threads you're in";
    }

    @Override
    public String getName() {
        return "bottle";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Message in a bottle")
                .addSubcommands(
                        new SubcommandData("send", "Cast a bottle into the sea"),
                        new SubcommandData("find", "Find a random bottle"),
                        new SubcommandData("saved", "Saved bottles (or open by id)")
                                .addOptions(new OptionData(OptionType.STRING, "id", "Bottle id", false)),
                        new SubcommandData("threads", "Bottles you've replied to or launched")
                )
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
