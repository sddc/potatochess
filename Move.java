// public class Move implements Comparable<Move> {
public class Move {
/*
	public static final String[] squareNames = {
		"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
		"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", 
		"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", 
		"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", 
		"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5", 
		"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", 
		"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7", 
		"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8" 
	};
*/

	/* Move Encoding
	 * All flags:
	 * 	1: true
	 * 	0: false
	 *
	 * LSB
	 * Bits 0-5: fromSquare
	 * Bits 6-11: toSquare
	 * Bits 12-15: piece type
	 * Bits 16-19: capture piece type
	 * Bit 20: double pawn push flag
	 * Bit 21: capture flag
	 * Bit 22: en passant capture flag
	 * Bit 23: castle flag
	 * Bit 24: promotion flag
	 * Bit 25: check flag
	 * Bit 26: checkmate flag
	 * Bit 27: castle type
	 * 	0: queenside
	 * 	1: kingside
	 * Bits 28-31: promotion type
	 * MSB
	 */
	private int move = 0;

	public Move(Square from, Square to) {
		move |= to.ordinal();
		move = move << 6;
		move |= from.ordinal();
	}

	public Square getFromSquare() {
		return Square.intToEnum(move & 0x3F);
	}

	public Square getToSquare() {
		return Square.intToEnum((move & 0xFC0) >>> 6);
	}

	/////////////////////////////////////
	public void setPieceType(Piece type) {
		move |= (type.ordinal() << 12);
	}

	public Piece getPieceType() {
		return Piece.intToEnum((move & 0xF000) >>> 12);
	}

	public void setCapturePieceType(Piece type) {
		setFlag(Flag.CAPTURE);
		move |= (type.ordinal() << 16);
	}

	public Piece getCapturePieceType() {
		return Piece.intToEnum((move & 0xF0000) >>> 16);
	}

	public void setCastleType(boolean squares) {
		setFlag(Flag.CASTLE);
		if(squares) {
			move |= 0x8000000;
		}
	}

	public boolean getCastleType() {
		if((move & 0x8000000) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public void setPromotionType(Piece type) {
		setFlag(Flag.PROMOTION);
		move |= (type.ordinal() << 28);
	}

	public Piece getPromotionType() {
		return Piece.intToEnum((move & 0x30000000) >>> 28);
	}
	//
	//
	/////////////////////////////////////

	public boolean getFlag(Flag flag) {
		int mask = getFlagMask(flag);

		if((move & mask) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public void setFlag(Flag flag) {
		int mask = getFlagMask(flag);

		move |= mask;
	}

	private int getFlagMask(Flag flag) {
		int mask = 0;
		switch(flag) {
			case DOUBLE_PAWN_PUSH:
				mask = 0x100000;
				break;
			case CAPTURE:
				mask = 0x200000;
				break;
			case EP_CAPTURE:
				mask = 0x400000;
				break;
			case CASTLE:
				mask = 0x800000;
				break;
			case PROMOTION:
				mask = 0x1000000;
				break;
			case CHECK:
				mask = 0x2000000;
				break;
			case CHECKMATE:
				mask = 0x4000000;
				break;
			default:
				break;
		}
		return mask;
	}



/*
	public String toString() {
		return squareNames[fromSquare] + squareNames[toSquare];
	}

	public int compareTo(Move m) {
		return toString().compareTo(m.toString());
	}
*/
}
