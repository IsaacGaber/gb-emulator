package video;

import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import memory.Memory;

// pixel processing unit, main class of package
public class PPU {

    @SuppressWarnings("unused")
    private PPU(){};

    private Memory _memory; 

    public PPU(Memory memory) {
        _memory = memory;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showDisplay(); 
            }});
    }

    public byte getVram(int i) {
        if (i >= 0 && i < Memory.VRAM_SIZE) {
            return (byte) _memory.getByte(i + 0x8000);
        } else {
            throw new IndexOutOfBoundsException(i);
        }
    }

    private void showDisplay() {
        JFrame f = new JFrame("VRAM tile view");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowStateListener(null);
        Display d = new Display(this);
        f.add(d);
        f.pack();
        f.setResizable(false);
        // d.repaint();
        f.setVisible(true);
    }
    
}
