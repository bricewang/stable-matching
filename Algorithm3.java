public class Algorithm3 {

    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader("preferences.txt"));

        int n = Integer.parseInt(input.readLine());
        int[][] ps = new int[n][n + 1];
        int[][] pr = new int[n][n + 1];
        for (int i = 0; i < n; i++) {
            String[] prefList = input.readLine().split();
            for (int j = 0; j < prefList.length; j++) {
                ps[i][Integer.parseInt(prefList[j])] = n - j;
            }
        }
        for (int i = 0; i < n; i++) {
            String[] prefList = input.readLine().split(" ");
            for (int j = 0; j < prefList.length; j++) {
                pr[i][Integer.parseInt(prefList[j])] = n - j;
            }
        }

        StableMatch match = ObliviousGaleShapely(new int[n], new int[n], ps, pr);
        match.printPrefs();
    }

    public static StableMatch ObliviousGaleShapely( int[] S, int[] R, int[][] Ps, int[][] Pr) {

        int i = 0;
        while (i < S.length) {

            for (int j: S) {

                for (int k: R) {

                    int c = ((Ps[j][S[j]] < Ps[j][k]) && (Pr[k][R[k]] < Pr[k][j]) ? 1 : 0;
                    S[j] = operator(k, S[j], c);
                    R[k] = operator(j, R[k], c);
                }
            }
            i++;
        }

        return new StableMatch(S, R);
    }

    public static int operator(int x, int y, int c) {
        return c * (x - y) + y;
    }

    private class StableMatch {
        private final int[] s;
        private final int[] r;

        public StableMatch(int[] s, int[] r) {
            this.s = s.clone();
            this.r = r.clone();
        }

        public void printPrefs() {
            System.out.println(Arrays.toString(s));
            System.out.println(Arrays.toString(r));
        }
    }
}
    
         
                                                                                                                                                                   1,1           Top
