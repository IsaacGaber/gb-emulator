package video;

import memory.Memory;

public class PPU {
    private PixelFifo _pixelFifo;
    private Memory _memory;

    public PPU(Memory memory) {
        _pixelFifo = new PixelFifo();
        _memory = memory;
    }

    // public void drawLine(){
    //     // int l = memory.
    //     if (l < 144) {
            
    //     } else if (i < 154) {

    //     } else {
    //         throw new RuntimeException();
    //     }
    // }
}
