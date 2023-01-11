package com.marsss.callerphone.bot;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Advertisement {
    public static MessageEmbed generateAd(){
        EmbedBuilder advEmd = new EmbedBuilder()
                .setColor(Colour.randColor())
                .setTitle("**UPCOMING ECONOMY BOT!!!**")
                .setDescription("You can earn credits that can be converted to currency by interacting with **Callerphone**. See `" + Callerphone.Prefix + "profile` and `" + Callerphone.Prefix + "help creds`...")
                .appendDescription("\n\nJoin " + Callerphone.support + " and view <#1055259828135657482> for more information!");

        return advEmd.build();
    }
}
