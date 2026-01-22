package nl.finnt730;

import com.jsonstructure.DynamicJson;
import haxe.root.JsonStructureLib;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import nl.finnt730.commands.CommandMigrator;
import nl.finnt730.listeners.CommandListener;
import nl.finnt730.listeners.DeleteListener;
import nl.finnt730.listeners.PasteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumSet;

public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Logger discordLogger = LoggerFactory.getLogger("nl.finnt730.discord");
    
    public static void main(String[] args) {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
                logger.info("Created logs directory: {}", logsDir.getAbsolutePath());
            }
            
            logger.info("Starting ForgeBot initialization...");
            
            // Initialize database and migrate existing commands
            logger.info("Initializing database...");
            DatabaseManager.getInstance(); // This will create the database and load cache
            logger.info("Database initialization completed successfully");
            
            // Migrate existing JSON commands to database (only if they exist)
            logger.info("Migrating existing commands...");
            CommandMigrator.migrateAllCommands();
            logger.info("Command migration completed successfully");
            
            // Migrate user database
            logger.info("Migrating user database...");
            CommandMigrator.migrateUserDatabase();
            logger.info("User database migration completed successfully");
            
            // Load environment configuration
            logger.info("Loading environment configuration...");
            DynamicJson json = JsonStructureLib.createReader().readFile("env.json");
            String botToken = json.getString("botToken", "");
            
            if (botToken.isEmpty()) {
                logger.error("Bot token is empty or not found in env.json");
                throw new IllegalStateException("Bot token is required but not found");
            }
            logger.info("Environment configuration loaded successfully");

            discordLogger.info("Starting Discord bot...");
            JDABuilder.createLight(botToken, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_POLLS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS))
                    .addEventListeners(new CommandListener())
                    .addEventListeners(new PasteListener())
                    .addEventListeners(new DeleteListener())
                    .enableCache(CacheFlag.ROLE_TAGS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL) // Would do ONLINE but I don't think that will work if you aren't literally set to Online status.
                    .build();
            
            discordLogger.info("Discord bot started successfully");
            logger.info("ForgeBot initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start ForgeBot", e);
            System.err.println("Failed to start ForgeBot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}