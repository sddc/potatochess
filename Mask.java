public class Mask {
	public long squareMask;
	public int offset;

	public Mask(long squareMask, int offset) {
		this.squareMask = squareMask;
		this.offset = offset;
	}

	public long blockBitMask(int square) {
		long m = 0L;
		switch(offset) {
			case 1:
				m = Move.plus1Mask[square];
				break;
			case 7:
				m = Move.plus7Mask[square];
				break;
			case 8:
				m = Move.plus8Mask[square];
				break;
			case 9:
				m = Move.plus9Mask[square];
				break;
			case -1:
				m = Move.minus1Mask[square];
				break;
			case -7:
				m = Move.minus7Mask[square];
				break;
			case -8:
				m = Move.minus8Mask[square];
				break;
			case -9:
				m = Move.minus9Mask[square];
				break;
			default:
				break;
		}
		return m;
	}
}
