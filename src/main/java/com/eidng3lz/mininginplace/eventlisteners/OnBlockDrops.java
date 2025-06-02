package com.eidng3lz.mininginplace.eventlisteners;

import com.eidng3lz.mininginplace.MiningInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MiningInPlace.MODID)
public class OnBlockDrops {
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        //设置方块和物品实体尺寸和弹射力度
        final double FORCE = 0.1;
        final double BLOCK_SIZE = 1.0;
        final double ITEM_ENTITY_SIZE = 0.25;
        final Vec3 ITEM_INITIAL_VELOCITY = new Vec3(0, 0.1, 0);

        final boolean USE_NEW_OFFSET_ALGORITHM = true;

        Entity breakerEntity = event.getBreaker();
        List<ItemEntity> dropItemEntityList = event.getDrops();
        BlockPos eventPos = event.getPos();
        //如果事件的breaker存在
        if (breakerEntity != null) {
            //判断是否是目标玩家进行的破坏
            if (MiningInPlace.blockBreakEventFlag.containsKey(breakerEntity.toString())) {
                //计算相关向量
                Vec3 relativelyVec = eventPos.getCenter().vectorTo(breakerEntity.getEyePosition());
//                Vec3 offsetVec = relativelyVec.normalize().multiply(OFFSET, OFFSET, OFFSET);
                Vec3 moveToVec = eventPos.getCenter();
                if (!USE_NEW_OFFSET_ALGORITHM) {
                    Vec3 offsetVec = getOffsetVec(relativelyVec, BLOCK_SIZE, ITEM_ENTITY_SIZE);
                    moveToVec = eventPos.getCenter().add(offsetVec).add(0, -ITEM_ENTITY_SIZE / 2, 0);
                } else {
                    Tuple<Vec3, Integer> result0 = getIntersection(
                            breakerEntity.getEyePosition(),
                            breakerEntity.getViewVector(1),
                            new Vec3(eventPos.getX(), eventPos.getY(), eventPos.getZ())
                    );
                    double offset = (BLOCK_SIZE + ITEM_ENTITY_SIZE) / 2;
                    moveToVec = switch (result0.getB()) {
                        case 0 -> eventPos.getCenter().add(-offset, 0, 0);
                        case 1 -> eventPos.getCenter().add(0, -offset, 0);
                        case 2 -> eventPos.getCenter().add(0, 0, -offset);
                        case 3 -> eventPos.getCenter().add(offset, 0, 0);
                        case 4 -> eventPos.getCenter().add(0, offset, 0);
                        case 5 -> eventPos.getCenter().add(0, 0, offset);
                        default -> moveToVec;
                    };
                    moveToVec = moveToVec.add(0, -ITEM_ENTITY_SIZE / 2, 0);
                }
                Vec3 pushVec = moveToVec.vectorTo(breakerEntity.getEyePosition()).multiply(FORCE, FORCE, FORCE);
                //遍历掉落物列表将实体移动出方块碰撞范围并弹向玩家
                for (ItemEntity item : dropItemEntityList) {
                    item.setDeltaMovement(ITEM_INITIAL_VELOCITY);//先重置初速度以免乱飞
                    item.setPos(moveToVec);
                    item.push(pushVec);
                }
                //删除flag
                MiningInPlace.blockBreakEventFlag.remove(breakerEntity.toString());
            }
        }
    }

    private static @NotNull Vec3 getOffsetVec(Vec3 relativelyVec, double BLOCK_SIZE, double ITEM_ENTITY_SIZE) {
        double max = Math.max(Math.max(Math.abs(relativelyVec.x()), Math.abs(relativelyVec.y())), Math.abs(relativelyVec.z()));
        double zoomMultiplier = ((BLOCK_SIZE + ITEM_ENTITY_SIZE) / 2) / max;
        return relativelyVec.multiply(zoomMultiplier, zoomMultiplier, zoomMultiplier);
    }

    private static Tuple<Vec3, Integer> getIntersection(Vec3 startPos, Vec3 direction, Vec3 aabbMinPos) {
        double[] startPosArr = {startPos.x(), startPos.y(), startPos.z()};
        double[] directionArr = {direction.x(), direction.y(), direction.z()};
        double[] aabbMinPosArr = {aabbMinPos.x(), aabbMinPos.y(), aabbMinPos.z()};

        //(x,y,z)=(x0+t*xd,y0+t*yd,z0+t*zd)

        List<Tuple<Double, Integer>> tList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (directionArr[i] == 0) {
                continue;
            }
            double t;
            t = (aabbMinPosArr[i] - startPosArr[i]) / directionArr[i];
            double v0 = startPosArr[(i + 1) % 3] + t * directionArr[(i + 1) % 3];
            double v1 = startPosArr[(i + 2) % 3] + t * directionArr[(i + 2) % 3];
            if (
                    v0 > aabbMinPosArr[(i + 1) % 3]
                            && v0 < aabbMinPosArr[(i + 1) % 3] + 1
                            && v1 > aabbMinPosArr[(i + 2) % 3]
                            && v1 < aabbMinPosArr[(i + 2) % 3] + 1
            ) {
                tList.add(new Tuple<>(t, i));
            }
            t = (aabbMinPosArr[i] + 1 - startPosArr[i]) / directionArr[i];
            v0 = startPosArr[(i + 1) % 3] + t * directionArr[(i + 1) % 3];
            v1 = startPosArr[(i + 2) % 3] + t * directionArr[(i + 2) % 3];
            if (
                    v0 > aabbMinPosArr[(i + 1) % 3]
                            && v0 < aabbMinPosArr[(i + 1) % 3] + 1
                            && v1 > aabbMinPosArr[(i + 2) % 3]
                            && v1 < aabbMinPosArr[(i + 2) % 3] + 1
            ) {
                tList.add(new Tuple<>(t, i + 3));
            }
        }
        Tuple<Double, Integer> tMin = tList.getFirst();
        for (Tuple<Double, Integer> i : tList) {
            if (i.getA() < tMin.getA()) {
                tMin = i;
            }
        }
        return new Tuple<>(
                new Vec3(
                        startPos.x() + tMin.getA() * direction.x(),
                        startPos.y() + tMin.getA() * direction.y(),
                        startPos.z() + tMin.getA() * direction.z()
                ),
                tMin.getB()
        );
    }
}
