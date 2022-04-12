import org.jetbrains.annotations.NotNull;

/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author : Bulkina Milena
 */
public class Solution implements MonotonicClock {
    private final RegularInt c1 = new RegularInt(0);
    private final RegularInt c2 = new RegularInt(0);
    private final RegularInt c3 = new RegularInt(0);
    private final RegularInt b1 = new RegularInt(0);
    private final RegularInt b2 = new RegularInt(0);

    @Override
    public void write(@NotNull Time time) {
        // write right-to-left
        c1.setValue(time.getD1());
        c2.setValue(time.getD2());
        c3.setValue(time.getD3());

        b2.setValue(time.getD2());
        b1.setValue(time.getD1());
    }

    @NotNull
    @Override
    public Time read() {
        // read left-to-right
        int b11 = b1.getValue();
        int b12 = b2.getValue();

        int c13 = c3.getValue();
        int c12 = c2.getValue();
        int c11 = c1.getValue();
        int d1, d2 = 0, d3 = 0;
        d1 = c11;
        if (b11 == c11) {
            d2 = c12;
            if (b12 == c12) {
                d3 = c13;
            }
        }
        return new Time(d1, d2, d3);
    }
}
