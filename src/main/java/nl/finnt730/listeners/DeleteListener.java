package nl.finnt730.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.finnt730.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("nl.finnt730.delete");
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        try {
            var orig = event.getMessageId();
            var hist = event.getChannel().getHistoryAround(orig, 5).submit().get().getRetrievedHistory(); // Nearby history centered around deleted message.
            for (Message message : hist) {
                var ref = message.getMessageReference();
                if (ref != null && ref.getMessageId().equals(orig) && message.getContentRaw().equals(Utils.youAreInTheWrongChannel)) {
                    logger.debug("Auto-deleted {} from {}",orig, event.getChannel().getName());
                    message.delete().queue();
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}
