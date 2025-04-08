/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author : Nochevkina Natalia
 */
class Solution : MonotonicClock {
    private var f1 by RegularInt(0)
    private var f2 by RegularInt(0)
    private var f3 by RegularInt(0)

    private var s1 by RegularInt(0)
    private var s2 by RegularInt(0)



    override fun write(time: Time) {
        s1 = time.d1
        s2 = time.d2
        f3 = time.d3
        f2 = s2
        f1 = s1
    }

    override fun read(): Time {
        val tf1 = f1
        val tf2 = f2
        val tf3 = f3
        val ts2 = s2
        val ts1 = s1
        if (tf2 != ts2) return Time(ts1, ts2, 0)
        if (tf1 != ts1) return Time(ts1, 0, 0)
        return Time(tf1, tf2, tf3)
    }
}