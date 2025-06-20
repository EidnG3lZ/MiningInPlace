package com.eidng3lz.mininginplace.functions.searchblocks;

import com.eidng3lz.mininginplace.util.BlockGroup;
import com.eidng3lz.mininginplace.util.KeyValuePair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bfs implements BlockSearcher {

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

    @Override
    public List<KeyValuePair<BlockPos, Integer>> search(LevelAccessor world, BlockPos startPos, BlockGroup targetBlockGroup, int depthLimit, int searchStepsLimit) {
        int depth = 0;
        Set<BlockPos> searchedPos = new HashSet<>();
        List<KeyValuePair<BlockPos, Integer>> resultList = new ArrayList<>();
        List<BlockPos> searchingList = new ArrayList<>();
        searchingList.add(startPos);
        searchedPos.add(startPos);
        while (!searchingList.isEmpty() && depth <= depthLimit) {
            List<BlockPos> nextSearchList = new ArrayList<>();
            for (BlockPos searchingPos : searchingList) {
                resultList.add(new KeyValuePair<>(searchingPos, depth));
                if (resultList.size() >= searchStepsLimit) {
                    continue;
                }
                for (int[] offset : CONNECTED_BLOCK_RELATIVE_POS) {
                    BlockPos nextLayerPos = new BlockPos(
                            searchingPos.getX() + offset[0],
                            searchingPos.getY() + offset[1],
                            searchingPos.getZ() + offset[2]
                    );
                    if (targetBlockGroup.contains(world.getBlockState(nextLayerPos).getBlock()) && !searchedPos.contains(nextLayerPos)) {
                        nextSearchList.add(nextLayerPos);
                        searchedPos.add(nextLayerPos);
                    }
                }
            }
            searchingList = nextSearchList;
            depth = depth + 1;
        }
        return resultList;
    }
}
