package video;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import memory.Memory;

// pixel processing unit, main class of package
public class Video {
    private Memory _memory; 
    
    @SuppressWarnings("unused")
    private Video(){};


    public Video(Memory memory) {
        _memory = memory;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showDisplay(_memory);
                showVramDisplay(_memory); 

            }});

    }


    private void showDisplay(Memory memory) {
        JFrame f = new JFrame("gb-emulator");
        Display d = new Display(memory);
        int delay = 16; // milliseconds roughly to 60 FPS
    
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                d.repaint();
            }
        };
        new Timer(delay, actionListener).start();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(d);
        f.pack();
        f.setResizable(false);
        f.setVisible(true);


    }


    private void showVramDisplay(Memory memory) {
        JFrame f = new JFrame("VRAM tile view");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowStateListener(null);
        VRAMDisplay d = new VRAMDisplay();

        int delay = 100; // milliseconds equivalent to 10 FPS
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                d.updateTiles(_memory.getVramView());
                d.repaint();
            }
        };
        new Timer(delay, actionListener).start();

        f.add(d);
        f.pack();
        f.setResizable(false);
        // d.repaint();
        f.setVisible(true);
    }
    
}
