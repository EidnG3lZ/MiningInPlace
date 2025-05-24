package com.eidng3lz.mininginplace.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Dfs {

    private class SearchNode {
        BlockPos pos;
        int depth;

        SearchNode(BlockPos pos, int depth) {
            this.pos = pos;
            this.depth = depth;
        }
    }

    //定义相连方块的相对坐标
    private static final int[][] CONNECTED_BLOCK_RELATIVE_POS = {
            {-1, -1, -1}, {0, -1, -1}, {1, -1, -1},
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 1, -1}, {0, 1, -1}, {1, 1, -1},
            {-1, -1, 0}, {0, -1, 0}, {1, -1, 0},
            {-1, 0, 0},/*{0,0,0},*/ {1, 0, 0},
            {-1, 1, 0}, {0, 1, 0}, {1, 1, 0},
            {-1, -1, 1}, {0, -1, 1}, {1, -1, 1},
            {-1, 0, 1}, {0, 0, 1}, {1, 0, 1},
            {-1, 1, 1}, {0, 1, 1}, {1, 1, 1},
    };

    private List<KeyValuePair<BlockPos, Integer>> searchedPosList = new ArrayList<>();

    private boolean containsPos(BlockPos pos) {
        for (KeyValuePair<BlockPos, Integer> keyValuePair : searchedPosList) {
            if (keyValuePair.getKey().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    //private final int DEPTH_LIMIT = 16;

    public List<KeyValuePair<BlockPos, Integer>> dfs(LevelAccessor world, BlockPos startPos, Set<Block> blockGroups, int depthLimit) {
        Stack<SearchNode> searchNodeStack = new Stack<>();
        searchNodeStack.push(new SearchNode(startPos, 0));

//        Set<Block> blockGroups = new HashSet<>();
//        for (Set<Block> blocks : ExampleMod.blockGroups) {
//            if (blocks.contains(startState.getBlock())) {
//                blockGroups.addAll(blocks);
//            }
//        }

        while (!searchNodeStack.isEmpty()) {
            SearchNode currentNode = searchNodeStack.pop();
            BlockPos pos = currentNode.pos;
            int depth = currentNode.depth;
            searchedPosList.add(new KeyValuePair<>(pos, depth));
            if (depth + 1 <= depthLimit) {
                for (int[] offset : CONNECTED_BLOCK_RELATIVE_POS) {
                    BlockPos newPos = new BlockPos(
                            pos.getX() + offset[0],
                            pos.getY() + offset[1],
                            pos.getZ() + offset[2]
                    );
                    if (blockGroups.contains(world.getBlockState(newPos).getBlock()) && !containsPos(newPos)) {
                        searchNodeStack.push(new SearchNode(newPos, depth + 1));
                    }
                }
            }
        }

        searchedPosList.sort((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()));
        return searchedPosList;
    }

}
