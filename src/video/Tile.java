package video;

import util.BitUtil;

// simplifies converting from video memory bytes to pixels
public class Tile {
    private byte[] bytes;
    public static final int SIZE = 8;
    public static final int BYTE_LENGTH = 16;

    public Tile(byte[] bytes) {
        if (bytes.length == BYTE_LENGTH) {
            this.bytes = bytes;
        } else {
            throw new RuntimeException("invalid byte length: " + bytes.length);
        }
    }

    public Colors[] tileColors(){
        Colors[] colors = new Colors[64];
        int tileWidth = bytes.length/2;
        for (int y = 0; y < tileWidth; y++) {
            byte a = bytes[y * 2];
            byte b = bytes[y * 2 + 1];
            for (int x = 0; x < tileWidth; x++) {  
                boolean aSet = BitUtil.getBit(a, tileWidth-x-1);
                boolean bSet = BitUtil.getBit(b, tileWidth-x-1);
                
                int i = (y * tileWidth) + x;
                if (aSet && bSet) {
                    colors[i] = Colors.A;
                } else if (!aSet && bSet) {
                    colors[i] = Colors.B;
                } else if (aSet && !bSet) {
                    colors[i] = Colors.C;
                } else {
                    colors[i] = Colors.D;
                }
            }
        }
        return colors;
    }
}
