package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Http;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.models.commands.ApplicationCommandOption;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Lyrics extends Plugin {

    private static final String baseUrl = "https://lyrics.therealgeodash.workers.dev/?title=";
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @Override
    public void start(Context context) {
        ApplicationCommandOption songNameArg = new ApplicationCommandOption(ApplicationCommandType.STRING, "name", "The song name to search lyrics for", null, true, false, null, null);
        ApplicationCommandOption shouldSendArg = new ApplicationCommandOption(ApplicationCommandType.BOOLEAN, "send", "To send output in the chat or not", null, false, false, null, null);
        List<ApplicationCommandOption> arguments = Arrays.asList(songNameArg, shouldSendArg);

        commands.registerCommand("lyrics", "Grab a song lyrics", arguments, args -> {
            Boolean shouldSend = (Boolean) args.get("send");

            if (shouldSend == null) {
                shouldSend = false;
            }

            String songName = (String) args.get("name");
            try {
                ResponseModel.Data data = fetch(songName);
                return shouldSend ? lyricsText(data) : lyricsEmbed(data);
            } catch (Exception e) {
                e.printStackTrace();
                return new CommandsAPI.CommandResult("Failed to fetch data", null, false);
            }
        });
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }

    private CommandsAPI.CommandResult lyricsText(ResponseModel.Data data) {
        String lyrics = data.lyrics;

        if (lyrics.length() > MAX_MESSAGE_LENGTH) {
            String fullLyricsText = String.format("\n\nFull Lyrics: %s", data.url);
            lyrics = lyrics.substring(0, MAX_MESSAGE_LENGTH - fullLyricsText.length() - 3) + "..." + fullLyricsText;
        }

        return new CommandsAPI.CommandResult(lyrics);
    }

    private CommandsAPI.CommandResult lyricsEmbed(ResponseModel.Data data) {
        MessageEmbed embed = new MessageEmbedBuilder()
                .setAuthor(data.artist.name, null, null)
                .setTitle(data.title)
                .setDescription(data.lyrics)
                .setUrl(data.url)
                .setColor(0x209CEE)
                .setFooter(String.format("Lyrics provided by Geo Music | © %s", data.artist.name), "https://external-content.duckduckgo.com/iu/?u=https://cdn.discordapp.com/avatars/806224910284095518/cd043a987352c57fafb97b3913537d0c.webp?size=256")
                .build();

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed), false, "Lyrics", data.image);
    }

    private ResponseModel.Data fetch(String song) throws Exception {
        ResponseModel responseModel = Http.simpleJsonGet(baseUrl + song, ResponseModel.class);
        return responseModel.data.get(0);
    }

    @SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})
    public static class ResponseModel {

        private List<Data> data;

        private static class Data {
            private String lyrics;
            private Object artist;
            private String image;
            private String title;
            private String url;
        }

    }

}
