package memory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import util.BitUtil;
import util.Util;

public class Memory {
    private static final int ADDRESS_SPACE = 65536;
    private static final int VRAM_START = 0x8000;
    private static final int VRAM_END = 0xA000;
    private static enum Area { BIOS, ROM0, ROM1, VRAM, ERAM, WRAM0, WRAM1, ECHO, OAM, IO, HRAM, IE, IF, NONE};

    private byte[] _ram;
    private byte[] _bios;
    private Area _at;

    private boolean inBios;
    
    public Memory(String romPath) {
        Random random = new Random();
        _ram = new byte[ADDRESS_SPACE];
        for (int i = 0; i < _ram.length; i++) {
            _ram[i] = (byte) random.nextInt();
        }
        _bios = new byte[256];
        _at = Area.NONE;

        // set VBLANK to true for debugging purposes
        _ram[0xFF44] = (byte)0x90;
        // 
        _ram[0xFF42] = (byte)0x64;

        loadBIOS();
        loadROM(romPath);
        
        // should load a bunch of sprites right in the middle of Vram
        // byte[] testTile = new byte[]{0x3C, 0x7E, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 
        //                             0x7E, 0x5E, 0x7E, 0x0A, 0x7C, 0x56, 0x38, 0x7C};
        // for (int i = 0x8800; i < 0x9800; i++) {
        //     _ram[i] = testTile[i % testTile.length];
        // }


    }

    public void loadBIOS() {
        // load boot rom into memory
        try {
            File f = new File("assets/dmg-boot.bin");
            FileInputStream input = new FileInputStream(f);
            for (int i = 0; i < _bios.length; i++) {
                _bios[i] = (byte) input.read();
            }
            inBios = true;
            input.close();
        } catch (Exception e) {
            System.err.println("Could not load BIOS file.");
        }
    }

    public void loadROM(String romPath){
        try {
            File f = new File(romPath);
            FileInputStream input = new FileInputStream(f);
            // reads 32KiB into the ROM banks
            for (int i = 0; i < 0x8000; i++) {
                _ram[i] = (byte) input.read();
            }
            input.close();
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Could not load ROM file"); 
        }

    }

    // shadow WRAM not emulated
    public int getByte(int addr) {
        _at = ramArea(addr);
        if (inBios && addr < _bios.length) {
            return Byte.toUnsignedInt(_bios[addr]);
        } else {
            // System.out.println("Getting value: " + Util.byteToHexstring(_ram[addr]) + " at address: " +  Util.wordToHexstring(addr));
            return Byte.toUnsignedInt(_ram[addr]);
        }
    }

    public int getWord(int addr) {
        return (getByte(addr) | getByte(addr+1) << 8);
    }


    public void setByte(int addr, int b) {
        _at = ramArea(addr);
        // System.out.println("setting address: " + Util.wordToHexstring(addr) + " to value: " + Util.byteToHexstring(b));
        if (_at == Area.ROM0 || _at == Area.ROM1) {
            throw new RuntimeException("Cannot modify ROM");
        }
        _ram[addr] = (byte) b;
    }

    // update location of "_at"
    public Area ramArea(int addr) {
        Area area = switch (addr & 0xF000) {
            // mask out lower bits
            // 256 byte BIOS / start of 16 KiB ROM 0
            case 0x0000:
                if (inBios && addr < _bios.length) {
                    yield Area.BIOS;
                } else {
                    yield Area.ROM0;
                }
            // the rest of 16 KiB ROM 0
            case 0x1000, 0x2000, 0x3000: 
                yield Area.ROM0;
            // 16 KiB ROM 1
            case 0x4000, 0x5000, 0x6000, 0x7000:
                yield Area.ROM1;
            // 8 KiB VRAM
            case 0x8000, 0x9000:
                yield Area.VRAM;
            // 8 KiB EXTERNAL CARTRIDGE RAM
            case 0xA000, 0xB000:
                yield Area.ERAM;
            // 4KiB work RAM 0
            case 0xC000:
                yield Area.WRAM0;
            // 4KiB work RAM 1, In CGB mode, switchable bank 1–7
            case 0xD000:
                yield Area.WRAM1;
            // Echo RAM (unemulated), I/O, OAM, Zero-Page:
            case 0xF000:
                switch (addr & 0x0F00) {
                    // OAM is 160 bytes, remaining bytes read as 0
                    case 0xE00:
                        yield Area.OAM;
                        // if (addr < 0xFEA0) {
                        //     return _ram[addr];
                        // } else {
                        //     return 0;
                        // }
                    // High-Speed Zero-page
                    case 0xF00:
                        //HRAM
                        if (addr >= 0xFF80) {
                            yield Area.HRAM;
                        } else {
                            // IO and interrupts
                            switch (addr) {
                                case 0xFFFF:
                                    yield Area.IE;
                                case 0xFFF0:
                                    yield Area.IF;
                                default:
                                    yield Area.IO;
                            }
                        }
                    default:
                        yield Area.ECHO;
                }
            default:
                yield Area.NONE;
        };
        return area;
    }

    // access drawing related registers
    public int getLCDC(){
        return _ram[0xFF40];
    }

    public void setLCDC(int i){
        _ram[0xFF40] = (byte) i;
    }

    // set background and windows tile data, false = 8800–97FF; true = 8000–8FFF
    public void setBGWTileData(boolean b){
        int i = BitUtil.setBit(_ram[0xFF40], 4, b);
        _ram[0xFF40] = (byte) i;
    }
    
    // get background and windows tile data, false = 8800–97FF; true = 8000–8FFF
    public boolean getBGWTileData(){
        return BitUtil.getBit(_ram[0xFF40], 4);
    }


    public int getLY() {
        return _ram[0xFF44];
    }

    public void setLY(int i) {
        _ram[0xFF44] = (byte) i;
    }

    public int getLYC() {
        return _ram[0xFF45];
    }
    // bg scroll Y
    public int getSCY() {
        return _ram[0xFF42];
    }

    public void setSCY(int i) {
        _ram[0xFF42] = (byte) i;
    }

    // bg scroll Y
    public int getSCX() {
        return _ram[0xFF43];
    }
    
    public void setSCX(int i) {
        _ram[0xFF43] = (byte) i;
    }
    


    public String lastArea() {
        return _at.name();
    }

    public String toString() {
        return lastArea();
    }

    public byte getVram(int i) {
        Area a = ramArea(i);
        if (a == Area.VRAM) {
            return _ram[i];
        } else {
            throw new IndexOutOfBoundsException("Index: " + i + " not in VRAM");
        }
    }

    public byte[] getVramView() {
        byte[] view = new byte[VRAM_END - VRAM_START];
        for (int i = VRAM_START; i < VRAM_END; i++) {
            view[i - VRAM_START] = _ram[i];
        }
        return view;
    }
}
