package cpu.instruction;

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
        final Flag[] cc = new Flag[]{Flag.NZ, Flag.Z, Flag.NC, Flag.C};              // all implemented instruction flags

        SafetyMap _UNPREFIXED_MAP = new SafetyMap();
        SafetyMap _CBPREFIXED_MAP = new SafetyMap();

        // LARGELY DISREGARD HALF CARRY FLAGS
        // access memory through cpu.nextByte/Word() when memory is part of instruction, otherwise, use memory.getByte/Word() and increment PC manually
        
        _UNPREFIXED_MAP.put(0x00, new Instruction("NOP", 1, 4, 
                                            (Op)o -> {}));

        // ld n16 -> r16
        for (int i = 0; i < r16.length; i++) {
            Register X = r16[i];
            int offset = 16 * i;
            _UNPREFIXED_MAP.put(0x01 + offset, new Instruction("LD", 3, 12, 
                                                            new Operands(Operand.R16, Operand.N16), 
                                                            (Op) o -> {
                                                                X.set(cpu.nextWord());
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
        _UNPREFIXED_MAP.put(0x05 + offset, new Instruction("dec", 1, 4, 
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
                                                                // System.out.println("Setting byte at address: " + r16[i].get() + " To: " + A.get());        
                                                                memory.setByte(X.get(), A.get());
                                                            }));

            _UNPREFIXED_MAP.put(0x0A + offset,  new Instruction("LD", 1, 8, 
                                                            new Operands(Operand.R8, Operand.R8), 
                                                            (Op) o -> {
                                                                // load left register with val in memory pointed to by right
                                                                // System.out.println("Setting Register A to byte at address: " + X.get() + " To: " + A.get());        

                                                                A.set(memory.getByte(X.get()));
                                                            }));
        }

        // n8 -> r8 loads 
        // 0x06, 0x16, 0x26, 0x36 and 0x0E, 0x1E, 0x2E, 0x3E
        for (int i = 0; i < r8.length/2; i++) {
            int offset = 0x10 * i;
            Register X = r8[i * 2];
            Register Y = r8[i * 2 + 1];
            _UNPREFIXED_MAP.put(0x06 + offset, new Instruction("LD", 2, 8, 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {X.set(cpu.nextByte());}));
            _UNPREFIXED_MAP.put(0x0E + offset, new Instruction("LD", 2, 8, 
                                                        new Operands(Operand.R8, Operand.N8), 
                                                        (Op) o -> {Y.set(cpu.nextByte());}));

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
                                                                    if (BitUtil.bitCarried(x, a, 8)) {
                                                                        cpu.setFlag(Flag.C);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 4)) {
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

                                                                    int result = a - x;
                                                                    A.set(result);
                                                                    // set flags
                                                                    if (x > a) {
                                                                        cpu.setFlag(Flag.C);
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);;
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 4)) {
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
                                                                    if (BitUtil.bitCarried(x, a, 8)) {
                                                                        cpu.setFlag(Flag.C);
                                                                        A.set(result + 1);;
                                                                    } else {
                                                                        cpu.clearFlag(Flag.C);
                                                                    }

                                                                    if (BitUtil.bitCarried(x, a, 4)) {
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
        // r8-r8 OR operations, 0xB0-0xB7
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
        }

        // r8-r8 compare operations, 0xB8 - 0xBF
        for (int i = 0xB8; i < 0xC0; i++) {
            Register A = cpu.A;
            Register right = r8[i%r8.length];
            _UNPREFIXED_MAP.put(i,  new Instruction("CP", 1, 4 + indirectPenalty(A, right),
                                                    new Operands(Operand.R8, Operand.R8),
                                                    (Op) o -> {
                                                        int a = A.get();
                                                        int x = right.get();

                                                        int result = a - x;
                                                        // set flags
                                                        if (x > a) {
                                                            cpu.setFlag(Flag.C);
                                                        } else {
                                                            cpu.clearFlag(Flag.C);
                                                        }

                                                        if (BitUtil.bitCarried(a, x, 4)) {
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

        // r8-n8 compare operation, 0xFE
        _UNPREFIXED_MAP.put(0xFE,  new Instruction("CP", 2, 8,
                                                        new Operands(Operand.R8, Operand.N8),
                                                        (Op) o -> {
                                                            int a = cpu.A.get();
                                                            int x = cpu.nextByte();

                                                            int result = a - x;
                                                            // set flags
                                                            if (x > a) {
                                                                cpu.setFlag(Flag.C);
                                                            } else {
                                                                cpu.clearFlag(Flag.C);
                                                            }

                                                            if (BitUtil.bitCarried(a, x, 4)) {
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


        // add with n8                       
        _UNPREFIXED_MAP.put(0xC6, new Instruction("ADD", 2, 8,
                                            new Operands(Operand.R8, Operand.N8),
                                            (Op) o -> {
                                                int x = cpu.A.get();
                                                int y = cpu.nextByte();
                                                int result = x + y;
                                                cpu.A.set(result);
                                                // set flags
                                                if (BitUtil.bitCarried(x, y, 8)) {
                                                    cpu.setFlag(Flag.C);
                                                } else {
                                                    cpu.clearFlag(Flag.C);
                                                }

                                                if (BitUtil.bitCarried(x, y, 4)) {
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
                                                        
        // indirect register to ALU, ALU to register loads
        _UNPREFIXED_MAP.put(0xEA, new Instruction("LD", 3, 16, new Operands(Operand.A16, Operand.R8), (Op) o -> {
                                                        int addr = cpu.nextWord();
                                                        // System.out.println("Setting byte at address: " + addr + " To: " + cpu.A.get());
                                                        memory.setByte(addr, cpu.A.get());
                                                        }));

        _UNPREFIXED_MAP.put(0xFA, new Instruction("LD", 3, 16, new Operands(Operand.R8, Operand.A16), (Op) o -> {
                                                        int addr = cpu.nextWord();
                                                        // System.out.println("Setting register A to byte: " + memory.getByte(addr) + " at: " + addr + " To: " + cpu.A.get());

                                                        cpu.A.set(memory.getByte(addr));
                                                        }));
        
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
