package cpu.instruction;

import cpu.*;
import cpu.register.*;
import memory.Memory;
import util.BitUtil;
import util.Util;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;


    // used for simplifying construction of similar instructions                                
    private static final String[] regIter = new String[]{"B", "C", "D", "E", "H", "L", "A", "_HL"}; // F not included as F never directly addressed
    private static final String[] doubleRegIter = new String[]{"BC", "DE", "HL", "SP"};              // PC never operand? -- TODO confirm

    public InstructionSet(CPU cpu, Memory memory) {
        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];

        // DISREGARD HALF CARRY FLAGS

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, 4, 
                                            (Op)o -> {});



        // increment register B
        _UNPREFIXED[0x04] = new Instruction("INC", 1, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op)o -> {    
                                                            Register B = cpu.reg("B");

                                                            // overflow and flags
                                                            if (B.get() + 1 > 255) {
                                                                B.set(0);
                                                                cpu.setFlag(Flag.Z);
                                                            } else {
                                                                B.inc();
                                                            }
                                                            cpu.clearFlag(Flag.N);
                                                        });

        // decrement register B                                                        
        _UNPREFIXED[0x05] = new Instruction("DEC", 1, 4, 
                                            new Operands(Operand.R8, null), 
                                            (Op) o -> {
                                                Register B = cpu.reg("B");

                                                // overflow and flags
                                                if (B.get()-1 == 0) {
                                                    cpu.setFlag(Flag.Z);
                                                } else if (B.get()-1 < 0) {
                                                    B.set(255);
                                                } 
                                                B.dec();
                                                cpu.setFlag(Flag.C);
                                            });

        // load register B to 8-bit value
        _UNPREFIXED[0x06] = new Instruction("LD", 2, 8, 
                                            new Operands(Operand.R8, Operand.N8), 
                                            (Op) o -> {cpu.setReg("B", cpu.nextByte());});

        // all 8-bit register to register loads, indices 0x40-0x7F
        // for (int i = 0x40; i < 0x80; i++) {
        //     String leftStr = regIter[(i >> 3) - 8];
        //     String rightStr = regIter[i % 8]; 
        //     _UNPREFIXED[i] = new Instruction(null, 1, false, 4, 
        //                                     new Operands(Operand.R8, Operand.R8), 
        //                                     (Op) o -> {
        //                                         Register left, right;
        //                                         if (leftStr.equals("[HL]")) {
                                                    
        //                                         } else if (rightStr.equals("[HL]")) {

        //                                         } else {
        //                                             left = cpu.reg(leftStr);
        //                                             right = cpu.reg(rightStr);
        //                                         }
        //                                     });
        // }

        // all r8-r8 add operations, indices 0x80-0x87
        for (int i = 0; i < 8; i++) {
            Register A = cpu.reg("A");
            Register X = cpu.reg(regIter[i]);
            _UNPREFIXED[0x80+i] = new Instruction("ADD", 1, 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int x = A.get();
                                                                    int y = X.get();
                                                                    int result = x + y;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (BitUtil.bitCarried(x, y, 8)) {
                                                                        cpu.setFlag(Flag.C);
                                                                    }
                                                                    if (BitUtil.bitCarried(x, y, 4)) {
                                                                        cpu.setFlag(Flag.H);
                                                                    }
                                                                    if (result == 0) {
                                                                        cpu.setFlag(Flag.Z);
                                                                    }
                                                                    cpu.clearFlag(Flag.N);
                                                                });
        }      
        // add with n8                       
        _UNPREFIXED[0xC6] = new Instruction("ADD", 2, 8,
                                            new Operands(Operand.R8, Operand.N8),
                                            (Op) o -> {
                                                Register A = cpu.reg("A");
                                                int x = A.get();
                                                int y = cpu.nextByte();
                                                int result = x + y;
                                                A.set(result);
                                                // set flags
                                                if (BitUtil.bitCarried(x, y, 8)) {
                                                    cpu.setFlag(Flag.C);
                                                }
                                                if (BitUtil.bitCarried(x, y, 4)) {
                                                    cpu.setFlag(Flag.H);
                                                }
                                                if (result == 0) {
                                                    cpu.setFlag(Flag.Z);
                                                }
                                                cpu.clearFlag(Flag.N);
                                            });          
                                        
        // conditional jump -- if Z(ero) Flag not set
        _UNPREFIXED[0xC2] = new Instruction("JP", 3, 16, // can also be 12 cycles
                                            new Operands(Operand.CC, Operand.N16),
                                            (Op) o -> {    
                                                            if (!cpu.flagSet(Flag.Z)) {
                                                                Register PC = cpu.reg("PC");
                                                                PC.set(cpu.nextWord());           
                                                            }
                                                            
                                                        });
        // unconditional jump
        _UNPREFIXED[0xC3] = new Instruction("JP", 3, 16, 
                                            new Operands(Operand.N16, null),
                                            (Op) o -> {    
                                                            Register PC = cpu.reg("PC");
                                                            PC.set(cpu.nextWord());                                                                       
                                                        });
                                                        
        // wait until interrupt
        _UNPREFIXED[0x76] = new Instruction("HALT", 1, 4, null);
        
        System.out.println(this);
    }
    
    public Instruction getUnprefixed(int i) {
        return _UNPREFIXED[i];
    }

    public Instruction getCBPrefixed(int i) {
        return _CBPREFIXED[i];
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        Instruction[] instructions  = _UNPREFIXED;
        sb.append("IMPLEMENTED INSTRUCTIONS\n\n");
        sb.append("UNPREFIXED:\n");
        for (int i = 0; i < 2; i++) {
            sb.append("      ");
            for (int j = 0; j < 16; j++) {
                sb.append(Util.byteToHexstring(j) + " ");
            }
            for (int y = 0; y < 16; y++) {
                sb.append("\n" + Util.byteToHexstring(y << 4) + " | ");
                for (int x = 0; x < 16; x++) {
                    String s = instructions[y * 16 + x] != null ? "(O) "  : "( ) ";
                    sb.append(s);
                }
                sb.append(" |");
            }    

            sb.append("\n\nPREFIXED:\n");
            instructions = _CBPREFIXED;
        }
        return sb.toString();
    }
}
