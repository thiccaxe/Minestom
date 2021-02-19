package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatMessagePacket implements ServerPacket {

    public JsonMessage jsonMessage;
    public Position position;
    public UUID uuid;

    private ChatMessagePacket() {
        this("", Position.CHAT, new UUID(0, 0));
    }

    public ChatMessagePacket(String jsonMessage, Position position, UUID uuid) {
        this(ColoredText.of(jsonMessage), position, uuid);
    }

    public ChatMessagePacket(JsonMessage jsonMessage, Position position, UUID uuid) {
        this.jsonMessage = jsonMessage;
        this.position = position;
        this.uuid = uuid;
    }

    public ChatMessagePacket(String jsonMessage, Position position) {
        this(jsonMessage, position, new UUID(0, 0));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(jsonMessage.toString());
        writer.writeByte((byte) position.ordinal());
        writer.writeUuid(uuid);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        jsonMessage = reader.readJsonMessage(Integer.MAX_VALUE);
        position = Position.values()[reader.readByte()];
        uuid = reader.readUuid();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    public enum Position {
        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO
    }
}
