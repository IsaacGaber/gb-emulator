package cpu.instruction;

// import javax.management.RuntimeErrorException;

// utility for operand types
public class Operand {
    public final OperandType OPERAND_TYPE;

    public final boolean INCREMENT;
    public final boolean IMMEDIATE;

    // public Operable operable;   // length unchecked

    // // make setting and getting data illegal for registers?
    // public int getData() {
    //     if (OperandType.isRegister(OPERAND_TYPE)) {
    //         throw new UnsupportedOperationException("");
    //     }
    //     return operable.get();
    // }

    // public void setData(int data) {
    //     if (IMMEDIATE) {
    //         operable.set(data);
    //     } else {
            
    //     }
    // }


    Operand(OperandType o, boolean increment, boolean immediate) {
        OPERAND_TYPE = o;
        INCREMENT = increment;
        IMMEDIATE = immediate;

    }

    Operand(OperandType o)  {
        OPERAND_TYPE = o;
        INCREMENT = false;
        IMMEDIATE = true;

        // Operand(n, false, true);
    }

    

    // init operand from string - used when building instruction set
    // can ensure operand is register
    // Operand(String s) { //boolean immediate,  boolean increment) {
    //     OPERAND_TYPE = OperandType.stringToOperand(s);
    //     if (!OperandType.isRegister(OPERAND_TYPE)) {
    //         throw new RuntimeException("Cannot init data Operand without value");
    //     }
        
    //     if (s.charAt(0) == '[') {
    //         if (parenthesisBalanced(s)) {
    //             IMMEDIATE = false;
    //             INCREMENT = (s.charAt(s.length()-2) == '+');
    //         } else {
    //             throw new RuntimeException("Invalid Operand Type '" + s + "'");
    //         }
    //     } else {
    //         IMMEDIATE = true;
    //         INCREMENT = (s.charAt(s.length()-1) == '+');
    //     }
    // }

    // Operand(String s, T d) {
    //     OPERAND_TYPE = OperandType.stringToOperand(s);
    //     if (OperandType.isRegister(OPERAND_TYPE)) {
    //         throw new RuntimeException("Cannot init Register Operand with value");
    //     }

    //     if (s.charAt(0) == '[' && parenthesisBalanced(s)) {
    //         IMMEDIATE = true;
    //     } else {
    //         IMMEDIATE = false;
    //     }

    //     INCREMENT = false;
    //     _data = d;
    // }

    // public boolean isData(){
    //     return (OPERAND_TYPE != null 
    //             && (OPERAND_TYPE == OperandType.N8 
    //             || OPERAND_TYPE == OperandType.N16 
    //             || OPERAND_TYPE == OperandType.A8 
    //             || OPERAND_TYPE == OperandType.A16 
    //             || OPERAND_TYPE == OperandType.E8));
    // }

    // private boolean parenthesisBalanced(String s){
    //     int cnt = 0;
    //     for (char c : s.toCharArray()) {
    //         switch (c) {
    //             case '[' -> cnt++;
    //             case ']' -> cnt--;
    //         }
    //     }
    //     return cnt == 0;
    // }

    public int byteLength(){
        if (OPERAND_TYPE == null) {
            return 0;
        } else {
            switch (OPERAND_TYPE) {
                case OperandType.N8:
                case OperandType.A8:
                    return 1;
                case OperandType.N16:
                case OperandType.A16:
                    return 2;
                default:
                    return 0;
            }
        }  
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Operand other = (Operand)obj;
            return other.OPERAND_TYPE == OPERAND_TYPE
                    && other.IMMEDIATE == IMMEDIATE
                    && other.INCREMENT == INCREMENT;
                    // && other.BYTES == BYTES;
        } catch (Exception e) {
            return false;
        }
    }


    // correctly format operand based on type and data
    public String toString() {
        String str;
        if (OPERAND_TYPE == null) {
            str = "";
        } else if (!isData()) {
            str = OPERAND_TYPE.name().toLowerCase();
            if (INCREMENT) {
                str += 'i';
            }
        } else {
            // if (OPERAND_TYPE == OperandType.N8 || OPERAND_TYPE == OperandType.A8 || OPERAND_TYPE == OperandType.E8) { // to 8 bit values
            //     str = StringUtil.byteToHexstring(data);
            // } else if (OPERAND_TYPE == OperandType.N16 || OPERAND_TYPE == OperandType.A16) {                          // to 16 bit values
            //     str = StringUtil.wordToHexstring(data);
            // } else {
                throw new TypeNotPresentException(OPERAND_TYPE.name(), null);
            // }   
        }

        if (!IMMEDIATE) {
            str = '[' + str + ']';
        }
        return str;
    }
}
