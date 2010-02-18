package jidnet.idnet;

public class DeterminantBits {

    /** Bitmask with 1 for bit being determinant, 0 for not */
    public int mask = 0;
    /** Values of bits (those, which are not determinant are ignored */
    public int values = 0;

    public DeterminantBits(int mask, int values) {
        super();
        this.mask = mask;
        this.values = values;
    }

    public DeterminantBits() {
        super();
    }
}
