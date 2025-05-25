package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.MiningInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.List;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnBlockDrops {
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        //设置偏移量和弹射力度
        final double OFFSET = 0.87;
        final double FORCE = 0.1;

        Entity breakerEntity = event.getBreaker();
        List<ItemEntity> dropItemEntityList = event.getDrops();
        BlockPos eventPos = event.getPos();
        //如果事件的breaker存在
        if (breakerEntity != null) {
            //判断是否是目标玩家进行的破坏
            if (MiningInPlace.blockBreakEventFlag.containsKey(breakerEntity.toString())) {
                //计算相关向量
                Vec3 relativelyVec = eventPos.getCenter().vectorTo(breakerEntity.position());
                Vec3 offsetVec = relativelyVec.normalize().multiply(OFFSET, OFFSET, OFFSET);
                Vec3 moveToVec = eventPos.getCenter().add(offsetVec);
                Vec3 pushVec = moveToVec.vectorTo(breakerEntity.position()).multiply(FORCE, FORCE, FORCE);
                //遍历掉落物列表将实体移动出方块碰撞范围并弹向玩家
                for (ItemEntity item : dropItemEntityList) {
                    item.moveTo(moveToVec);
                    item.push(pushVec);
                }
                //删除flag
                MiningInPlace.blockBreakEventFlag.remove(breakerEntity.toString());
            }
        }
    }
}
