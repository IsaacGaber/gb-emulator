package cpu.instruction;

import java.lang.management.OperatingSystemMXBean;
import java.util.TreeMap;

import cpu.*;
import cpu.register.*;
import memory.Memory;
import util.BitUtil;
import util.Util;

public class InstructionSet {
    private Instruction[] _UNPREFIXED;
    private Instruction[] _CBPREFIXED;

    public class SafetyMap extends TreeMap<Integer, Instruction>{
        @Override
        public Instruction put(Integer key, Instruction value) {
            if (key < 0x0 || key >= 0xFF) {
                throw new RuntimeException("Key out of bounds: " + key);
            }
            if (this.containsKey(key)) {
                throw new RuntimeException(String.format("attempted to put duplicate instruction key pair: %s, %s", Util.byteToHexstring(key), value));
            }
            return super.put(key, value);
        }

    }

    // all operations on register "indirecly" incur a 4-clock penalty
    private static int indirectPenalty(Register a, Register b) {
        return (a instanceof IndirectRegister || b instanceof IndirectRegister) ? 4 : 0;
    }

    public InstructionSet(CPU cpu, Memory memory) {
        // used for simplifying construction of similar instructions                                
        final Register[] r8 = new Register[]{cpu.B, cpu.C, cpu.D, cpu.E, cpu.H, cpu.L, cpu._HL, cpu.A}; // specifically used for arithmetic
        final Register[] r8_i = new Register[] {cpu.BC, cpu.DE, cpu._HLi, cpu._HLd}; // indirect registers, excluding _HL
        final Register[] r16 = new Register[]{cpu.BC, cpu.DE, cpu.HL, cpu.SP};
        final Register[] r16_2 = new Register[]{cpu.BC, cpu.DE, cpu.HL, cpu.AF}; // variant with AF instead of SP - used for PUSH and POP

        final Flag[] cc = new Flag[]{Flag.NZ, Flag.Z, Flag.NC, Flag.C};              // all implemented instruction flags

        SafetyMap _UNPREFIXED_MAP = new SafetyMap();
        SafetyMap _CBPREFIXED_MAP = new SafetyMap();

        // LARGELY DISREGARD HALF CARRY FLAGS
        // access memory through cpu.nextByte/Word() when memory is part of instruction, otherwise, use memory.getByte/Word() and increment PC manually
        
        _UNPREFIXED_MAP.put(0x00, new Instruction("NOP", 1, 4, 
                                            (Op) o -> {}));

        // Write SP to address
        _UNPREFIXED_MAP.put(0x08, new Instruction("LD", 3, 20, 
                            new Operands(Operand.A16, Operand.R16),
                            (Op) o -> {
                                int val = cpu.SP.get();
                                int addr = cpu.nextWord();
                                memory.setByte(addr, val >> 8); 
                                memory.setByte(addr + 1, val); 
                            }));


        // Conditional Jump Relative -- operand signed
        for (int i = 0; i < cc.length; i++) {
            int offset = i * 8;
            Flag f = cc[i];
            _UNPREFIXED_MAP.put(0x20 + offset, new Instruction("JR", 2, 12, 
                                new Operands(Operand.CC, Operand.E8), 
                                (Op) o -> {
                                    byte jumpOffset = (byte) cpu.nextByte();
                                    if (cpu.F.flagSet(f)) {
                                        // System.out.println("performing relative jump to:  " + jumpOffset + " because " + f.name() + " is true");
                                        cpu.PC.set(cpu.PC.get() + jumpOffset);
                                        cpu.incCycles(4);
                                        // throw new RuntimeException("conditional jump performed");
                                    } 
                                }));

        }

        // unconditional jump relative -- operand signed
        _UNPREFIXED_MAP.put(0x18, new Instruction("JR", 2, 12, 
                                                            new Operands(Operand.CC, Operand.E8), 
                                                            (Op) o -> {
                                                                int jumpOffset = cpu.nextByte();
                                                                cpu.PC.set(cpu.PC.get() + (byte) jumpOffset);
                                                            }));

        // ld n16 -> r16
        for (int i = 0; i < r16.length; i++) {
            Register X = r16[i];
            int offset = 16 * i;
            _UNPREFIXED_MAP.put(0x01 + offset, new Instruction("LD", 3, 12, 
                                                            new Operands(Operand.R16, Operand.N16), 
                                                            (Op) o -> {
                                                                int w = cpu.nextWord();
                                                                X.set(w);
                                                                // if (w == 0xd8) {
                                                                //     System.out.println(cpu);
                                                                //     throw new RuntimeException("loading register DE with copyright tiles");    
                                                                // }
                                                                // System.out.println("loading 16-bit register to n8");
                                                                }));
        }

        // increment r16
        for (int i = 0; i < r16.length; i++) {
            Register X = r16[i];
            int offset = 16 * i;
            _UNPREFIXED_MAP.put(0x03 + offset, new Instruction("INC", 1, 8, 
                                                            new Operands(Operand.R16, null), 
                                                            (Op) o -> {
                                                                X.inc();
                                                            }));
        }
    
        // increment r8
        for (int i = 0; i < r8.length; i++) {
            Register X = r8[i];
            int offset = 8 * i;
            _UNPREFIXED_MAP.put(0x04 + offset, new Instruction("INC", 1, 4, 
                                                            new Operands(Operand.R8, null), 
                                                            (Op) o -> {
                                                                X.inc();
                                                                if (X.get() == 0) {
                                                                    cpu.setFlag(Flag.Z);
                                                                } else {
                                                                    cpu.clearFlag(Flag.Z);
                                                                }
                                                                // half carry not set
                                                                // subtraction always cleared
                                                                cpu.clearFlag(Flag.N);
                                                            }));
        }

        // decrement r8
        for (int i = 0; i < r8.length; i++) {
        Register X = r8[i];
        int offset = 8 * i;
        _UNPREFIXED_MAP.put(0x05 + offset, new Instruction("DEC", 1, 4 + (indirectPenalty(X, null) *  2), 
                                                        new Operands(Operand.R8, null), 
                                                        (Op) o -> {
                                                            X.dec();
                                                            if (X.get() == 0) {
                                                                cpu.setFlag(Flag.Z);
                                                            } else {
                                                                cpu.clearFlag(Flag.Z);
                                                            }

                                                            // half carry not set
                                                            // subtraction always set
                                                            cpu.setFlag(Flag.N);
                                                        }));
        }

        // decrement r16
        for (int i = 0; i < r16.length; i++) {
            Register X = r16[i];
            int offset = 16 * i;
            _UNPREFIXED_MAP.put(0x0B + offset , new Instruction("DEC", 1, 8, 
                                                            new Operands(Operand.R16, null), 
                                                            (Op) o -> {
                                                                X.dec();
                                                            }));
        }



        // misc 8-bit loads
        // 0x02, 0x12, 0x22, 0x32 and 0x0A, 0x1A, 0x2A, 0x3A
        for (int i = 0; i < r8_i.length; i++) {
            Register A = cpu.A;
            Register X = r8_i[i]; // indirect operand
            int offset = 0x10 * i;
            // all operations take 8 cycles as each reads memory
            _UNPREFIXED_MAP.put(0x02 + offset, new Instruction("LD", 1, 8, 
                                                            new Operands(Operand.R8, Operand.R8), 
                                                            (Op) o -> {
                                                                // load A into location in memory pointed to by left register
                                                                X.set(A.get());
                                                            }));

            _UNPREFIXED_MAP.put(0x0A + offset,  new Instruction("LD", 1, 8, 
                                                            new Operands(Operand.R8, Operand.R8), 
                                                            (Op) o -> {
                                                                // load left register with val in memory pointed to by right
                                                                A.set(memory.getByte(X.get()));
                                                            }));
        }

        // Unprefixed rotate left
        _UNPREFIXED_MAP.put(0x17, new Instruction("RLA", 2, 4,
                                                        new Operands(Operand.R8, null), 
                                                        (Op) o -> {
                                                            int result = cpu.A.get() << 1;
                                                            // rotate through carry flag
                                                            if (cpu.F.flagSet(Flag.C)) {
                                                                result |= 0x1;
                                                            }
                                                            if (BitUtil.getBit(result, 8)) {
                                                                cpu.setFlag(Flag.C);
                                                            } else {
                                                                cpu.clearFlag(Flag.C);
                                                            }
                                                            cpu.A.set(result);
                                                            cpu.clearFlag(Flag.Z);
                                                            cpu.clearFlag(Flag.N);
                                                            cpu.setFlag(Flag.H);
                                                        }));


        // n8 -> r8 loads 
        // 0x06, 0x16, 0x26, 0x36 and 0x0E, 0x1E, 0x2E, 0x3E
        for (int i = 0; i < r8.length/2; i++) {
            int offset = 0x10 * i;
            Register X = r8[i * 2];
            Register Y = r8[i * 2 + 1];
            _UNPREFIXED_MAP.put(0x06 + offset, new Instruction("LD", 2, 8 + indirectPenalty(X, X), 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {X.set(cpu.nextByte());}));
            _UNPREFIXED_MAP.put(0x0E + offset, new Instruction("LD", 2, 8, 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {

                                                            // int b = n;
                                                            // if (Y ==cpu.H) {
                                                            //     System.out.println("loading register H to " + Util.byteToHexstring(b));
                                                            // }

                                                            Y.set(cpu.nextByte());}));

        }

        // main block of 8-bit register to register loads, indices 0x40-0x7F
        for (int i = 0x40; i < 0x80; i++){
            // for (int x = 0; x < r8.length; x++) {
            Register left = r8[(i - 0x40) / r8.length];
            Register right = r8[i % r8.length];
                // invalid operation, yield HALT instead
                if (left == cpu._HL && right == cpu._HL) {
                    _UNPREFIXED_MAP.put(i, new Instruction("HALT", 1, 4, (Op) o -> {cpu.halt();}));
                } else {                 // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
                    _UNPREFIXED_MAP.put(i, new Instruction("LD", 1, indirectPenalty(left, right) + 4, 
                                                                new Operands(Operand.R8, Operand.R8), 
                                                                (Op) o -> {
                                                                    // load left register with right register value
                                                                    left.set(right.get());
                                                                }));
                }
        }


        // all r8-r8 add operations, indices 0x80-0x87
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
            _UNPREFIXED_MAP.put(0x80+i, new Instruction("ADD", 1, indirectPenalty(A, X) + 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int a = A.get();
                                                                    int x = X.get();

                                                                    int result = a + x;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (BitUtil.bitCarried(x, a, 7)) {
                                                                        cpu.setFlag(Flag.C);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 3)) {
                                                                        cpu.setFlag(Flag.H);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.H);
                                                                    }

                                                                    if (result == 0) {
                                                                        cpu.setFlag(Flag.Z);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.Z);
                                                                    }
                                                                    cpu.clearFlag(Flag.N);
                                                                }));
        }  

        // all r8-r8 sub operations, indices 0x90-0x97
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            // if either left or right register is [HL] then instruction takes 8 cycles instead of 4
            _UNPREFIXED_MAP.put(0x90+i, new Instruction("SUB", 1, indirectPenalty(A, X) + 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int a = A.get();
                                                                    int x = X.get();

                                                                    int result = Util.unsignedSub(a, x);
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (x > a) {
                                                                        cpu.setFlag(Flag.C);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);;
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 3)) {
                                                                        cpu.setFlag(Flag.H);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.H);
                                                                    }

                                                                    if (result == 0) {
                                                                        cpu.setFlag(Flag.Z);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.Z);
                                                                    }
                                                                    cpu.setFlag(Flag.N);
                                                                }));
        }  
        

        // all r8-r8 ADC operations, indices 0x88-0x90
        // does add operation, then adds carry flag to A
        for (int i = 0; i < 8; i++) {
            // result always stored in register A
            Register A = cpu.A;
            Register X = r8[i];
            _UNPREFIXED_MAP.put(0x88+i, new Instruction("ADC", 1,  indirectPenalty(A, X)+ 4, 
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {    
                                                                    int a = A.get();
                                                                    int x = X.get();

                                                                    int result = a + x;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (BitUtil.bitCarried(x, a, 7)) {
                                                                        cpu.setFlag(Flag.C);
                                                                        A.set(result + 1);;
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 3)) {
                                                                        cpu.setFlag(Flag.H);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.H);
                                                                    }

                                                                    if (result == 0) {
                                                                        cpu.setFlag(Flag.Z);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.Z);
                                                                    }

                                                                    cpu.clearFlag(Flag.N);
                                                                }));
        } 
        // r8-r8 AND operations, 0xA0-0xA7 + r8-r8 XOR operations, 0xA8-0xAF
        for (int i = 0xA0; i < 0xA8; i++) {
            Register A = cpu.A;
            Register right = r8[i%r8.length];
            _UNPREFIXED_MAP.put(i,  new Instruction("AND", 1, 4 + indirectPenalty(A, right),
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {
                                                        int a = A.get();
                                                        int x = right.get();

                                                        int result = a & x;
                                                        A.set(result);

                                                        // set flags
                                                        if (result == 0) {
                                                            cpu.setFlag(Flag.Z);
                                                        } else {
                                                            cpu.clearFlag(Flag.Z);
                                                        }
                                                        cpu.clearFlag(Flag.N);
                                                        cpu.setFlag(Flag.H);
                                                        cpu.clearFlag(Flag.C);
                                                    }));

            _UNPREFIXED_MAP.put(i + 8,  new Instruction("XOR", 1, 4 + indirectPenalty(A, right),
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {
                                                        int a = A.get();
                                                        int x = right.get();

                                                        int result = a ^ x;
                                                        A.set(result);

                                                        // set flags
                                                        if (result == 0) {
                                                            cpu.setFlag(Flag.Z);
                                                        } else {
                                                            cpu.clearFlag(Flag.Z);
                                                        }
                                                        cpu.clearFlag(Flag.N);
                                                        cpu.clearFlag(Flag.H);
                                                        cpu.clearFlag(Flag.C);
                                                    }));

        }

        // r8-r8 OR operations, 0xB0-0xB7 + r8-r8 compare operations, 0xB8 - 0xBF
        for (int i = 0xB0; i < 0xB8; i++) {
            Register A = cpu.A;
            Register right = r8[i%r8.length];
            _UNPREFIXED_MAP.put(i,  new Instruction("OR", 1, 4 + indirectPenalty(A, right),
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {
                                                        int a = A.get();
                                                        int x = right.get();

                                                        int result = a | x;
                                                        A.set(result);

                                                        // set flags
                                                        if (result == 0) {
                                                            cpu.setFlag(Flag.Z);
                                                        } else {
                                                            cpu.clearFlag(Flag.Z);
                                                        }
                                                        cpu.clearFlag(Flag.N);
                                                        cpu.clearFlag(Flag.H);
                                                        cpu.clearFlag(Flag.C);
                                                    }));

            _UNPREFIXED_MAP.put(i + 8,  new Instruction("CP", 1, 4 + indirectPenalty(A, right),
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {
                                                        int a = A.get();
                                                        int x = right.get();

                                                        int result = Util.unsignedSub(a, x);
                                                        result = result < 0 ? 0 : result;
                                                        // set flags
                                                        if (x > a) {
                                                            cpu.setFlag(Flag.C);
                                                        } else {
                                                            cpu.clearFlag(Flag.C);
                                                        }

                                                        if (BitUtil.bitCarried(a, x, 3)) {
                                                            cpu.setFlag(Flag.H);
                                                        } else {
                                                            cpu.clearFlag(Flag.H);
                                                        }

                                                        if (x - a == 0) {
                                                            cpu.setFlag(Flag.Z);
                                                        } else {
                                                            cpu.clearFlag(Flag.Z);
                                                        }
                                                        cpu.setFlag(Flag.N);
                                                    }));
        }

        // r8-n8 compare operation, 0xFE
        _UNPREFIXED_MAP.put(0xFE,  new Instruction("CP", 2, 8,
                                        new Operands(Operand.R8, Operand.N8),
                                        (Op) o -> {
                                            int a = cpu.A.get();
                                            int x = cpu.nextByte();

                                            // int result = Util.unsignedSub(a, x);
                                            // System.out.println("subtracting " + Util.byteToHexstring(x) + " from " + Util.byteToHexstring(a));
                                            // System.out.println("result is: " + result);
                                            // set flags
                                            if (x > a) {
                                                cpu.setFlag(Flag.C);
                                            } else {
                                                cpu.clearFlag(Flag.C);
                                            }

                                            if (BitUtil.bitCarried(a, x, 3)) {
                                                cpu.setFlag(Flag.H);
                                            } else {
                                                cpu.clearFlag(Flag.H);
                                            }

                                            if (x - a == 0) {
                                                cpu.setFlag(Flag.Z);
                                            } else {
                                                cpu.clearFlag(Flag.Z);
                                            }
                                            cpu.setFlag(Flag.N);
                                        }));

        // Return instructions
        _UNPREFIXED_MAP.put(0xC9,  new Instruction("RET", 1, 16,
                                    new Operands(null, null),
                                    (Op) o -> {
                                        cpu.PC.set(memory.getWord(cpu.SP.get()));
                                        cpu.SP.inc();
                                        cpu.SP.inc();
                                        // System.out.println("Value at Stack: " + Util.wordToHexstring(r));
                                        // throw new RuntimeException("returning from subroutine");
                                    }));

        

        // add with n8                       
        _UNPREFIXED_MAP.put(0xC6, new Instruction("ADD", 2, 8,
                                            new Operands(Operand.R8, Operand.N8),
                                            (Op) o -> {
                                                int x = cpu.A.get();
                                                int y = cpu.nextByte();
                                                int result = x + y;
                                                cpu.A.set(result);
                                                // set flags
                                                if (BitUtil.bitCarried(x, y, 7)) {
                                                    cpu.setFlag(Flag.C);
                                                } else {
                                                    cpu.clearFlag(Flag.C);
                                                }

                                                if (BitUtil.bitCarried(x, y, 3)) {
                                                    cpu.setFlag(Flag.H);
                                                } else {
                                                    cpu.clearFlag(Flag.H);
                                                }

                                                if (result == 0) {
                                                    cpu.setFlag(Flag.Z);
                                                } else {
                                                    cpu.clearFlag(Flag.Z);
                                                }
                                                cpu.clearFlag(Flag.N);
                                            }));          
                                        
        
        // conditional jumps
        for (int i = 0; i < cc.length; i++) {
            Flag f = cc[i];
            _UNPREFIXED_MAP.put(0xC2 + (i * 8), new Instruction("JP", 3, 12, // can also be 16 cycles if condition evaluates true
                                                                new Operands(Operand.CC, Operand.A16),
                                                                (Op) o -> {    
                                                                                int A16 = cpu.nextWord();
                                                                                if (cpu.F.flagSet(f)) {
                                                                                    // System.out.println(f.name());
                                                                                    // System.out.println("jumping");
                                                                                    cpu.PC.set(A16);
                                                                                    cpu.incCycles(4);
                                                                                } 
                                                                            }));
        }

        // unconditional jump
        _UNPREFIXED_MAP.put(0xC3, new Instruction("JP", 3, 16, 
                                                        new Operands(Operand.N16, null),
                                                        (Op) o -> {    
                                                                        cpu.PC.set(cpu.nextWord());                                                                       
                                                                    }));
        // conditional calls
        for (int i = 0; i < cc.length; i++) {
            Flag f = cc[i];
            int offset = i * 8;
            _UNPREFIXED_MAP.put(0xC4 + offset, new Instruction("CALL", 3, 12, 
                                                                new Operands(Operand.CC, Operand.A16),
                                                                (Op) o -> {     // push next instruction location onto stack, move stack
                                                                                if (cpu.F.flagSet(f)) {
                                                                                    int callAddr = cpu.nextWord(); // also increments PC to next instruction
                                                                                    cpu.SP.dec();
                                                                                    memory.setByte(cpu.SP.get(), cpu.PC.get() >> 8); // set lower byte
                                                                                    cpu.SP.dec();
                                                                                    memory.setByte(cpu.SP.get(), cpu.PC.get() & 0xFF);      // set upper byte
                                                                                    cpu.PC.set(callAddr);        
                                                                                    cpu.incCycles(12);                                                               
                                                                                }
                                                                            }));                  
    
        }
        
        // unconditional call
        _UNPREFIXED_MAP.put(0xCD, new Instruction("CALL", 3, 24, 
                                                        new Operands(Operand.A16, null),
                                                        (Op) o ->  {     // push next instruction onto stack, move stack
                                                                        int callAddr = cpu.nextWord(); // also increments PC to next instruction
                                                                        cpu.SP.dec();
                                                                        memory.setByte(cpu.SP.get(), cpu.PC.get() >> 8); // set lower byte
                                                                        cpu.SP.dec();
                                                                        memory.setByte(cpu.SP.get(), cpu.PC.get() & 0xFF);      // set upper byte
                                                                        cpu.PC.set(callAddr);        
                                                                    }));     
        // POP operations
        for (int i = 0; i < r16_2.length; i++) {
            int offset = i * 16;
            Register X = r16_2[i];
            _UNPREFIXED_MAP.put(0xC1 + offset, new Instruction("POP", 1, 12, 
                                                        new Operands(Operand.R16, null),
                                                        (Op) o -> {     // pop register off stack, increment stack pointer
                                                                        int r = memory.getByte(cpu.SP.get());
                                                                        cpu.SP.inc();
                                                                        r |= memory.getByte(cpu.SP.get()) << 8;
                                                                        cpu.SP.inc();
                                                                        X.set(r);
                                                                    }));
        }
 
        // PUSH operations 
        for (int i = 0; i < r16_2.length; i++) {
            int offset = i * 16;
            Register X = r16_2[i];
            _UNPREFIXED_MAP.put(0xC5 + offset, new Instruction("PUSH", 1, 16, 
                                                        new Operands(Operand.R16, null),
                                                        (Op) o -> {     // push next instruction onto stack, move stack
                                                                        cpu.SP.dec();
                                                                        memory.setByte(cpu.SP.get(), X.get() >> 8);
                                                                        cpu.SP.dec();
                                                                        memory.setByte(cpu.SP.get(), X.get() & 0xFF);     
                                                                    }));
        }
    
        // load to and from HRAM (address is left-hand operand + 0xFF00)
        // with addresses in memory
        _UNPREFIXED_MAP.put(0xE0, new Instruction("LDH", 2, 12, 
                                                        new Operands(Operand.A8, Operand.R8),
                                                        (Op) o -> {
                                                            memory.setByte(cpu.nextByte() + 0xFF00, cpu.A.get());
                                                        }));

        _UNPREFIXED_MAP.put(0xF0, new Instruction("LDH", 2, 12, 
                                                        new Operands(Operand.R8, Operand.A8),
                                                        (Op) o -> {
                                                            cpu.A.set(memory.getByte(cpu.nextByte() + 0xFF00));
                                                        }));
        // with C and A registers
        _UNPREFIXED_MAP.put(0xE2, new Instruction("LDH", 1, 8, 
                                                        new Operands(Operand.R8, Operand.R8),
                                                        (Op) o -> {
                                                            memory.setByte(cpu.C.get() + 0xFF00, cpu.A.get());
                                                        }));

        _UNPREFIXED_MAP.put(0xF2, new Instruction("LDH", 1, 8, 
                                                        new Operands(Operand.R8, Operand.R8),
                                                        (Op) o -> {
                                                            cpu.A.set(memory.getByte(cpu.C.get() + 0xFF00));
                                                        }));

        // indirect register to ALU, ALU to register loads
        _UNPREFIXED_MAP.put(0xEA, new Instruction("LD", 3, 16, 
                                                        new Operands(Operand.A16, Operand.R8), 
                                                        (Op) o -> {
                                                            int addr = cpu.nextWord();
                                                            // System.out.println("Setting byte at address: " + addr + " To: " + cpu.A.get());
                                                            memory.setByte(addr, cpu.A.get());
                                                        }));

        _UNPREFIXED_MAP.put(0xFA, new Instruction("LD", 3, 16, 
                                                    new Operands(Operand.R8, Operand.A16), 
                                                    (Op) o -> {
                                                            int addr = cpu.nextWord();
                                                            // System.out.println("Setting register A to byte: " + memory.getByte(addr) + " at: " + addr + " To: " + cpu.A.get());

                                                            cpu.A.set(memory.getByte(addr));
                                                        }));
        
        // PREFIXED INSTRUCTIONS

        // rotate left
        for (int i = 0; i < r8.length; i++) {
            Register X = r8[i];
            _CBPREFIXED_MAP.put(i + 0x10, new Instruction("RL", 2, 8 + (indirectPenalty(X, null) * 2),
                                                            new Operands(Operand.R8, null), 
                                                            (Op) o -> {
                                                                int result = X.get() << 1;
                                                                // rotate through carry flag
                                                                if (cpu.F.flagSet(Flag.C)) {
                                                                    result |= 0x1;
                                                                }
                                                                
                                                                if (BitUtil.getBit(result, 8)) {
                                                                    cpu.setFlag(Flag.C);
                                                                } else {
                                                                    cpu.clearFlag(Flag.C);
                                                                }
                                                                // limit to 8 bits
                                                                result &= 0xFF;
                                                                if (result == 0) {
                                                                    cpu.setFlag(Flag.Z);
                                                                } else {
                                                                    cpu.clearFlag(Flag.Z);
                                                                }
                                                                X.set(result);

                                                                cpu.clearFlag(Flag.N);
                                                                cpu.setFlag(Flag.H);
                                                            }));
        }
        // bit test instructions
        for (int i = 0; i < 0x40; i++) {
            int bit = i / 8;
            Register X = r8[i % r8.length];
            _CBPREFIXED_MAP.put(i + 0x40, new Instruction("BIT", 2, 8 + indirectPenalty(X, null),
                                                            new Operands(Operand.U3, Operand.R8), 
                                                            (Op) o -> {
                                                                if (!BitUtil.getBit(X.get(), bit)) {
                                                                    cpu.setFlag(Flag.Z);
                                                                } else {
                                                                    cpu.clearFlag(Flag.Z);
                                                                }
                                                                cpu.clearFlag(Flag.N);
                                                                cpu.setFlag(Flag.H);
                                                            }));
        }


        _UNPREFIXED = new Instruction[256];
        _CBPREFIXED = new Instruction[256];
        // copy maps to arrays for strictly constant time access, not entirely necessary
        for (Integer opcode : _UNPREFIXED_MAP.keySet()) {
            _UNPREFIXED[opcode] = _UNPREFIXED_MAP.get(opcode);
        }

        for (Integer opcode : _CBPREFIXED_MAP.keySet()) {
            _CBPREFIXED[opcode] = _CBPREFIXED_MAP.get(opcode);
        }

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
