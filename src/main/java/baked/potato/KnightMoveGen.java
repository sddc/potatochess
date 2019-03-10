package baked.potato;

public class KnightMoveGen extends MoveGen {
	private static KnightMoveGen instance = new KnightMoveGen();
	public static final long[] knightMoves = genKnightMoves();

	private KnightMoveGen() {
	}

	public static KnightMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
		return knightMoves[fromSquare];
	}

	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_KNIGHT;
		} else {
			return Piece.BLACK_KNIGHT;
		}
	}

	@Override
	public boolean squareAttacked(Board b, boolean side, int square) {
		return (knightMoves[square] & b.getKnightBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return knightMoves[square] & b.getKnightBitboard(!side);
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

	public static long knightMoves(long knightPos) {
		long moves = 0;

		// pos 1
		moves |= (knightPos & Mask.clearFileA) << 15;
		// pos 2
		moves |= (knightPos & Mask.clearFileH) << 17;
		// pos 3
		moves |= (knightPos & Mask.clearFileH & Mask.clearFileG) << 10;
		// pos 4
		moves |= (knightPos & Mask.clearFileH & Mask.clearFileG) >>> 6;
		// pos 5
		moves |= (knightPos & Mask.clearFileH) >>> 15;
		// pos 6
		moves |= (knightPos & Mask.clearFileA) >>> 17;
		// pos 7
		moves |= (knightPos & Mask.clearFileA & Mask.clearFileB) >>> 10;
		// pos 8
		moves |= (knightPos & Mask.clearFileA & Mask.clearFileB) << 6;

		return moves;
	}

	public static long[] genKnightMoves() {
		long[] genMoves = new long[64];
		long knightPos = 0x0000000000000001L;

		for(int i = 0; i < 64; i++) {
			genMoves[i] = knightMoves(knightPos);
			knightPos = knightPos << 1;
		}
		return genMoves;
	}
}
