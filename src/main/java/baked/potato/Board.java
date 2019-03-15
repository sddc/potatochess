package baked.potato;

import java.util.*;

public class Board {
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	public static final boolean KINGSIDE = true;
	public static final boolean QUEENSIDE = false;
	public static final long WKS_CASTLE_MASK = 0x90L;
	public static final long WQS_CASTLE_MASK = 0x11L;
	public static final long BKS_CASTLE_MASK = 0x9000000000000000L;
	public static final long BQS_CASTLE_MASK = 0x1100000000000000L;
	private static final Piece[][] promoPiece = {
		{Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP},
		{Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP}
	};

	private long castleRights; // square has lost castle rights if the corresponding bit is set

	private boolean activeColor;

	private int epSquare = Square.NO_SQ.intValue;

	public int getFiftyMove() {
		return fiftyMove;
	}

	private int fiftyMove;
	private int fullMove;

	/* Bitboard Board Representation
	 * Square to bit mapping
	 * H8 = MSB
	 * A1 = LSB
	 */
	private long[] bitboards;
	private int[] pieceBoard;
	private long whitePieces;
	private long blackPieces;
    private int[] pieceCounts;

    private long positionKey = 0;

    private int ply = 0;
    private long kingStorage = 0;
	
	private List<PreviousMove> previousMoves = new ArrayList<>();

	public static final TT tt = new TT(64);

	public Board(long[] bitboards, long castleRights, int epSquare, boolean activeColor, int fiftyMove, int fullMove) {
		this.bitboards = bitboards;
		this.castleRights = castleRights;
		this.activeColor = activeColor;
		this.fiftyMove = fiftyMove;
		this.fullMove = fullMove;

		if(epSquare != Square.NO_SQ.intValue) {
			long epSquareMask = 1L << epSquare + (activeColor ? -8 : 8);
			long sidePawns = ((epSquareMask & Mask.clearFileH) << 1) | ((epSquareMask & Mask.clearFileA) >>> 1);

			if (activeColor && (sidePawns & bitboards[Piece.WHITE_PAWN.intValue]) != 0 ||
					!activeColor && (sidePawns & bitboards[Piece.BLACK_PAWN.intValue]) != 0) {
				this.epSquare = epSquare;
			}
		}

		whitePieces = genSidePieces(Board.WHITE);
		blackPieces = genSidePieces(Board.BLACK); 

        // count pieces
        pieceCounts = new int[12];
        for(int i = 0; i < 12; i++) {
            pieceCounts[i] = Long.bitCount(bitboards[i]);
        }

		initPieceBoard();
		positionKeyInit();
        ply = 0;
	}

	private void positionKeyInit() {
		positionKey = 0;

		for(int i = 0; i < 64; i++) {
			int pieceIdx = getPieceType(i);

			if(pieceIdx != Piece.EMPTY.intValue) {
				positionKey ^= Zobrist.randSquare[pieceIdx][i];
			}
		}

		if(!activeColor) {
			// xor if black
			positionKey ^= Zobrist.randSide;
		}

		if(epSquare != Square.NO_SQ.intValue) {
			positionKey ^= Zobrist.randEp[epSquare];
		}

		if((WKS_CASTLE_MASK & castleRights) == 0) positionKey ^= Zobrist.randCastle[0];
		if((WQS_CASTLE_MASK & castleRights) == 0) positionKey ^= Zobrist.randCastle[1];
		if((BKS_CASTLE_MASK & castleRights) == 0) positionKey ^= Zobrist.randCastle[2];
		if((BQS_CASTLE_MASK & castleRights) == 0) positionKey ^= Zobrist.randCastle[3];
	}

	public void hideKing(boolean side) {
		if(kingStorage != 0) {
			throw new UnsupportedOperationException("previous king hidden");
		}

		if(side) {
			kingStorage = bitboards[Piece.WHITE_KING.intValue];
			whitePieces ^= kingStorage;
		} else {
			kingStorage = bitboards[Piece.BLACK_KING.intValue];
			blackPieces ^= kingStorage;
		}
	}

	public void showKing(boolean side) {
		if(kingStorage == 0) {
			throw new UnsupportedOperationException("no king to show");
		}

		if(side) {
			whitePieces ^= kingStorage;
		} else {
			blackPieces ^= kingStorage;
		}

		kingStorage = 0;
	}

    public long getPositionKey() {
        return positionKey;
    }

    public int[] getPieceCounts() {
        return pieceCounts.clone();
    }

    public long[] getBitboards() {
        return bitboards.clone();
    }

	public boolean getActiveColor() {
		return activeColor;
	}

	public int getEpSquare() {
		return epSquare;
	}

	private long genSidePieces(boolean side) {
		long result = 0L;

		if(side == Board.WHITE) {
			for(int i = 0; i < 6; i++) {
				result |= bitboards[i];
			}
		} else {
			for(int i = 6; i < 12; i++) {
				result |= bitboards[i];
			}
		}
		return result;
	}

	public long getPawnBitboard(boolean side) {
		if(side) {
			return bitboards[0];
		} else {
			return bitboards[6];
		}
	}

	public long getRookBitboard(boolean side) {
		if(side) {
			return bitboards[1];
		} else {
			return bitboards[7];
		}
	}

	public long getKnightBitboard(boolean side) {
		if(side) {
			return bitboards[2];
		} else {
			return bitboards[8];
		}
	}

	public long getBishopBitboard(boolean side) {
		if(side) {
			return bitboards[3];
		} else {
			return bitboards[9];
		}
	}

	public long getQueenBitboard(boolean side) {
		if(side) {
			return bitboards[4];
		} else {
			return bitboards[10];
		}
	}

	public long getKingBitboard(boolean side) {
		if(side) {
			return bitboards[5];
		} else {
			return bitboards[11];
		}
	}

	public long getPieceBitboard(Piece p) {
		return bitboards[p.intValue];
	}

	public long getSidePieces(boolean side) {
		if(side == Board.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
	}

	public long getAllPieces() {
		return whitePieces | blackPieces;
	}
	
	private void printPiece(Piece type) {
		switch(type) {
			case EMPTY: System.out.print(" ");
				break;
			case WHITE_PAWN: System.out.print("P");
				break;
			case WHITE_ROOK: System.out.print("R");
				break;
			case WHITE_KNIGHT: System.out.print("N");
				break;
			case WHITE_BISHOP: System.out.print("B");
				break;
			case WHITE_QUEEN: System.out.print("Q");
				break;
			case WHITE_KING: System.out.print("K");
				break;
			case BLACK_PAWN: System.out.print("p");
				 break;
			case BLACK_ROOK: System.out.print("r");
				 break;
			case BLACK_KNIGHT: System.out.print("n");
				 break;
			case BLACK_BISHOP: System.out.print("b");
				 break;
			case BLACK_QUEEN: System.out.print("q");
				 break;
			case BLACK_KING: System.out.print("k");
				 break;
			default:
				break;
		}
	}

	public void print() {
		int rank = 8;
		System.out.println("     A   B   C   D   E   F   G   H");
		System.out.println("   +---+---+---+---+---+---+---+---+");
		for(int i = 56; i >= 0; i -= 8) {
			System.out.print(" " + rank + " |");
			for(int j = i; j < (i + 8); j++) {
				System.out.print(" ");
				printPiece(Piece.toEnum(getPieceType(j)));
				System.out.print(" |");
			}
			System.out.print(" " + rank--);
			System.out.println("\n   +---+---+---+---+---+---+---+---+");
		}
		System.out.println("     A   B   C   D   E   F   G   H");
		System.out.println("Position Key: " + String.format("%016X", positionKey));
		if(activeColor == Board.WHITE) {
			System.out.println("Active color: White (uppercase)");
		} else {
			System.out.println("Active color: Black (lowercase)");
		}
		System.out.println("ep square: " + Square.toEnum(epSquare).toString());
		System.out.println("half move: " + fiftyMove + " full move: " + fullMove);
	}

	private void initPieceBoard() {
		pieceBoard = new int[64];
		Arrays.fill(pieceBoard, Piece.EMPTY.intValue);
		long mask = 1L;

		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 12; j++) {
				if((bitboards[j] & mask) != 0) {
					pieceBoard[i] = j;
					break;
				}
			}

			mask <<= 1;
		}
	}

	public int getPieceType(int s) {
		return pieceBoard[s];
	}

	private void modify(int type, long modifier) {
		// type selects which bitboard to modify
		// modifier is bitmask or which bits to toggle
		if(type == Piece.EMPTY.intValue) {
			return;
		}
		if(type < 6) {
			bitboards[type] ^= modifier;
			whitePieces ^= modifier;
		} else {
			bitboards[type] ^= modifier;
			blackPieces ^= modifier;
		}
	}

	public boolean move(Move m) {
		// save board state to undo move
		PreviousMove pm = new PreviousMove(m, castleRights, epSquare, fiftyMove++, fullMove, positionKey);

		int fromSquare = m.move & Move.SQUARE_MASK;
		int fromPiece = pieceBoard[fromSquare];
		int toSquare = (m.move >>> 6) & Move.SQUARE_MASK;
		int toPiece = pieceBoard[toSquare];
		long fromMask = 1L << fromSquare;
		long toMask = 1L << toSquare;

		// update castle rights
		castleRights |= fromMask | toMask;
		long diff = pm.castleRights ^ castleRights;
		// check if have not already lost castle right
		if((WKS_CASTLE_MASK & diff) != 0 && (WKS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[0];
		if((WQS_CASTLE_MASK & diff) != 0 && (WQS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[1];
		if((BKS_CASTLE_MASK & diff) != 0 && (BKS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[2];
		if((BQS_CASTLE_MASK & diff) != 0 && (BQS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[3];

		if(epSquare != Square.NO_SQ.intValue) {
			// xor out previous position ep square
			positionKey ^= Zobrist.randEp[epSquare];
			epSquare = Square.NO_SQ.intValue;
		}

		if(toPiece != Piece.EMPTY.intValue) {
			// capture

			// remove bit from toPiece bitboard
			modify(toPiece, toMask);

			// update pos key
			positionKey ^= Zobrist.randSquare[toPiece][toSquare];

			// update pieceCounts
			pieceCounts[toPiece]--;

			// save piece type
			pm.capture = toPiece;

			// reset fiftyMove
			fiftyMove = 0;
		}

		// move bit within fromPiece bitboard
		modify(fromPiece, fromMask | toMask);
		pieceBoard[fromSquare] = Piece.EMPTY.intValue;
		pieceBoard[toSquare] = fromPiece;

		// update pos key
		positionKey ^= Zobrist.randSquare[fromPiece][fromSquare];
		positionKey ^= Zobrist.randSquare[fromPiece][toSquare];

		if(fromPiece == Piece.WHITE_PAWN.intValue || fromPiece == Piece.BLACK_PAWN.intValue) {

			// reset fiftyMove
			fiftyMove = 0;

			if((m.move & Move.FLAG_MASK) == Move.PROMOTION_FLAG) {
				// promotion
				int type = (m.move & Move.PROMOTION_MASK) >>> 14;
				Piece promoType = promoPiece[activeColor ? 0 : 1][type];

				// remove bit from fromPiece bitboard
				// moved within earlier
				modify(fromPiece, toMask);

				positionKey ^= Zobrist.randSquare[fromPiece][toSquare];

				// update pieceCounts
				pieceCounts[fromPiece]--;

				// add bit to promotion piece bitboard
				modify(promoType.intValue, toMask);

				positionKey ^= Zobrist.randSquare[promoType.intValue][toSquare];

				// update pieceCounts and pieceBoard
				pieceCounts[promoType.intValue]++;
				pieceBoard[toSquare] = promoType.intValue;
			} else if((m.move & Move.FLAG_MASK) == Move.EP_FLAG) {
				if(activeColor) {
					// white
					modify(Piece.BLACK_PAWN.intValue, toMask >>> 8);

					positionKey ^= Zobrist.randSquare[Piece.BLACK_PAWN.intValue][toSquare - 8];

					// update pieceCounts and pieceBoard
					pieceCounts[Piece.BLACK_PAWN.intValue]--;
					pieceBoard[toSquare - 8] = Piece.EMPTY.intValue;
				} else {
					// black
					modify(Piece.WHITE_PAWN.intValue, toMask << 8);

					positionKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][toSquare + 8];

					// update pieceCounts and pieceBoard
					pieceCounts[Piece.WHITE_PAWN.intValue]--;
					pieceBoard[toSquare + 8] = Piece.EMPTY.intValue;
				}
			} else {

				// set ep square
				if(activeColor && Long.bitCount((fromMask | toMask) & (Mask.maskRank2 | Mask.maskRank4)) == 2) {
					// white moved forward 2 squares
					long sidePawns = ((toMask & Mask.clearFileH) << 1) | ((toMask & Mask.clearFileA) >>> 1);

					if((sidePawns & bitboards[Piece.BLACK_PAWN.intValue]) != 0) {
						epSquare = toSquare - 8;
						positionKey ^= Zobrist.randEp[epSquare];
					}
				} else if(!activeColor && Long.bitCount((fromMask | toMask) & (Mask.maskRank7 | Mask.maskRank5)) == 2) {
					// black moved forward 2 squares
					long sidePawns = ((toMask & Mask.clearFileH) << 1) | ((toMask & Mask.clearFileA) >>> 1);

					if((sidePawns & bitboards[Piece.WHITE_PAWN.intValue]) != 0) {
						epSquare = toSquare + 8;
						positionKey ^= Zobrist.randEp[epSquare];
					}
				}

			}

		} else {

			if((m.move & Move.FLAG_MASK) == Move.CASTLE_FLAG) {
				// determine type of castle using the square masks
				long mask = fromMask | toMask;
				if(mask < 0x100) {
					// white castle
					if(mask == 0x14) {
						// queenside
						modify(Piece.WHITE_ROOK.intValue, 0x9);
						pieceBoard[Square.A1.intValue] = Piece.EMPTY.intValue;
						pieceBoard[Square.D1.intValue] = Piece.WHITE_ROOK.intValue;
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.A1.intValue];
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.D1.intValue];
					} else {
						// kingside
						modify(Piece.WHITE_ROOK.intValue, 0xA0);
						pieceBoard[Square.H1.intValue] = Piece.EMPTY.intValue;
						pieceBoard[Square.F1.intValue] = Piece.WHITE_ROOK.intValue;
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.H1.intValue];
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.F1.intValue];
					}
				} else {
					// black castle
					if(mask == 0x1400000000000000L) {
						// queenside
						modify(Piece.BLACK_ROOK.intValue, 0x900000000000000L);
						pieceBoard[Square.A8.intValue] = Piece.EMPTY.intValue;
						pieceBoard[Square.D8.intValue] = Piece.BLACK_ROOK.intValue;
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.A8.intValue];
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.D8.intValue];
					} else {
						// kingside
						modify(Piece.BLACK_ROOK.intValue, 0xA000000000000000L);
						pieceBoard[Square.H8.intValue] = Piece.EMPTY.intValue;
						pieceBoard[Square.F8.intValue] = Piece.BLACK_ROOK.intValue;
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.H8.intValue];
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.F8.intValue];
					}
				}
			}

		}

		// store previous move
		previousMoves.add(pm);

		if(!activeColor) {
			// black to white
			fullMove++;
		}

		ply++;
		activeColor = !activeColor;
		positionKey ^= Zobrist.randSide;

		if(fromPiece == Piece.WHITE_KING.intValue || fromPiece == Piece.BLACK_KING.intValue || (m.move & Move.FLAG_MASK) == Move.EP_FLAG) {
			if(MoveGen.isKingInCheck(this, !this.activeColor)) {
				return false;
			}
		}

		return true;
	}

	public void undoMove() {
		if(previousMoves.isEmpty()) {
			throw new UnsupportedOperationException("no previous moves");
		}

		PreviousMove pm = previousMoves.remove(previousMoves.size() - 1);

		positionKey ^= Zobrist.randSide;
		activeColor = !activeColor;
		ply--;

		Move m = pm.move;
		fiftyMove = pm.fiftyMove;
		fullMove = pm.fullMove;

		int fromSquare = m.move & Move.SQUARE_MASK;
		int toSquare = (m.move >>> 6) & Move.SQUARE_MASK;
		// should be piece that moved or if promotion, the piece the pawn promoted to
		int toPiece = pieceBoard[toSquare];
		long fromMask = 1L << fromSquare;
		long toMask = 1L << toSquare;

		if((m.move & Move.FLAG_MASK) == Move.PROMOTION_FLAG) {
			// promotion

			// remove promoted piece from bitboard
			modify(toPiece, toMask);
			positionKey ^= Zobrist.randSquare[toPiece][toSquare];

			// update pieceCounts
			pieceCounts[toPiece]--;

			toPiece = activeColor ? Piece.WHITE_PAWN.intValue : Piece.BLACK_PAWN.intValue;

			// add bit to pawn piece bitboard
			modify(toPiece, toMask);
			positionKey ^= Zobrist.randSquare[toPiece][toSquare];

			// update pieceCounts and pieceBoard
			pieceCounts[toPiece]++;
			pieceBoard[toSquare] = toPiece;
		}

		// move bit within fromPiece bitboard
		modify(toPiece, fromMask | toMask);
		pieceBoard[fromSquare] = toPiece; // piece that moved
		pieceBoard[toSquare] = Piece.EMPTY.intValue; // fromPiece is Piece.EMPTY
		positionKey ^= Zobrist.randSquare[toPiece][toSquare];
		positionKey ^= Zobrist.randSquare[toPiece][fromSquare];

		if(pm.capture != Piece.EMPTY.intValue) {
			// capture

			// add bit to capture bitboard
			modify(pm.capture, toMask);
			positionKey ^= Zobrist.randSquare[pm.capture][toSquare];

			// update pieceCounts annd pieceBoard
			pieceCounts[pm.capture]++;
			pieceBoard[toSquare] = pm.capture;
		}

		if(toPiece == Piece.WHITE_PAWN.intValue || toPiece == Piece.BLACK_PAWN.intValue) {
			if((m.move & Move.FLAG_MASK) == Move.EP_FLAG) {
				if(activeColor) {
					// white
					modify(Piece.BLACK_PAWN.intValue, toMask >>> 8);

					// update pieceCounts and pieceBoard
					pieceCounts[Piece.BLACK_PAWN.intValue]++;
					pieceBoard[toSquare - 8] = Piece.BLACK_PAWN.intValue;
					positionKey ^= Zobrist.randSquare[Piece.BLACK_PAWN.intValue][toSquare - 8];
				} else {
					// black
					modify(Piece.WHITE_PAWN.intValue, toMask << 8);

					// update pieceCounts and pieceBoard
					pieceCounts[Piece.WHITE_PAWN.intValue]++;
					pieceBoard[toSquare + 8] = Piece.WHITE_PAWN.intValue;
					positionKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][toSquare + 8];
				}
			}
		} else {

			if((m.move & Move.FLAG_MASK) == Move.CASTLE_FLAG) {
				// determine type of castle using the square masks
				long mask = fromMask | toMask;
				if(mask < 0x100) {
					// white castle
					if(mask == 0x14) {
						// queenside
						modify(Piece.WHITE_ROOK.intValue, 0x9);
						pieceBoard[Square.A1.intValue] = Piece.WHITE_ROOK.intValue;
						pieceBoard[Square.D1.intValue] = Piece.EMPTY.intValue;
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.A1.intValue];
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.D1.intValue];
					} else {
						// kingside
						modify(Piece.WHITE_ROOK.intValue, 0xA0);
						pieceBoard[Square.H1.intValue] = Piece.WHITE_ROOK.intValue;
						pieceBoard[Square.F1.intValue] = Piece.EMPTY.intValue;
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.H1.intValue];
						positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.F1.intValue];
					}
				} else {
					// black castle
					if(mask == 0x1400000000000000L) {
						// queenside
						modify(Piece.BLACK_ROOK.intValue, 0x900000000000000L);
						pieceBoard[Square.A8.intValue] = Piece.BLACK_ROOK.intValue;
						pieceBoard[Square.D8.intValue] = Piece.EMPTY.intValue;
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.A8.intValue];
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.D8.intValue];
					} else {
						// kingside
						modify(Piece.BLACK_ROOK.intValue, 0xA000000000000000L);
						pieceBoard[Square.H8.intValue] = Piece.BLACK_ROOK.intValue;
						pieceBoard[Square.F8.intValue] = Piece.EMPTY.intValue;
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.H8.intValue];
						positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.F8.intValue];
					}
				}
			}

		}

		// restore castle rights
		long diff = pm.castleRights ^ castleRights;
		if((WKS_CASTLE_MASK & diff) != 0 && (WKS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[0];
		if((WQS_CASTLE_MASK & diff) != 0 && (WQS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[1];
		if((BKS_CASTLE_MASK & diff) != 0 && (BKS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[2];
		if((BQS_CASTLE_MASK & diff) != 0 && (BQS_CASTLE_MASK & pm.castleRights) == 0) positionKey ^= Zobrist.randCastle[3];
		castleRights = pm.castleRights;

		if(epSquare != Square.NO_SQ.intValue) {
			// xor out previous position ep square
			positionKey ^= Zobrist.randEp[epSquare];
		}

		epSquare = pm.epSquare;

		if(epSquare != Square.NO_SQ.intValue) {
			// xor in saved position ep square
			positionKey ^= Zobrist.randEp[epSquare];
		}

		assert pm.positionKey == positionKey;

	}

	public boolean castlingAvailable(boolean side, boolean squares) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long pieceMask;
		if(side == Board.WHITE) {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if((WKS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0x60L;
			} else {
				// check if queenside castle available
				if((WQS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0xEL;
			}
		} else {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if((BKS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0x6000000000000000L;
			} else {
				// check if queenside castle available
				if((BQS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0xE00000000000000L;
			}
		}

		// check if any pieces between king and rook
		if((pieceMask & (whitePieces | blackPieces)) == 0L) {
			return true;
		} else {
			return false;
		}
	}

	public int getPly() {
		return ply;
	}

	public boolean repetition() {
		int repStart = ply - fiftyMove;
		for(int i = repStart < 0 ? 0 : repStart; i < previousMoves.size(); i += 2) {
			if(previousMoves.get(i).positionKey == positionKey) {
				return true;
			}
		}

		return false;
	}
}
