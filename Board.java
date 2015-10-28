public class Board {
	public static final int EMPTY = 0;
	public static final int WHITE_PAWN = 1;
	public static final int WHITE_ROOK = 2;
	public static final int WHITE_KNIGHT = 3;
	public static final int WHITE_BISHOP = 4;
	public static final int WHITE_QUEEN = 5;
	public static final int WHITE_KING = 6;
	public static final int BLACK_PAWN = -1;
	public static final int BLACK_ROOK = -2;
	public static final int BLACK_KNIGHT = -3;
	public static final int BLACK_BISHOP = -4;
	public static final int BLACK_QUEEN = -5;
	public static final int BLACK_KING = -6;

	long WP = 0x000000000000FF00L;
	long WR = 0x0000000000000081L;
	long WN = 0x0000000000000042L;
	long WB = 0x0000000000000024L;
	long WQ = 0x0000000000000008L;
	long WK = 0x0000000000000010L;

	long BP = 0x00FF000000000000L;
	long BR = 0x8100000000000000L;
	long BN = 0x4200000000000000L;
	long BB = 0x2400000000000000L;
	long BQ = 0x0800000000000000L;
	long BK = 0x1000000000000000L;

	long whitePieces = WP | WR | WN | WB | WQ | WK;
	long blackPieces = BP | BR | BN | BB | BQ | BK;
	long allPieces = whitePieces | blackPieces;
}
