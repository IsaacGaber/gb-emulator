package dmg;

import java.util.Scanner;

import cpu.CPU;
import memory.Memory;
import video.PPU;

public class DMG {
    // timings in hertz
    private final int CPU_CLOCK = 4294304;
    private final int OSCILLATOR_CLOCK = 1048576;
    public static void main(String[] args) throws Exception {
        String romPath;
        Scanner in = new Scanner(System.in);
        // load file to run 
        // assumes file will be gameboy binary
        // while (true) {
        //     try {
        //         System.out.print("Input the path of the ROM you would like to run: ");
        //         romPath = in.nextLine();
        //         break;
        //     } catch (Exception e) {
        //         System.out.println("Invalid file path");
        //     }
        // }
        romPath = "assets\\hello-world.gb";
        Memory memory = new Memory(romPath);
        // init vram display
        PPU ppu = new PPU(memory);
        // CPU must be initialized with reference to memory -- imagine conduits connecting elements
        CPU cpu = new CPU(memory);

    
        // handle different run modes
        System.out.println("Would you like to step through the ROM or run until Halted?\nType (S) for Step Mode and (R) for Run Mode\nType anything else to exit:");
        String input = in.nextLine().toLowerCase();
        if (input.equals("s")) {
            boolean answer = true;
            while (answer && cpu.running()) {
                cpu.step();
                System.out.println(cpu);
                System.out.print("Continue stepping CPU? Y/N/R(run for 100 steps)/RR(run for 1000 steps): ");

                input = in.nextLine().toLowerCase();
                if (input.equals("r")) {
                    for (int i = 0; i < 100; i++) {
                        cpu.step();
                    }
                } else if(input.equals("rr")) {
                    for (int i = 0; i < 1000; i++) {
                        cpu.step();
                    }
                } else {
                    answer = input.equals("y");    
                }
            }
        } else if(input.equals("r")) {
            while (cpu.running()) {
                cpu.step();
                // System.out.println(cpu);
            }
        }


        in.close();
        System.out.println("Exiting");
        System.exit(0);
    }
    

}
