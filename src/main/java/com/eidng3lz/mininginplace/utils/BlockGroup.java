package com.eidng3lz.mininginplace.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class BlockGroup {
    private Set<Block> blockSet;
    private Set<TagKey<Block>> tagKeySet;

    public BlockGroup() {
        this.blockSet = new HashSet<>();
        this.tagKeySet = new HashSet<>();
    }

    public boolean addFromStr(String blockIDOrTag) {
        if (blockIDOrTag.charAt(0) == '#') {
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(blockIDOrTag.substring(1)));
            return tagKeySet.add(tagKey);
        }
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockIDOrTag));
        return blockSet.add(block);
    }

    public boolean add(Block block) {
        return blockSet.add(block);
    }

    public boolean add(TagKey<Block> tagKey) {
        return tagKeySet.add(tagKey);
    }

    public boolean contains(Block block) {
        if (blockSet.contains(block)) {
            return true;
        }
        for (TagKey<Block> tagKey : tagKeySet) {
            if (block.defaultBlockState().is(tagKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return blockSet.isEmpty() && tagKeySet.isEmpty();
    }

    public boolean addBlockGroup(BlockGroup blockGroup) {
        boolean a = blockSet.addAll(blockGroup.getBlockSet());
        boolean b = tagKeySet.addAll(blockGroup.getTagKeySet());
        return a || b;
    }

    public Set<Block> getBlockSet() {
        return blockSet;
    }

    public Set<TagKey<Block>> getTagKeySet() {
        return tagKeySet;
    }
}
