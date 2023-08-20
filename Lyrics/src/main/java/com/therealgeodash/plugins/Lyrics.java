package com.therealgeodash.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Http;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.therealgeodash.plugins.lyrics.Data;
import com.therealgeodash.plugins.lyrics.ResponseModel;
import com.aliucord.utils.RxUtils;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.models.commands.ApplicationCommandOption;
import com.discord.models.domain.spotify.ModelSpotifyTrack;
import com.discord.stores.StoreStream;
import com.discord.utilities.spotify.SpotifyApiClient;

import java.util.Arrays;
import java.util.Collections;

import rx.Subscriber;

@SuppressWarnings("unused")
public class Lyrics extends Plugin {

    private static final String baseUrl = "https://utils.azul.one/api/aliucord-lyrics/auto?api_key=52AA3025351E47D1925C24557B48EB4B&query=";
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[] { new Manifest.Author("TheRealGeoDash",710268763844640839L) };
        manifest.description = "Get lyrics to a specific song.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/X1nto/AliucordPlugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        var songNameArg = new ApplicationCommandOption(ApplicationCommandType.STRING, "name", "The song name to search lyrics for", null, false, false, null, null);
        var shouldSendArg = new ApplicationCommandOption(ApplicationCommandType.BOOLEAN, "send", "To send output in the chat or not", null, false, false, null, null);
        var arguments = Arrays.asList(songNameArg, shouldSendArg);

        commands.registerCommand("lyrics", "Grab lyrics to Songs", arguments, ctx -> {
            var shouldSend = ctx.getBool("send");
            final String[] songName = { ctx.getString("name") };

            if (shouldSend == null) {
                shouldSend = false;
            }

            if (songName[0] == null) {
                try {
                    var storeSpotify = StoreStream.getSpotify();

                    var _spotifyApiClient = storeSpotify.getClass().getDeclaredField("spotifyApiClient");
                    _spotifyApiClient.setAccessible(true);

                    var spotifyApiClient = (SpotifyApiClient) _spotifyApiClient.get(storeSpotify);

                    if (spotifyApiClient == null) {
                        return new CommandsAPI.CommandResult("Failed to get current Spotify activity.", null, false);
                    }

                    //This sucks so much
                    RxUtils.subscribe(spotifyApiClient.getSpotifyTrack(), new Subscriber<>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable th) {
                            songName[0] = "";
                        }

                        @Override
                        public void onNext(ModelSpotifyTrack modelSpotifyTrack) {
                            songName[0] = String.format("%s - %s", modelSpotifyTrack.getName(), modelSpotifyTrack.getArtists().get(0).getName());
                        }
                    });

                } catch (Exception ignored) {}
            }

            //don't fetch anything until songName is not null
            //noinspection StatementWithEmptyBody, LoopConditionNotUpdatedInsideLoop
            while (songName[0] == null) {}

            if (songName[0].equals("")) {
                return new CommandsAPI.CommandResult("Failed to get current Spotify activity.", null, false);
            }

            try {
                var data = fetch(songName[0]);
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

    private CommandsAPI.CommandResult lyricsText(Data data) {
        var lyrics = data.lyrics;

        if (lyrics.length() > MAX_MESSAGE_LENGTH) {
            var fullLyricsText = String.format("\n\nFull Lyrics: %s", data.url);
            lyrics = lyrics.substring(0, MAX_MESSAGE_LENGTH - fullLyricsText.length() - 3) + "..." + fullLyricsText;
        }

        return new CommandsAPI.CommandResult(lyrics);
    }

    private CommandsAPI.CommandResult lyricsEmbed(Data data) {
        var embed = new MessageEmbedBuilder()
                .setAuthor(data.artist, null, null)
                .setTitle(data.name)
                .setDescription(data.lyrics)
                .setUrl(data.url)
                .setThumbnail(data.album_art)
                .setColor(0x209CEE)
                .setFooter(String.format("%s | Powered by Azuleum", data.artist, "https://external-content.duckduckgo.com/iu/?u=https://azul.one/assets/icon.png"))
                .build();

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed), false, "Lyrics");
    }

    private Data fetch(String song) throws Exception {
        var responseModel = (ResponseModel) Http.simpleJsonGet(baseUrl + song, ResponseModel.class);
        return responseModel.data.get(0);
    }

}
