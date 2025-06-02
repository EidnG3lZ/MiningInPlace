package com.eidng3lz.mininginplace.network;

import com.eidng3lz.mininginplace.MiningInPlace;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Register {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MiningInPlace.MODID);

        registrar.playBidirectional(
                Packets.RequestPacket.TYPE,
                Packets.RequestPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        Packets.RequestPacket::onClientHandleDate,
                        Packets.RequestPacket::onServerHandleDate
                )
        );
    }
}
