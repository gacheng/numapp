package com.relic.numapp.utils;

import java.util.Random;

public class RandomNumber {
    static Random rnd = new Random();

    /**
     * Helper method to get next random 9-digit number
     *
     * @return
     */
    public static String getNextNumber() {
        int number = rnd.nextInt(999999999);
        return String.format("%09d", number);
    }
}
