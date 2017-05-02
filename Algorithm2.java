public class Algorithm2 {

    public static void main(String[] args) {

/**
 * Testing operator code
 */
        int x = 1;
        int y = 0;
        int c = 1;
        int z = operator(x,y,c);
        System.out.println(z);
    }

    public static int operator(int x, int y, int c) {
        int z;
        z = c * (x - y) + y;
        return z;
    }

}
