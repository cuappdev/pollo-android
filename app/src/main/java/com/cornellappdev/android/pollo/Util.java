package com.cornellappdev.android.pollo;

public class Util {
    public class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    public class Triple<X, Y, Z> {
        public final X x;
        public final Y y;
        public final Z z;
        public Triple(X x, Y y, Z z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static int[] splitToComponentTimes(long time)
    {
        int[] dates = new int[7];
        dates[0] = (int) time / 31536000;
        time = (int) time % 31536000;
        dates[1] = (int) time / 2628000;
        time = (int) time % 2628000;
        dates[2] = (int) time / 604800;
        time = (int) time % 604800;
        dates[3] = (int) time / 86400;
        time = (int) time % 86400;
        dates[4] = (int) time / 3600;
        time = (int) time % 3600;
        dates[5] = (int) time / 60;
        time = time % 60;
        dates[6] = (int) time;

        return dates;
    }
}
