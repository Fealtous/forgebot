package nl.finnt730.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.finnt730.util.Utils;

public class GnomeBotDevListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("nl.finnt730.linkconverter");
	private static final String GNOMEBOT_DOMAIN = "gnomebot.dev";
	private static final String MCLOGS_PATH = "/paste/mclogs/";
	private static final String LINK_EMOJI = "🔗";
	private static final Emoji LINK_EMOJI_OBJ = Emoji.fromUnicode(LINK_EMOJI);

	private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\((<?https://"
			+ Pattern.quote(GNOMEBOT_DOMAIN) + Pattern.quote(MCLOGS_PATH) + "([a-zA-Z0-9]+)>?)\\)");

	private static final Pattern BARE_LINK_PATTERN = Pattern
			.compile("(https://" + Pattern.quote(GNOMEBOT_DOMAIN) + Pattern.quote(MCLOGS_PATH) + "([a-zA-Z0-9]+))");

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		try {
			if (event.getAuthor().isBot()) {
				return;
			}

			// Only react to CA dump messages
			if (Utils.isProbablyCADump(event.getMessage())) {
				logger.debug("Detected CA dump from user {}", event.getAuthor().getName());
				event.getMessage().addReaction(LINK_EMOJI_OBJ).queue();
			}
		} catch (Exception e) {
			logger.error("Error processing message for link conversion setup", e);
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		try {
			if (event.getUser().isBot()) {
				return;
			}

			if (LINK_EMOJI.equals(event.getReaction().getEmoji().getName())) {
				logger.info("Link conversion reaction triggered by user {} on message {}", event.getUser().getName(),
						event.getMessageId());

				event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
					message.clearReactions().queue(v -> {
						try {
							String content = message.getContentRaw();
							String result = extractAndConvertLinks(content);
							if (!result.isEmpty()) {
								message.reply("MCLogs: " + result).mentionRepliedUser(false).queue();
							}
						} catch (Exception e) {
							logger.error("Error processing link conversion reaction", e);
						}
					});
				});
			}
		} catch (Exception e) {
			logger.error("Error handling link conversion reaction", e);
		}
	}

	private String extractAndConvertLinks(String content) {
		StringBuilder output = new StringBuilder();

		Matcher markdownMatcher = MARKDOWN_LINK_PATTERN.matcher(content);
		while (markdownMatcher.find()) {
			String rawLabel = markdownMatcher.group(1);
			String linkId = markdownMatcher.group(3);
			String mcloLink = "https://mclo.gs/" + linkId;

			String label = (rawLabel != null) ? rawLabel.trim() : "";
			if (label.isEmpty()) {
				label = "link";
			}

			if (output.length() > 0)
				output.append(" | ");
			output.append("[").append(label).append("](<").append(mcloLink).append(">)");
		}

		String withoutMarkdown = MARKDOWN_LINK_PATTERN.matcher(content).replaceAll("");
		Matcher bareMatcher = BARE_LINK_PATTERN.matcher(withoutMarkdown);
		while (bareMatcher.find()) {
			String linkId = bareMatcher.group(2);
			String mcloLink = "https://mclo.gs/" + linkId;

			if (output.length() > 0)
				output.append(" | ");
			output.append("<").append(mcloLink).append(">");
		}

		return output.toString();
	}
}