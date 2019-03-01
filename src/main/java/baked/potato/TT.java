package baked.potato;

public class TT {
    private TTEntry[] entries;
    private byte age = 0;

    public TT(int sizeInMb) {
        resize(sizeInMb);
    }

    public void resize(int sizeInMb) {
        // TTEntry size 35 bytes

        if(sizeInMb <= 0) sizeInMb = 1;
        entries = new TTEntry[sizeInMb * 1000000 / 35];

        for(int i = 0; i < entries.length; i++) {
            entries[i] = new TTEntry();
        }
    }

    public void clear() {
        for(int i = 0; i < entries.length; i++) {
            entries[i].flag = 0;
        }
    }

    public void incAge() {
        if(++age == 16) age = 0;
    }

    public void put(long positionKey, int bestMove, int score, int depth, int flag) {
        int i = (int)(positionKey % entries.length);

        if(entries[i].flag == 0 || entries[i].age != age || entries[i].depth <= depth) {
            entries[i].positionKey = positionKey;
            entries[i].bestMove = bestMove;
            entries[i].score = score;
            entries[i].depth = (byte)depth;
            entries[i].age = age;
            entries[i].flag = (byte)(flag | TTEntry.flagOccupied);
        }
    }

    public TTEntry get(long positionKey) {
        int i = (int)(positionKey % entries.length);

        if(entries[i].flag != 0 && entries[i].positionKey == positionKey) {
            return entries[i];
        }

        return null;
    }
}
