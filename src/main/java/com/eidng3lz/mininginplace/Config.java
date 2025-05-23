package com.eidng3lz.mininginplace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue INVENT_CONTROL = BUILDER
            .comment("反转shift连锁控制")
            .define("invent_control",false);

    private static final ModConfigSpec.IntValue DEPTH_LIMIT = BUILDER
            .comment("最大搜索深度")
            .defineInRange("depth_limit",16,0,Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> CHAINLAND_BLOCKS_GROUPS = BUILDER
            .comment("连锁方块，每组逗号隔开")
            .defineListAllowEmpty(
                    "chainland_blocks_groups",
                    List.of(
                            ""
                    ),
                    Config::validateBlockGroups
            );


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean inventControl;
    public static int depthLimit;
    public static List<Set<Block>> chainlandBlocksGroups;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        inventControl = INVENT_CONTROL.get();
        depthLimit = DEPTH_LIMIT.get();
        chainlandBlocksGroups = CHAINLAND_BLOCKS_GROUPS.get().stream()
                .map(
                        group -> Arrays.stream(group.split(","))
                                .map(String::trim)
                                .filter(name -> !name.isEmpty())
                                .map(name -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name)))
                                .collect(Collectors.toSet())
                )
                .collect(Collectors.toList());
    }

    //判断方块组配置合法
    private static boolean validateBlockGroups(final Object obj) {
        if (!(obj instanceof String group)) {
            return false;
        }
        for (String blockID : group.split(",")) {
            blockID = blockID.trim();
            if (blockID.isEmpty()){
                continue;
            }
            try {
                ResourceLocation resourceLocation = ResourceLocation.parse(blockID);
                if (!BuiltInRegistries.BLOCK.containsKey(resourceLocation)){
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
