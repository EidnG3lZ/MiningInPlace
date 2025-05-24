package com.eidng3lz.mininginplace.network;

import com.eidng3lz.mininginplace.MiningInPlace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigPayload(boolean config) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MiningInPlace.MODID, "configpayload"));
    public static final StreamCodec<ByteBuf, ConfigPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ConfigPayload::config,
            ConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
