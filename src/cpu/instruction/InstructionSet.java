package cpu.instruction;

import cpu.*;
import cpu.register.*;
import memory.Memory;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;

    public InstructionSet(CPU cpu, Memory memory) {
        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];

        // DON'T REGARD HALF CARRY FLAGS
        _UNPREFIXED[0x00] = new Instruction("NOP", 1, false, 4, 
                                            (Op)(o) -> {});

        _UNPREFIXED[0x01] = new Instruction("LD", 3, true, 12, 
                                            new Operands(Operand.R16, Operand.N16), 
                                            (Op)(o) -> {cpu.setReg("BC", cpu.nextByte());});

        _UNPREFIXED[0x3C] = new Instruction("INC", 1, true, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op)(o) -> {    
                                                            Register A = cpu.reg("A");
                                                            A.inc();
                                                            // flags
                                                            if (A.get() == 0) {
                                                                cpu.setFlag(Flag.Z);
                                                            }
                                                            cpu.clearFlag(Flag.N);
                                                        });

        _UNPREFIXED[0x04] = new Instruction("INC", 1, true, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op)(o) -> {    
                                                            Register B = cpu.reg("B");
                                                            B.inc();
                                                            // flags
                                                            if (B.get() == 0) {
                                                                cpu.setFlag(Flag.Z);
                                                            }
                                                            cpu.clearFlag(Flag.N);
                                                        });
        _UNPREFIXED[0xCA] = new Instruction("JP", 3, true, 16 + 12, 
                                            new Operands(Operand.CC, Operand.N16),
                                            (Op)(o) -> {    
                                                            if (!cpu.flagSet(Flag.Z)) {
                                                                Register PC = cpu.reg("PC");
                                                                PC.set(memory.getWord(cpu.nextWord()));           
                                                            }
                                                        });


    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }
}
