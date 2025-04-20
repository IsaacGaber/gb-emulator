package cpu.instruction;

import cpu.CPU;
import cpu.register.Register;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;

    public InstructionSet() {

        _UNPREFIXED[0x00] = new Instruction(null, 1, false, 0, 
                                            (Op)(o)->{});

        _UNPREFIXED[0x01] = new Instruction("LD", 3, false, 0, {new Operand(OperandType.BC), new Operand(OperandType.N16)}, null)

    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }
}
