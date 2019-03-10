package baked.potato;

public class Mask {
    public static final long[] rank;
    public static final long[] file;
    public static final long[] diag;
    public static final int[] diagIdx;
    public static final long[] antiDiag;
    public static final int[] antiDiagIdx;

    public static final long clearFileA = 0xFEFEFEFEFEFEFEFEL;
    public static final long clearFileB = 0xFDFDFDFDFDFDFDFDL;
    public static final long clearFileG = 0xBFBFBFBFBFBFBFBFL;
    public static final long clearFileH = 0x7F7F7F7F7F7F7F7FL;

    public static final long clearRank1 = 0xFFFFFFFFFFFFFF00L;
    public static final long clearRank8 = 0xFFFFFFFFFFFFFFL;

    public static final long maskRank1 = 0x00000000000000FFL;
    public static final long maskRank2 = 0x000000000000FF00L;
    public static final long maskRank3 = 0x0000000000FF0000L;
    public static final long maskRank4 = 0x00000000FF000000L;
    public static final long maskRank5 = 0x000000FF00000000L;
    public static final long maskRank6 = 0x0000FF0000000000L;
    public static final long maskRank7 = 0x00FF000000000000L;
    public static final long maskRank8 = 0xFF00000000000000L;

    public static final long maskFileA = 0x101010101010101L;
    public static final long maskFileH = 0x8080808080808080L;

    static {
        // init rank
        rank = new long[8];
        long mask = 0xFF;
        for(int i = 0; i < 8; i++) {
            rank[i] = mask;
            mask <<= 8;
        }

        // init rank
        file = new long[8];
        mask = 0x101010101010101L;
        for(int i = 0; i < 8; i++) {
            file[i] = mask;
            mask <<= 1;
        }

        // init diag
        diag = new long[15];
        mask = 0x8040201008040201L;
        for(int i = 7; i >= 0; i--) {
            diag[i] = mask;
            mask <<= 8;
        }

        mask = 0x8040201008040201L >>> 8;
        for(int i = 8; i < 15; i++) {
            diag[i] = mask;
            mask >>>= 8;
        }

        diagIdx = new int[64];
        int index = 0;
        for(int i = 7; i >= 0; i--) {
            for(int j = i; j < i + 8; j++) {
                diagIdx[index++] = j;
            }
        }

        // init antiDiag
        antiDiag = new long[15];
        mask = 0x102040810204080L;
        for(int i = 7; i >= 0; i--) {
            antiDiag[i] = mask;
            mask >>>= 8;
        }

        mask = 0x102040810204080L << 8;
        for(int i = 8; i < 15; i++) {
            antiDiag[i] = mask;
            mask <<= 8;
        }

        antiDiagIdx = new int[64];
        index = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = i; j < i + 8; j++) {
                antiDiagIdx[index++] = j;
            }
        }

    }
}
