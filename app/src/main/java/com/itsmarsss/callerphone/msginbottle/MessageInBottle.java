package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.msginbottle.entities.Page;
import com.itsmarsss.database.categories.MIB;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageInBottle {
    public static final Logger logger = LoggerFactory.getLogger(MessageInBottle.class);

    /**
     * @param signed whether the page should show the author's identity (param was historically misnamed {@code anon})
     */
    public static MIBStatus sendBottle(String id, String message, boolean signed, String mibId) {
        if (mibId != null) {
            Bottle existing = MIB.getBottle(mibId);
            if (existing == null) {
                return MIBStatus.NOT_FOUND;
            }
            if (existing.getPages() != null && existing.getPages().size() >= Constants.MIB_MAX_PAGES) {
                return MIBStatus.THREAD_FULL;
            }
        }
        Bottle bottle = mibId == null
                ? MIB.createMIB(id, message, signed)
                : MIB.addMIBPage(id, message, signed, mibId);
        if (bottle == null) {
            return MIBStatus.ERROR;
        }
        log(bottle);
        return MIBStatus.SENT;
    }

    public static Bottle findBottle() {
        return MIB.findBottle();
    }

    public static boolean isFull(Bottle bottle) {
        return bottle != null
                && bottle.getPages() != null
                && bottle.getPages().size() >= Constants.MIB_MAX_PAGES;
    }

    public static MessageCreateData createMessage(Bottle bottle, int pageNum) {
        if (bottle == null || bottle.getPages() == null || bottle.getPages().isEmpty()) {
            return null;
        }

        int lastIndex = bottle.getPages().size() - 1;
        if (pageNum == Integer.MAX_VALUE) {
            pageNum = lastIndex;
        }
        pageNum = Math.max(0, Math.min(pageNum, lastIndex));

        Page page = bottle.getPages().get(pageNum);
        String sign = resolveSign(page);

        boolean multipage = bottle.getPages().size() > 1;
        boolean full = isFull(bottle);

        EmbedBuilder bottleEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTimestamp(Instant.now());

        if (multipage) {
            int replies = bottle.getPages().size() - 1;
            bottleEmbed.setTitle(full
                    ? "Bottle thread (full)"
                    : "A bottle with " + replies + " repl" + (replies == 1 ? "y" : "ies"));
            Page original = bottle.getPages().get(0);
            Page latest = bottle.getPages().get(lastIndex);
            StringBuilder body = new StringBuilder();
            body.append("**Original**\n").append(clip(original.getMessage(), 320));
            if (pageNum != 0 && pageNum != lastIndex) {
                body.append("\n\n**This page**\n").append(clip(page.getMessage(), 400));
            }
            body.append("\n\n**Latest**\n").append(clip(latest.getMessage(), 320));
            body.append("\n\n\u3000**\\- ").append(sign).append("** · <t:")
                    .append(page.getReleased()).append(":R>");
            if (full) {
                body.append("\n\n_Reply limit reached. Connect through Match if you want to keep talking._");
            }
            bottleEmbed.setDescription(body.toString());
        } else {
            bottleEmbed.setTitle("<:MessageInBottle:1089648266284638339> **A message in bottle has arrived!**")
                    .setDescription(page.getMessage()
                            + "\n\n\u3000**\\- " + sign + "** from  <t:" + page.getReleased() + ":R>");
        }
        bottleEmbed.setFooter("Pages " + (page.getPageNum() + 1) + "/" + bottle.getPages().size()
                + (full ? " · full" : ""));

        List<Button> row1 = new ArrayList<>();
        if (full) {
            row1.add(Button.secondary("adp-" + bottle.getId(), "Thread full").asDisabled());
        } else {
            row1.add(Button.success("adp-" + bottle.getId(), multipage ? "Reply" : "Add Page"));
        }
        row1.add(Button.secondary("pvp-" + bottle.getId() + "-" + Math.max(page.getPageNum() - 1, 0), "\u25C0\uFE0F"));
        row1.add(Button.secondary("nxp-" + bottle.getId() + "-" + Math.min(page.getPageNum() + 1, lastIndex), "\u25B6\uFE0F"));
        row1.add(Button.danger("rpt-" + bottle.getId() + "-" + page.getPageNum(), "Report"));
        row1.add(Button.secondary("sve", "Save"));

        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setEmbeds(bottleEmbed.build())
                .setComponents(ActionRow.of(row1));

        List<Button> row2 = new ArrayList<>();
        // Plan §26 / §10.E — signed pages offer mutual interest
        if (page.isSigned()
                && page.getAuthor() != null
                && !page.getAuthor().isBlank()
                && !"unknown".equals(sign)
                && !sign.equals("anonymous")) {
            row2.add(Button.success(
                    MatchComponentIds.of(MatchComponentIds.ACTION_BOTTLE_INTEREST, page.getAuthor()),
                    "Interested"
            ));
        }
        // At cap, also surface connect CTA for any other signed participant on the thread
        if (full) {
            String otherSigned = firstOtherSignedAuthor(bottle, page.getAuthor());
            if (otherSigned != null && row2.stream().noneMatch(b -> b.getCustomId() != null
                    && b.getCustomId().contains(otherSigned))) {
                row2.add(Button.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_BOTTLE_INTEREST, otherSigned),
                        "Connect via Match"
                ));
            }
        }
        row2.add(Button.primary(
                BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                "Next bottle"
        ));
        if (!row2.isEmpty()) {
            // Discord max 5 buttons per row
            if (row2.size() <= 5) {
                builder.addComponents(ActionRow.of(row2));
            } else {
                builder.addComponents(ActionRow.of(row2.subList(0, 5)));
            }
        }

        return builder.build();
    }

    private static String firstOtherSignedAuthor(Bottle bottle, String exclude) {
        if (bottle.getPages() == null) {
            return null;
        }
        for (Page p : bottle.getPages()) {
            if (p.isSigned()
                    && p.getAuthor() != null
                    && !p.getAuthor().isBlank()
                    && !"unknown".equals(p.getAuthor())
                    && (exclude == null || !exclude.equals(p.getAuthor()))) {
                return p.getAuthor();
            }
        }
        return null;
    }

    private static String resolveSign(Page page) {
        String sign = "anonymous";
        if (!page.isSigned()) {
            return sign;
        }
        RestAction<User> userAction = ToolSet.getUser(page.getAuthor());
        if (userAction != null) {
            try {
                User lastUser = userAction.complete();
                String prefix = Users.getPrefix(lastUser.getId());
                sign = (prefix.isEmpty() ? "" : "*[" + prefix + "]* ") + lastUser.getName();
            } catch (Exception e) {
                logger.warn("Could not resolve bottle author {}: {}", page.getAuthor(), e.getMessage());
                sign = "unknown";
            }
        } else {
            sign = "unknown";
        }
        return sign;
    }

    private static String clip(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }

    private static void log(Bottle bottle) {
        MessageCreateData message = createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            return;
        }
        TextChannel tempChannel = ToolSet.getTextChannel(Callerphone.config.getTempChatChannel());
        if (tempChannel != null) {
            tempChannel.sendMessage("**ID:** " + bottle.getId()).addEmbeds(message.getEmbeds()).queue();
        }
    }
}
