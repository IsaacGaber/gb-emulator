package video;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import memory.Memory;

// pixel processing unit, main class of package
public class PPU {
    private Memory _memory; 
    @SuppressWarnings("unused")
    private PPU(){};


    public PPU(Memory memory) {
        _memory = memory;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showVramDisplay(_memory); 
            }});

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
