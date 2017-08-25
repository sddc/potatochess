public class Evaluation {
    public static int materialScore(Board b) {
        int[] pieceCounts = b.getPieceCounts();
        int sum = 0;

        // pawns
        sum += 100 * (pieceCounts[0] - pieceCounts[6]);

        // rooks 
        sum += 500 * (pieceCounts[1] - pieceCounts[7]);

        // knights 
        sum += 300 * (pieceCounts[2] - pieceCounts[8]);

        // bishops 
        sum += 300 * (pieceCounts[3] - pieceCounts[9]);

        // queens 
        sum += 900 * (pieceCounts[4] - pieceCounts[10]);

        // kings 
        sum += 20000 * (pieceCounts[5] - pieceCounts[11]);

        return sum;
    }
}
