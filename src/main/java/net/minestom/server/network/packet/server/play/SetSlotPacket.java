package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetSlotPacket implements ServerPacket {

    public byte windowId;
    public short slot;
    @NotNull
    public ItemStack itemStack;
    @Nullable
    public Player player;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(slot);
        writer.writeItemStack(itemStack, player);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SLOT;
    }
}
