public class C {
    A a;
    int g;
    int f;

    C(int g, A a) {
        this.a = a;
        this.g = g;
    }
    C(int f, int g) {
        this.f = f;
        this.g =g;
    }

    public static int m(C obj) { 
        return obj.g + obj.a.f; 
    }

    public static int partThree(C obj) {
        return obj.f + obj.g;
    }

    public static int norm(int a, int b) {
        return a + b;
    }

    public static void main(String... args) {
        m(new C(2, new A(2)));
        m(new C(2, new A(2)));
        norm(1,2);
        norm(1,2);
        partThree(new C(1,2));
        partThree(new C(1,2));
    }



}