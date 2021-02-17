package net.minestom.server.network.packet.server.login;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LoginDisconnectPacket implements ServerPacket {

    private String kickMessage; // JSON text

    private LoginDisconnectPacket() {
        this("This constructor should not be used, tell your server devs.");
    }

    public LoginDisconnectPacket(@NotNull String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public LoginDisconnectPacket(@NotNull JsonMessage jsonKickMessage) {
        this(jsonKickMessage.toString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(kickMessage);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        kickMessage = reader.readSizedString(Integer.MAX_VALUE);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_DISCONNECT;
    }

}
