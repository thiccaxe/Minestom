package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DisconnectPacket implements ServerPacket {

    public JsonMessage message; // Only text

    public DisconnectPacket(@NotNull JsonMessage message){
        this.message = message;
    }

    private DisconnectPacket() {
        this(ColoredText.of("Disconnected."));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message.toString());
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        message = reader.readJsonMessage(Integer.MAX_VALUE);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
