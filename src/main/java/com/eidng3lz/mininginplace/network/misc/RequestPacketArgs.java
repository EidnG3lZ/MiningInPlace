package com.eidng3lz.mininginplace.network.misc;

public class RequestPacketArgs {
    public record BlockDestroyEffectArgs(int[] pos, int blockId) {
    }
}
