package com.dnk.smart.util;

import java.util.Random;

public abstract class RandomUtils {

    public static int randomInteger(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

}
