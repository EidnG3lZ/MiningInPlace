package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.MiningInPlaceClient;
import com.eidng3lz.mininginplace.network.packets.KeyStatePacket;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.eidng3lz.mininginplace.MiningInPlaceClient.KEY_MAPPING;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnClientTick {
    @SubscribeEvent
    public static void checkingKey(ClientTickEvent.Post event) {
        while (KEY_MAPPING.get().consumeClick()) {
            if (!MiningInPlaceClient.previousTickKeyState) {
                MiningInPlaceClient.previousTickKeyState = true;
                //执行按键按下时的逻辑
                LogUtils.getLogger().info("1");
                MiningInPlaceClient.keyState = true;
                PacketDistributor.sendToServer(new KeyStatePacket(true));
                return;
            }

        }
        if (!KEY_MAPPING.get().isDown() && MiningInPlaceClient.previousTickKeyState) {
            MiningInPlaceClient.previousTickKeyState = false;
            //执行按键松开时的逻辑
            LogUtils.getLogger().info("0");
            MiningInPlaceClient.keyState = false;
            PacketDistributor.sendToServer(new KeyStatePacket(false));
        }

    }
}
