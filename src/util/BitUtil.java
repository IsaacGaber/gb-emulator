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

    /** returns 
     * 
     * @param x operand one
     * @param y operand two
     * @param bitPos bit to be compared e.g. bit 8 to check 8-bit arithmetic carry
     * @return
     */
    public static boolean bitCarried(int x, int y, int bitPos) {
        int mask = (1 << bitPos);
        return (((x & (mask - 1)) + (y & (mask - 1))) & mask) == mask;
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
