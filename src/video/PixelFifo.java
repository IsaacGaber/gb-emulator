package video;

import java.util.ArrayDeque;

public class PixelFifo extends ArrayDeque<Colors> {
    @Override 
    public boolean add(Colors e) {
        // TODO Auto-generated method stub
        if (this.size() <= 16) {
            return super.add(e);
        } else {
            throw new IndexOutOfBoundsException("Cannot have more than 16 elements in Pixel Fifo");
        }
    }
}
