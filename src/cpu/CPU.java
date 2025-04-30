package cpu;

import java.util.*;
import java.util.Map.Entry;

import memory.*;
import util.*;
import cpu.instruction.*;
import cpu.register.*;

public class CPU {

    public final ByteRegister A, B, C, D, E, H, L;
    public final FlagRegister F;
    public final DoubleRegister AF, BC, DE, HL, PC, SP;
    // _HLi and _HLd are increment and decrement versions of indirect HL respectively
    public final IndirectRegister _BC, _DE, _HL, _HLi, _HLd;
    public final TreeMap<String, Register> _registers;

    // private final TreeSet<Register> _registers; 
    private int currentByte;
    private Instruction currentInstruction;

    private InstructionSet _instructionSet;
    private Memory _memory;

    public enum States {
        HALTED, RUNNING
    }

    private States _state;

    // cpu cycles
    private int _cycles; 

    public CPU(Memory memory) {
        _memory = memory;
        // init register array - used only for toString
        _registers = new TreeMap<>();

        // init _registers
        A = new ByteRegister(); _registers.put("A", A);
        B = new ByteRegister(); _registers.put("B", B);
        C = new ByteRegister(); _registers.put("C", C);
        D = new ByteRegister(); _registers.put("D", D);
        E = new ByteRegister(); _registers.put("E", E);
        H = new ByteRegister(); _registers.put("H", H);
        L = new ByteRegister(); _registers.put("L", L);
        // special flag register
        F = new FlagRegister(); _registers.put("F", F);
        // double _registers
        AF = new DoubleRegister(A, F);  _registers.put("AF", AF);
        BC = new DoubleRegister(B, C);  _registers.put("BC", BC);
        DE = new DoubleRegister(D, E);  _registers.put("DE", DE);
        HL = new DoubleRegister(H, L);  _registers.put("HL", HL);
        // program counter and stack pointer
        SP = new DoubleRegister(new ByteRegister(), new ByteRegister());    _registers.put("SP", SP);
        PC = new DoubleRegister(new ByteRegister(), new ByteRegister());    _registers.put("PC", PC);
        // indirect registers
        _BC = new IndirectRegister(BC, 0, memory);
        _DE = new IndirectRegister(DE, 0, memory);
        _HL = new IndirectRegister(HL, 0, memory);
        _HLi = new IndirectRegister(HL, 1, memory);
        _HLd = new IndirectRegister(HL, -1, memory);

        // init program counter
        PC.set(0);
        SP.set(0); // start of stack set by boot ROM

        _instructionSet = new InstructionSet(this, _memory);

        _state = States.RUNNING;
    }

    int furthestReached = 0;
    Instruction furthestInstruction;
    public void step() {
        // skip cartridge DRM check and audio -- jump straight to ROM entrypoint
        if (PC.get() == 0x008F) {
            PC.set(0x100);
        }
        // SKIP ZEROING OUT VRAM LOOP
        // if (PC.get() == 0x000A) {
        //     PC.set(0x000C);
        // }

        // if (PC.get() == 0x002E) {
        //     System.out.println(graphicsCalls);
        //     throw new RuntimeException("Finished Graphics Routine");
        // }
        // loop VRAM
        // if (PC.get() == 0x00A1) {
        //     PC.set(0x0098);
        // }

        currentByte = nextByte();

        if (currentByte == 0xCB) {
            currentByte = nextByte();
            // System.out.println("Fetching Prefixed Instruction");
            currentInstruction = _instructionSet.getCBPrefixed(currentByte);
        } else {
            currentInstruction = _instructionSet.getUnprefixed(currentByte);
        }

        // System.out.println(Util.byteToHexstring(furthestReached) +  " + "  + furthestInstruction);

        // if (PC.get() < 0x95 && PC.get() > furthestReached) {
        //     furthestReached = PC.get();
        //     furthestInstruction = currentInstruction;
        // }

        // System.err.println(Util.byteToHexstring(currentByte));
        _cycles += currentInstruction.run();
    }
    
    public void setFlag(Flag f) {
        F.set(f);
    }

    public void incCycles(int cycles){
        _cycles += cycles;
    }

    public void clearFlag(Flag f) {
        F.set(BitUtil.setBit(F.get(), f.POS, false));
    }

    // fetches next byte and incremements program counter
    public int nextByte() {
        int i = _memory.getByte(PC.get());
        PC.inc();
        return i;
    }

    // confirm this is actually how the values are stored 
    public int nextWord() {
        return nextByte() | (nextByte() << 8);
    }

    public void halt() {
        _state = States.HALTED;
    }

    // TODO implement interrupts
    public void interrupt(){

    }

    public boolean running() {
        return _state == States.RUNNING;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbTwo = new StringBuilder();
        sb.append("Byte Value: " + Util.byteToHexstring(currentByte));
        sb.append("\nInstruction: " + currentInstruction);

        sb.append("\nInstruction Cycles: " + _cycles);
        sb.append("\nRegisters:\n");

        for (Entry<String, Register> entry : _registers.entrySet()) {
            String name = entry.getKey();
            Register reg = entry.getValue();
            if (reg instanceof DoubleRegister) {
                sbTwo.append(String.format("%s: %10.5s\n", name, Util.wordToHexstring(reg.get())));
            } else if (reg instanceof ByteRegister) {
                sb.append(String.format("%s: %11.3s\n", name, Util.byteToHexstring(reg.get())));
            }
        }

        sbTwo.append("\nFlags:\n");
        Flag[] flags = new Flag[]{Flag.Z, Flag.N, Flag.H, Flag.C};
        for (Flag flag : flags) {
            sbTwo.append(String.format("\tFlag %s is%sset.\n", flag.name(), F.flagSet(flag) ? " " : " not "));
        }

        return sb.toString() + sbTwo.toString();
    }
}
