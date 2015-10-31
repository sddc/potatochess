public class Move {

	public static int firstOneBit(long x) {
		// =64 when x is 0
		return Long.numberOfTrailingZeros(x);
	}

	public static int lastOneBit(long x) {
		// =-1 when x is 0
		return (int)(63L - Long.numberOfLeadingZeros(x));
	}

	// +1, +7, +8, +9, -1, -7, -8, -9
	public static long shiftBits(int direction, long x) {
		long result = 0L;
		switch(direction) {
			case 7:
			case 8:
			case 9: 
			case 1:
				result = x << direction;
				break;
			case -7:
			case -8:
			case -9: 
			case -1:
				result = x >>> -direction;
				break;	
			default:
				break;

		}
		return result;
	}

	public static long[] genRayMask(int direction) {
		long[] genMask = new long[64];
		long pos = 0x0000000000000001L;
		long clearFile = 0xFFFFFFFFFFFFFFFFL;

		switch(direction) {
			case 7:
			case -1: 
			case -9:
				clearFile = Board.clearAFile;
				break;
			case 9:
			case 1:
			case -7:
				clearFile = Board.clearHFile;
				break;
			default:
				break;

		}

		for(int i = 0; i < 64; i++) {
			/* posNext initial position is LSB.
			 * Shift left or right x bits.
			 * If in file that will over/underflow to the next rank
			 * or the previous rank
			 * when shifted, AND it with the appropriate clearFile
			 * to end while loop and move pos to next bit.
			 */

			long posNext = shiftBits(direction, pos & clearFile);
			while(posNext != 0L) {
				genMask[i] = genMask[i] | posNext;
				posNext = shiftBits(direction, posNext & clearFile);
			}

			pos = pos << 1;
		}

		return genMask;
	}

	public static long genPawnPush(boolean side, long x) {
		// side:
		// white = true
		// black = false
		if(side) {
			return x << 8;
		} else {
			return x >>> 8;
		}
	}

	public static long genDoublePawnPush(boolean side, long x) {
		// side:
		// white = true
		// black = false
		if(side) {
			return (Board.maskRank3 & genPawnPush(side, x)) << 8;
		} else {
			return (Board.maskRank6 & genPawnPush(side, x)) >>> 8;
		}
	}

	public static long genPawnAttack(boolean side, long x) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ((Board.clearHFile & x) << 9) |
				((Board.clearAFile & x) << 7);
		} else {
			return ((Board.clearAFile & x) >>> 9) |
				((Board.clearHFile & x) >>> 7);
		}
	}

	/*
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * 1 2 3 _ _ _ _ _
	 * 8 x 4 _ _ _ _ _
	 * 7 6 5 _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 *
	 */
	public static long[] genKingMoves() {
		long[] genMoves = new long[64];
		long kingPos = 0x0000000000000001L;

		for(int i = 0; i < 64; i++) {
			long pos1 = (kingPos & Board.clearAFile) << 7;
			long pos8 = (kingPos & Board.clearAFile) >>> 1;
			long pos7 = (kingPos & Board.clearAFile) >>> 9;

			long pos2 = kingPos << 8;
			long pos6 = kingPos >>> 8;

			long pos3 = (kingPos & Board.clearHFile) << 9;
			long pos4 = (kingPos & Board.clearHFile) << 1;
			long pos5 = (kingPos & Board.clearHFile) >>> 7;

			genMoves[i] = pos1 | pos2 | pos3 | pos4 | pos5 | pos6 | pos7 | pos8;
			kingPos = kingPos << 1;
		}

		return genMoves;
	}

	/*
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ 1 _ 2 _ _
	 * _ _ 8 _ _ _ 3 _
	 * _ _ _ _ x _ _ _
	 * _ _ 7 _ _ _ 4 _
	 * _ _ _ 6 _ 5 _ _
	 *
	 */
	public static long[] genKnightMoves() {
		long[] genMoves = new long[64];
		long knightPos = 0x0000000000000001L;

		for(int i = 0; i < 64; i++) {
			long pos8 = (knightPos & Board.clearAFile & Board.clearBFile) << 6;
			long pos7 = (knightPos & Board.clearAFile & Board.clearBFile) >>> 10;

			long pos1 = (knightPos & Board.clearAFile) << 15;
			long pos6 = (knightPos & Board.clearAFile) >>> 17;

			long pos2 = (knightPos & Board.clearHFile) << 17;
			long pos5 = (knightPos & Board.clearHFile) >>> 15;

			long pos3 = (knightPos & Board.clearHFile & Board.clearGFile) << 10;
			long pos4 = (knightPos & Board.clearHFile & Board.clearGFile) >>> 6;

			genMoves[i] = pos1 | pos2 | pos3 | pos4 | pos5 | pos6 | pos7 | pos8;
			knightPos = knightPos << 1;
		}
		return genMoves;
	}
}
