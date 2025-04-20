package cpu;

public enum Flag {

    Z (0b10000000), // operation resulted in zero
    H (0b01000000),  // half-carry
    N (0b01000000),  // operation used subtraction
    C (0b01000000),   // carry
    NONE (0); // no flag set

    public final int VALUE;

    private Flag(int val) {
        this.VALUE = val;
    }
}
