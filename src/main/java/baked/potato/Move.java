package baked.potato;

import java.util.Objects;

public class Move implements Comparable<Move> {
	public static final int SQUARE_MASK = 0x3F;

	public static final int FLAG_MASK = 0x3000;
	public static final int EP_FLAG = 0x1000;
	public static final int CASTLE_FLAG = 0x2000;
	public static final int PROMOTION_FLAG = 0x3000;

	public static final int PROMOTION_MASK = 0xC000;
	public static final int QUEEN_PROMO_TYPE = 0x0;
	public static final int ROOK_PROMO_TYPE = 0x4000;
	public static final int KNIGHT_PROMO_TYPE = 0x8000;
	public static final int BISHOP_PROMO_TYPE = 0xC000;

	/* Move Encoding
	 * LSB
	 * Bits 0-5: fromSquare
	 * Bits 6-11: toSquare
	 * Bits 12-13: Flag
	 * 	01 ep flag
	 * 	10 castle flag
	 * 	11 promotion flag
	 * Bits 14-15: promotion piece
	 * 	00 queen
	 * 	01 rook
	 * 	10 knight
	 * 	11 bishop
	 * MSB
	 */
	public int move = 0;
	public int score = 0;

	public Move(Square from, Square to) {
		// to square
		move |= to.intValue;
		move = move << 6;
		// from square
		move |= from.intValue;
	}

	public Move(int from, int to) {
		// to square
		move |= to;
		move = move << 6;
		// from square
		move |= from;
	}

	public Move(int move) {
		this.move = move;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append(Square.toEnum(move & SQUARE_MASK).toString());
		output.append(Square.toEnum((move >>> 6) & SQUARE_MASK).toString());

		if((move & FLAG_MASK) == PROMOTION_FLAG) {
			switch(move & PROMOTION_MASK) {
				case QUEEN_PROMO_TYPE:
					output.append("q");
					break;
				case ROOK_PROMO_TYPE:
					output.append("r");
					break;
				case KNIGHT_PROMO_TYPE:
					output.append("n");
					break;
				case BISHOP_PROMO_TYPE:
					output.append("b");
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

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || !(obj instanceof Move)) return false;
		Move move = (Move) obj;
		return this.move == move.move;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.move);
	}

}
