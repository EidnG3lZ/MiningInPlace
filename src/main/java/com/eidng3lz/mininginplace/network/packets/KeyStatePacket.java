package com.eidng3lz.mininginplace.network.packets;

import com.eidng3lz.mininginplace.MiningInPlace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KeyStatePacket(boolean keyState) implements CustomPacketPayload {
    public static final Type<KeyStatePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MiningInPlace.MODID, "key_state_packet")
    );
    public static final StreamCodec<ByteBuf, KeyStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            KeyStatePacket::keyState,
            KeyStatePacket::new
    );

    public static void onServerHandleDate(final KeyStatePacket packet, IPayloadContext context) {
        Player player = context.player();
        MiningInPlace.playerKeyState.put(player.getUUID(), packet.keyState());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
