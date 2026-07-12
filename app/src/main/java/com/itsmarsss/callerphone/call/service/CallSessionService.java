package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.discord.CallPresenter;
import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.model.CallMatchSource;
import com.itsmarsss.callerphone.call.model.CallMessage;
import com.itsmarsss.callerphone.call.model.CallQueueEntry;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.call.repository.CallSessionRepository;
import com.itsmarsss.callerphone.experience.ControlMessageStore;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Random live call between two guild channels. */
public final class CallSessionService {
    private static final Logger logger = LoggerFactory.getLogger(CallSessionService.class);
    private static volatile CallSessionService instance;

    private final ConcurrentHashMap<String, CallSession> byChannel = new ConcurrentHashMap<>();
    private final CallQueueService queue = new CallQueueService();
    private final CallMessageService messages = new CallMessageService();
    private final CallSessionRepository repository = new CallSessionRepository();

    public CallSessionService() {
        instance = this;
    }

    public static CallSessionService get() {
        if (instance == null) {
            instance = new CallSessionService();
        }
        return instance;
    }

    public synchronized CallResult start(String channelId, String starterUserId) {
        if (byChannel.containsKey(channelId)) {
            return CallResult.conflict();
        }
        if (queue.isQueued(channelId)) {
            queue.cleanup();
            int pos = queue.position(channelId);
            int size = Math.max(queue.size(), 1);
            if (pos < 1) {
                queue.enqueue(CallEndpoint.guild(channelId, starterUserId));
                return CallResult.queued(queue.position(channelId), queue.size());
            }
            return CallResult.alreadyQueued(pos, size);
        }

        Optional<CallQueueEntry> peer = queue.dequeueOther(channelId);
        if (peer.isEmpty()) {
            queue.enqueue(CallEndpoint.guild(channelId, starterUserId));
            return CallResult.queued(Math.max(queue.position(channelId), 1), queue.size());
        }

        CallEndpoint a = peer.get().endpoint();
        CallEndpoint b = CallEndpoint.guild(channelId, starterUserId);
        CallSession session = new CallSession(a, b, CallMatchSource.LIVE_QUEUE);

        TextChannel chA = ToolSet.getTextChannel(a.channelId());
        TextChannel chB = ToolSet.getTextChannel(b.channelId());
        if (chA == null || chB == null) {
            if (chA != null) {
                queue.enqueue(a);
            } else if (chB != null) {
                queue.enqueue(b);
            }
            return CallResult.failed("The other channel became unavailable. You're no longer in queue.");
        }

        byChannel.put(a.channelId(), session);
        byChannel.put(b.channelId(), session);

        MessageCreateData connected = connectedMessage(session);
        sendLobby(chA, a.channelId(), connected);
        // receiver already gets slash reply as matched; store lobby id when they reply
        logger.info("Call matched {} <-> {} source={}", a.channelId(), b.channelId(), session.getSource());
        return CallResult.matched(session);
    }

    public MessageCreateData connectedMessage(CallSession session) {
        return ExperienceRenderer.toMessage(CallPresenter.connected(session));
    }

    /** Remember the public lobby message for later edit-in-place transitions. */
    public void rememberLobbyMessage(String channelId, String messageId) {
        ControlMessageStore.get().put(ControlMessageStore.callLobbyKey(channelId), messageId);
    }

    public synchronized MessageCreateData end(String channelId) {
        CallSession session = byChannel.get(channelId);
        if (session == null) {
            if (queue.remove(channelId)) {
                ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(channelId));
                return ExperienceRenderer.toMessage(CallPresenter.leftQueue());
            }
            return ExperienceRenderer.toMessage(CallPresenter.noCall());
        }
        TextChannel other = ToolSet.getTextChannel(session.otherChannelId(channelId));
        if (other != null) {
            sendLobby(other, other.getId(), ExperienceRenderer.toMessage(CallPresenter.peerHungUp(session.getId())));
        }
        MessageCreateData ended = ExperienceRenderer.toMessage(CallPresenter.ended(session.getId()));
        finalize(session);
        ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(channelId));
        if (other != null) {
            ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(other.getId()));
        }
        return ended;
    }

    private void sendLobby(TextChannel channel, String channelId, MessageCreateData data) {
        if (channel == null) {
            return;
        }
        channel.sendMessage(data).queue(msg -> rememberLobbyMessage(channelId, msg.getId()));
    }

    public boolean isInCall(String channelId) {
        return byChannel.containsKey(channelId);
    }

    public Optional<CallSession> get(String channelId) {
        return Optional.ofNullable(byChannel.get(channelId));
    }

    public Optional<CallSession> getBySessionId(String sessionId) {
        for (CallSession s : byChannel.values()) {
            if (s.getId().equals(sessionId)) {
                return Optional.of(s);
            }
        }
        return repository.findById(sessionId);
    }

    public boolean handleMessage(String channelId, User author, String raw) {
        CallSession session = byChannel.get(channelId);
        if (session == null) {
            return false;
        }
        boolean fromA = session.isSideA(channelId);
        CallMessage msg = messages.create(author, raw, channelId, fromA);
        session.addMessage(msg);
        if (!session.canSend(channelId, ToolSet.MESSAGE_COOLDOWN)) {
            return true;
        }
        session.touchMessage(channelId);
        String outbound = messages.prepareOutbound(raw);
        TextChannel dest = ToolSet.getTextChannel(session.otherChannelId(channelId));
        if (!messages.send(dest, author, outbound)) {
            terminateError(session);
        }
        return true;
    }

    public void reportById(String sessionId) {
        Optional<CallSession> active = getBySessionId(sessionId);
        active.ifPresent(s -> {
            s.markReported();
            messages.sendTranscript(ToolSet.getTextChannel(Callerphone.config.getReportChatChannel()), s);
            if (!byChannel.containsValue(s)) {
                repository.markReported(sessionId);
            }
        });
    }

    public boolean reportActive(String channelId) {
        CallSession s = byChannel.get(channelId);
        if (s == null) {
            return false;
        }
        s.markReported();
        return true;
    }

    private void terminateError(CallSession session) {
        TextChannel a = ToolSet.getTextChannel(session.getChannelA());
        TextChannel b = ToolSet.getTextChannel(session.getChannelB());
        if (a != null) {
            a.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }
        if (b != null) {
            b.sendMessage(Response.CONNECTION_ERROR.toString()).queue();
        }
        finalize(session);
    }

    private void finalize(CallSession session) {
        session.end();
        messages.sendTranscript(ToolSet.getTextChannel(Callerphone.config.getTempChatChannel()), session);
        repository.save(session);
        if (session.isReported()) {
            messages.sendTranscript(ToolSet.getTextChannel(Callerphone.config.getReportChatChannel()), session);
        }
        byChannel.remove(session.getChannelA(), session);
        byChannel.remove(session.getChannelB(), session);
    }

    public CallQueueService queue() {
        return queue;
    }

    public CallSessionRepository repository() {
        return repository;
    }
}
