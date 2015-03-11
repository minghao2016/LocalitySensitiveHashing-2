/**
 *
 * @author Tharindu Kumara
 *
 * Implementation of the FNV1 hashing function
 *
 */
public class FNV1 {
	private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 16777619;
    
    public static int hashfnv1(int[] k) {
        int rv = FNV_32_INIT;
        final int len = k.length;
        for(int i = 0; i < len; i++) {
            rv ^= k[i];
            rv *= FNV_32_PRIME;
        }
        return rv;
    }
    
    public static int hashfnv1(String k) {
        int rv = FNV_32_INIT;
        final int len = k.length();
        for(int i = 0; i < len; i++) {
            rv ^= k.charAt(i);
            rv *= FNV_32_PRIME;
        }
        return rv;
    }
}
