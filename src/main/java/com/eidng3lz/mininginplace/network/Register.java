package com.eidng3lz.mininginplace.network;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlace;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Register {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConfigPayloadHandler::onClientHandleDate,
                        ConfigPayloadHandler::onServerHandleDate
                )
        );
    }

    public class ConfigPayloadHandler {
        public static void onClientHandleDate(final ConfigPayload payload, IPayloadContext context) {
            LogUtils.getLogger().info("client get a message");
            PacketDistributor.sendToServer(new ConfigPayload(Config.inventControl));
        }

        public static void onServerHandleDate(final ConfigPayload payload, IPayloadContext context) {
            LogUtils.getLogger().info("server get a message:{}", payload.config());
            MiningInPlace.playerConfigs.put(context.player().getName().getString(), payload.config());
        }
    }
}
