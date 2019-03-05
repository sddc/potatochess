package baked.potato;

public class Movelist {
    private int size = 0;
    public Move[] moves = new Move[256];

    public int size() {
        return size;
    }

    public int addMove(Move m) {
        moves[size++] = m;
        return size;
    }
}
