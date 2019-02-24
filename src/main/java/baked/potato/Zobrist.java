package baked.potato;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class Zobrist {
    public static final long[][] randSquare = new long[12][64];
    public static final long[] randCastle = new long[16];
    public static final long randSide;
    public static final long[] randEp = new long[64];

    private static final int total = (randSquare.length * randSquare[0].length) + randCastle.length + 1 + randEp.length;

    static {
        // init random keys:
        // 12 pieces x 64 squares = 768
        // castling combinations = 16
        // side
        // 64 ep target squares
        // 849 total

        Random rand = new Random();
        HashSet<Long> randomNums = new HashSet<>();

        for(int i = 0; i < total; i++) {
            long rn;

            do {
                rn = rand.nextLong();
            } while(rn == 0 || randomNums.contains(rn));

            randomNums.add(rn);
        }

        Iterator<Long> iter = randomNums.iterator();

        randSide = iter.next();

        // fill random squares
        for(int i = 0; i < randSquare.length; i++) {
            for(int j = 0; j < randSquare[0].length; j++) {
                randSquare[i][j] = iter.next();
            }
        }

        // fill random ep target squares
        for(int i = 0; i < randEp.length; i++) {
            randEp[i] = iter.next();
        }

        // fill random castle rights
        for(int i = 0; i < randCastle.length; i++) {
            randCastle[i] = iter.next();
        }

    }
}
