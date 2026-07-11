package com.itsmarsss.callerphone.discord.mod;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.safety.Report;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Staff DM commands. Replaces ad-hoc logic with a clearer Match + legacy surface.
 */
public final class ModCommandRouter extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        if (!Users.isModerator(event.getAuthor().getId())) {
            return;
        }

        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String content = message.getContentRaw().trim();
        final String prefix = Callerphone.config.getPrefix();
        if (!content.toLowerCase().startsWith(prefix.toLowerCase())) {
            return;
        }

        String body = content.substring(prefix.length()).trim();
        if (body.isEmpty()) {
            return;
        }
        String[] args = body.split("\\s+");
        String command = args[0].toLowerCase();

        try {
            switch (command) {
                case "help" -> {
                    if (args.length > 1 && args[1].equalsIgnoreCase("mod")) {
                        sendModHelp(author);
                    }
                }
                case "blacklist" -> requireId(message, args, id -> {
                    if (Users.isBlacklisted(id)) {
                        message.reply("ID blacklisted already").queue();
                    } else {
                        Users.addBlacklist(id);
                        message.reply("ID: `" + id + "` added to blacklist").queue();
                    }
                });
                case "rblacklist" -> requireId(message, args, id -> {
                    if (!Users.isBlacklisted(id)) {
                        message.reply("ID not blacklisted").queue();
                    } else {
                        Users.addUser(id);
                        message.reply("ID: `" + id + "` removed from blacklist").queue();
                    }
                });
                case "mod" -> requireId(message, args, id -> {
                    if (Users.isModerator(id)) {
                        message.reply("ID is mod already").queue();
                    } else {
                        Users.addModerator(id);
                        message.reply("ID: `" + id + "` added to mod list").queue();
                    }
                });
                case "rmod" -> requireId(message, args, id -> {
                    if (!Users.isModerator(id)) {
                        message.reply("ID is not a mod").queue();
                    } else if (id.equals(Callerphone.config.getOwnerID())) {
                        message.reply("You cannot remove this mod").queue();
                    } else {
                        Users.addUser(id);
                        message.reply("ID: `" + id + "` removed from mod list").queue();
                    }
                });
                case "prefix" -> {
                    if (args.length < 3) {
                        message.reply(ToolSet.CP_EMJ + "`" + prefix + "prefix <id> <prefix>`").queue();
                        return;
                    }
                    String id = args[1];
                    String userPrefix = args[2];
                    if (userPrefix.length() > 15) {
                        message.reply("Prefix too long (max. length is 15 chars)").queue();
                        return;
                    }
                    Users.setPrefix(id, userPrefix);
                    message.reply("ID: `" + id + "` now has prefix `" + userPrefix + "`").queue();
                }
                case "rprefix" -> requireId(message, args, id -> {
                    Users.setPrefix(id, "");
                    message.reply("ID: `" + id + "` no longer has a prefix").queue();
                });
                case "mreview" -> handleMatchReview(message, args);
                case "mapprove" -> requireId(message, args, id -> {
                    if (!ApplicationContext.isReady()) {
                        message.reply("Match not ready").queue();
                        return;
                    }
                    // Force-active after a mistaken pause — users publish themselves
                    var result = ApplicationContext.get().profiles().forceActive(author.getId(), id);
                    message.reply(result.message()).queue();
                });
                case "mpause" -> {
                    if (args.length < 2) {
                        message.reply("`" + prefix + "mpause <id> [reason]`").queue();
                        return;
                    }
                    String reason = args.length > 2
                            ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length))
                            : "paused after report";
                    var result = ApplicationContext.get().profiles().forcePause(author.getId(), args[1], reason);
                    message.reply(result.message()).queue();
                }
                case "mreject" -> {
                    // alias of mpause for muscle memory
                    if (args.length < 2) {
                        message.reply("`" + prefix + "mreject <id> [reason]` (pauses profile)").queue();
                        return;
                    }
                    String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "paused";
                    var result = ApplicationContext.get().profiles().forcePause(author.getId(), args[1], reason);
                    message.reply(result.message()).queue();
                }
                case "msuspend" -> {
                    if (args.length < 2) {
                        message.reply("`" + prefix + "msuspend <id> [reason]`").queue();
                        return;
                    }
                    String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "suspended";
                    ApplicationContext.get().safety().suspendMatch(author.getId(), args[1], reason);
                    message.reply("Match suspended for `" + args[1] + "`").queue();
                }
                case "mrestore" -> requireId(message, args, id -> {
                    ApplicationContext.get().safety().restoreMatch(author.getId(), id, "restored");
                    message.reply("Match restored for `" + id + "`").queue();
                });
                case "mreports" -> {
                    if (!ApplicationContext.isReady()) {
                        message.reply("Match not ready").queue();
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Open Match reports:\n");
                    for (Report report : ApplicationContext.get().safety().openReports(15)) {
                        sb.append("• `").append(report.getId()).append("` ")
                                .append(report.getCategory()).append(" subject=")
                                .append(report.getSubjectId()).append(" prio=")
                                .append(report.getPriority())
                                .append(report.isAutoPaused() ? " AUTO-PAUSE" : "")
                                .append(" evidence=").append(report.getEvidence() == null ? 0 : report.getEvidence().size())
                                .append('\n');
                    }
                    message.reply(sb.toString()).queue();
                }
                case "mpremium" -> requireId(message, args, id -> {
                    ApplicationContext.get().premium().grant(id);
                    message.reply("Premium entitlement granted for `" + id + "` (test grant).").queue();
                });
                case "mresolve" -> {
                    if (args.length < 3) {
                        message.reply("`" + prefix + "mresolve <reportId> <status>`").queue();
                        return;
                    }
                    ApplicationContext.get().safety().resolveReport(author.getId(), args[1], args[2]);
                    message.reply("Report updated.").queue();
                }
                default -> {
                    // not a mod command
                }
            }
        } catch (Exception ex) {
            message.reply("Mod command failed: " + ex.getMessage()).queue();
        }
    }

    private void handleMatchReview(Message message, String[] args) {
        if (!ApplicationContext.isReady()) {
            message.reply("Match not ready").queue();
            return;
        }
        // Profiles self-publish; staff queue is reports, not pre-approval
        StringBuilder sb = new StringBuilder(
                "Match staff queue = **reports**, not profile approval.\nUse `mreports` / `mresolve`.\n\nSample live profiles:\n");
        for (MatchProfile profile : ApplicationContext.get().profiles().pendingReview(10)) {
            sb.append("• `").append(profile.getUserId()).append("` ")
                    .append(profile.getDisplayName()).append(" [")
                    .append(profile.getAgeCohort() == null ? "?" : profile.getAgeCohort().label())
                    .append("]\n");
        }
        message.reply(sb.toString()).queue();
    }

    private interface IdAction {
        void run(String id);
    }

    private void requireId(Message message, String[] args, IdAction action) {
        if (args.length < 2) {
            message.reply(ToolSet.CP_EMJ + "Usage: `" + Callerphone.config.getPrefix() + args[0] + " <id>`").queue();
            return;
        }
        action.run(args[1]);
    }

    private void sendModHelp(User member) {
        String p = Callerphone.config.getPrefix();
        String desc = """
                **Legacy**
                `%smod <id>` / `%srmod <id>`
                `%sblacklist <id>` / `%srblacklist <id>`
                `%sprefix <id> <prefix>` / `%srprefix <id>`

                **Match** (users self-publish; you review **reports**)
                `%smreports` — open reports
                `%smresolve <reportId> <status>` — resolve report
                `%smpause <id> [reason]` — force-pause profile after report
                `%smapprove <id>` — force-active if wrongly paused
                `%smsuspend <id> [reason]` — Match suspension
                `%smrestore <id>` — clear Match sanctions
                `%smreview` — sample live profiles
                `%smpremium <id>` — test-grant Premium entitlements
                """.formatted(p, p, p, p, p, p, p, p, p, p, p, p, p, p);
        EmbedBuilder help = new EmbedBuilder()
                .setTitle("Mod")
                .setDescription(desc)
                .setColor(ToolSet.COLOR);
        ToolSet.sendPrivateEmbed(member, help.build());
    }
}
