package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.MiningInPlaceClient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class OnRegisterKeyMappings {
    @SubscribeEvent
    public static void registerKey(RegisterKeyMappingsEvent event) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        event.register(MiningInPlaceClient.KEY_MAPPING.get());
    }
}
