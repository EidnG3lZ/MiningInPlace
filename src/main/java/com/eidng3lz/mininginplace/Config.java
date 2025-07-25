package com.eidng3lz.mininginplace;

import com.eidng3lz.mininginplace.network.packets.RequestPacket;
import com.eidng3lz.mininginplace.util.BlockGroup;
import com.google.common.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MiningInPlace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue INVENT_CONTROL = BUILDER
            .comment("反转潜行连锁控制", "客户端配置", "默认潜行时停止连锁。", "Invent control", "Client configuration", "By default, function stops while sneaking")
            .translation("mininginplace.config.invent_control")
            .define("invent_control", false);

    private static final ModConfigSpec.BooleanValue DISABLE_AT_CREATIVE = BUILDER
            .comment("创造模式下禁用", "客户端配置", "Disable at creative", "Client configuration")
            .translation("mininginplace.config.disable_at_creative")
            .define("disable_at_creative", true);

    private static final ModConfigSpec.IntValue DEPTH_LIMIT = BUILDER
            .comment("最大搜索深度", "服务端配置", "请勿设置过大的数值，以免出现性能问题。", "Maximum search depth", "Server-side configuration", "Do not set too large a number to avoid performance issues.")
            .translation("mininginplace.config.depth_limit")
            .defineInRange("depth_limit", 256, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SEARCH_STEPS_LIMIT = BUILDER
            .comment("最大搜索步数", "服务端配置", "请勿设置过大的数值，以免出现性能问题。", "Search steps limit", "Server-side configuration", "Do not set too large a number to avoid performance issues.")
            .translation("mininginplace.config.search_steps_limit")
            .defineInRange("search_steps_limit", 1024, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue DISABLE_TEMPORARY_CHAINED_FUNCTION = BUILDER
            .comment("禁用临时连锁挖掘功能", "服务端配置", "Disable temporary chained digging function", "Server-side configuration")
            .translation("mininginplace.config.disable_temporary_chained_function")
            .define("disable_temporary_chained_function", false);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> CHAINED_BLOCKS_GROUPS = BUILDER
            .comment(
                    "连锁方块组", "服务端配置", "每个字符串为一组，组内的多个ID或Tag之间用逗号分隔，Tag需要以#开头。", "命名空间为minecraft时命名空间可省略。",
                    "Chained blocks groups", "Server-side configuration", "Each string is a group, separated by a comma between multiple IDs or Tags in the group, and the Tags need to start with #.", "If the namespace is minecraft, the namespace can be omitted."
            )
            .translation("mininginplace.config.blocks_groups")
            .defineListAllowEmpty(
                    "chained_blocks_groups",
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
                            "#minecraft:coal_ores",
                            "#minecraft:iron_ores,minecraft:raw_iron_block",
                            "#minecraft:copper_ores,minecraft:raw_copper_block",
                            "#minecraft:gold_ores",
                            "#minecraft:redstone_ores",
                            "#minecraft:emerald_ores",
                            "#minecraft:lapis_ores",
                            "#minecraft:diamond_ores",
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
    public static boolean disableAtCreative;
    public static int depthLimit;
    public static int searchStepsLimit;
    public static boolean disableTemporaryChainedFunction;
    public static List<BlockGroup> chainedBlocksGroups;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        inventControl = INVENT_CONTROL.get();
        disableAtCreative = DISABLE_AT_CREATIVE.get();
        depthLimit = DEPTH_LIMIT.get();
        searchStepsLimit = SEARCH_STEPS_LIMIT.get();
        disableTemporaryChainedFunction = DISABLE_TEMPORARY_CHAINED_FUNCTION.get();
        chainedBlocksGroups = decodeChainedBlocksGroups(CHAINED_BLOCKS_GROUPS.get());
        //尝试向服务端同步用户配置
        //当前处于未进入世界等情况时向服务端发数据包会抛错误，这里直接选择用try catch拦截掉这个错误
        if (FMLEnvironment.dist.isClient()) {
            try {
                PacketDistributor.sendToServer(new RequestPacket(RequestPacket.SET_CLIENT_CONFIGS, getClientConfigsToJSON()));
            } catch (NullPointerException ignored) {
//                LogUtils.getLogger().info(e.toString());
            }
        }
        //向客户端同步配置，目前仅用于判断是否应该取消方块破坏的视觉效果
        try {
            PacketDistributor.sendToAllPlayers(new RequestPacket(RequestPacket.SET_SERVER_CONFIGS, getServerConfigsToJSON()));
        } catch (NullPointerException ignored) {
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

    public static List<BlockGroup> decodeChainedBlocksGroups(List<? extends String> stringList) {
        List<BlockGroup> blocksGroups = new ArrayList<>();
        for (String group : stringList) {
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
            blocksGroups.add(blockGroup);
        }
        return blocksGroups;
    }

    //将所有客户端配置转换成json字符串
    public static String getClientConfigsToJSON() {
        Map<ClientConfigs, Object> map = new HashMap<>();
        for (ClientConfigs clientConfig : ClientConfigs.values()) {
            map.put(clientConfig, clientConfig.getConfig().get());
        }
        Type type = new TypeToken<Map<ClientConfigs, Object>>() {
        }.getType();
        String json = MiningInPlace.gson.toJson(map, type);
//        LogUtils.getLogger().info(json);//test
        return json;
    }

    public static String getServerConfigsToJSON() {
        Map<ServerConfigs, Object> map = new HashMap<>();
        for (ServerConfigs serverConfig : ServerConfigs.values()) {
            map.put(serverConfig, serverConfig.getConfig().get());
        }
        Type type = new TypeToken<Map<ServerConfigs, Object>>() {
        }.getType();
        String json = MiningInPlace.gson.toJson(map, type);
        return json;
    }

    public enum ClientConfigs {
        INVENT_CONTROL(Config.INVENT_CONTROL),
        DISABLE_AT_CREATIVE(Config.DISABLE_AT_CREATIVE);

        private final Supplier<?> config;

        ClientConfigs(Supplier<?> config) {
            this.config = config;
        }

        public Supplier<?> getConfig() {
            return config;
        }
    }

    public enum ServerConfigs {
        DEPTH_LIMIT(Config.DEPTH_LIMIT),
        SEARCH_STEPS_LIMIT(Config.SEARCH_STEPS_LIMIT),
        DISABLE_TEMPORARY_CHAINED_FUNCTION(Config.DISABLE_TEMPORARY_CHAINED_FUNCTION),
        CHAINED_BLOCKS_GROUPS(Config.CHAINED_BLOCKS_GROUPS);

        private final Supplier<?> config;

        ServerConfigs(Supplier<?> config) {
            this.config = config;
        }

        public Supplier<?> getConfig() {
            return config;
        }
    }
}
