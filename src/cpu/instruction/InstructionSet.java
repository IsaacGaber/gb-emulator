package cpu.instruction;

import cpu.CPU;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;

    public InstructionSet() {

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, false, 4, 
                                            (Op)(o) -> {});

        _UNPREFIXED[0x01] = new Instruction("LD", 3, true, 12, 
                                            new Operands(Operand.R16, Operand.N16), 
                                            (Op)(o) -> {CPU.setReg("BC", CPU.nextByte());});

    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }
}
