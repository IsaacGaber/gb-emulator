package cpu.instruction;

public enum Operand {
    // descriptions according to pandocs
    R8,  // Any of the 8-bit registers (A, B, C, D, E, H, L).
    R16, // Any of the general-purpose 16-bit registers (BC, DE, HL).
    N8,  // 8-bit integer constant (signed or unsigned, -128 to 255).
    N16, // 16-bit integer constant (signed or unsigned, -32768 to 65535).
    E8,  // 8-bit signed offset (-128 to 127).
    U3, // 3-bit unsigned bit index (0 to 7, with 0 as the least significant bit).
    CC; // conditional codes


    // // 8 bit registers
    // A,
    // B,
    // C,
    // D,
    // E,
    // F,
    // H,
    // L,
    // // 16 bit registers
    // AF,
    // BC,
    // DE,
    // HL,
    // // program counter
    // PC,
    // // stack pointer
    // SP,
    // other operand types
    // N8,  // immediate 8-bit data
    // N16, // immediate little-endian 16 bit data
    // A8,  // immediate 8-bit unsigned data, sometimes added to 0xFF00 to create a 16 bit HRAM address
    // A16, // little-endian 16-bit address
    // E8,  // 8-bit integer offset data
    // // conditional codes
    // // CC; // all conditional codes
    // NZ, // addition and subtraction
    // Z, // zero
    // CC_C, // carry
    // CC_H; // half carry

    // public static OperandType intToOperand(int i) {
    //     if (((i >> 8) & 0xFF) != 0) {
    //         return A16;
    //     } else {
    //         return A8;
    //     }
    // }

    // public static boolean isRegister(OperandType o) {
    //     return (o != N8 &&  // immediate 8-bit data
    //             o != N16 &&// immediate little-endian 16 bit data
    //             o != A8 &&  // immediate 8-bit unsigned data, sometimes added to 0xFF00 to create a 16 bit HRAM address
    //             o != A16 && // little-endian 16-bit address
    //             o != E8 &&  // 8-bit integer offset data
    //             // conditional codes
    //             o != NZ && // addition and subtraction
    //             o != Z && // zero
    //             o != CC_C && // carry
    //             o != CC_H); // half carry
    // }

    // public static boolean isDoubleRegister(OperandType o) {
    //     return (o == AF ||
    //             o == BC ||
    //             o == DE ||
    //             o == HL ||
    //             o == PC ||
    //             o == SP);
    // }

    // public static OperandType stringToOperand(String s) {
    //     try {
    //         return OperandType.valueOf(s.toUpperCase());
    //     } catch (Exception e) {
    //         throw new RuntimeException("Invalid Operand Type: " + s);
    //     }
    // }

    // public String toString(){
    //     return this.name();
    // }
}