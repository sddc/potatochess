import java.util.ArrayList;

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

	protected static Board board = null;

	private static MoveGen[] moveGens = {
	       PawnMoveGen.getInstance(),	
	       RookMoveGen.getInstance(),	
	       KnightMoveGen.getInstance(),	
	       BishopMoveGen.getInstance(),	
	       QueenMoveGen.getInstance(),	
	       KingMoveGen.getInstance()
	};

	public static void setBoard(Board board) {
		MoveGen.board = board;
	}

	public static ArrayList<Move> getMoves(boolean side) {
		if(board == null) {
			// check if board is set
			return null;
		}

		ArrayList<Move> moves = new ArrayList<Move>();

		for(MoveGen mg : moveGens) {
			moves.addAll(mg.generateMoves(side));
		}

		return moves;
	}

	public ArrayList<Move> generateMoves(boolean side) {
		ArrayList<Move> moves = new ArrayList<Move>();

		for(int fromSquare : getOccupancyIndexes(board.getPieceBitboard(sidePiece(side)))) {
			long moveBitboard = genMoveBitboard(side);

			for(int toSquare : getOccupancyIndexes(moveBitboard)) {
				Move move = new Move(Square.toEnum(fromSquare), Square.toEnum(toSquare), sidePiece(side));
				Piece type = board.getPieceType(Square.toEnum(toSquare));

				if(type != Piece.EMPTY) {
					move.setFlag(Flag.CAPTURE);
					move.setCapturePieceType(type);
				}
				
				board.move(side, move);
				if(!isKingInCheck(side)) {
					moves.add(move);
				}
				board.undoMove(side);
			}
		}

		return moves;
	}

	abstract public long genMoveBitboard(boolean side);
	abstract public Piece sidePiece(boolean side);
	abstract public boolean isKingInCheck(boolean side);

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

	public static long getSquareMask(int squareIndex) {
		long mask = 0x0000000000000001L;
		if(squareIndex == 0) {
			return mask;
		} else {
			return mask << squareIndex;
		}
	}
}
