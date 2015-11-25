public class KingMoveGen extends MoveGen {
	public static final long[] kingMoves = genKingMoves();
	
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
}
