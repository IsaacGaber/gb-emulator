package video;

import util.BitUtil;

// simplifies converting from video memory bytes to pixels
public class Tile {
    public byte[] bytes;
    public final int SIZE = 8;
    public Tile(byte[] bytes) {
        if (bytes.length == 16) {
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
                boolean aSet = BitUtil.getBit(a, tileWidth-x);
                boolean bSet = BitUtil.getBit(b, tileWidth-x);
                
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
