package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public class MapDataPacket implements ServerPacket {

    public int mapId;
    public byte scale;
    public boolean trackingPosition;
    public boolean locked;

    public Icon[] icons = new Icon[0];

    public short columns;
    public short rows;
    public byte x;
    public byte z;
    public byte[] data = new byte[0];

    public MapDataPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(mapId);
        writer.writeByte(scale);
        writer.writeBoolean(trackingPosition);
        writer.writeBoolean(locked);

        if (icons != null && icons.length > 0) {
            writer.writeVarInt(icons.length);
            for (Icon icon : icons) {
                icon.write(writer);
            }
        } else {
            writer.writeVarInt(0);
        }

        writer.writeByte((byte) columns);
        if (columns <= 0) {
            return;
        }

        writer.writeByte((byte) rows);
        writer.writeByte(x);
        writer.writeByte(z);
        if (data != null && data.length > 0) {
            writer.writeVarInt(data.length);
            writer.writeBytes(data);
        } else {
            writer.writeVarInt(0);
        }

    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        mapId = reader.readVarInt();
        scale = reader.readByte();
        trackingPosition = reader.readBoolean();
        locked = reader.readBoolean();

        int iconCount = reader.readVarInt();
        icons = new Icon[iconCount];
        for (int i = 0; i < iconCount; i++) {
            icons[i] = new Icon();
            icons[i].read(reader);
        }

        columns = reader.readByte();
        if(columns <= 0) {
            return;
        }

        rows = reader.readByte();
        x = reader.readByte();
        z = reader.readByte();
        int dataLength = reader.readVarInt();
        data = reader.readBytes(dataLength);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MAP_DATA;
    }

    public static class Icon implements Writeable, Readable {
        public int type;
        public byte x, z;
        public byte direction;
        public JsonMessage displayName; // Only text

        public void write(BinaryWriter writer) {
            writer.writeVarInt(type);
            writer.writeByte(x);
            writer.writeByte(z);
            writer.writeByte(direction);

            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName) {
                writer.writeSizedString(displayName.toString());
            }
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            type = reader.readVarInt();
            x = reader.readByte();
            z = reader.readByte();
            direction = reader.readByte();

            boolean hasDisplayName = reader.readBoolean();
            if(hasDisplayName) {
                displayName = reader.readJsonMessage(Integer.MAX_VALUE);
            } else {
                displayName = null;
            }
        }
    }

}
