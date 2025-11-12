/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.slf4j.Logger
 */
package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerScores;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class Scoreboard {
    public static final String HIDDEN_SCORE_PREFIX = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<String, Objective> objectivesByName = new Object2ObjectOpenHashMap(16, 0.5f);
    private final Reference2ObjectMap<ObjectiveCriteria, List<Objective>> objectivesByCriteria = new Reference2ObjectOpenHashMap();
    private final Map<String, PlayerScores> playerScores = new Object2ObjectOpenHashMap(16, 0.5f);
    private final Map<DisplaySlot, Objective> displayObjectives = new EnumMap<DisplaySlot, Objective>(DisplaySlot.class);
    private final Object2ObjectMap<String, PlayerTeam> teamsByName = new Object2ObjectOpenHashMap();
    private final Object2ObjectMap<String, PlayerTeam> teamsByPlayer = new Object2ObjectOpenHashMap();

    @Nullable
    public Objective getObjective(@Nullable String string) {
        return (Objective)this.objectivesByName.get((Object)string);
    }

    public Objective addObjective(String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType, boolean bl, @Nullable NumberFormat numberFormat) {
        if (this.objectivesByName.containsKey((Object)string)) {
            throw new IllegalArgumentException("An objective with the name '" + string + "' already exists!");
        }
        Objective objective = new Objective(this, string, objectiveCriteria, component, renderType, bl, numberFormat);
        ((List)this.objectivesByCriteria.computeIfAbsent((Object)objectiveCriteria, object -> Lists.newArrayList())).add(objective);
        this.objectivesByName.put((Object)string, (Object)objective);
        this.onObjectiveAdded(objective);
        return objective;
    }

    public final void forAllObjectives(ObjectiveCriteria objectiveCriteria, ScoreHolder scoreHolder, Consumer<ScoreAccess> consumer) {
        ((List)this.objectivesByCriteria.getOrDefault((Object)objectiveCriteria, Collections.emptyList())).forEach(objective -> consumer.accept(this.getOrCreatePlayerScore(scoreHolder, (Objective)objective, true)));
    }

    private PlayerScores getOrCreatePlayerInfo(String string2) {
        return this.playerScores.computeIfAbsent(string2, string -> new PlayerScores());
    }

    public ScoreAccess getOrCreatePlayerScore(ScoreHolder scoreHolder, Objective objective) {
        return this.getOrCreatePlayerScore(scoreHolder, objective, false);
    }

    public ScoreAccess getOrCreatePlayerScore(final ScoreHolder scoreHolder, final Objective objective, boolean bl) {
        final boolean bl2 = bl || !objective.getCriteria().isReadOnly();
        PlayerScores playerScores = this.getOrCreatePlayerInfo(scoreHolder.getScoreboardName());
        final MutableBoolean mutableBoolean = new MutableBoolean();
        final Score score2 = playerScores.getOrCreate(objective, score -> mutableBoolean.setTrue());
        return new ScoreAccess(){

            @Override
            public int get() {
                return score2.value();
            }

            @Override
            public void set(int n) {
                Component component;
                if (!bl2) {
                    throw new IllegalStateException("Cannot modify read-only score");
                }
                boolean bl = mutableBoolean.isTrue();
                if (objective.displayAutoUpdate() && (component = scoreHolder.getDisplayName()) != null && !component.equals(score2.display())) {
                    score2.display(component);
                    bl = true;
                }
                if (n != score2.value()) {
                    score2.value(n);
                    bl = true;
                }
                if (bl) {
                    this.sendScoreToPlayers();
                }
            }

            @Override
            @Nullable
            public Component display() {
                return score2.display();
            }

            @Override
            public void display(@Nullable Component component) {
                if (mutableBoolean.isTrue() || !Objects.equals(component, score2.display())) {
                    score2.display(component);
                    this.sendScoreToPlayers();
                }
            }

            @Override
            public void numberFormatOverride(@Nullable NumberFormat numberFormat) {
                score2.numberFormat(numberFormat);
                this.sendScoreToPlayers();
            }

            @Override
            public boolean locked() {
                return score2.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean bl) {
                score2.setLocked(bl);
                if (mutableBoolean.isTrue()) {
                    this.sendScoreToPlayers();
                }
                Scoreboard.this.onScoreLockChanged(scoreHolder, objective);
            }

            private void sendScoreToPlayers() {
                Scoreboard.this.onScoreChanged(scoreHolder, objective, score2);
                mutableBoolean.setFalse();
            }
        };
    }

    @Nullable
    public ReadOnlyScoreInfo getPlayerScoreInfo(ScoreHolder scoreHolder, Objective objective) {
        PlayerScores playerScores = this.playerScores.get(scoreHolder.getScoreboardName());
        if (playerScores != null) {
            return playerScores.get(objective);
        }
        return null;
    }

    public Collection<PlayerScoreEntry> listPlayerScores(Objective objective) {
        ArrayList<PlayerScoreEntry> arrayList = new ArrayList<PlayerScoreEntry>();
        this.playerScores.forEach((string, playerScores) -> {
            Score score = playerScores.get(objective);
            if (score != null) {
                arrayList.add(new PlayerScoreEntry((String)string, score.value(), score.display(), score.numberFormat()));
            }
        });
        return arrayList;
    }

    public Collection<Objective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<ScoreHolder> getTrackedPlayers() {
        return this.playerScores.keySet().stream().map(ScoreHolder::forNameOnly).toList();
    }

    public void resetAllPlayerScores(ScoreHolder scoreHolder) {
        PlayerScores playerScores = this.playerScores.remove(scoreHolder.getScoreboardName());
        if (playerScores != null) {
            this.onPlayerRemoved(scoreHolder);
        }
    }

    public void resetSinglePlayerScore(ScoreHolder scoreHolder, Objective objective) {
        PlayerScores playerScores = this.playerScores.get(scoreHolder.getScoreboardName());
        if (playerScores != null) {
            boolean bl = playerScores.remove(objective);
            if (!playerScores.hasScores()) {
                PlayerScores playerScores2 = this.playerScores.remove(scoreHolder.getScoreboardName());
                if (playerScores2 != null) {
                    this.onPlayerRemoved(scoreHolder);
                }
            } else if (bl) {
                this.onPlayerScoreRemoved(scoreHolder, objective);
            }
        }
    }

    public Object2IntMap<Objective> listPlayerScores(ScoreHolder scoreHolder) {
        PlayerScores playerScores = this.playerScores.get(scoreHolder.getScoreboardName());
        return playerScores != null ? playerScores.listScores() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(Objective objective) {
        this.objectivesByName.remove((Object)objective.getName());
        for (DisplaySlot displaySlot : DisplaySlot.values()) {
            if (this.getDisplayObjective(displaySlot) != objective) continue;
            this.setDisplayObjective(displaySlot, null);
        }
        List list = (List)this.objectivesByCriteria.get((Object)objective.getCriteria());
        if (list != null) {
            list.remove(objective);
        }
        for (PlayerScores playerScores : this.playerScores.values()) {
            playerScores.remove(objective);
        }
        this.onObjectiveRemoved(objective);
    }

    public void setDisplayObjective(DisplaySlot displaySlot, @Nullable Objective objective) {
        this.displayObjectives.put(displaySlot, objective);
    }

    @Nullable
    public Objective getDisplayObjective(DisplaySlot displaySlot) {
        return this.displayObjectives.get(displaySlot);
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String string) {
        return (PlayerTeam)this.teamsByName.get((Object)string);
    }

    public PlayerTeam addPlayerTeam(String string) {
        PlayerTeam playerTeam = this.getPlayerTeam(string);
        if (playerTeam != null) {
            LOGGER.warn("Requested creation of existing team '{}'", (Object)string);
            return playerTeam;
        }
        playerTeam = new PlayerTeam(this, string);
        this.teamsByName.put((Object)string, (Object)playerTeam);
        this.onTeamAdded(playerTeam);
        return playerTeam;
    }

    public void removePlayerTeam(PlayerTeam playerTeam) {
        this.teamsByName.remove((Object)playerTeam.getName());
        for (String string : playerTeam.getPlayers()) {
            this.teamsByPlayer.remove((Object)string);
        }
        this.onTeamRemoved(playerTeam);
    }

    public boolean addPlayerToTeam(String string, PlayerTeam playerTeam) {
        if (this.getPlayersTeam(string) != null) {
            this.removePlayerFromTeam(string);
        }
        this.teamsByPlayer.put((Object)string, (Object)playerTeam);
        return playerTeam.getPlayers().add(string);
    }

    public boolean removePlayerFromTeam(String string) {
        PlayerTeam playerTeam = this.getPlayersTeam(string);
        if (playerTeam != null) {
            this.removePlayerFromTeam(string, playerTeam);
            return true;
        }
        return false;
    }

    public void removePlayerFromTeam(String string, PlayerTeam playerTeam) {
        if (this.getPlayersTeam(string) != playerTeam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
        }
        this.teamsByPlayer.remove((Object)string);
        playerTeam.getPlayers().remove(string);
    }

    public Collection<String> getTeamNames() {
        return this.teamsByName.keySet();
    }

    public Collection<PlayerTeam> getPlayerTeams() {
        return this.teamsByName.values();
    }

    @Nullable
    public PlayerTeam getPlayersTeam(String string) {
        return (PlayerTeam)this.teamsByPlayer.get((Object)string);
    }

    public void onObjectiveAdded(Objective objective) {
    }

    public void onObjectiveChanged(Objective objective) {
    }

    public void onObjectiveRemoved(Objective objective) {
    }

    protected void onScoreChanged(ScoreHolder scoreHolder, Objective objective, Score score) {
    }

    protected void onScoreLockChanged(ScoreHolder scoreHolder, Objective objective) {
    }

    public void onPlayerRemoved(ScoreHolder scoreHolder) {
    }

    public void onPlayerScoreRemoved(ScoreHolder scoreHolder, Objective objective) {
    }

    public void onTeamAdded(PlayerTeam playerTeam) {
    }

    public void onTeamChanged(PlayerTeam playerTeam) {
    }

    public void onTeamRemoved(PlayerTeam playerTeam) {
    }

    public void entityRemoved(Entity entity) {
        if (entity instanceof Player || entity.isAlive()) {
            return;
        }
        this.resetAllPlayerScores(entity);
        this.removePlayerFromTeam(entity.getScoreboardName());
    }

    protected ListTag savePlayerScores(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        this.playerScores.forEach((string, playerScores) -> playerScores.listRawScores().forEach((objective, score) -> {
            CompoundTag compoundTag = score.write(provider);
            compoundTag.putString("Name", (String)string);
            compoundTag.putString("Objective", objective.getName());
            listTag.add(compoundTag);
        }));
        return listTag;
    }

    protected void loadPlayerScores(ListTag listTag, HolderLookup.Provider provider) {
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Score score = Score.read(compoundTag, provider);
            String string = compoundTag.getString("Name");
            String string2 = compoundTag.getString("Objective");
            Objective objective = this.getObjective(string2);
            if (objective == null) {
                LOGGER.error("Unknown objective {} for name {}, ignoring", (Object)string2, (Object)string);
                continue;
            }
            this.getOrCreatePlayerInfo(string).setScore(objective, score);
        }
    }
}

