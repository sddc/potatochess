public class KnightMoveGen extends MoveGen {
	public ArrayList<Move> genMoves() {
	}
	 
	public boolean isKingAttacked() {
	}
	public static final long[] knightMoves = genKnightMoves();

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
