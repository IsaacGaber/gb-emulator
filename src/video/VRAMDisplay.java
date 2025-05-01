package video;

import javax.swing.*;

import memory.Memory;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.ArrayList;


public class VRAMDisplay extends JPanel {
    private ArrayList<Tile> _tiles;
    private BufferedImage _framebuffer; 
    private Memory _memory;

    public VRAMDisplay(Memory memory) {
        _memory = memory;
        _framebuffer = new BufferedImage(128, 256, BufferedImage.TYPE_3BYTE_BGR);
        _tiles = new ArrayList<>();    
    }

    public void updateTiles() {
        _tiles.clear();
        byte[] VRAM = _memory.getVramView();
        // iterate through all tiles in video RAM
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
        updateTiles();
        drawTiles(_framebuffer);
        ((Graphics2D)g).drawImage(_framebuffer.getScaledInstance(getWidth(), getHeight(), 0), 0, 0, null);
    }  
}