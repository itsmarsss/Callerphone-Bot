package com.marsss.callerphone.msginbottle;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageInBottle {

    public static final HashMap<String, MIBBottle> bottles = new HashMap<>();

    public static MIBStatus sendBottle(String id, String page) {
        if(System.currentTimeMillis()-Storage.getMIBSendCoolDown(id) < ToolSet.SENDBOTTLE_COOLDOWN) {
            return MIBStatus.RATE_LIMITED;
        }

        MIBBottle newBottle = new MIBBottle(id, page);

        bottles.put(newBottle.getUuid(), newBottle);

        Storage.updateMIBSendCoolDown(id);

        return MIBStatus.SENT;
    }

    public static MIBBottle findBottle(String id) {
        if(System.currentTimeMillis()-Storage.getMIBFindCoolDown(id) < ToolSet.FINDBOTTLE_COOLDOWN) {
            return null;
        }

        MIBBottle bottle = new MIBBottle(Callerphone.jda.getSelfUser().getId(), "No bottle to be found " + ToolSet.CP_ERR);

        Iterator it = bottles.entrySet().iterator();
        while (it.hasNext()) {
            MIBBottle tempBottle = (MIBBottle) ((Map.Entry)it.next()).getValue();
            //if(!tempBottle.getParticipantID().getLast().equals(id)) {
                bottle = tempBottle;
                it.remove();
                break;
            //}
        }

        Storage.updateMIBFindCoolDown(id);

        return bottle;
    }

    public static MIBBottle getBottle(String uuid) {
        return bottles.get(uuid);
    }

}
