package test.org.fun4j;

public class CPS3 {
    
    static final int MAX_DEPTH = 128;

    public static class Thunk {
        final Object r;

        final boolean isDelayed;

        public Object force() {
            Thunk t = this;
            while (t.isDelayed)
                t = t.compute();
            return t.r;
        }

        public Thunk compute() {
            return this;
        }

        public Thunk(Object answer) {
            isDelayed = false;
            r = answer;
        }

        public Thunk() {
            isDelayed = true;
            r = null;
        }
    }

    public static class Continuation {
        public Thunk apply(Object result) {
            return new Thunk(result);
        }
    }

    public static Thunk even(final int n, final Continuation c, final int depth) {
        if (depth >= MAX_DEPTH) {
            return new Thunk() {
                public Thunk compute() {
                    return even(n, c, 0);
                }
            };
        }
        if (n == 0)
            return c.apply(true);
        else
            return odd(n - 1, c, depth + 1);
    }

    public static Thunk odd(final int n, final Continuation c, final int depth) {
        if (depth >= MAX_DEPTH) {
            return new Thunk() {
                public Thunk compute() {
                    return odd(n, c, 0);
                }
            };
        }
        if (n == 0)
            return c.apply(false);
        else
            return even(n - 1, c, depth + 1);
    }

    public static void main(String args[]) {
        long time1 = System.currentTimeMillis();
        Object b = even(1000000001, new Continuation(), 0).force();
        long time2 = System.currentTimeMillis();
        System.out.println("time = " + (time2 - time1) + ", result = " + b);
        
//        for (int i = 1; i < 4097; i = i +10) {
//            MAX_DEPTH = i;
//            time1 = System.currentTimeMillis();
//            b = even(10000000, new Continuation(), 0).force();
//            time2 = System.currentTimeMillis();
//            System.out.println(i + ";" + (time2 - time1));
//        }
    }

}
