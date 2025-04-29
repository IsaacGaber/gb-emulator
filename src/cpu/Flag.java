package cpu;

public enum Flag {

    Z (7), // operation resulted in zero
    N (6),  // half-carry
    H (5),  // operation used subtraction
    C (4),   // carry
    NC (4),          // not carry
    NZ (7),         // not zero
    NONE; // no flag set

    public final Integer POS;

    private Flag(){
        this(0);
    }

    private Flag(int pos) {
        // this.VALUE = val;
        this.POS = pos;
    }
}
