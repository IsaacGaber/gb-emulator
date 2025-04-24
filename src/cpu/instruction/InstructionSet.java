package cpu.instruction;

import cpu.*;
import cpu.register.*;
import memory.Memory;
import util.BitUtil;
import util.Util;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;


    // all operations on register "indirecly" incur a 4-clock penalty
    private static int indirectPenalty(Register a, Register b) {
        return (a instanceof IndirectRegister || b instanceof IndirectRegister) ? 4 : 0;
    }

    public InstructionSet(CPU cpu, Memory memory) {
        // used for simplifying construction of similar instructions                                
        final Register[] r8 = new Register[]{cpu.B, cpu.C, cpu.D, cpu.E, cpu.H, cpu.L, cpu._HL, cpu.A}; // specifically used for arithmetic
        final Register[] r8_i = new Register[] {cpu.BC, cpu.DE, cpu._HLi, cpu._HLd}; // indirect registers, excluding _HL
        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];


        // DISREGARD HALF CARRY FLAGS
        // access memory through cpu.nextByte/Word() when memory is part of instruction, otherwise, use memory.getByte/Word() and increment PC manually
        
        // HALT - wait until interrupt
        _UNPREFIXED[0x76] = new Instruction("HALT", 1, 4, (Op) o -> {cpu.halt();});

        _UNPREFIXED[0x00] = new Instruction("NOP", 1, 4, 
                                            (Op)o -> {});



        // increment register B
        _UNPREFIXED[0x04] = new Instruction("INC", 1, 4, 
                                            new Operands(Operand.R8, null),
                                            (Op) o -> {    
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
        
        // misc 8-bit loads
        // 0x02, 0x12, 0x22, 0x32 and 0x0A, 0x1A, 0x2A, 0x3A
        for (int i = 0; i < 4; i++) {
            Register A = cpu.A;
            Register X = r8_i[i]; // indirect operand
            int offset = 0x10 * i;
            // all operations take 8 cycles as each reads memory
            _UNPREFIXED[0x02 + offset] = new Instruction("LD", 1, 8, 
                                                            new Operands(Operand.R8, Operand.R8), 
                                                            (Op) o -> {
                                                                // load left register with right register value
                                                                X.set(A.get());
                                                            });
            _UNPREFIXED[0x0A + offset] = new Instruction("LD", 1, 8, 
                                                            new Operands(Operand.R8, Operand.R8), 
                                                            (Op) o -> {
                                                                // load left register with right register value
                                                                A.set(X.get());
                                                            });
        }

        // n8 -> r8 loads 
        // 0x06, 0x16, 0x26, 0x36 and 0x0E, 0x1E, 0x2E, 0x3E
        for (int i = 0; i < r8.length/2; i++) {
            int offset = 0x10 * i;
            Register X = r8[i * 2];
            Register Y = r8[i * 2 + 1];
            _UNPREFIXED[0x06 + offset] = new Instruction("LD", 2, 8, 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {X.set(cpu.nextByte());});
            _UNPREFIXED[0x0E + offset] = new Instruction("LD", 2, 8, 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {Y.set(cpu.nextByte());});

        }

        // main block of 8-bit register to register loads, indices 0x40-0x7F
        for (int y = 0; y < r8.length; y++){
            Register left = r8[y];
            for (int x = 0; x < r8.length; x++) {
                Instruction instruction;
                Register right = r8[x];
                // invalid operation, yield HALT instead
                if (left == cpu._HL && right == cpu._HL) {
                    instruction = _UNPREFIXED[0x76];
                } else {
                    instruction = new Instruction("LD", 1, indirectPenalty(left, right) + 4, 
                                                                new Operands(Operand.R8, Operand.R8), 
                                                                (Op) o -> {
                                                                    // load left register with right register value
                                                                    left.set(right.get());
                                                                });
                }
    
                // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
                _UNPREFIXED[0x40 + (y*r8.length + x)] = instruction;
            }
        }


        // all r8-r8 add operations, indices 0x80-0x87
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
            _UNPREFIXED[0x80+i] = new Instruction("ADD", 1, indirectPenalty(A, X) + 4, 
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

        // all r8-r8 ADC operations, indices 0x88-0x90
        // does add operation, then adds carry flag to A
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            _UNPREFIXED[0x88+i] = new Instruction("ADC", 1,  indirectPenalty(A, X)+ 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int a = A.get();
                                                                    int x = X.get();

                                                                    int result = a + x;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (BitUtil.bitCarried(x, a, 8)) {
                                                                        cpu.setFlag(Flag.C);
                                                                        A.set(result + 1);;
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
