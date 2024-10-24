package com.marsss.callerphone.msginbottle;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.database.categories.Cooldown;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageInBottle {

    public static final HashMap<String, MIBBottle> bottles = new HashMap<>();

    public static MIBStatus sendBottle(String id, String page) {
        if (System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(id) < ToolSet.SENDBOTTLE_COOLDOWN) {
            return MIBStatus.RATE_LIMITED;
        }

        MIBBottle newBottle = new MIBBottle(id, page);

        bottles.put(newBottle.getUuid(), newBottle);

        Cooldown.updateMIBSendCoolDown(id);

        return MIBStatus.SENT;
    }

    public static MIBBottle findBottle(String id) {
        if (System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(id) < ToolSet.FINDBOTTLE_COOLDOWN) {
            return null;
        }

        MIBBottle bottle = new MIBBottle(Callerphone.selfUser.getId(), "No bottle to be found " + ToolSet.CP_ERR);

        Iterator it = bottles.entrySet().iterator();
        while (it.hasNext()) {
            MIBBottle tempBottle = (MIBBottle) ((Map.Entry) it.next()).getValue();
            //if(!tempBottle.getParticipantID().getLast().equals(id)) {
            bottle = tempBottle;
            it.remove();
            break;
            //}
        }

        Cooldown.updateMIBFindCoolDown(id);

        return bottle;
    }

    public static MIBBottle getBottle(String uuid) {
        return bottles.get(uuid);
    }

}
