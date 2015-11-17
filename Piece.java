public enum Piece {
	WHITE_PAWN,
	WHITE_ROOK,
	WHITE_KNIGHT,
	WHITE_BISHOP,
	WHITE_QUEEN,
	WHITE_KING,
	BLACK_PAWN,
	BLACK_ROOK,
	BLACK_KNIGHT,
	BLACK_BISHOP,
	BLACK_QUEEN,
	BLACK_KING,
	EMPTY;

	private static final Piece[] ordinalEnum = Piece.values();

	public static Piece intToEnum(int position) {
		return ordinalEnum[position];
	}
}
