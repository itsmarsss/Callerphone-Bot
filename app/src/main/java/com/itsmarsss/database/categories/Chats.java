package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;
import com.itsmarsss.callerphone.tccallerphone.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Thin facade over {@link ConversationRepository} for backward compatibility.
 */
public class Chats {
    public static final Logger logger = LoggerFactory.getLogger(Chats.class);
    private static final ConversationRepository repository = new ConversationRepository();

    public static boolean createChat(Conversation convo) {
        return repository.save(convo);
    }

    public static Optional<Conversation> queryChat(String id) {
        return repository.findById(id);
    }
}
