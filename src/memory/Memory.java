package memory;
import java.io.File;
import java.io.FileInputStream;

public class Memory {
    private static final int ADDRESS_SPACE = 65536;
    private static enum Area {BIOS, ROM0, ROM1, VRAM, ERAM, WRAM0, WRAM1, ECHO, OAM, IO, HRAM, IE, IF, NONE};

    private byte[] _ram = new byte[ADDRESS_SPACE];
    private byte[] _bios = new byte[256];
    private Area _at = Area.NONE;

    private boolean inBios = true;
    
    public Memory() {
        loadBIOS();
    }

    public void loadBIOS() {
        // load boot rom into memory
        try {
            File f = new File("src\\dmg_boot.bin");
            FileInputStream input = new FileInputStream(f);
            for (int i = 0; i < _bios.length; i++) {
                _bios[i] = (byte) input.read();
            }
            input.close();
        } catch (Exception e) {
            System.err.println("Could not load BIOS file.");
        }
    }

    // shadow WRAM not emulated
    public int getByte(int addr) {
        update(addr);
        if (inBios && addr < _bios.length) {
            return Byte.toUnsignedInt(_bios[addr]);
        } else {
            return Byte.toUnsignedInt(_ram[addr]);
        }
    }

    public void setByte(int addr, int b) {
        update(addr);
        _ram[addr] = (byte) b;
    }

    // update location of "_at"
    public void update(int addr) {
        _at = switch (addr & 0xF000) {
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
    }

    public int getWord(int addr) {
        return (getByte(addr) << 8 | getByte(addr+1));
    }

    public String lastArea() {
        return _at.name();
    }

    public String toString() {
        return lastArea();
    }
}
