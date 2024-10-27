package com.marsss.callerphone.msginbottle;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.entities.Bottle;
import com.marsss.callerphone.msginbottle.entities.Page;
import com.marsss.database.categories.Cooldown;
import com.marsss.database.categories.MIB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MessageInBottle {
    public static final Logger logger = LoggerFactory.getLogger(MessageInBottle.class);

    public static MIBStatus sendBottle(String id, String message, boolean anon, String uuid) {
        boolean stat = uuid == null ? MIB.createMIB(id, message, anon) : MIB.addMIBPage(id, message, anon, uuid);

        if (stat) {
            return MIBStatus.SENT;
        }

        return MIBStatus.ERROR;
    }

    public static Bottle findBottle(String id) {
        Bottle mib = MIB.findBottle();

        if (mib != null) {
            return mib;
        }
        return null;
    }

    public static MessageCreateData createMessage(Bottle bottle, int pageNum) {
        if (pageNum == Integer.MAX_VALUE) {
            pageNum = Math.min(Math.max(bottle.getPages().size() - 1, 0), bottle.getPages().size() - 1);
        }

        Page page = bottle.getPages().get(pageNum);

        CompletableFuture<MessageCreateData> future = new CompletableFuture<>();

        ToolSet.getUser(page.getAuthor()).queue(lastUser -> {
                    EmbedBuilder bottleEmbed = new EmbedBuilder()
                            .setTitle("<:MessageInBottle:1089648266284638339> **A message in bottle has arrived!**")
                            .setDescription(page.getMessage())
                            .appendDescription("\n\n\u3000**\\- " + (page.isSigned() ? lastUser.getName() : "anonymous") + "** from  <t:" + page.getReleased() + ":R>")
                            .setFooter("Pages " + (page.getPageNum() + 1) + "/" + bottle.getPages().size())
                            .setTimestamp(Instant.now())
                            .setColor(ToolSet.COLOR);

                    Button addPage = Button.success("adp-" + bottle.getUuid(), "Add Page");
                    Button previousPage = Button.secondary("pvp-" + bottle.getUuid() + "-" + Math.max(page.getPageNum() - 1, 0), "\u2B05");
                    Button nextPage = Button.secondary("nxp-" + bottle.getUuid() + "-" + Math.min(page.getPageNum() + 1, bottle.getPages().size() - 1), "\u2B95");
                    Button reportButton = Button.danger("rpt-" + bottle.getUuid() + "-" + page.getPageNum(), "Report");
                    Button saveACopy = Button.secondary("sve", "Save");

                    MessageCreateData message = new MessageCreateBuilder()
                            .setEmbeds(bottleEmbed.build())
                            .setComponents(ActionRow.of(addPage, previousPage, nextPage, reportButton, saveACopy)).build();

                    future.complete(message);
                },
                future::completeExceptionally
        );

        try {
            return future.get();
        } catch (Exception e) {
        }

        return null;
    }
}