package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.network.packets.RequestPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnPlayerLoggedIn {
    @SubscribeEvent
    public static void initializePlayerState(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
//        LogUtils.getLogger().info("player logged in:{}", player.getName().getString());
        PacketDistributor.sendToPlayer((ServerPlayer) player, new RequestPacket(RequestPacket.GET_CLIENT_CONFIGS, ""));
        PacketDistributor.sendToPlayer((ServerPlayer) player, new RequestPacket(RequestPacket.SET_SERVER_CONFIGS, Config.getServerConfigsToJSON()));
        MiningInPlace.playerKeyState.put(player.getUUID(), false);
    }
}
