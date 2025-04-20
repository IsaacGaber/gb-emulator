package cpu.instruction;

import cpu.CPU;
import memory.Memory;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;

    public InstructionSet(CPU cpu, Memory memory) {
        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, false, 4, 
                                            (Op)(o) -> {});

        _UNPREFIXED[0x01] = new Instruction("LD", 3, true, 12, 
                                            new Operands(Operand.R16, Operand.N16), 
                                            (Op)(o) -> {cpu.setReg("BC", cpu.nextByte());});

    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }
}
