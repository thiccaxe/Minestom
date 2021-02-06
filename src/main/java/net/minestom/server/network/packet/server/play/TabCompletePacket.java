package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class TabCompletePacket implements ServerPacket {

    public int transactionId;
    public int start;
    public int length;
    public Match[] matches;

    public TabCompletePacket() {
        matches = new Match[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.length);
        for (Match match : matches) {
            writer.writeSizedString(match.match);
            writer.writeBoolean(match.hasTooltip);
            if (match.hasTooltip)
                writer.writeSizedString(match.tooltip.toString());
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        transactionId = reader.readVarInt();
        start = reader.readVarInt();
        length = reader.readVarInt();

        int matchCount = reader.readVarInt();
        matches = new Match[matchCount];
        for (int i = 0; i < matchCount; i++) {
            String match = reader.readSizedString(Integer.MAX_VALUE);
            boolean hasTooltip = reader.readBoolean();
            JsonMessage tooltip = null;
            if(hasTooltip) {
                tooltip = reader.readJsonMessage(Integer.MAX_VALUE);
            }
            Match newMatch = new Match();
            newMatch.match = match;
            newMatch.hasTooltip = hasTooltip;
            newMatch.tooltip = tooltip;
            matches[i] = newMatch;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    public static class Match {
        public String match;
        public boolean hasTooltip;
        public JsonMessage tooltip; // Only text
    }

}
