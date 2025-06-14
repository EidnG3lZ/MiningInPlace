package com.eidng3lz.mininginplace;

import com.google.gson.Gson;
import net.minecraft.util.Tuple;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MiningInPlace.MODID)
public class MiningInPlace {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mininginplace";
    private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedDeque<>();
    public static Gson gson = new Gson();
    public static ConcurrentHashMap<String, Map<Config.ClientConfigs, Object>> playerConfigs = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> blockBreakEventFlag = new ConcurrentHashMap<>();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MiningInPlace(IEventBus modEventBus, ModContainer modContainer) {

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new Tuple<>(action, tick));
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
        workQueue.forEach(work -> {
            work.setB(work.getB() - 1);
            if (work.getB() == 0) actions.add(work);
        });
        actions.forEach(e -> e.getA().run());
        workQueue.removeAll(actions);
    }

    @SubscribeEvent
    public void clearBlockBreakEventFlag(ServerTickEvent.Pre event) {
        if (!blockBreakEventFlag.isEmpty()) {
            blockBreakEventFlag.clear();
        }
    }
}
