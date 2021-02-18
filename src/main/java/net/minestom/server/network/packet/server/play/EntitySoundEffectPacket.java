package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntitySoundEffectPacket implements ServerPacket {

    public int soundId;
    public SoundCategory soundCategory;
    public int entityId;
    public float volume;
    public float pitch;

    public EntitySoundEffectPacket() {
        soundCategory = SoundCategory.NEUTRAL;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(soundCategory.ordinal());
        writer.writeVarInt(entityId);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        soundId = reader.readVarInt();
        soundCategory = SoundCategory.values()[reader.readVarInt()];
        entityId = reader.readVarInt();
        volume = reader.readFloat();
        pitch = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_SOUND_EFFECT;
    }
}
