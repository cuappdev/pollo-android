package com.cornellappdev.android.pollo;

public class Util {

    //Average number of seconds in years, months, weeks, days, hours, and minutes.
    static final int[] TIME_BLOCKS_IN_SECONDS = {31536000, 2628000, 604800, 86400, 3600, 60};

    public static int[] splitToComponentTimes(long time) {
        int[] dates = new int[7];
        for (int i = 0; i < TIME_BLOCKS_IN_SECONDS.length; i++) {
            dates[i] = (int) time / TIME_BLOCKS_IN_SECONDS[i];
            time = (int) time % TIME_BLOCKS_IN_SECONDS[i];
        }
        dates[6] = (int) time;

        return dates;
    }
}
