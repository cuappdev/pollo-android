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
}
