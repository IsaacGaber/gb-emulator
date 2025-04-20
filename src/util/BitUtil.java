package util;

public class BitUtil {

    public static final int MASK_8  = 0x000000FF;
    public static final int MASK_16 = 0x0000FFFF;
    
    public static boolean is8Bit(int i) {
        return (i & MASK_8) == 0;
    }

    public static boolean is16Bit(int i) {
        return (i & MASK_16) == 0;
    }

    public static boolean getBit(int value, int position) {
        return ((value >> position) & 1) != 0;
    }

    /** sets bit value to true or false and returns result
     * 
     * @param value
     * @param position
     * @param set
     * @return
     */
    public static int setBit(int value, int position, boolean set) {
        if (getBit(value, position) != set) {
            return flipBit(value, position);
        } else {
            return value;
        }
    }

    public static int flipBit(int value, int bitPosition) {
        if (getBit(value, bitPosition)) {
            return ~(1 << bitPosition) & value;
        } else {
            return (value | 1 << bitPosition);
        }
    }
}
