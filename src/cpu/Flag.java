package cpu;

public enum Flag {

    Z (7), // operation resulted in zero
    H (6),  // half-carry
    N (5),  // operation used subtraction
    C (4),   // carry
    NONE (0); // no flag set

    public final int POS;

    private Flag(int pos) {
        // this.VALUE = val;
        this.POS = pos;
    }
}
