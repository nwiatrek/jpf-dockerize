public class MemDemo {
    public static void main(String... args) {
        m(30,12);
        m(30,2);
        m(20,12);
        addThree(1,2,3);
        addThree(1,2,3);
        C.partThree(new C(1,2));
        C.partThree(new C(2,2));
        C.partThree(new C(1,2));
        C.m(new C(2, new A(2)));
        C.m(new C(2, new A(2)));
        q(2,3.1);
        q(2,3.1);
    }


    public static double q(double a, double b) {
        return a + b;
    }

    public static int m(int a, int b) {
        return a + b;
    }
    public static int addThree(int a, int b, int c) {
        return a + b + c;
    }
}