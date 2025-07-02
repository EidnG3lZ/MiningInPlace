package com.eidng3lz.mininginplace.network.packets;

import com.eidng3lz.mininginplace.MiningInPlace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BlockDestroyPacket(int x, int y, int z, int blockId) implements CustomPacketPayload {
    public static final Type<BlockDestroyPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MiningInPlace.MODID, "block_destroy_packet")
    );
    public static final StreamCodec<ByteBuf, BlockDestroyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            BlockDestroyPacket::x,
            ByteBufCodecs.INT,
            BlockDestroyPacket::y,
            ByteBufCodecs.INT,
            BlockDestroyPacket::z,
            ByteBufCodecs.INT,
            BlockDestroyPacket::blockId,
            BlockDestroyPacket::new
    );

    public static void onClientHandleDate(final BlockDestroyPacket packet, IPayloadContext context) {
        Level level = context.player().level();
        BlockPos pos = new BlockPos(packet.x(), packet.y(), packet.z());
        level.levelEvent(2001, pos, packet.blockId());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
