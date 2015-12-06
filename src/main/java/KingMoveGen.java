import java.util.ArrayList;

public class KingMoveGen extends MoveGen {
	private static KingMoveGen instance = new KingMoveGen();
	public static final boolean KINGSIDE = true;
	public static final boolean QUEENSIDE = false;

	private KingMoveGen() {
	}

	public static KingMoveGen getInstance() {
		return instance;
	}

	@Override
	public long genMoveBitboard(boolean side) {
		return 1L;
	}

	@Override
	public Piece sidePiece(boolean side) {
		return Piece.WHITE_PAWN;
	}

	@Override
	public boolean isKingInCheck(boolean side) {
		return false;
	}
	 
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
			long pos1 = (kingPos & clearFileA) << 7;
			long pos8 = (kingPos & clearFileA) >>> 1;
			long pos7 = (kingPos & clearFileA) >>> 9;

			long pos2 = kingPos << 8;
			long pos6 = kingPos >>> 8;

			long pos3 = (kingPos & clearFileH) << 9;
			long pos4 = (kingPos & clearFileH) << 1;
			long pos5 = (kingPos & clearFileH) >>> 7;

			genMoves[i] = pos1 | pos2 | pos3 | pos4 | pos5 | pos6 | pos7 | pos8;
			kingPos = kingPos << 1;
		}

		return genMoves;
	}
/*
	public boolean castlingAvailable(boolean side, boolean squares, long attacks) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long pieceMask;
		long attackMask;
		if(side == Board.WHITE) {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if(!castleStatus[0]) {
					return false;
				}
				pieceMask = 0x60L;
				attackMask = pieceMask;
			} else {
				// check if queenside castle available
				if(!castleStatus[1]) {
					return false;
				}
				pieceMask = 0xEL;
				attackMask = 0xCL;
			}
		} else {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if(!castleStatus[2]) {
					return false;
				}
				pieceMask = 0x6000000000000000L;
				attackMask = pieceMask;
			} else {
				// check if queenside castle available
				if(!castleStatus[3]) {
					return false;
				}
				pieceMask = 0xE00000000000000L;
				attackMask = 0xC00000000000000L;
			}
		}

		// check if any pieces between king and rook. also check if opponent
		// is attacking squares king passes or ends up on
		if(((pieceMask & board.getAllPieces()) == 0L) && ((attackMask & attacks) == 0L)) {
			return true;
		} else {
			return false;
		}
	}
	
	private void genCastlingMoves(boolean side, ArrayList<Move> moves) {
		long opponentAttacks = genAttackSquares(!side);
		Move m;
		// check if king in check
		if(kingInCheck(side, opponentAttacks)) {
			return;
		}

		// check if kingside castling is available
		if(chessboard.castlingAvailable(side, Board.KINGSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				m = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.KINGSIDE);
				moves.add(m);
			} else {
				m = new Move(Square.E8, Square.G8, Piece.BLACK_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.KINGSIDE);
				moves.add(m);
			}
		}

		// check if queenside castling is available
		if(chessboard.castlingAvailable(side, Board.QUEENSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				m = new Move(Square.E1, Square.C1, Piece.WHITE_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.QUEENSIDE);
				moves.add(m);
			} else {
				m = new Move(Square.E8, Square.C8, Piece.BLACK_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.QUEENSIDE);
				moves.add(m);
			}
		}
	}
	*/
}
