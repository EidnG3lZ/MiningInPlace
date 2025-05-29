package com.eidng3lz.mininginplace;

import com.eidng3lz.mininginplace.network.ConfigPayload;
import com.eidng3lz.mininginplace.utils.BlockGroup;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue INVENT_CONTROL = BUILDER
            .comment("反转潜行连锁控制", "默认潜行时停止连锁")
            .translation("mininginplace.config.invent_control")
            .define("invent_control", false);

    private static final ModConfigSpec.IntValue DEPTH_LIMIT = BUILDER
            .comment("最大搜索深度", "请勿设置过大的数值，以免可能出现的性能问题。")
            .translation("mininginplace.config.depth_limit")
            .defineInRange("depth_limit", 64, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SEARCH_STEPS_LIMIT = BUILDER
            .comment("最大搜索步数", "请勿设置过大的数值，以免可能出现的性能问题。")
            .translation("mininginplace.config.search_steps_limit")
            .defineInRange("search_steps_limit", 128, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> CHAINLAND_BLOCKS_GROUPS = BUILDER
            .comment("连锁方块组", "每个字符串为一组，组内的多个ID或Tag之间用逗号分隔，Tag需要以#开头。", "命名空间为minecraft时可省略。")
            .translation("mininginplace.config.blocks_groups")
            .defineListAllowEmpty(
                    "chainland_blocks_groups",
                    List.of(
                            "minecraft:oak_log",
                            "minecraft:spruce_log",
                            "minecraft:birch_log",
                            "minecraft:jungle_log",
                            "minecraft:acacia_log",
                            "minecraft:dark_oak_log",
                            "minecraft:mangrove_log",
                            "minecraft:cherry_log",
                            "minecraft:pale_oak_log",
                            "minecraft:crimson_stem",
                            "minecraft:warped_stem",
                            "minecraft:coal_ore,minecraft:deepslate_coal_ore",
                            "minecraft:iron_ore,minecraft:deepslate_iron_ore,minecraft:raw_iron_block",
                            "minecraft:copper_ore,minecraft:deepslate_copper_ore,minecraft:raw_copper_block",
                            "minecraft:gold_ore,minecraft:deepslate_gold_ore",
                            "minecraft:redstone_ore,minecraft:deepslate_redstone_ore",
                            "minecraft:emerald_ore,minecraft:deepslate_emerald_ore",
                            "minecraft:lapis_ore,minecraft:deepslate_lapis_ore",
                            "minecraft:diamond_ore,minecraft:deepslate_diamond_ore",
                            "minecraft:nether_gold_ore",
                            "minecraft:nether_quartz_ore",
                            "minecraft:ancient_debris",
                            "minecraft:glowstone"
                    ),
                    () -> "",
                    Config::validateBlockGroups
            );


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean inventControl;
    public static int depthLimit;
    public static int searchStepsLimit;
    public static List<BlockGroup> chainlandBlocksGroups;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        inventControl = INVENT_CONTROL.get();
        depthLimit = DEPTH_LIMIT.get();
        searchStepsLimit = SEARCH_STEPS_LIMIT.get();
//        chainlandBlocksGroups = CHAINLAND_BLOCKS_GROUPS.get().stream()
//                .map(
//                        group -> Arrays.stream(group.split(","))
//                                .map(String::trim)
//                                .filter(name -> !name.isEmpty())
//                                .map(name -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name)))
//                                .collect(Collectors.toSet())
//                )
//                .collect(Collectors.toList());
        chainlandBlocksGroups = new ArrayList<>();
        for (String group : CHAINLAND_BLOCKS_GROUPS.get()) {
            List<String> blockIDOrTagList = new ArrayList<>();
            String[] splitGroup = group.split(",");
            BlockGroup blockGroup = new BlockGroup();
            for (String s : splitGroup) {
                String blockIDOrTag = s.trim();
                if (!blockIDOrTag.isEmpty()) {
                    blockIDOrTagList.add(blockIDOrTag);
                }
            }
            for (String s : blockIDOrTagList) {
                blockGroup.addFromStr(s);
            }
            chainlandBlocksGroups.add(blockGroup);
        }
        //尝试向服务端同步用户配置
        //当前处于未进入世界等情况时向服务端发数据包会抛错误，这里直接选择用try catch拦截掉这个错误
        try {
            PacketDistributor.sendToServer(new ConfigPayload(inventControl));
        } catch (Exception e) {
            LogUtils.getLogger().info(e.getMessage());
        }
    }

    //判断方块组配置合法
    private static boolean validateBlockGroups(final Object obj) {
        if (!(obj instanceof String group)) {
            return false;
        }
        for (String blockIDOrTag : group.split(",")) {
            blockIDOrTag = blockIDOrTag.trim();
            if (blockIDOrTag.isEmpty()) {
                continue;
            }

            //检查tag是否合法。我不知道怎么检查tag是否存在，因此暂时只检查格式。
            if (blockIDOrTag.charAt(0) == '#') {
                if (!blockIDOrTag.matches("^#([a-zA-Z0-9._-]+:)?[a-zA-Z0-9._-]+(/?[a-zA-Z0-9._-]+)*$")) {
                    return false;
                }
                continue;
            }

            try {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(blockIDOrTag);
                if (!BuiltInRegistries.BLOCK.containsKey(resourceLocation)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
