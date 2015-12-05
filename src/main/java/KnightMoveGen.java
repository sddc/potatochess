import java.util.ArrayList;

public class KnightMoveGen extends MoveGen {
	public KnightMoveGen(Board board, boolean side) {
		super(board, side);
	}
	public ArrayList<Move> genMoves() {
		return null;
	}
	 
	public boolean isKingAttacked() {
		return false;
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
			long pos8 = (knightPos & clearFileA & clearFileB) << 6;
			long pos7 = (knightPos & clearFileA & clearFileB) >>> 10;

			long pos1 = (knightPos & clearFileA) << 15;
			long pos6 = (knightPos & clearFileA) >>> 17;

			long pos2 = (knightPos & clearFileH) << 17;
			long pos5 = (knightPos & clearFileH) >>> 15;

			long pos3 = (knightPos & clearFileH & clearFileG) << 10;
			long pos4 = (knightPos & clearFileH & clearFileG) >>> 6;

			genMoves[i] = pos1 | pos2 | pos3 | pos4 | pos5 | pos6 | pos7 | pos8;
			knightPos = knightPos << 1;
		}
		return genMoves;
	}
}
