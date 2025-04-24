package video;

import java.awt.Color;

public enum Colors {
    A (Color.WHITE.getRGB()),
    B (Color.GRAY.getRGB()),
    C (Color.DARK_GRAY.getRGB()), 
    D (0);

    public final int COLOR;
    Colors(int c){
        this.COLOR = c;
    }
    
}
