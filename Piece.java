public enum Piece {
	WHITE_PAWN(0),
	WHITE_ROOK(1),
	WHITE_KNIGHT(2),
	WHITE_BISHOP(3),
	WHITE_QUEEN(4),
	WHITE_KING(5),
	BLACK_PAWN(6),
	BLACK_ROOK(7),
	BLACK_KNIGHT(8),
	BLACK_BISHOP(9),
	BLACK_QUEEN(10),
	BLACK_KING(11),
	EMPTY(12);

	private static final Piece[] valueIndexedEnums = Piece.values();
	public final int intValue;

	public static Piece toEnum(int value) {
		return valueIndexedEnums[value];
	}

	private Piece(int value) {
		intValue = value;
	}
}
