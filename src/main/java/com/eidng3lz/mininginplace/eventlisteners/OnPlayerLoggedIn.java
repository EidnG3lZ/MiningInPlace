package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.network.ConfigPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnPlayerLoggedIn {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level world = player.level();
        LogUtils.getLogger().info("player logged in:{}", player.getName().getString());
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ConfigPayload(false));
    }
}
