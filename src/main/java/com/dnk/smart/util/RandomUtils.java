package com.dnk.smart.util;

import java.util.Random;

@SuppressWarnings("ALL")
public abstract class RandomUtils {

    public static int randomInteger(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static int randomInteger(int max) {
        return randomInteger(0, max);
    }

}
