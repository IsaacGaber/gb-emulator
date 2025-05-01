package cpu.instruction;

public class Instruction {
    
    private String _mnemonic;
    private Operands _operands;
    private int _bytes;
    private int _cycles;
    private Op _op;

    @SuppressWarnings("unused")
    private Instruction() {};

    // some instructions have no operands
    Instruction(String mnemonic, int bytes, int cycles, Op op) {
        this(mnemonic, bytes, cycles, null, op);
    }

    Instruction(String mnemonic, int bytes, int cycles, Operands operands, Op op){
        _mnemonic = mnemonic;
        _bytes = bytes;
        _cycles = cycles;
        _operands = operands;
        _op = op;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Instruction) {
            Instruction instruction = (Instruction) obj;
            return (this  == obj || (this._mnemonic.equals(instruction._mnemonic)
                && this._operands.left().equals(instruction._operands.left()) 
                && this._operands.right().equals(instruction._operands.right())));
        } else {
            return false;
        }
    }

    /** performs Op on Operands
     * 
     * @return number of machine cycles taken
     */
    public int run() {
        _op.accept(_operands);
        return _cycles;
    }

    public String toString() {
        String str = _mnemonic;
        if (_operands != null && _operands.left() != null) {
            str += " " + _operands.left();
            if (_operands.right() != null) {
                str += ", " + _operands.right();
            }
        } 
        return str;
    }
}
