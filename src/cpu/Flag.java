package cpu;

public enum Flag {

    Z (0b10000000, 7), // operation resulted in zero
    H (0b01000000, 6),  // half-carry
    N (0b00100000, 5),  // operation used subtraction
    C (0b00010000, 4),   // carry
    NONE (0, 0); // no flag set

    public final int VALUE, POS;

    private Flag(int val, int pos) {
        this.VALUE = val;
        this.POS = pos;
    }
}
