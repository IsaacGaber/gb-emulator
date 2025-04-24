package cpu.instruction;

import cpu.*;
import cpu.register.*;
import memory.Memory;
import util.BitUtil;
import util.Util;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;



    public InstructionSet(CPU cpu, Memory memory) {
        // used for simplifying construction of similar instructions                                
        final Register[] r8 = new Register[]{cpu.B, cpu.C, cpu.D, cpu.E, cpu.H, cpu.L, cpu._HL, cpu.A}; // specifically used for arithmetic

        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];

        // DISREGARD HALF CARRY FLAGS
        // access memory through cpu.nextByte/Word() when memory is part of instruction, otherwise, use memory.getByte/Word() and increment PC manually

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, 4, 
                                            (Op)o -> {});



        // increment register B
        _UNPREFIXED[0x04] = new Instruction("INC", 1, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op)o -> {    
                                                            // overflow and flags
                                                            if (cpu.B.get() + 1 > 255) {
                                                                cpu.B.set(0);
                                                                cpu.setFlag(Flag.Z);
                                                            } else {
                                                                cpu.B.inc();
                                                            }
                                                            cpu.clearFlag(Flag.N);
                                                        });

        // decrement register B                                                        
        _UNPREFIXED[0x05] = new Instruction("DEC", 1, 4, 
                                            new Operands(Operand.R8, null), 
                                            (Op) o -> {
                                                // overflow and flags
                                                if (cpu.B.get()-1 == 0) {
                                                    cpu.setFlag(Flag.Z);
                                                } else if (cpu.B.get()-1 < 0) {
                                                    cpu.B.set(255);
                                                } 
                                                cpu.B.dec();
                                                cpu.setFlag(Flag.C);
                                            });

        // load register B to 8-bit value
        _UNPREFIXED[0x06] = new Instruction("LD", 2, 8, 
                                            new Operands(Operand.R8, Operand.N8), 
                                            (Op) o -> {cpu.B.set(cpu.nextByte());});

        // all 8-bit register to register loads, indices 0x40-0x7F
        for (int y = 0; y < r8.length; y++){
            Register left = r8[y];
            for (int x = 0; x < r8.length; x++) {
                Register right = r8[x];
                // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
                _UNPREFIXED[0x40 + (y*r8.length + x)] = new Instruction("LD", 1, (left == cpu._HL|| right == cpu._HL) ? 8 : 4, 
                                                                new Operands(Operand.R8, Operand.R8), 
                                                                (Op) o -> {
                                                                    // load left register with right register value
                                                                    left.set(right.get());
                                                                });
            }
        }

        // all r8-r8 add operations, indices 0x80-0x87
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            _UNPREFIXED[0x80+i] = new Instruction("ADD", 1, 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int a = A.get();
                                                                    int x = X.get();

                                                                    int result = a + x;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (BitUtil.bitCarried(x, a, 8)) {
                                                                        cpu.setFlag(Flag.C);
                                                                    }
                                                                    if (BitUtil.bitCarried(x, a, 4)) {
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
                                                int x = cpu.A.get();
                                                int y = cpu.nextByte();
                                                int result = x + y;
                                                cpu.A.set(result);
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
                                                                cpu.PC.set(cpu.nextWord());      
                                                            } else {
                                                                cpu.PC.set(cpu.PC.get() + 2);
                                                            }
                                                        });
        // unconditional jump
        _UNPREFIXED[0xC3] = new Instruction("JP", 3, 16, 
                                            new Operands(Operand.N16, null),
                                            (Op) o -> {    
                                                            cpu.PC.set(cpu.nextWord());                                                                       
                                                        });
                                                        
        // wait until interrupt
        _UNPREFIXED[0x76] = new Instruction("HALT", 1, 4, (Op) o -> {cpu.halt();});
        
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
        sb.append("CURRENTLY IMPLEMENTED INSTRUCTIONS\n\n");
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
