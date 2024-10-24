package com.marsss.callerphone.bot;

import com.marsss.callerphone.utils.Colour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Advertisement {
    public static MessageEmbed generateAd(){
        EmbedBuilder advEmd = new EmbedBuilder()
                .setColor(Colour.randColor())
                .setTitle("**Send a message into the vast sea!**")
                .setDescription("You can now send and find random message floating around in the sea! </findbottle:1089656103391985668> and </sendbottle:1089656103391985667>")
                .appendDescription("");

        return advEmd.build();
    }
}
