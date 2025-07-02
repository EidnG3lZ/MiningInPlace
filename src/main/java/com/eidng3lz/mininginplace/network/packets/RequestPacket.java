package com.eidng3lz.mininginplace.network.packets;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.MiningInPlaceClient;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record RequestPacket(String request, String args) implements CustomPacketPayload {
    //requests
    public static final String GET_CLIENT_CONFIGS = "get-client-configs";
    public static final String SET_CLIENT_CONFIGS = "set-client-configs";
    public static final String SET_SERVER_CONFIGS = "set-server-configs";

    public static final Type<RequestPacket> TYPE = new Type<>(
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
            case RequestPacket.SET_SERVER_CONFIGS -> {
                Map<Config.ServerConfigs, Object> map = MiningInPlace.gson.fromJson(packet.args(), new TypeToken<Map<Config.ServerConfigs, Object>>() {
                });
                MiningInPlaceClient.serverConfigs.clear();
                MiningInPlaceClient.serverConfigs.putAll(map);
                //一些需要转换的配置可以将转换后的值替换掉原始值，以减少使用时转换带来的性能浪费
                Config.ServerConfigs key = Config.ServerConfigs.CHAINED_BLOCKS_GROUPS;
                MiningInPlaceClient.serverConfigs.put(key, Config.decodeChainedBlocksGroups((List<String>) map.get(key)));
            }
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
                MiningInPlace.playerConfigs.put(context.player().getUUID(), map);
            }
            case RequestPacket.SET_SERVER_CONFIGS -> LogUtils.getLogger().info("server side not supported");
            default -> {
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
