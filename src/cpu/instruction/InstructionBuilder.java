// package cpu.instruction;

// import java.io.*;
// import org.json.simple.*;
// import org.json.simple.parser.*;

// public class InstructionBuilder {
//     final static String OPCODES_PATH  = "opcodes/Opcodes.json";

//     public static InstructionSet buildInstructions() {
//         // System.out.println("Started Building Instructions");
//         InstructionSet instructionSet = new InstructionSet();
//         JSONObject a = new JSONObject();
//         try {
//             // read json file
//             File f = new File(OPCODES_PATH);
//             FileReader fReader = new FileReader(f);
//             JSONParser parser =  new JSONParser();
//             a = (JSONObject) parser.parse(fReader);
//         } catch (Exception e) {
//             System.err.println("Could not load Opcodes.json");
//         }        
//         // initialize instruction arrays
//         Instruction[] unprefixed = null;
//         Instruction[] cbprefixed = null;
//         // Set<String> mnemonics = new HashSet<>();

//         // iterate through both sets of Strings
//         for (int i = 0; i < 2; i++) {
//             // loop setup
//             Instruction[] instructions;
//             JSONObject o = new JSONObject();

//             if (i == 0) {
//                 o = (JSONObject)a.get("unprefixed");
//                 unprefixed = new Instruction[o.size()];
//                 instructions = unprefixed; 
//             } else {
//                 o = (JSONObject)a.get("cbprefixed");
//                 cbprefixed = new Instruction[o.size()];
//                 instructions = cbprefixed; 
//             }
//             // iter through - works regardless of JSON order
//             for (Object j : o.keySet()) {
//                 String key = j.toString();
//                 // convert key to corresponding index in array and get JSON
//                 int code = Integer.parseUnsignedInt(key.substring(2), 16);
//                 JSONObject jsonInstruction = (JSONObject) o.get(key);
//                 // get attributes
//                 String mnemonic = (String) jsonInstruction.get("mnemonic");
//                 // bytes
//                 int bytes = Integer.parseInt((String) jsonInstruction.get("bytes"));
//                 // immediate
//                 boolean immediate = Boolean.parseBoolean((String) jsonInstruction.get("immediate"));
//                 // read dictionary which holds the number of cycles instruction takes
//                 JSONArray jsonCycles = (JSONArray)jsonInstruction.get("cycles");
//                 int[] cycles = new int[jsonCycles.size()];
//                 for (int k = 0; k < jsonCycles.size(); k++) {
//                     cycles[k] = Integer.parseInt(jsonCycles.get(k).toString());
//                 }
//                 // init operands array
//                 JSONArray jsonOperands = (JSONArray) jsonInstruction.get("operands");

//                 // get operands - sometimes null
//                 Operand lOperand = null;
//                 Operand rOperand = null;
//                 if (jsonOperands.size() > 0) {
//                     JSONObject jsonOperand = (JSONObject)jsonOperands.get(0);
//                     String name = jsonOperand.get("name").toString();
//                     Boolean operandImmediate = Boolean.parseBoolean(jsonOperand.get("immediate").toString());
//                     Boolean increment = false;
//                     if (jsonOperand.containsKey("increment")) {
//                         increment = Boolean.parseBoolean(jsonOperand.get("increment").toString());
//                     }
//                     // int bytes = 0;
//                     // if (jsonOperand.containsKey("bytes")) {
//                     //     bytes = Integer.parseInt(jsonOperand.get("bytes").toString());
//                     // }
//                     lOperand = new Operand(name, operandImmediate, increment);

//                 } 
                
//                 if (jsonOperands.size() > 1){
                
//                     JSONObject jsonOperand = (JSONObject)jsonOperands.get(1);
//                     String name = jsonOperand.get("name").toString();
//                     Boolean operandImmediate = Boolean.parseBoolean(jsonOperand.get("immediate").toString());
//                     Boolean increment = false;
//                     if (jsonOperand.containsKey("increment")) {
//                         increment = Boolean.parseBoolean(jsonOperand.get("increment").toString());
//                     }
//                     // int bytes = 0;
//                     // if (jsonOperand.containsKey("bytes")) {
//                     //     bytes = Integer.parseInt(jsonOperand.get("bytes").toString());
//                     // }
//                     // if (bytes != 0) {
//                     //     System.out.println(name + " has " + bytes + " bytes");
//                     // }
//                     rOperand = new Operand(name, operandImmediate, increment);
//                 }

//                 // initialize new instruction object and add to map
//                 Instruction instruction = new Instruction(mnemonic, lOperand, rOperand, bytes, immediate);

//                 instructions[code] = instruction;
//             }
//         }
//         instructionSet.UNPREFIXED = unprefixed;
//         instructionSet.CBPREFIXED = cbprefixed;

//         return instructionSet;
//     }

//     /**
//     //  * @return
//     //  */
//     // public ArrayList<Op> buildOPs(Instruction i){
//     //     NOP op = new NOP();
//     // ArrayList<Op> ops = new ArrayList<>();
//     // ops.add(op);
//     // return ops;
//     // }
// }
