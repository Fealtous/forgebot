package nl.finnt730.util;

import net.dv8tion.jda.api.entities.Message;

import java.util.regex.Pattern;

public class Utils {

    private static final Pattern gnomePattern = Pattern.compile("gnomebot\\.dev");
    private static final Pattern modpackLine = Pattern.compile("Mod list changes on top of the modpack");
    private static final Pattern latestLaunch = Pattern.compile("Mod list changes after the latest successful launch");
    public static final String youAreInTheWrongChannel = "This channel is not for user support. Please move to <#1129069799545241703> for assistance. If you have a game log or crash report available, please put them there as well.";

    public static boolean isProbablyCADump(Message message) {
        String raw = message.getContentRaw();
        long linkPatterns = gnomePattern.matcher(raw).results().count();
        if (linkPatterns < 3) return false; // If under 3 this cannot be a CA post as there are ALWAYS at least 3
        long diffPattern = modpackLine.matcher(raw).results().count() + latestLaunch.matcher(raw).results().count();
        return linkPatterns + diffPattern >= 4;
        // We can guarantee that either the message has an excessive # of gnomebot links
        // which absolutely indicates it's a CA dump OR it has the diff pattern plus a ton of links,
        // which is also a guaranteed CA dump
    }

}
