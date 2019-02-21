package baked.potato;

public class Move implements Comparable<Move> {
	/* baked.potato.Move Encoding
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

	public Move(Square from, Square to, Piece type) {
		// to square
		move |= to.intValue;
		move = move << 6;
		// from square
		move |= from.intValue;
		// piece type
		move |= (type.intValue << 12);
	}

	public Square getFromSquare() {
		return Square.toEnum(move & 0x3F);
	}

	public Square getToSquare() {
		return Square.toEnum((move & 0xFC0) >>> 6);
	}

	public Piece getPieceType() {
		return Piece.toEnum((move & 0xF000) >>> 12);
	}

	public void setCapturePieceType(Piece type) {
		move |= (type.intValue << 16);
	}

	public Piece getCapturePieceType() {
		return Piece.toEnum((move & 0xF0000) >>> 16);
	}

	public void setCastleType(boolean squares) {
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
		move |= (type.intValue << 28);
	}

	public Piece getPromotionType() {
		return Piece.toEnum((move & 0xF0000000) >>> 28);
	}

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

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append(getFromSquare().toString());
		output.append(getToSquare().toString());
		
		if(getFlag(Flag.PROMOTION)) {
			switch(getPromotionType()) {
				case WHITE_QUEEN:
				case BLACK_QUEEN:
					output.append("Q");
					break;
				case WHITE_ROOK:
				case BLACK_ROOK:
					output.append("R");
					break;
				case WHITE_KNIGHT:
				case BLACK_KNIGHT:
					output.append("N");
					break;
				case WHITE_BISHOP:
				case BLACK_BISHOP:
					output.append("B");
					break;
				default:
					break;

			}
		}
		return output.toString();
	}

	@Override
	public int compareTo(Move m) {
		return toString().compareTo(m.toString());
	}

}
