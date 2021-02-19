package net.minestom.server.network.packet.server.play;

import net.minestom.server.bossbar.BarColor;
import net.minestom.server.bossbar.BarDivision;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BossBarPacket implements ServerPacket {

    public UUID uuid = new UUID(0, 0);
    public Action action = Action.ADD;

    public JsonMessage title = ColoredText.of(""); // Only text
    public float health;
    public BarColor color = BarColor.BLUE;
    public BarDivision division = BarDivision.SOLID;
    public byte flags;

    public BossBarPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case ADD:
                writer.writeSizedString(title.toString());
                writer.writeFloat(health);
                writer.writeVarInt(color.ordinal());
                writer.writeVarInt(division.ordinal());
                writer.writeByte(flags);
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                writer.writeFloat(health);
                break;
            case UPDATE_TITLE:
                writer.writeSizedString(title.toString());
                break;
            case UPDATE_STYLE:
                writer.writeVarInt(color.ordinal());
                writer.writeVarInt(division.ordinal());
                break;
            case UPDATE_FLAGS:
                writer.writeByte(flags);
                break;
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        uuid = reader.readUuid();
        action = Action.values()[reader.readVarInt()];

        switch (action) {
            case ADD:
                title = reader.readJsonMessage(Integer.MAX_VALUE);
                health = reader.readFloat();
                color = BarColor.values()[reader.readVarInt()];
                division = BarDivision.values()[reader.readVarInt()];
                flags = reader.readByte();
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                health = reader.readFloat();
                break;
            case UPDATE_TITLE:
                title = reader.readJsonMessage(Integer.MAX_VALUE);
                break;
            case UPDATE_STYLE:
                color = BarColor.values()[reader.readVarInt()];
                division = BarDivision.values()[reader.readVarInt()];
                break;
            case UPDATE_FLAGS:
                flags = reader.readByte();
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BOSS_BAR;
    }

    public enum Action {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS
    }

}
