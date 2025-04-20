package cpu;

import java.util.*;
import memory.*;
import cpu.register.*;

public class CPU {
    private static ByteRegister A, B, C, D, E, H, L, F;
    private static DoubleRegister AF, BC, DE, HL, PC, SP;
    private static TreeMap<String, Register> registers;

    private static int currentByte;
    
    CPU() {
        // init registers
        A = new ByteRegister(); registers.put("A", A);
        B = new ByteRegister(); registers.put("B", B);
        C = new ByteRegister(); registers.put("C", C);
        D = new ByteRegister(); registers.put("D", D);
        E = new ByteRegister(); registers.put("E", E);
        H = new ByteRegister(); registers.put("H", H);
        L = new ByteRegister(); registers.put("L", L);
        // special flag register
        F = new FlagRegister(); registers.put("F", F);
        // double registers
        AF = new DoubleRegister(A, F);  registers.put("AF", AF);
        BC = new DoubleRegister(B, C);  registers.put("BC", BC);
        DE = new DoubleRegister(D, E);  registers.put("DE", DE);
        HL = new DoubleRegister(H, L);  registers.put("HL", HL);
        // program counter and stack pointer
        PC = new DoubleRegister(new ByteRegister(), new ByteRegister());    registers.put("PC", PC);
        SP = new DoubleRegister(new ByteRegister(), new ByteRegister());    registers.put("SP", SP);
        // init program counter
        PC.set(0);
    }

    public void step() {
        int current = nextByte();
        
        System.out.println(this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Register> entry : registers.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue());
        }
        return sb.toString();
    }

    public static void setReg(String reg, int val) {
        registers.get(reg).set(val);
    }

    public static int getReg(String reg) {
        return registers.get(reg).get();
    }
    public static void setFlag(Flag f) {

    }

    public static int nextByte() {
        int i = Memory.getByte(PC.get());
        PC.inc();
        return i;

    }
}
