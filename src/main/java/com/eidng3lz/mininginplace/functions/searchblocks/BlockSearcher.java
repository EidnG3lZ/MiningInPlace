package com.eidng3lz.mininginplace.functions.searchblocks;

import com.eidng3lz.mininginplace.util.BlockGroup;
import com.eidng3lz.mininginplace.util.KeyValuePair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public interface BlockSearcher {
    List<KeyValuePair<BlockPos, Integer>> search(LevelAccessor world, BlockPos startPos, BlockGroup targetBlockGroup, int depthLimit, int searchStepsLimit);
}
