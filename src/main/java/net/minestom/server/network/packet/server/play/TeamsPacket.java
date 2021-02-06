package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The packet creates or updates teams
 */
public class TeamsPacket implements ServerPacket {

    /**
     * The registry name of the team
     */
    public String teamName;
    /**
     * The action of the packet
     */
    public Action action;

    /**
     * The display name for the team
     */
    public JsonMessage teamDisplayName;
    /**
     * The friendly flags to
     */
    public byte friendlyFlags;
    /**
     * Visibility state for the name tag
     */
    public NameTagVisibility nameTagVisibility;
    /**
     * Rule for the collision
     */
    public CollisionRule collisionRule;
    /**
     * The color of the team
     */
    public int teamColor;
    /**
     * The prefix of the team
     */
    public JsonMessage teamPrefix;
    /**
     * The suffix of the team
     */
    public JsonMessage teamSuffix;
    /**
     * An array with all entities in the team
     */
    public String[] entities;

    public TeamsPacket() {
        teamName = "";
        action = Action.REMOVE_TEAM;
    }

    /**
     * Writes data into the {@link BinaryWriter}
     *
     * @param writer The writer to writes
     */
    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(this.teamName);
        writer.writeByte((byte) this.action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                writer.writeSizedString(this.teamDisplayName.toString());
                writer.writeByte(this.friendlyFlags);
                writer.writeSizedString(this.nameTagVisibility.getIdentifier());
                writer.writeSizedString(this.collisionRule.getIdentifier());
                writer.writeVarInt(this.teamColor);
                writer.writeSizedString(this.teamPrefix.toString());
                writer.writeSizedString(this.teamSuffix.toString());
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            if (entities == null || entities.length == 0) {
                writer.writeVarInt(0); // Empty
            } else {
                writer.writeStringArray(entities);
            }
        }

    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        teamName = reader.readSizedString(Integer.MAX_VALUE);
        action = Action.values()[reader.readByte()];

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                this.teamDisplayName = reader.readJsonMessage(Integer.MAX_VALUE);
                this.friendlyFlags = reader.readByte();
                nameTagVisibility = NameTagVisibility.fromIdentifier(reader.readSizedString(Integer.MAX_VALUE));
                collisionRule = CollisionRule.fromIdentifier(reader.readSizedString(Integer.MAX_VALUE));
                this.teamColor = reader.readVarInt();
                this.teamPrefix = reader.readJsonMessage(Integer.MAX_VALUE);
                this.teamSuffix = reader.readJsonMessage(Integer.MAX_VALUE);
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            entities = reader.readSizedStringArray(Integer.MAX_VALUE);
        }
    }

    /**
     * Gets the identifier of the packet
     *
     * @return the identifier
     */
    @Override
    public int getId() {
        return ServerPacketIdentifier.TEAMS;
    }

    /**
     * An enumeration which representing all actions for the packet
     */
    public enum Action {
        /**
         * An action to create a new team
         */
        CREATE_TEAM,
        /**
         * An action to remove a team
         */
        REMOVE_TEAM,
        /**
         * An action to update the team information
         */
        UPDATE_TEAM_INFO,
        /**
         * An action to add player to the team
         */
        ADD_PLAYERS_TEAM,
        /**
         * An action to remove player from the team
         */
        REMOVE_PLAYERS_TEAM
    }

    /**
     * An enumeration which representing all visibility states for the name tags
     */
    public enum NameTagVisibility {
        /**
         * The name tag is visible
         */
        ALWAYS("always"),
        /**
         * Hides the name tag for other teams
         */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        /**
         * Hides the name tag for the own team
         */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        /**
         * The name tag is invisible
         */
        NEVER("never");

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The client identifier
         */
        NameTagVisibility(String identifier) {
            this.identifier = identifier;
        }

        @NotNull
        public static NameTagVisibility fromIdentifier(String identifier) {
            for(NameTagVisibility v : values()) {
                if(v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for NameTagVisibility is invalid: "+identifier);
            return null;
        }

        /**
         * Gets the client identifier
         *
         * @return the identifier
         */
        @NotNull
        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * An enumeration which representing all rules for the collision
     */
    public enum CollisionRule {
        /**
         * Can push all objects and can be pushed by all objects
         */
        ALWAYS("always"),
        /**
         * Can push objects of other teams, but teammates cannot
         */
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        /**
         * Can only push objects of the same team
         */
        PUSH_OWN_TEAM("pushOwnTeam"),
        /**
         * Cannot push an object, but neither can they be pushed
         */
        NEVER("never");

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The identifier for the client
         */
        CollisionRule(String identifier) {
            this.identifier = identifier;
        }

        @NotNull
        public static CollisionRule fromIdentifier(String identifier) {
            for(CollisionRule v : values()) {
                if(v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for CollisionRule is invalid: "+identifier);
            return null;
        }

        /**
         * Gets the identifier of the rule
         *
         * @return the identifier
         */
        @NotNull
        public String getIdentifier() {
            return identifier;
        }
    }

}
