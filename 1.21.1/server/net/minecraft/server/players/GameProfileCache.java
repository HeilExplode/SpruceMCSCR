/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.authlib.ProfileLookupCallback
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class GameProfileCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GAMEPROFILES_MRU_LIMIT = 1000;
    private static final int GAMEPROFILES_EXPIRATION_MONTHS = 1;
    private static boolean usesAuthentication;
    private final Map<String, GameProfileInfo> profilesByName = Maps.newConcurrentMap();
    private final Map<UUID, GameProfileInfo> profilesByUUID = Maps.newConcurrentMap();
    private final Map<String, CompletableFuture<Optional<GameProfile>>> requests = Maps.newConcurrentMap();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File file;
    private final AtomicLong operationCount = new AtomicLong();
    @Nullable
    private Executor executor;

    public GameProfileCache(GameProfileRepository gameProfileRepository, File file) {
        this.profileRepository = gameProfileRepository;
        this.file = file;
        Lists.reverse(this.load()).forEach(this::safeAdd);
    }

    private void safeAdd(GameProfileInfo gameProfileInfo) {
        GameProfile gameProfile = gameProfileInfo.getProfile();
        gameProfileInfo.setLastAccess(this.getNextOperation());
        this.profilesByName.put(gameProfile.getName().toLowerCase(Locale.ROOT), gameProfileInfo);
        this.profilesByUUID.put(gameProfile.getId(), gameProfileInfo);
    }

    private static Optional<GameProfile> lookupGameProfile(GameProfileRepository gameProfileRepository, String string) {
        if (!StringUtil.isValidPlayerName(string)) {
            return GameProfileCache.createUnknownProfile(string);
        }
        final AtomicReference atomicReference = new AtomicReference();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

            public void onProfileLookupSucceeded(GameProfile gameProfile) {
                atomicReference.set(gameProfile);
            }

            public void onProfileLookupFailed(String string, Exception exception) {
                atomicReference.set(null);
            }
        };
        gameProfileRepository.findProfilesByNames(new String[]{string}, profileLookupCallback);
        GameProfile gameProfile = (GameProfile)atomicReference.get();
        return gameProfile != null ? Optional.of(gameProfile) : GameProfileCache.createUnknownProfile(string);
    }

    private static Optional<GameProfile> createUnknownProfile(String string) {
        if (GameProfileCache.usesAuthentication()) {
            return Optional.empty();
        }
        return Optional.of(UUIDUtil.createOfflineProfile(string));
    }

    public static void setUsesAuthentication(boolean bl) {
        usesAuthentication = bl;
    }

    private static boolean usesAuthentication() {
        return usesAuthentication;
    }

    public void add(GameProfile gameProfile) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(2, 1);
        Date date = calendar.getTime();
        GameProfileInfo gameProfileInfo = new GameProfileInfo(gameProfile, date);
        this.safeAdd(gameProfileInfo);
        this.save();
    }

    private long getNextOperation() {
        return this.operationCount.incrementAndGet();
    }

    public Optional<GameProfile> get(String string) {
        Optional<GameProfile> optional;
        String string2 = string.toLowerCase(Locale.ROOT);
        GameProfileInfo gameProfileInfo = this.profilesByName.get(string2);
        boolean bl = false;
        if (gameProfileInfo != null && new Date().getTime() >= gameProfileInfo.expirationDate.getTime()) {
            this.profilesByUUID.remove(gameProfileInfo.getProfile().getId());
            this.profilesByName.remove(gameProfileInfo.getProfile().getName().toLowerCase(Locale.ROOT));
            bl = true;
            gameProfileInfo = null;
        }
        if (gameProfileInfo != null) {
            gameProfileInfo.setLastAccess(this.getNextOperation());
            optional = Optional.of(gameProfileInfo.getProfile());
        } else {
            optional = GameProfileCache.lookupGameProfile(this.profileRepository, string2);
            if (optional.isPresent()) {
                this.add(optional.get());
                bl = false;
            }
        }
        if (bl) {
            this.save();
        }
        return optional;
    }

    public CompletableFuture<Optional<GameProfile>> getAsync(String string) {
        if (this.executor == null) {
            throw new IllegalStateException("No executor");
        }
        CompletableFuture<Optional<GameProfile>> completableFuture = this.requests.get(string);
        if (completableFuture != null) {
            return completableFuture;
        }
        CompletionStage completionStage = CompletableFuture.supplyAsync(() -> this.get(string), Util.backgroundExecutor()).whenCompleteAsync((optional, throwable) -> this.requests.remove(string), this.executor);
        this.requests.put(string, (CompletableFuture<Optional<GameProfile>>)completionStage);
        return completionStage;
    }

    public Optional<GameProfile> get(UUID uUID) {
        GameProfileInfo gameProfileInfo = this.profilesByUUID.get(uUID);
        if (gameProfileInfo == null) {
            return Optional.empty();
        }
        gameProfileInfo.setLastAccess(this.getNextOperation());
        return Optional.of(gameProfileInfo.getProfile());
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void clearExecutor() {
        this.executor = null;
    }

    private static DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public List<GameProfileInfo> load() {
        ArrayList arrayList = Lists.newArrayList();
        try (BufferedReader bufferedReader = Files.newReader((File)this.file, (Charset)StandardCharsets.UTF_8);){
            JsonArray jsonArray = (JsonArray)this.gson.fromJson((Reader)bufferedReader, JsonArray.class);
            if (jsonArray == null) {
                ArrayList arrayList2 = arrayList;
                return arrayList2;
            }
            DateFormat dateFormat = GameProfileCache.createDateFormat();
            jsonArray.forEach(jsonElement -> GameProfileCache.readGameProfile(jsonElement, dateFormat).ifPresent(arrayList::add));
            return arrayList;
        }
        catch (FileNotFoundException fileNotFoundException) {
            return arrayList;
        }
        catch (JsonParseException | IOException throwable) {
            LOGGER.warn("Failed to load profile cache {}", (Object)this.file, (Object)throwable);
        }
        return arrayList;
    }

    public void save() {
        JsonArray jsonArray = new JsonArray();
        DateFormat dateFormat = GameProfileCache.createDateFormat();
        this.getTopMRUProfiles(1000).forEach(gameProfileInfo -> jsonArray.add(GameProfileCache.writeGameProfile(gameProfileInfo, dateFormat)));
        String string = this.gson.toJson((JsonElement)jsonArray);
        try (BufferedWriter bufferedWriter = Files.newWriter((File)this.file, (Charset)StandardCharsets.UTF_8);){
            bufferedWriter.write(string);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private Stream<GameProfileInfo> getTopMRUProfiles(int n) {
        return ImmutableList.copyOf(this.profilesByUUID.values()).stream().sorted(Comparator.comparing(GameProfileInfo::getLastAccess).reversed()).limit(n);
    }

    private static JsonElement writeGameProfile(GameProfileInfo gameProfileInfo, DateFormat dateFormat) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", gameProfileInfo.getProfile().getName());
        jsonObject.addProperty("uuid", gameProfileInfo.getProfile().getId().toString());
        jsonObject.addProperty("expiresOn", dateFormat.format(gameProfileInfo.getExpirationDate()));
        return jsonObject;
    }

    private static Optional<GameProfileInfo> readGameProfile(JsonElement jsonElement, DateFormat dateFormat) {
        if (jsonElement.isJsonObject()) {
            UUID uUID;
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElement2 = jsonObject.get("name");
            JsonElement jsonElement3 = jsonObject.get("uuid");
            JsonElement jsonElement4 = jsonObject.get("expiresOn");
            if (jsonElement2 == null || jsonElement3 == null) {
                return Optional.empty();
            }
            String string = jsonElement3.getAsString();
            String string2 = jsonElement2.getAsString();
            Date date = null;
            if (jsonElement4 != null) {
                try {
                    date = dateFormat.parse(jsonElement4.getAsString());
                }
                catch (ParseException parseException) {
                    // empty catch block
                }
            }
            if (string2 == null || string == null || date == null) {
                return Optional.empty();
            }
            try {
                uUID = UUID.fromString(string);
            }
            catch (Throwable throwable) {
                return Optional.empty();
            }
            return Optional.of(new GameProfileInfo(new GameProfile(uUID, string2), date));
        }
        return Optional.empty();
    }

    static class GameProfileInfo {
        private final GameProfile profile;
        final Date expirationDate;
        private volatile long lastAccess;

        GameProfileInfo(GameProfile gameProfile, Date date) {
            this.profile = gameProfile;
            this.expirationDate = date;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }

        public void setLastAccess(long l) {
            this.lastAccess = l;
        }

        public long getLastAccess() {
            return this.lastAccess;
        }
    }
}

