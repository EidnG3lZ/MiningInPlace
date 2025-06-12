package com.eidng3lz.mininginplace.network;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlace;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class Packets {
    public record RequestPacket(String request, String args) implements CustomPacketPayload {
        //requests
        public static final String GET_CLIENT_CONFIGS = "get-client-configs";
        public static final String SET_CLIENT_CONFIGS = "set-client-configs";

        public static final CustomPacketPayload.Type<RequestPacket> TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath(MiningInPlace.MODID, "request_packet")
        );
        public static final StreamCodec<ByteBuf, RequestPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                RequestPacket::request,
                ByteBufCodecs.STRING_UTF8,
                RequestPacket::args,
                RequestPacket::new
        );

        public static void onClientHandleDate(final RequestPacket packet, IPayloadContext context) {
            switch (packet.request()) {
                case RequestPacket.GET_CLIENT_CONFIGS ->
                        PacketDistributor.sendToServer(new RequestPacket(RequestPacket.SET_CLIENT_CONFIGS, Config.getClientConfigsToJSON()));
                case RequestPacket.SET_CLIENT_CONFIGS -> LogUtils.getLogger().info("client side not supported");
                default -> {
                }
            }
        }

        public static void onServerHandleDate(final RequestPacket packet, IPayloadContext context) {
            switch (packet.request()) {
                case RequestPacket.GET_CLIENT_CONFIGS -> LogUtils.getLogger().info("server side not supported");
                case RequestPacket.SET_CLIENT_CONFIGS -> {
                    Map<Config.ClientConfigs, Object> map;
                    map = MiningInPlace.gson.fromJson(packet.args(), new TypeToken<Map<Config.ClientConfigs, Object>>() {
                    });
                    MiningInPlace.playerConfigs.put(context.player().getName().getString(), map);
                }
                default -> {
                }
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
