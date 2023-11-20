package com.jetcemetery.calculusPhoneNumber.helper;

import java.security.SecureRandom;
import java.util.Random;

public class getRandomInRange {

    public static int getRandomNumberInRange(int min, int max) {
        return getRandomNumberInRangePrivate(min, max);
    }

    private static int getRandomNumberInRangePrivate(int min, int max) {
        //from https://mkyong.com/java/java-generate-random-integers-in-a-range/
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new SecureRandom();
        return r.nextInt((max - min) + 1) + min;
    }
}
