package net.minestom.server.scoreboard;

import com.google.common.collect.MapMaker;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This object represents a team on a scoreboard that has a common display theme and other properties.
 */
public class Team implements PacketGroupingAudience {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    /**
     * A collection of all registered entities who are on the team.
     */
    private final Set<String> members;

    /**
     * The registry name of the team.
     */
    private final String teamName;
    /**
     * The display name of the team.
     */
    private Component teamDisplayName;
    /**
     * A BitMask.
     */
    private byte friendlyFlags;
    /**
     * The visibility of the team.
     */
    private NameTagVisibility nameTagVisibility;
    /**
     * The collision rule of the team.
     */
    private CollisionRule collisionRule;

    /**
     * Used to color the name of players on the team <br>
     * The color of a team defines how the names of the team members are visualized.
     */
    private NamedTextColor teamColor;

    /**
     * Shown before the names of the players who belong to this team.
     */
    private Component prefix;
    /**
     * Shown after the names of the player who belong to this team.
     */
    private Component suffix;

    private final Set<Player> playerMembers = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    private boolean isPlayerMembersUpToDate;

    // Adventure
    private final Pointers pointers;

    /**
     * Default constructor to creates a team.
     *
     * @param teamName The registry name for the team
     */
    protected Team(@NotNull String teamName) {
        this.teamName = teamName;

        this.teamDisplayName = Component.empty();
        this.friendlyFlags = 0x00;
        this.nameTagVisibility = NameTagVisibility.ALWAYS;
        this.collisionRule = CollisionRule.ALWAYS;

        this.teamColor = NamedTextColor.WHITE;
        this.prefix = Component.empty();
        this.suffix = Component.empty();

        this.members = new CopyOnWriteArraySet<>();

        this.pointers = Pointers.builder()
                .withDynamic(Identity.NAME, this::getTeamName)
                .withDynamic(Identity.DISPLAY_NAME, this::getTeamDisplayName)
                .build();
    }

    /**
     * Adds a member to the {@link Team}.
     * <br>
     * This member can be a {@link Player} or an {@link LivingEntity}.
     *
     * @param member The member to be added
     */
    public void addMember(@NotNull String member) {
        // Adds a new member to the team
        this.members.add(member);

        // Initializes add player packet
        final TeamsPacket addPlayerPacket = new TeamsPacket();
        addPlayerPacket.teamName = this.teamName;
        addPlayerPacket.action = TeamsPacket.Action.ADD_PLAYERS_TEAM;
        addPlayerPacket.entities = members.toArray(new String[0]);

        // Sends to all online players the add player packet
        PacketUtils.sendGroupedPacket(CONNECTION_MANAGER.getOnlinePlayers(), addPlayerPacket);

        // invalidate player members
        this.isPlayerMembersUpToDate = false;
    }

    /**
     * Removes a member from the {@link Team}.
     *
     * @param member The member to be removed
     */
    public void removeMember(@NotNull String member) {
        // Initializes remove player packet
        final TeamsPacket removePlayerPacket = new TeamsPacket();
        removePlayerPacket.teamName = this.teamName;
        removePlayerPacket.action = TeamsPacket.Action.REMOVE_PLAYERS_TEAM;
        removePlayerPacket.entities = new String[]{member};

        // Sends to all online player teh remove player packet
        PacketUtils.sendGroupedPacket(CONNECTION_MANAGER.getOnlinePlayers(), removePlayerPacket);

        // Removes the member from the team
        this.members.remove(member);

        // invalidate player members
        this.isPlayerMembersUpToDate = false;
    }

    /**
     * Changes the display name of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed <b>server side</b>.
     *
     * @param teamDisplayName The new display name
     * @deprecated Use {@link #setTeamDisplayName(Component)}
     */
    @Deprecated
    public void setTeamDisplayName(JsonMessage teamDisplayName) {
        this.setTeamDisplayName(teamDisplayName.asComponent());
    }

    /**
     * Changes the display name of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed <b>server side</b>.
     *
     * @param teamDisplayName The new display name
     */
    public void setTeamDisplayName(Component teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    /**
     * Changes the display name of the team and sends an update packet.
     *
     * @param teamDisplayName The new display name
     * @deprecated Use {@link #updateTeamDisplayName(Component)}
     */
    @Deprecated
    public void updateTeamDisplayName(JsonMessage teamDisplayName) {
        this.updateTeamDisplayName(teamDisplayName.asComponent());
    }

    /**
     * Changes the display name of the team and sends an update packet.
     *
     * @param teamDisplayName The new display name
     */
    public void updateTeamDisplayName(Component teamDisplayName) {
        this.setTeamDisplayName(teamDisplayName);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link NameTagVisibility} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param visibility The new tag visibility
     * @see #updateNameTagVisibility(NameTagVisibility)
     */
    public void setNameTagVisibility(@NotNull NameTagVisibility visibility) {
        this.nameTagVisibility = visibility;
    }

    /**
     * Changes the {@link NameTagVisibility} of the team and sends an update packet.
     *
     * @param nameTagVisibility The new tag visibility
     */
    public void updateNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        this.setNameTagVisibility(nameTagVisibility);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link CollisionRule} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param rule The new rule
     * @see #updateCollisionRule(CollisionRule)
     */
    public void setCollisionRule(@NotNull CollisionRule rule) {
        this.collisionRule = rule;
    }

    /**
     * Changes the collision rule of the team and sends an update packet.
     *
     * @param collisionRule The new collision rule
     */
    public void updateCollisionRule(@NotNull CollisionRule collisionRule) {
        this.setCollisionRule(collisionRule);
        sendUpdatePacket();
    }

    /**
     * Changes the color of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param color The new team color
     * @see #updateTeamColor(ChatColor)
     * @deprecated Use {@link #setTeamColor(NamedTextColor)}
     */
    @Deprecated
    public void setTeamColor(@NotNull ChatColor color) {
        this.setTeamColor(NamedTextColor.nearestTo(color.asTextColor()));
    }

    /**
     * Changes the color of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param color The new team color
     * @see #updateTeamColor(NamedTextColor)
     */
    public void setTeamColor(@NotNull NamedTextColor color) {
        this.teamColor = color;
    }

    /**
     * Changes the color of the team and sends an update packet.
     *
     * @param chatColor The new team color
     * @deprecated Use {@link #updateTeamColor(NamedTextColor)}
     */
    @Deprecated
    public void updateTeamColor(@NotNull ChatColor chatColor) {
        this.updateTeamColor(NamedTextColor.nearestTo(chatColor.asTextColor()));
    }

    /**
     * Changes the color of the team and sends an update packet.
     *
     * @param color The new team color
     */
    public void updateTeamColor(@NotNull NamedTextColor color) {
        this.setTeamColor(color);
        sendUpdatePacket();
    }

    /**
     * Changes the prefix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param prefix The new prefix
     * @deprecated Use {@link #setPrefix(Component)}
     */
    @Deprecated
    public void setPrefix(JsonMessage prefix) {
        this.setPrefix(prefix.asComponent());
    }

    /**
     * Changes the prefix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param prefix The new prefix
     */
    public void setPrefix(Component prefix) {
        this.prefix = prefix;
    }

    /**
     * Changes the prefix of the team and sends an update packet.
     *
     * @param prefix The new prefix
     * @deprecated Use {@link #updatePrefix(Component)}
     */
    @Deprecated
    public void updatePrefix(JsonMessage prefix) {
        this.updatePrefix(prefix.asComponent());
    }

    /**
     * Changes the prefix of the team and sends an update packet.
     *
     * @param prefix The new prefix
     */
    public void updatePrefix(Component prefix) {
        this.setPrefix(prefix);
        sendUpdatePacket();
    }

    /**
     * Changes the suffix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param suffix The new suffix
     * @deprecated Use {@link #setSuffix(Component)}
     */
    @Deprecated
    public void setSuffix(JsonMessage suffix) {
        this.setSuffix(suffix.asComponent());
    }

    /**
     * Changes the suffix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param suffix The new suffix
     */
    public void setSuffix(Component suffix) {
        this.suffix = suffix;
    }

    /**
     * Changes the suffix of the team and sends an update packet.
     *
     * @param suffix The new suffix
     * @deprecated Use {@link #updateSuffix(Component)}
     */
    @Deprecated
    public void updateSuffix(JsonMessage suffix) {
        this.updateSuffix(suffix.asComponent());
    }

    /**
     * Changes the suffix of the team and sends an update packet.
     *
     * @param suffix The new suffix
     */
    public void updateSuffix(Component suffix) {
        this.setSuffix(suffix);
        sendUpdatePacket();
    }

    /**
     * Changes the friendly flags of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param flag The new friendly flag
     */
    public void setFriendlyFlags(byte flag) {
        this.friendlyFlags = flag;
    }

    /**
     * Changes the friendly flags of the team and sends an update packet.
     *
     * @param flag The new friendly flag
     */
    public void updateFriendlyFlags(byte flag) {
        this.setFriendlyFlags(flag);
        this.sendUpdatePacket();
    }

    /**
     * Gets the registry name of the team.
     *
     * @return the registry name
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Creates the creation packet to add a team.
     *
     * @return the packet to add the team
     */
    @NotNull
    public TeamsPacket createTeamsCreationPacket() {
        TeamsPacket teamsCreationPacket = new TeamsPacket();
        teamsCreationPacket.teamName = teamName;
        teamsCreationPacket.action = TeamsPacket.Action.CREATE_TEAM;
        teamsCreationPacket.teamDisplayName = this.teamDisplayName;
        teamsCreationPacket.friendlyFlags = this.friendlyFlags;
        teamsCreationPacket.nameTagVisibility = this.nameTagVisibility;
        teamsCreationPacket.collisionRule = this.collisionRule;
        teamsCreationPacket.teamColor = this.teamColor;
        teamsCreationPacket.teamPrefix = this.prefix;
        teamsCreationPacket.teamSuffix = this.suffix;
        teamsCreationPacket.entities = this.members.toArray(new String[0]);

        return teamsCreationPacket;
    }

    /**
     * Creates an destruction packet to remove the team.
     *
     * @return the packet to remove the team
     */
    @NotNull
    public TeamsPacket createTeamDestructionPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        return teamsPacket;
    }

    /**
     * Obtains an unmodifiable {@link Set} of registered players who are on the team.
     *
     * @return an unmodifiable {@link Set} of registered players
     */
    @NotNull
    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the display name of the team.
     *
     * @return the display name
     * @deprecated Use {@link #getTeamDisplayName()}
     */
    @Deprecated
    public JsonMessage getTeamDisplayNameJson() {
        return JsonMessage.fromComponent(this.teamDisplayName);
    }

    /**
     * Gets the display name of the team.
     *
     * @return the display name
     */
    public Component getTeamDisplayName() {
        return teamDisplayName;
    }

    /**
     * Gets the friendly flags of the team.
     *
     * @return the friendly flags
     */
    public byte getFriendlyFlags() {
        return friendlyFlags;
    }

    /**
     * Gets the tag visibility of the team.
     *
     * @return the tag visibility
     */
    @NotNull
    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Gets the collision rule of the team.
     *
     * @return the collision rule
     */
    @NotNull
    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Gets the color of the team.
     *
     * @return the team color
     * @deprecated Use {@link #getTeamColor()}
     */
    @Deprecated
    @NotNull
    public ChatColor getTeamColorOld() {
        return ChatColor.fromId(AdventurePacketConvertor.getNamedTextColorValue(teamColor));
    }

    /**
     * Gets the color of the team.
     *
     * @return the team color
     */
    @NotNull
    public NamedTextColor getTeamColor() {
        return teamColor;
    }

    /**
     * Gets the prefix of the team.
     *
     * @return the team prefix
     * @deprecated Use {@link #getPrefix()}
     */
    @Deprecated
    public JsonMessage getPrefixJson() {
        return JsonMessage.fromComponent(prefix);
    }

    /**
     * Gets the prefix of the team.
     *
     * @return the team prefix
     */
    public Component getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of the team.
     *
     * @return the suffix team
     * @deprecated Use {@link #getSuffix()}
     */
    @Deprecated
    public JsonMessage getSuffixJson() {
        return JsonMessage.fromComponent(suffix);
    }

    /**
     * Gets the suffix of the team.
     *
     * @return the suffix team
     */
    public Component getSuffix() {
        return suffix;
    }

    /**
     * Sends an {@link TeamsPacket.Action#UPDATE_TEAM_INFO} packet.
     */
    public void sendUpdatePacket() {
        final TeamsPacket updatePacket = new TeamsPacket();
        updatePacket.teamName = this.teamName;
        updatePacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        updatePacket.teamDisplayName = this.teamDisplayName;
        updatePacket.friendlyFlags = this.friendlyFlags;
        updatePacket.nameTagVisibility = this.nameTagVisibility;
        updatePacket.collisionRule = this.collisionRule;
        updatePacket.teamColor = this.teamColor;
        updatePacket.teamPrefix = this.prefix;
        updatePacket.teamSuffix = this.suffix;

        PacketUtils.sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), updatePacket);
    }

    @Override
    public @NotNull Collection<Player> getPlayers() {
        if (!this.isPlayerMembersUpToDate) {
            this.playerMembers.clear();

            for (String member : this.members) {
                Player player = MinecraftServer.getConnectionManager().getPlayer(member);

                if (player != null) {
                    this.playerMembers.add(player);
                }
            }

            this.isPlayerMembersUpToDate = true;
        }

        return this.playerMembers;
    }

    @Override
    public @NotNull Pointers pointers() {
        return this.pointers;
    }
}
