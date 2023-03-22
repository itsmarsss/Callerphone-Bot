package com.marsss.callerphone.msginbottle;

import com.marsss.callerphone.Storage;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageInBottle {

    public static final HashMap<String, MIBBottle> bottles = new HashMap<>();

    public MIBStatus sendBottle(String id, String page) {
        if(Storage.getMIBSendCoolDown(id)-System.currentTimeMillis() > 86400000) {
            return MIBStatus.RATE_LIMITED;
        }

        MIBBottle newBottle = new MIBBottle(id, page);

        bottles.put(newBottle.getUuid(), newBottle);

        return MIBStatus.SENT;
    }

    public MIBBottle findBottle(String id) {
        if(Storage.getMIBFindCoolDown(id)-System.currentTimeMillis() > 43200000) {
            return null;
        }

        MIBBottle bottle = null;
        int index = 0;

        Iterator it = bottles.entrySet().iterator();
        while (it.hasNext()) {
            MIBBottle tempBottle = (MIBBottle) ((Map.Entry)it.next()).getValue();
            if(!tempBottle.getParticipantID().getLast().equals(id)) {
                bottle = tempBottle;
                bottles.remove(index);
                break;
            }
            index++;
        }

        return bottle;
    }

    public MIBBottle getBottle(String uuid) {
        return bottles.get(uuid);
    }

}
