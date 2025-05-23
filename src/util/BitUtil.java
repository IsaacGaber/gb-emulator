package util;

public class BitUtil {
    
    public static boolean is8Bit(int i) {
        return (i & 0xFF) == 0;
    }

    public static boolean is16Bit(int i) {
        return (i & 0xFFFF) == 0;
    }

    public static boolean getBit(int value, int position) {
        return ((value >> position) & 1) != 0;
    }

    /** 
     * 
     * @param x operand one
     * @param y operand two
     * @param bitPos bit to be compared e.g. bit 7 to check 8-bit arithmetic add carry
     * @return whether bit carried
     */
    public static boolean bitCarried(int x, int y, int bitPos) {
        int mask = (1 << bitPos - 1);
        return (((x & (mask - 1)) + (y & (mask - 1))) & mask) == mask;
    }


    /** sets bit value to true or false and returns result.
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

    /**
     * returns value with bit at bitPosition flipped.
     * @param value
     * @param bitPosition
     * @return
     */
    public static int flipBit(int value, int bitPosition) {
        if (getBit(value, bitPosition)) {
            return ~(1 << bitPosition) & value;
        } else {
            return (value | 1 << bitPosition);
        }
    }
}
