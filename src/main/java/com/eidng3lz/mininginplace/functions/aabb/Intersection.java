package com.eidng3lz.mininginplace.functions.aabb;

import net.minecraft.world.phys.Vec3;

public class Intersection {
    /**
     * 计算射线与轴对齐包围盒（AABB）的相交面索引
     * 该方法不进行任何参数有效性检查，假定输入的参数是有效的
     *
     * @param startPos   射线的起始位置
     * @param direction  射线的方向
     * @param aabbMinPos AABB的最小角位置
     * @return 返回相交的面索引，如果没有相交则返回-1
     */
    public static int getRayIntersectSurfaceNoCheck(Vec3 startPos, Vec3 direction, Vec3 aabbMinPos) {
        // 将Vec3对象转换为double数组，便于计算
        double[] startPosArr = {startPos.x(), startPos.y(), startPos.z()};
        double[] directionArr = {direction.x(), direction.y(), direction.z()};
        double[] aabbMinPosArr = {aabbMinPos.x(), aabbMinPos.y(), aabbMinPos.z()};
        // 计算AABB的最大角位置，假设AABB的边长为1
        double[] aabbMaxPosArr = {aabbMinPos.x() + 1, aabbMinPos.y() + 1, aabbMinPos.z() + 1};

        // 初始化最近交点的距离为负无穷，用于后续比较
        double tNear = Double.NEGATIVE_INFINITY;
        // 初始化相交面索引为-1，表示尚未找到相交面
        int surfaceIndex = -1;

        //(x,y,z)=(x0+t*xd,y0+t*yd,z0+t*zd)

        // 遍历x、y、z三个维度，计算射线与AABB在每个维度上的相交情况
        for (int i = 0; i < 3; i++) {
            // 如果射线方向在当前维度上的分量为0，则不会与AABB相交，跳过当前维度
            if (directionArr[i] == 0) {
                continue;
            }
            // 计算射线方向在当前维度上的倒数，用于后续计算t值
            double invDir = 1.0 / directionArr[i];
            // 计算射线与AABB在当前维度上的两个可能的交点t值
            double t1 = (aabbMinPosArr[i] - startPosArr[i]) * invDir;
            double t2 = (aabbMaxPosArr[i] - startPosArr[i]) * invDir;

            // 获得交点在这个轴上的可能相交面的标志和最小t值
            int intersectIn;
            double tMin;
            if (t1 < t2) {
                intersectIn = -1;
                tMin = t1;
            } else {
                intersectIn = 1;
                tMin = t2;
            }

            // 如果当前维度上的最小t值大于已知的最近交点距离，更新最近交点距离和相交面索引
            if (tMin > tNear) {
                tNear = tMin;
                if (intersectIn == -1) {
                    surfaceIndex = i;
                } else {
                    surfaceIndex = i + 3;
                }
            }
        }
        // 返回相交的面索引
        return surfaceIndex;
    }
}
