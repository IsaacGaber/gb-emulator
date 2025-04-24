package dmg;
import java.io.FileInputStream;
import java.util.Scanner;

import cpu.CPU;
import memory.Memory;

public class DMG {

    public static void main(String[] args) throws Exception {
        String romPath;
        Scanner in = new Scanner(System.in);
        // load file to disassemble 
        // assumes file will be gameboy binary
        while (true) {
            try {
                System.out.print("Input the path of the ROM you would like to run: ");
                romPath = in.nextLine();
                break;
            } catch (Exception e) {
                System.out.println("Invalid file path");
            }
        }

        Memory memory = new Memory(romPath);
        // CPU must be initialized with reference to memory -- imagine conduits connecting elements
        CPU cpu = new CPU(memory);

        // handle different run modes
        System.out.println("Would you like to step through the ROM or run until Halted?\nType (S) for Step Mode and (R) for Run Mode");
        String input = in.nextLine().toLowerCase();
        if (input.equals("s")) {
            boolean answer = true;
            while (answer && cpu.running()) {
                cpu.step();
                System.out.print("Continue stepping CPU? (Y/N): ");
                answer = in.nextLine().toLowerCase().equals("y");    
            }
        } else if(input.equals("r")) {
            while (cpu.running()) {
                cpu.step();
            }
        }
        in.close();
        System.out.println("Exiting");
    }

}
