package video;

import java.awt.Color;

public enum Colors {
    A (Color.BLACK.getRGB()),
    B (Color.DARK_GRAY.getRGB()),
    C (Color.GRAY.getRGB()), 
    D (Color.WHITE.getRGB());

    public final int COLOR;
    Colors(int c){
        this.COLOR = c;
    }
    
}
