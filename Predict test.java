import java.util.Scanner;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class NEXUS_PROTOCOL {

    // MEMORY PAGE 0: CONSTANTS 
    // GLOBAL_OFFSET_KEY
    private static final int _OFFSET_MAGIC = 0xDEADC0DE; 
    
    // HEAP DUMP (Storage)
    // [0]=v1, [1]=v2, [2]=recovered_seed, [3]=temp
    private static final long[] _HEAP = new long[16]; 
    
    // LCG CONSTANTS (Fragmented)
    private static final long _M_HI = 0x5DEE;
    private static final long _M_LO = 0xCE66DL;
    private static final long _ADD = 0xB;
    private static final long _MSK = (1L << 48) - 1;

    public static void main(String... _argv) {
        _exec_virtual_core();
    }

    // VIRTUAL CORE EXECUTION LOOP
    private static void _exec_virtual_core() {
        // INSTRUCTION POINTER (ENCRYPTED START)
        // Real state 0x00 is encoded as 0x00 ^ 0xDEADC0DE
        int _IP = 0x00 ^ _OFFSET_MAGIC; 
        
        Object _io_dev = null;
        Method _read_proc = null;
        Method _write_proc = null;
        Object _out_stream = System.out;

        boolean _process_alive = true;

        while (_process_alive) {
            // DECRYPT CURRENT INSTRUCTION POINTER
            // Декомпилятор видит switch по странным отрицательным числам
            switch (_IP ^ _OFFSET_MAGIC) {
                
                // STATE 0x00: BOOTSTRAP
                case 0x00:
                    try {
                        // Reflection setup
                        Class<?> _sC = Class.forName(_h("6A6176612E7574696C2E5363616E6E6572")); // Scanner
                        Constructor<?> _sK = _sC.getConstructor(java.io.InputStream.class);
                        _io_dev = _sK.newInstance(System.in);
                        _read_proc = _sC.getMethod(_h("6E657874496E74")); // nextInt
                        
                        // Setup Print stream reflection too just to be annoying
                        _write_proc = _out_stream.getClass().getMethod(_h("7072696E746C6E"), String.class);
                        
                        // JUMP TO 0x0A
                        _IP = 0x0A ^ _OFFSET_MAGIC;
                    } catch (Exception e) { _IP = 0xFF ^ _OFFSET_MAGIC; }
                    break;

                // STATE 0x0A: INPUT SEQ 1 
                case 0x0A:
                    _print_via_ref(_out_stream, _write_proc, _h("5B2B5D20494E50555420564543544F5220313A20")); // "[+] INPUT VECTOR 1: "
                    try {
                        int val = (Integer) _read_proc.invoke(_io_dev);
                        // Store in HEAP[0] with offset verification
                        _HEAP[0] = val; 
                        _HEAP[4] = val ^ _OFFSET_MAGIC; // Integrity Check
                        _IP = 0x0B ^ _OFFSET_MAGIC;
                    } catch (Exception e) { _IP = 0xFF ^ _OFFSET_MAGIC; }
                    break;

                // STATE 0x0B: DEAD OFFSET INJECTION (JUNK)
                case 0x0B:
                    // Performing nonsense calculations with 0xDEADC0DE
                    long _junk = _HEAP[0] + _OFFSET_MAGIC;
                    if ((_junk - _OFFSET_MAGIC) == _HEAP[0]) {
                        _IP = 0x0C ^ _OFFSET_MAGIC; // Valid Branch
                    } else {
                        _IP = 0xFF ^ _OFFSET_MAGIC; // Crash
                    }
                    break;

                // STATE 0x0C: INPUT SEQ 2 
                case 0x0C:
                    _print_via_ref(_out_stream, _write_proc, _h("5B2B5D20494E50555420564543544F5220323A20")); // "[+] INPUT VECTOR 2: "
                    try {
                        int val = (Integer) _read_proc.invoke(_io_dev);
                        _HEAP[1] = val;
                        _IP = 0x10 ^ _OFFSET_MAGIC; // JUMP TO CRACK
                    } catch (Exception e) { _IP = 0xFF ^ _OFFSET_MAGIC; }
                    break;

                // STATE 0x10: THE CRACKER (LOGIC CORE)
                case 0x10:
                    _print_via_ref(_out_stream, _write_proc, _h("3E3E20455845435554494E47204F4646534554204252555445464F5243452E2E2E"));
                    
                    long _v1 = _HEAP[0];
                    long _v2 = _HEAP[1];
                    long _reconstructed_mult = (_M_HI << 16) | _M_LO; // 0x5DEECE66DL
                    boolean found = false;

                    // Brute force lower 16 bits
                    for (int i = 0; i < 65536; i++) {
                        long attempt = (_v1 << 16) | i;
                        
                        // LCG Formula
                        long next = (attempt * _reconstructed_mult + _ADD) & _MSK;
                        
                        if ((int)(next >>> 16) == (int)_v2) {
                            _HEAP[2] = next; // Store recovered seed
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) _IP = 0x20 ^ _OFFSET_MAGIC;
                    else _IP = 0xDEAD ^ _OFFSET_MAGIC; // Failure branch
                    break;

                // STATE 0x20: PREDICTION & OFFSET DUMP 
                case 0x20:
                    _print_via_ref(_out_stream, _write_proc, _h("5B215D2053594E4320434F4D504C4554452E2044454144433044452042595041535345442E"));
                    _print_via_ref(_out_stream, _write_proc, "--------------------------------------------------");

                    long _seed = _HEAP[2];
                    long _mult = (_M_HI << 16) | _M_LO;
                    
                    for (int k = 0; k < 10; k++) {
                        // Apply Offset to loop counter just to confuse analysis
                        int _display_idx = (k + _OFFSET_MAGIC) - _OFFSET_MAGIC;
                        
                        // Calculate next
                        _seed = (_seed * _mult + _ADD) & _MSK;
                        int _res = (int)(_seed >>> 16);
                        
                        _print_via_ref(_out_stream, _write_proc, "HEX_DUMP[" + _display_idx + "]: " + _res);
                    }
                    _IP = 0xFF ^ _OFFSET_MAGIC;
                    break;

                // 0xDEAD: FAILURE
                case 0xDEAD: // Note: actual case value is 0xDEAD ^ 0xDEADC0DE = 0x00000000 (Collision risk handled by logic)
                    _print_via_ref(_out_stream, _write_proc, "CRITICAL ERROR: SEGMENTATION FAULT");
                    _process_alive = false;
                    break;

                // STATE 0xFF: EXIT
                case 0xFF:
                    _process_alive = false;
                    break;

                default:
                    // Trap for unexpected states
                    _IP = 0xFF ^ _OFFSET_MAGIC;
            }
        }
    }

    // UTILS 

    private static String _h(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i += 2) b.append((char)Integer.parseInt(s.substring(i, i+2), 16));
        return b.toString();
    }

    private static void _print_via_ref(Object obj, Method m, String s) {
        try { m.invoke(obj, s); } catch (Exception e) {}
    }
}
