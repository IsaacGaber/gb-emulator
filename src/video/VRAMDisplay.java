package video;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.ArrayList;


public class VRAMDisplay extends JPanel {
    private ArrayList<Tile> _tiles;
    private BufferedImage _framebuffer; 
    // private JLabel time;

    public VRAMDisplay() {
        _framebuffer = new BufferedImage(128, 256, BufferedImage.TYPE_3BYTE_BGR);
        // time = new JLabel("Time is: ");
        // this.add(time);
        // Tile testTile = new Tile(new byte[]{0x3C, 0x7E, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 
        //                                     0x7E, 0x5E, 0x7E, 0x0A, 0x7C, 0x56, 0x38, 0x7C});

        _tiles = new ArrayList<>();
        
        // for (int i = 0; i < 256; i++) {
        //     _tiles.add(testTile);
        // }
    
    }

    public void updateTiles(byte[] VRAM) {
        _tiles.clear();

        // iterate through ALL OF video ram
        for (int i = 0; i < VRAM.length / Tile.BYTE_LENGTH; i++) {
            byte[] tileBytes = new byte[Tile.BYTE_LENGTH];
            for (int j = 0; j < Tile.BYTE_LENGTH; j++) {
                tileBytes[j] = VRAM[i * Tile.BYTE_LENGTH + j];
            }
            Tile t = new Tile(tileBytes);
            _tiles.add(t);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(256, 512);
    }

    private void drawTiles(BufferedImage target) {
        int offX, offY;
        offX = offY = 0;

        for (Tile tile : _tiles) {

            Colors[] tileColors = tile.tileColors();
            for (int i = 0; i < tileColors.length; i++) {
                _framebuffer.setRGB(i % Tile.SIZE + offX, i / Tile.SIZE + offY, tileColors[i].COLOR);
            }

            offX += Tile.SIZE;
            if ((offX %= target.getWidth()) == 0) {
                offY += Tile.SIZE;
                offY %= target.getHeight();
            }
        }
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);       
        
        // setBackground(getBackground());
        // updateTiles(_memory.getVramView());
        drawTiles(_framebuffer);
        ((Graphics2D)g).drawImage(_framebuffer.getScaledInstance(getWidth(), getHeight(), 0), 1, 1, null);
        // Draw Text
        // g.drawString("This is my custom Panel!",10,20);
    }  
}