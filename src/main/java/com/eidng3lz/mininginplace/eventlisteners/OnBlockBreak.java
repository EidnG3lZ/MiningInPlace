package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlace;
import com.eidng3lz.mininginplace.functions.searchblocks.Bfs;
import com.eidng3lz.mininginplace.functions.searchblocks.BlockSearcher;
import com.eidng3lz.mininginplace.util.BlockGroup;
import com.eidng3lz.mininginplace.util.KeyValuePair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnBlockBreak {
    @SubscribeEvent
    public static void searchBlocksAndMoveBlock(BlockEvent.BreakEvent event) {
//        LogUtils.getLogger().info("block break");

        LevelAccessor world = event.getLevel();
        BlockPos eventBlockPos = event.getPos();
        BlockState eventBlockState = event.getState();
        Player player = event.getPlayer();

//        LogUtils.getLogger().info("player game mode:{}",((ServerPlayer)player).gameMode.getGameModeForPlayer().getName());

        //如果触发破坏的不是真玩家则直接跳过
        if (!player.getClass().equals(ServerPlayer.class)) {
            return;
        }

        //计算搜索目标
//        Set<Block> blockGroups = new HashSet<>();
//        for (Set<Block> blocksAndTags : Config.chainlandBlocksGroups) {
//            if (blocksAndTags.contains(eventBlockState.getBlock())) {
//                blockGroups.addAll(blocksAndTags);
//            }
//        }
        BlockGroup blockGroup = new BlockGroup();
        for (BlockGroup configBlockGroup : Config.chainedBlocksGroups) {
            if (configBlockGroup.contains(eventBlockState.getBlock())) {
                blockGroup.addBlockGroup(configBlockGroup);
            }
        }

//        LogUtils.getLogger().info("group:");
//        for (Block block : blockGroups) {
//            LogUtils.getLogger().info(block.toString());
//        }
//        LogUtils.getLogger().info("player conf:{}", MiningInPlace.playerConfigs.get(player.getName().getString()));
//        LogUtils.getLogger().info("player sneaking:{}", player.isShiftKeyDown());

        //判断方块属于目标且玩家当前需要连锁破坏
        Map<Config.ClientConfigs, Object> clientConfigsMap = MiningInPlace.playerConfigs.get(player.getName().getString());
        if (
                !blockGroup.isEmpty()
                        && (player.isShiftKeyDown() == (boolean) (clientConfigsMap.get(Config.ClientConfigs.INVENT_CONTROL)))
                        && !(((boolean) clientConfigsMap.get(Config.ClientConfigs.DISABLE_AT_CREATIVE)) && player.isCreative())
        ) {
            BlockSearcher searcher = new Bfs();
            List<KeyValuePair<BlockPos, Integer>> dfsResultsList = searcher.search(world, eventBlockPos, blockGroup, Config.depthLimit, Config.searchStepsLimit);
            BlockPos targetBlockPos;
            KeyValuePair<BlockPos, Integer> deepest = dfsResultsList.getFirst();
            for (KeyValuePair<BlockPos, Integer> keyValuePair : dfsResultsList) {
                if (keyValuePair.getValue() == Config.depthLimit) {
                    deepest = keyValuePair;
                    break;
                }
                if (keyValuePair.getValue() > deepest.getValue()) {
                    deepest = keyValuePair;
                }
            }
            targetBlockPos = deepest.getKey();
//            BlockPos togetBlockPos = dfs.dfs(world, eventBlockPos, blockGroup, Config.depthLimit).getFirst().getKey();
            MiningInPlace.queueServerWork(1, new MoveBlock(world, targetBlockPos, eventBlockPos));
            //设置flag通知BlockDrops事件监听逻辑，准备移动和弹射掉落物
            MiningInPlace.blockBreakEventFlag.put(player.toString(), 0);
        }
    }

    private static class MoveBlock implements Runnable {
        LevelAccessor world;
        BlockPos fromPos;
        BlockPos toPos;

        public MoveBlock(LevelAccessor world, BlockPos fromPos, BlockPos toPos) {
            this.world = world;
            this.fromPos = fromPos;
            this.toPos = toPos;
        }

        @Override
        public void run() {
            world.setBlock(toPos, world.getBlockState(fromPos), Block.UPDATE_ALL);
            world.destroyBlock(fromPos, false);
        }
    }
}
