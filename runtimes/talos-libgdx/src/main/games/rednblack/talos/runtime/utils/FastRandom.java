package games.rednblack.talos.runtime.utils;

import java.util.Random;

public class FastRandom extends Random {
    private int state;

    public FastRandom() {
        this((int) System.currentTimeMillis());
    }

    public FastRandom(int seed) {
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        setSeed((int) seed);
    }

    public void setSeed(int seed) {
        this.state = seed;
    }

    public int nextInt() {
        int x = state;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        state = x;
        return x;
    }

    @Override
    protected int next(int bits) {
        return nextInt() >>> (32 - bits);
    }

    @Override
    public float nextFloat() {
        return (nextInt() & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    @Override
    public double nextDouble() {
        return (nextInt() & 0x7FFFFFFF) / (double) 0x7FFFFFFF;
    }

    @Override
    public boolean nextBoolean() {
        return nextInt() < 0;
    }
}
