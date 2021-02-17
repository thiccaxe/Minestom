package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInfoPacket implements ServerPacket {

    public Action action;
    public List<PlayerInfo> playerInfos;

    PlayerInfoPacket() {
        this(Action.UPDATE_DISPLAY_NAME);
    }

    public PlayerInfoPacket(Action action) {
        this.action = action;
        this.playerInfos = new ArrayList<>();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(playerInfos.size());

        for (PlayerInfo playerInfo : this.playerInfos) {
            if (!playerInfo.getClass().equals(action.getClazz())) continue;
            writer.writeUuid(playerInfo.uuid);
            playerInfo.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        action = Action.values()[reader.readVarInt()];
        int playerInfoCount = reader.readVarInt();

        playerInfos = new ArrayList<>(playerInfoCount);

        for (int i = 0; i < playerInfoCount; i++) {
            UUID uuid = reader.readUuid();
            PlayerInfo info;
            switch (action) {
                case ADD_PLAYER:
                    info = new AddPlayer(uuid, reader);
                    break;
                case UPDATE_GAMEMODE:
                    info = new UpdateGamemode(uuid, reader);
                    break;
                case UPDATE_LATENCY:
                    info = new UpdateLatency(uuid, reader);
                    break;
                case UPDATE_DISPLAY_NAME:
                    info = new UpdateDisplayName(uuid, reader);
                    break;
                case REMOVE_PLAYER:
                    info = new RemovePlayer(uuid);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported action encountered: "+action.name());
            }

            playerInfos.set(i, info);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_INFO;
    }

    public enum Action {

        ADD_PLAYER(AddPlayer.class),
        UPDATE_GAMEMODE(UpdateGamemode.class),
        UPDATE_LATENCY(UpdateLatency.class),
        UPDATE_DISPLAY_NAME(UpdateDisplayName.class),
        REMOVE_PLAYER(RemovePlayer.class);

        private final Class<? extends PlayerInfo> clazz;

        Action(Class<? extends PlayerInfo> clazz) {
            this.clazz = clazz;
        }

        @NotNull
        public Class<? extends PlayerInfo> getClazz() {
            return clazz;
        }
    }

    public static abstract class PlayerInfo {

        public UUID uuid;

        public PlayerInfo(UUID uuid) {
            this.uuid = uuid;
        }

        public abstract void write(BinaryWriter writer);
    }

    public static class AddPlayer extends PlayerInfo {

        public String name;
        public List<Property> properties;
        public GameMode gameMode;
        public int ping;
        public JsonMessage displayName; // Only text

        public AddPlayer(UUID uuid, String name, GameMode gameMode, int ping) {
            super(uuid);
            this.name = name;
            this.properties = new ArrayList<>();
            this.gameMode = gameMode;
            this.ping = ping;
        }

        AddPlayer(UUID uuid, BinaryReader reader) {
            super(uuid);
            name = reader.readSizedString(Integer.MAX_VALUE);
            int propertyCount = reader.readVarInt();

            properties = new ArrayList<>(propertyCount);
            for (int i = 0; i < propertyCount; i++) {
                properties.set(i, new Property(reader));
            }

            gameMode = GameMode.fromId((byte) reader.readVarInt());
            ping = reader.readVarInt();
            boolean hasDisplayName = reader.readBoolean();

            if(hasDisplayName) {
                displayName = reader.readJsonMessage(Integer.MAX_VALUE);
            } else {
                displayName = null;
            }
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.writeVarInt(properties.size());
            for (Property property : properties) {
                property.write(writer);
            }
            writer.writeVarInt(gameMode.getId());
            writer.writeVarInt(ping);

            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName)
                writer.writeSizedString(displayName.toString());
        }

        public static class Property {

            public String name;
            public String value;
            public String signature;

            public Property(String name, String value, String signature) {
                this.name = name;
                this.value = value;
                this.signature = signature;
            }

            public Property(String name, String value) {
                this(name, value, null);
            }

            Property(BinaryReader reader) {
                name = reader.readSizedString(Integer.MAX_VALUE);
                value = reader.readSizedString(Integer.MAX_VALUE);
                boolean hasSignature = reader.readBoolean();

                if(hasSignature) {
                    signature = reader.readSizedString(Integer.MAX_VALUE);
                }
            }

            public void write(BinaryWriter writer) {
                writer.writeSizedString(name);
                writer.writeSizedString(value);

                final boolean signed = signature != null;
                writer.writeBoolean(signed);
                if (signed)
                    writer.writeSizedString(signature);
            }
        }
    }

    public static class UpdateGamemode extends PlayerInfo {

        public GameMode gameMode;

        public UpdateGamemode(UUID uuid, GameMode gameMode) {
            super(uuid);
            this.gameMode = gameMode;
        }

        UpdateGamemode(UUID uuid, BinaryReader reader) {
            super(uuid);
            gameMode = GameMode.fromId((byte) reader.readVarInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(gameMode.getId());
        }
    }

    public static class UpdateLatency extends PlayerInfo {

        public int ping;

        public UpdateLatency(UUID uuid, int ping) {
            super(uuid);
            this.ping = ping;
        }

        UpdateLatency(UUID uuid, BinaryReader reader) {
            super(uuid);
            ping = reader.readVarInt();
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(ping);
        }
    }

    public static class UpdateDisplayName extends PlayerInfo {

        public JsonMessage displayName; // Only text

        public UpdateDisplayName(UUID uuid, JsonMessage displayName) {
            super(uuid);
            this.displayName = displayName;
        }

        UpdateDisplayName(UUID uuid, BinaryReader reader) {
            super(uuid);
            boolean hasDisplayName = reader.readBoolean();
            if(hasDisplayName) {
                displayName = reader.readJsonMessage(Integer.MAX_VALUE);
            } else {
                displayName = null;
            }
        }

        @Override
        public void write(BinaryWriter writer) {
            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName)
                writer.writeSizedString(displayName.toString());
        }
    }

    public static class RemovePlayer extends PlayerInfo {

        public RemovePlayer(UUID uuid) {
            super(uuid);
        }

        @Override
        public void write(BinaryWriter writer) {
        }
    }
}
