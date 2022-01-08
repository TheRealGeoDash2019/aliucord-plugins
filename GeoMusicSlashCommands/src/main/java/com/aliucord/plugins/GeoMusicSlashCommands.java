package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.Http;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.utils.*;
import com.aliucord.plugins.geomusic.ApiResponse;
import com.discord.api.commands.ApplicationCommandType;

import java.io.IOException;
import java.util.Collections;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class GeoMusicSlashCommands extends Plugin {
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {
        /*
        commands.registerCommand(
                "botUptime",
                "Get Bot Uptime",
                Collections.emptyList(),
                ctx -> {
                    try {
                        // Fetch the api and deserialize the resulting Json string into the ApiResponse class
                        ApiResponse res = Http.simpleJsonGet("https://geomusic.tech/api/v2/bot/stats", ApiResponse.class);
                        String uptime = "Bot Uptime: ";
                        String result = uptime + res.uptime;
                        // Build a nice embed
                        var eb = new MessageEmbedBuilder()
                                .setRandomColor()
                                .setTitle("API Response")
                                // You should specify height and width but we don't have it in this case, so we have to use -1 (the default)
                                // Thus the image will end up square regardless of its dimensions. In a real plugin you would probably want to
                                // load the image and get its dimensions
                                .setDescription(result)
                                .setFooter("Powered by Geo Music");
                        // Embeds must be a list, so create a list with  the embed we just built
                        var embeds = Collections.singletonList(eb.build());
                        return new CommandsAPI.CommandResult(null, embeds, false);
                    } catch (IOException ex) {
                        return new CommandsAPI.CommandResult("I'm so sorry... Something went wrong while fetching", null, false);
                    }
                }
        );
        */

        commands.registerCommand("helloWorld", "Say Hello World", ctx -> {
            return new CommandsAPI.CommandResult("Hello World", null, false);
        });

        commands.registerCommand(
            "helloPerson",
            "Say Hello to a Person",
            Arrays.asList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "name", "Person to say hello to"),
                Utils.createCommandOption(ApplicationCommandType.USER, "user", "User to say hello to")
            ),
            ctx -> {
                // Check if a user argument was passed
                if (ctx.containsArg("user")) {
                    var user = ctx.getRequiredUser("user");
                    return new CommandsAPI.CommandResult("Hello " + user.getUsername() + "!");
                } else {
                    // Returns either the argument value if present, or the defaultValue ("World" in this case)
                    var name = ctx.getStringOrDefault("name", "World");
                    return new CommandsAPI.CommandResult("Hello " + name + "!");
                }
            }
        );
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Unregisters all commands
        commands.unregisterAll();
    }
}
