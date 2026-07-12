package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchMessage;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchMessageRepository;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class ExportService {
    private final MatchUserRepository users;
    private final MatchProfileRepository profiles;
    private final MatchConversationRepository conversations;
    private final MatchMessageRepository messages;

    public ExportService(
            MatchUserRepository users,
            MatchProfileRepository profiles,
            MatchConversationRepository conversations,
            MatchMessageRepository messages
    ) {
        this.users = users;
        this.profiles = profiles;
        this.conversations = conversations;
        this.messages = messages;
    }

    public String exportJson(String userId) {
        JSONObject root = new JSONObject();
        root.put("exportedAt", java.time.Instant.now().toString());
        root.put("schema", "callerphone-match-export-v1");
        root.put("userId", userId);

        Optional<MatchUser> user = users.findById(userId);
        if (user.isPresent()) {
            MatchUser u = user.get();
            JSONObject uo = new JSONObject();
            uo.put("enrolled", u.isEnrolled());
            uo.put("ageCohort", u.getAgeCohort() == null ? null : u.getAgeCohort().code());
            uo.put("notificationsEnabled", u.isNotificationsEnabled());
            uo.put("digestOptIn", u.isDigestOptIn());
            uo.put("browseStreakDays", u.getBrowseStreakDays());
            uo.put("createdAt", str(u.getCreatedAt()));
            uo.put("enrolledAt", str(u.getEnrolledAt()));
            root.put("matchUser", uo);
        }

        Optional<MatchProfile> profile = profiles.findByUserId(userId);
        if (profile.isPresent()) {
            MatchProfile p = profile.get();
            JSONObject po = new JSONObject();
            po.put("displayName", p.getDisplayName());
            po.put("state", p.getState() == null ? null : p.getState().name());
            po.put("bio", p.getBio());
            po.put("pronouns", p.getPronouns());
            po.put("interests", p.getInterests());
            po.put("ageCohort", p.getAgeCohort() == null ? null : p.getAgeCohort().code());
            po.put("ageCohort", p.getAgeCohort() == null ? null : p.getAgeCohort().code());
            root.put("profile", po);
        }

        JSONArray chats = new JSONArray();
        for (MatchConversation c : conversations.findActiveByUserId(userId)) {
            JSONObject co = new JSONObject();
            co.put("conversationId", c.getConversationId());
            co.put("stage", c.getStage() == null ? null : c.getStage().name());
            co.put("messageCount", c.getMessageCount());
            co.put("participants", c.getParticipants());
            JSONArray msgs = new JSONArray();
            List<MatchMessage> recent = messages.findByConversation(c.getConversationId(), 50);
            for (MatchMessage m : recent) {
                JSONObject mo = new JSONObject();
                mo.put("id", m.messageId());
                mo.put("senderId", m.senderId());
                mo.put("content", m.content());
                mo.put("createdAt", str(m.createdAt()));
                msgs.add(mo);
            }
            co.put("recentMessages", msgs);
            chats.add(co);
        }
        root.put("conversations", chats);
        return root.toJSONString();
    }

    private static String str(java.time.Instant i) {
        return i == null ? null : i.toString();
    }
}
