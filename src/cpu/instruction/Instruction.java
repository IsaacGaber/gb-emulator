package cpu.instruction;

public class Instruction {
    
    private String _mnemonic;
    private Operands _operands;
    private int _bytes;
    private int _cycles;
    // private final boolean _immediate;
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
        // _immediate = immediate;
        _cycles = cycles;
        _operands = operands;
        _op = op;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Instruction) {
            Instruction instruction = (Instruction) obj;
            return (this  == obj || (this._mnemonic.equals(instruction._mnemonic))); 
                // && this._lOperand.equals(instruction._lOperand) 
                //     && this._rOperand.equals(instruction._rOperand)
                //     && this._immediate == instruction._immediate
                //     && Collections.unmodifiableCollection));
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
    
    // public Operand getLOperand() {
    //     return _lOperand;
    // }

    // public Operand getROperand() {
    //     return _rOperand;
    // }

    public boolean isValid() {
        if (_mnemonic.length() >= 7) {
            return !_mnemonic.substring(0, 7).equals("ILLEGAL"); 
        } else {
            return true;
        }
    }

    // @Override
    // public byte[] toBinary() {
    //     try {
    //         return new byte(this.toString(); // extremely ugly, I know
    //     } catch (Exception e) {
    //         throw new RuntimeException("Invalid instruction: " + this.toString());
    //     }
    // }
}
