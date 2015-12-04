public abstract class MoveGen {
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

	private Board board;
	private boolean side;
	private Piece piece;

	public MoveGen(Board board, boolean side) {
		this.board = board;
		this.side = side;
	}

	abstract public ArrayList<Move> genMoves();
	abstract public boolean isKingAttacked();

	private static boolean kingInCheck(Board board, boolean side) {
		MoveGen moveGens = {
			new PawnMoveGen(board, side),
			new RookMoveGen(board, side),
			new KnightMoveGen(board, side),
			new BishopMoveGen(board, side),
			new QueenMoveGen(board, side),
			new KingMoveGen(board, side)
		};

		for(MoveGen mg : moveGens) {
			if(mg.isKingAttacked()) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] getOccupancyIndexes(long occupancy) {
		long mask = 1L;
		int[] occupancyIndexes = new int[Long.bitCount(occupancy)];
		int index = 0;

		for(int i = 0; i < 64; i++) {
			if((occupancy & mask) != 0L) {
				occupancyIndexes[index++] = i;
			}
			occupancy = occupancy >>> 1;
		}
		return occupancyIndexes;
	}

	public static long get1BitMask(Square s) {
		long mask = 0x0000000000000001L;
		if(s.intValue == 0) {
			return mask;
		} else {
			return mask << s.intValue;
		}
	}
}
