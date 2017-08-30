public class Evaluation {
    // table values from
    // https://chessprogramming.wikispaces.com/Simplified+evaluation+function

    private static final int[] pawnTable = {
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, -20, -20, 10, 10, 5,
        5, -5, -10, 0, 0, -10, -5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, 5, 10, 25, 25, 10, 5, 5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] rookTable = {
        0, 0, 0, 5, 5, 0, 0, 0,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        5, 10, 10, 10, 10, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] knightTable = {
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    };

    private static final int[] bishopTable = {
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    };

    private static final int[] queenTable = {
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 5, 0, 0, 0, 0, -10,
        -10, 5, 5, 5, 5, 5, 0, -10,
        0, 0, 5, 5, 5, 5, 0, -5,
        -5, 0, 5, 5, 5, 5, 0, -5,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20
    };

    private static final int[] kingMidGameTable = {
        20, 30, 10, 0, 0, 10, 30, 20,
        20, 20, 0, 0, 0, 0, 20, 20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30
    };

    private static final int[] kingEndGameTable = {
        -50, -30, -30, -30, -30, -30, -30, -50,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -50, -40, -30, -20, -20, -30, -40, -50
    };

    private static final int[] mirror = {
        56, 57, 58, 59, 60, 61, 62, 63,
        48, 49, 50, 51, 52, 53, 54, 55,
        40, 41, 42, 43, 44, 45, 46, 47,
        32, 33, 34, 35, 36, 37, 38, 39,
        24, 25, 26, 27, 28, 29, 30, 31,
        16, 17, 18, 19, 20, 21, 22, 23,
        8, 9, 10, 11, 12, 13, 14, 15,
        0, 1, 2, 3, 4, 5, 6, 7
    };

    public static int score(Board b) {
        int sum = 0;

        sum += materialScore(b);
        sum += positionalScore(b); 

        return sum;
    }

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

    public static int positionalScore(Board b) {
        int sum = 0;
        long[] bitboards = b.getBitboards();
        int[] table = null;

        for(int i = 0; i < 12; i++) {
            switch(Piece.toEnum(i)) {
                case WHITE_PAWN:
                case BLACK_PAWN:
                    table = pawnTable;
                    break;
                case WHITE_ROOK:
                case BLACK_ROOK:
                    table = rookTable;
                    break;
                case WHITE_KNIGHT:
                case BLACK_KNIGHT:
                    table = knightTable;
                    break;
                case WHITE_BISHOP:
                case BLACK_BISHOP:
                    table = bishopTable;
                    break;
                case WHITE_QUEEN:
                case BLACK_QUEEN:
                    table = queenTable;
                    break;
                case WHITE_KING:
                case BLACK_KING:
                    // use king end game table if black and white have no queens
                    if(Long.bitCount(b.getQueenBitboard(Board.WHITE)) == 0
                            && Long.bitCount(b.getQueenBitboard(Board.BLACK)) == 0) {
                        table = kingEndGameTable;
                    } else{
                        table = kingMidGameTable;
                    }
                    break;
                default:
                    break;
            }

            for(Square s : MoveGen.getOccupancyIndexes(bitboards[i])) {
                if(i < 6) {
                    sum += table[s.intValue];
                } else {
                    sum -= table[mirror[s.intValue]];
                }
            }
        }

        return sum;
    }
}
