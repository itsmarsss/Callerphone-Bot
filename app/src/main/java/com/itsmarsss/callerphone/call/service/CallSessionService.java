package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.Callerphone;
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
import com.itsmarsss.callerphone.experience.ExperienceView;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Live calls between guild channels or user DMs with the bot. */
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
        return start(CallEndpoint.guild(channelId, starterUserId));
    }

    public synchronized CallResult start(CallEndpoint self) {
        String channelId = self.channelId();
        if (byChannel.containsKey(channelId)) {
            return CallResult.conflict();
        }
        if (userAlreadyInCall(self.starterUserId())) {
            return CallResult.failed("You're already in a call elsewhere. End that one first.");
        }
        if (queue.isQueued(channelId)) {
            queue.cleanup();
            int pos = Math.max(queue.position(channelId), 1);
            int size = Math.max(queue.size(), 1);
            return CallResult.alreadyQueued(pos, size);
        }

        Optional<CallQueueEntry> peer = queue.dequeuePeer(self);
        if (peer.isEmpty()) {
            queue.enqueue(self);
            int pos = Math.max(queue.position(channelId), 1);
            int size = Math.max(queue.size(), 1);
            return CallResult.queued(pos, size);
        }

        CallEndpoint a = peer.get().endpoint();
        CallEndpoint b = self;
        CallSession session = new CallSession(a, b, CallMatchSource.LIVE_QUEUE);

        MessageChannel chA = ToolSet.getMessageChannel(a.channelId());
        MessageChannel chB = ToolSet.getMessageChannel(b.channelId());
        if (chA == null || chB == null) {
            if (chA != null) {
                queue.enqueue(a);
            } else if (chB != null) {
                queue.enqueue(b);
            }
            return CallResult.failed("Couldn't reach the other side. Try again in a moment.");
        }

        byChannel.put(a.channelId(), session);
        byChannel.put(b.channelId(), session);

        // Peer lobby copy depends on their endpoint kind (guild vs DM)
        ExperienceView peerConnected = a.isDm()
                ? CallPresenter.connectedDm(session)
                : CallPresenter.connected(session);
        publishLobby(chA, a.channelId(), peerConnected);
        logger.info("Call matched {} ({}) <-> {} ({}) source={}",
                a.channelId(), a.kind(), b.channelId(), b.kind(), session.getSource());
        return CallResult.matched(session);
    }

    public MessageCreateData connectedMessage(CallSession session) {
        // Prefer DM copy if the receiver side is a DM (side B is the slash invoker when matched)
        boolean dm = session.getSideB().isDm();
        return ExperienceRenderer.toMessage(dm
                ? CallPresenter.connectedDm(session)
                : CallPresenter.connected(session));
    }

    public void rememberLobbyMessage(String channelId, String messageId) {
        ControlMessageStore.get().put(ControlMessageStore.callLobbyKey(channelId), messageId);
    }

    public boolean publishLobby(MessageChannel channel, String channelId, ExperienceView view) {
        if (channel == null || channelId == null) {
            return false;
        }
        MessageEditData edit = ExperienceRenderer.toEdit(view);
        MessageCreateData create = ExperienceRenderer.toMessage(view);
        Optional<String> existing = ControlMessageStore.get().get(ControlMessageStore.callLobbyKey(channelId));
        if (existing.isPresent()) {
            try {
                channel.editMessageById(existing.get(), edit).queue(
                        ok -> {
                        },
                        err -> channel.sendMessage(create).queue(msg -> rememberLobbyMessage(channelId, msg.getId()))
                );
                return true;
            } catch (Exception e) {
                logger.debug("Lobby edit failed for {}: {}", channelId, e.getMessage());
            }
        }
        channel.sendMessage(create).queue(msg -> rememberLobbyMessage(channelId, msg.getId()));
        return false;
    }

    public boolean tryEditLobby(String channelId, ExperienceView view) {
        MessageChannel channel = ToolSet.getMessageChannel(channelId);
        if (channel == null) {
            return false;
        }
        Optional<String> existing = ControlMessageStore.get().get(ControlMessageStore.callLobbyKey(channelId));
        if (existing.isEmpty()) {
            return false;
        }
        try {
            channel.editMessageById(existing.get(), ExperienceRenderer.toEdit(view)).queue();
            return true;
        } catch (Exception e) {
            logger.debug("tryEditLobby failed for {}: {}", channelId, e.getMessage());
            return false;
        }
    }

    public synchronized EndOutcome end(String channelId) {
        CallSession session = byChannel.get(channelId);
        if (session == null) {
            if (queue.remove(channelId)) {
                boolean edited = tryEditLobby(channelId, CallPresenter.leftQueue());
                ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(channelId));
                return new EndOutcome(edited, ExperienceRenderer.toMessage(CallPresenter.leftQueue()));
            }
            return new EndOutcome(false, ExperienceRenderer.toMessage(CallPresenter.noCall()));
        }

        session.end();
        int messageCount = session.getMessages().size();
        long minutes = Duration.between(session.getStartedAt(), Instant.now()).toMinutes();
        String sessionId = session.getId();
        String otherId = session.otherChannelId(channelId);

        ExperienceView selfEnded = CallPresenter.ended(sessionId, minutes, messageCount);
        ExperienceView peerEnded = CallPresenter.peerHungUp(sessionId, minutes, messageCount);

        boolean editedSelf = tryEditLobby(channelId, selfEnded);
        if (!editedSelf) {
            MessageChannel self = ToolSet.getMessageChannel(channelId);
            if (self != null) {
                self.sendMessage(ExperienceRenderer.toMessage(selfEnded))
                        .queue(msg -> rememberLobbyMessage(channelId, msg.getId()));
            }
        }

        MessageChannel other = ToolSet.getMessageChannel(otherId);
        if (other != null) {
            boolean editedOther = tryEditLobby(otherId, peerEnded);
            if (!editedOther) {
                other.sendMessage(ExperienceRenderer.toMessage(peerEnded))
                        .queue(msg -> rememberLobbyMessage(otherId, msg.getId()));
            }
        }

        finalize(session);
        ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(channelId));
        ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(otherId));
        return new EndOutcome(editedSelf, ExperienceRenderer.toMessage(selfEnded));
    }

    public boolean isInCall(String channelId) {
        return byChannel.containsKey(channelId);
    }

    public boolean userAlreadyInCall(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        for (CallSession s : byChannel.values()) {
            if (userId.equals(s.getSideA().starterUserId()) || userId.equals(s.getSideB().starterUserId())) {
                return true;
            }
        }
        return false;
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
        MessageChannel dest = ToolSet.getMessageChannel(session.otherChannelId(channelId));
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
        MessageChannel a = ToolSet.getMessageChannel(session.getChannelA());
        MessageChannel b = ToolSet.getMessageChannel(session.getChannelB());
        ExperienceView lost = CallPresenter.warn("Connection lost", "The call ended unexpectedly.");
        if (a != null) {
            publishLobby(a, session.getChannelA(), lost);
        }
        if (b != null) {
            publishLobby(b, session.getChannelB(), lost);
        }
        finalize(session);
        ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(session.getChannelA()));
        ControlMessageStore.get().remove(ControlMessageStore.callLobbyKey(session.getChannelB()));
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

    public record EndOutcome(boolean editedInPlace, MessageCreateData message) {
    }
}
