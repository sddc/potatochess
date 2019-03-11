package baked.potato;

public class Mask {
    public static final long[] rank;
    public static final int[] rankIdx;
    public static final long[] file;
    public static final int[] fileIdx;
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

        rankIdx = new int[64];
        for(int i = 0; i < 64; i++) {
            rankIdx[i] = i / 8;
        }

        // init rank
        file = new long[8];
        mask = 0x101010101010101L;
        for(int i = 0; i < 8; i++) {
            file[i] = mask;
            mask <<= 1;
        }

        fileIdx = new int[64];
        for(int i = 0; i < 64; i++) {
            fileIdx[i] = i % 8;
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

    public static long between(int sq1, int sq2) {
        if(sq1 == sq2) return 0;
        long sq1Mask = 1L << sq1;
        long sq2Mask = 1L << sq2;
        long squares = sq1Mask | sq2Mask;
        long mask = 0;

        if((Mask.rank[Mask.rankIdx[sq1]] & squares) == squares) {
            mask = Mask.rank[Mask.rankIdx[sq1]];
        }

        if((Mask.file[Mask.fileIdx[sq1]] & squares) == squares) {
            mask = Mask.file[Mask.fileIdx[sq1]];
        }

        if((Mask.diag[Mask.diagIdx[sq1]] & squares) == squares) {
            mask = Mask.diag[Mask.diagIdx[sq1]];
        }

        if((Mask.antiDiag[Mask.antiDiagIdx[sq1]] & squares) == squares) {
            mask = Mask.antiDiag[Mask.antiDiagIdx[sq1]];
        }

        if(mask == 0) return 0;
        if(sq1 > sq2) {
            mask &= (sq1Mask - sq2Mask) ^ sq2Mask;
        } else {
            mask &= (sq2Mask - sq1Mask) ^ sq1Mask;
        }

        return mask;
    }
}
