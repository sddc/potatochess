package baked.potato;

public class TTEntry {
    public static final int flagOccupied = 1;
    public static final int flagPvNode = 2;
    public static final int flagAllNode = 4;
    public static final int flagCutNode = 8;


    public long positionKey = 0;
    public int bestMove = 0;
    public int score = 0;
    public byte depth = 0;
    public byte age = 0;
    public byte flag = 0;
}
