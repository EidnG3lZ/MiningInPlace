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

    /**
     * 在指定的世界中搜索特定的方块组
     *
     * @param world            世界访问器，用于获取世界中的方块状态
     * @param startPos         搜索的起始位置
     * @param targetBlockGroup 目标方块组，即搜索的目标方块类型集合
     * @param depthLimit       搜索的深度限制，防止无限搜索
     * @param searchStepsLimit 搜索的步数限制，达到限制后停止搜索
     * @return 返回一个列表，包含找到的方块位置和对应的搜索深度
     */
    @Override
    public List<KeyValuePair<BlockPos, Integer>> search(LevelAccessor world, BlockPos startPos, BlockGroup targetBlockGroup, int depthLimit, int searchStepsLimit) {
        // 初始化搜索深度为0
        int depth = 0;
        // 用于存储已经搜索过的位置，避免重复搜索
        Set<BlockPos> searchedPos = new HashSet<>();
        // 用于存储搜索结果的列表，包含方块位置和搜索深度
        List<KeyValuePair<BlockPos, Integer>> resultList = new ArrayList<>();
        // 用于存储当前层待搜索的方块位置
        List<BlockPos> searchingList = new ArrayList<>();
        // 用于存储下一层待搜索的方块位置
        List<BlockPos> nextSearchList = new ArrayList<>();
        // 将起始位置添加到待搜索列表和已搜索位置集合中
        searchingList.add(startPos);
        searchedPos.add(startPos);
        // 层层搜索，直到待搜索列表为空或达到深度限制
        search_layer_by_layer:
        while (!searchingList.isEmpty() && depth <= depthLimit) {
            // 遍历当前层的所有待搜索位置
            for (BlockPos searchingPos : searchingList) {
                // 将当前搜索位置和深度添加到结果列表中
                resultList.add(new KeyValuePair<>(searchingPos, depth));
                // 如果结果列表达到步数限制，提前结束搜索
                if (resultList.size() >= searchStepsLimit) {
                    break search_layer_by_layer;
                }
                // 遍历连接方块的相对位置，进行下一层的搜索
                for (int[] offset : CONNECTED_BLOCK_RELATIVE_POS) {
                    // 计算下一层的位置
                    BlockPos nextLayerPos = new BlockPos(
                            searchingPos.getX() + offset[0],
                            searchingPos.getY() + offset[1],
                            searchingPos.getZ() + offset[2]
                    );
                    // 如果下一层的位置是目标方块组的一部分且未被搜索过，则添加到下一层待搜索列表中
                    if (targetBlockGroup.contains(world.getBlockState(nextLayerPos).getBlock()) && !searchedPos.contains(nextLayerPos)) {
                        nextSearchList.add(nextLayerPos);
                        searchedPos.add(nextLayerPos);
                    }
                }
            }
            // 清空当前层的搜索列表，准备搜索下一层
            searchingList.clear();
            // 交换搜索列表
            List<BlockPos> temp = searchingList;
            searchingList = nextSearchList;
            nextSearchList = temp;
            // 深度增加1
            depth = depth + 1;
        }
        // 返回搜索结果列表
        return resultList;
    }
}
