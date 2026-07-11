package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
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

public class MessageInBottle {
    public static final Logger logger = LoggerFactory.getLogger(MessageInBottle.class);

    public static MIBStatus sendBottle(String id, String message, boolean anon, String mibId) {
        Bottle bottle = mibId == null ? MIB.createMIB(id, message, anon) : MIB.addMIBPage(id, message, anon, mibId);
        if (bottle == null) {
            return MIBStatus.ERROR;
        }
        log(bottle);
        return MIBStatus.SENT;
    }

    public static Bottle findBottle() {
        return MIB.findBottle();
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
        String sign = "anonymous";

        if (page.isSigned()) {
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
        }

        EmbedBuilder bottleEmbed = new EmbedBuilder()
                .setTitle("<:MessageInBottle:1089648266284638339> **A message in bottle has arrived!**")
                .setDescription(page.getMessage())
                .appendDescription("\n\n\u3000**\\- " + sign + "** from  <t:" + page.getReleased() + ":R>")
                .setFooter("Pages " + (page.getPageNum() + 1) + "/" + bottle.getPages().size())
                .setTimestamp(Instant.now())
                .setColor(ToolSet.COLOR);

        Button addPage = Button.success("adp-" + bottle.getId(), "Add Page");
        Button previousPage = Button.secondary("pvp-" + bottle.getId() + "-" + Math.max(page.getPageNum() - 1, 0), "\u25C0\uFE0F");
        Button nextPage = Button.secondary("nxp-" + bottle.getId() + "-" + Math.min(page.getPageNum() + 1, lastIndex), "\u25B6\uFE0F");
        Button reportButton = Button.danger("rpt-" + bottle.getId() + "-" + page.getPageNum(), "Report");
        Button saveACopy = Button.secondary("sve", "Save");

        return new MessageCreateBuilder()
                .setEmbeds(bottleEmbed.build())
                .setComponents(ActionRow.of(addPage, previousPage, nextPage, reportButton, saveACopy))
                .build();
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
