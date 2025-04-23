package cpu.instruction;

import cpu.*;
import cpu.register.*;
import memory.Memory;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;


    // used for simplifying construction of similar instructions
    private static final String[] registerIter = new String[]{"B", "C", "D", "E", "H", "L", "A", "[HL]"}; // F not included as F never directly addressed
    private static final String[] doubleRegisterIter = new String[]{"BC", "DE", "HL", "SP"};              // PC never operand? -- TODO confirm

    public InstructionSet(CPU cpu, Memory memory) {
        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];

        // DISREGARD HALF CARRY FLAGS

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, false, 4, 
                                            (Op)(o) -> {throw new RuntimeException("NOP instruction");});



        // increment register B
        _UNPREFIXED[0x04] = new Instruction("INC", 1, true, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op)(o) -> {    
                                                            Register B = cpu.reg("B");

                                                            B.inc();
                                                            // overflow and flags
                                                            if (B.get() > 255) {
                                                                B.set(0);
                                                                cpu.setFlag(Flag.Z);
                                                            } 
                                                            cpu.clearFlag(Flag.N);
                                                        });

        // decrement register B                                                        
        _UNPREFIXED[0x05] = new Instruction("DEC", 1, true, 4, 
                                            new Operands(Operand.R8, null), 
                                            (Op)(o) -> {
                                                Register B = cpu.reg("B");

                                                B.dec();
                                                // overflow and flags
                                                if (B.get() == 0) {
                                                    cpu.setFlag(Flag.Z);
                                                } else if (B.get() < 0) {
                                                    B.set(255);
                                                } 
                                                cpu.setFlag(Flag.C);
                                            });

        // load register B to 8-bit value
        _UNPREFIXED[0x06] = new Instruction("LD", 2, true, 8, 
                                            new Operands(Operand.R8, Operand.N8), 
                                            (Op)(o) -> {cpu.setReg("B", cpu.nextByte());});
                                            
        // add Registers A and B, store result in Register A                                    
        _UNPREFIXED[0x80] = new Instruction("ADD", 1, true, 4, 
                                            new Operands(Operand.R8, Operand.R8),
                                            (Op)(o) -> {    
                                                            Register A = cpu.reg("A");
                                                            Register B = cpu.reg("B");
                                                            A.set(A.get() + B.get());
                                                            // overflow and flags
                                                            if (A.get() > 255) {
                                                                A.set(0);
                                                                cpu.setFlag(Flag.Z);
                                                            } 
                                                            cpu.clearFlag(Flag.N);
                                                        });

        // conditional jump -- if Z(ero) Flag not set
        _UNPREFIXED[0xC2] = new Instruction("JP", 3, true, 12, // can also be 16 cycles
                                            new Operands(Operand.CC, Operand.N16),
                                            (Op)(o) -> {    
                                                            if (!cpu.flagSet(Flag.Z)) {
                                                                Register PC = cpu.reg("PC");
                                                                PC.set(cpu.nextWord());           
                                                            }
                                                            
                                                        });
                                                        
        
        _UNPREFIXED[0x76] = new Instruction("HALT", 1, false, 4, null);

    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }
}
