package com.eidng3lz.mininginplace.functions.dodropandmoveblock;

import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.functions.aabb.Intersection;
import com.eidng3lz.mininginplace.network.Packets;
import com.eidng3lz.mininginplace.network.misc.RequestPacketArgs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DropAndMoveBlock {
    private static final double ITEM_ENTITY_Y_POS_OFFSET = EntityType.ITEM.getHeight() / 2.0;

    public static void run(LevelAccessor world, ServerPlayer player, BlockPos destroyPos, BlockPos moveFromPos) {
        final double BLOCK_SIZE = 1.0;
        final double FORCE = 0.1;
        BlockState destroyBlockState = world.getBlockState(destroyPos);
        Block destroyBlock = destroyBlockState.getBlock();
        //处理一些不应该继续执行方块操作逻辑的情况
        if (destroyBlock instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            ((ServerLevel) world).sendBlockUpdated(destroyPos, destroyBlockState, destroyBlockState, 0);
            return;
        }
        if (player.blockActionRestricted((Level) world, destroyPos, player.gameMode.getGameModeForPlayer())) {
            return;
        }
        //触发玩家破坏前应执行的一些东西
        destroyBlockState = destroyBlock.playerWillDestroy((Level) world, destroyPos, destroyBlockState, player);
        //通知客户端播放粒子和声音
        String args = MiningInPlace.gson.toJson(new RequestPacketArgs.BlockDestroyEffectArgs(
                new int[]{destroyPos.getX(), destroyPos.getY(), destroyPos.getZ()},
                Block.getId(destroyBlockState)
        ));
        PacketDistributor.sendToAllPlayers(new Packets.RequestPacket(Packets.RequestPacket.PLAY_BLOCK_DESTROY_EFFECT, args));
        //创造模式下只需要移动方块就行了
        if (player.isCreative()) {
            //移动方块
            world.setBlock(destroyPos, world.getBlockState(moveFromPos), Block.UPDATE_ALL);
            world.destroyBlock(moveFromPos, false);
            return;
        }
        //而生存/冒险模式下要考虑的就多了
        //用于挖掘的工具
        ItemStack itemStack = player.getMainHandItem();
        //记录工具挖掘前的状态
        ItemStack itemStackOld = itemStack.copy();
        //消耗工具耐久
        itemStack.mineBlock((Level) world, destroyBlockState, destroyPos, player);
        //工具损坏时触发事件
        if (itemStack.isEmpty() && !itemStackOld.isEmpty()) {
            EventHooks.onPlayerDestroyItem(player, itemStackOld, InteractionHand.MAIN_HAND);
        }
        //移动方块
        world.setBlock(destroyPos, world.getBlockState(moveFromPos), Block.UPDATE_ALL);
        world.destroyBlock(moveFromPos, false);
        //判断能否挖掘出掉落物
        if (destroyBlockState.canHarvestBlock(world, destroyPos, player)) {
            //计算掉落物生成位置
            int dropsSpawnSurface = Intersection.getRayIntersectSurfaceNoCheck(
                    player.getEyePosition(),
                    player.getViewVector(1),
                    new Vec3(destroyPos.getX(), destroyPos.getY(), destroyPos.getZ())
            );
            double offset = (BLOCK_SIZE + EntityType.ITEM.getHeight()) / 2.0;
            Vec3 dropsSpawnPos = switch (dropsSpawnSurface) {
                case 0 -> destroyPos.getCenter().add(-offset, 0, 0);
                case 1 -> destroyPos.getCenter().add(0, -offset, 0);
                case 2 -> destroyPos.getCenter().add(0, 0, -offset);
                case 3 -> destroyPos.getCenter().add(offset, 0, 0);
                case 4 -> destroyPos.getCenter().add(0, offset, 0);
                case 5 -> destroyPos.getCenter().add(0, 0, offset);
                default -> destroyPos.getCenter();
            };
            //计算将掉落物推动的力
            Vec3 pushVec = dropsSpawnPos.vectorTo(player.getEyePosition()).scale(FORCE);
            //生成掉落物，触发事件，生成蠹虫等东西
            dropAndSpawn((ServerLevel) world, destroyBlockState, destroyPos, player, itemStackOld, dropsSpawnPos, pushVec);
            //添加统计和消耗饥饿
            player.awardStat(Stats.BLOCK_MINED.get(destroyBlock));
            player.causeFoodExhaustion(0.005F);
        }
    }

    private static void dropAndSpawn(
            ServerLevel world,
            BlockState blockState,
            BlockPos blockPos,
            Entity destroyer,
            ItemStack toolItemStack,
            Vec3 dropsSpawnPos,
            Vec3 pushVec
    ) {
        boolean shouldDrop = world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !world.restoringBlockSnapshots;
        //为生成蠹虫等需要BlockPos的逻辑生成一个BlockPos
        BlockPos spawnBlockPos = new BlockPos(
                (int) Math.floor(dropsSpawnPos.x()),
                (int) Math.floor(dropsSpawnPos.y()),
                (int) Math.floor(dropsSpawnPos.z())
        );
        //计算掉落物列表
        List<ItemEntity> dropsItemEntityList;
        if (shouldDrop) {
            List<ItemStack> dropItemStack = Block.getDrops(blockState, world, blockPos, null, destroyer, toolItemStack);
            dropsItemEntityList = dropItemStack.stream()
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(itemStack -> {
                        double x = dropsSpawnPos.x();
                        double y = dropsSpawnPos.y() - ITEM_ENTITY_Y_POS_OFFSET;
                        double z = dropsSpawnPos.z();
                        ItemEntity itemEntity = new ItemEntity(
                                world,
                                x,
                                y,
                                z,
                                itemStack,
                                world.random.nextDouble() * 0.2 - 0.1,
                                0.1,
                                world.random.nextDouble() * 0.2 - 0.1
                        );
                        itemEntity.setDefaultPickUpDelay();
                        itemEntity.push(pushVec);
                        return itemEntity;
                    })
                    .collect(Collectors.toList());
        } else {
            dropsItemEntityList = new ArrayList<>();
        }
        //触发事件，获取事件结果，执行对应操作
        BlockDropsEvent event = new BlockDropsEvent(world, blockPos, blockState, null, dropsItemEntityList, destroyer, toolItemStack);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }
        //生成掉落物
        event.getDrops().forEach(world::addFreshEntity);
        //生成经验球
        int droppedExperience = event.getDroppedExperience();
        if (droppedExperience > 0 && shouldDrop) {
            ExperienceOrb.award(world, dropsSpawnPos, droppedExperience);
        }
        //生成蠹虫等东西
        blockState.spawnAfterBreak(world, spawnBlockPos, toolItemStack, false);
    }
}
