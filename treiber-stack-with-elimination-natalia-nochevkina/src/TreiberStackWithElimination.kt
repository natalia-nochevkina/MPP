import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * @author Nochevkina Natalia
 */
open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()
    private val eliminationArray = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushElimination(element: E): Boolean {
        val cell = randomCellIndex()
        if (! eliminationArray.compareAndSet(cell, CELL_STATE_EMPTY, element)) return false
        for (i: Int in 0..ELIMINATION_WAIT_CYCLES) {
            if (eliminationArray.compareAndSet(cell, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) return true
        }
        return eliminationArray.getAndSet(cell, CELL_STATE_EMPTY) == CELL_STATE_RETRIEVED
    }

    override fun pop(): E? = tryPopElimination() ?: stack.pop()

    @Suppress("UNCHECKED_CAST")
    private fun tryPopElimination(): E? {
        val cell = randomCellIndex()
        val el: Any? = eliminationArray.getAndSet(cell, CELL_STATE_RETRIEVED)
        if (el == CELL_STATE_RETRIEVED) return null
        el ?: eliminationArray.set(cell, CELL_STATE_EMPTY)
        return el as E
    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(eliminationArray.length())

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 2 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        // Initially, all cells are in EMPTY state.
        private val CELL_STATE_EMPTY = null

        // `tryPopElimination()` moves the cell state
        // to `RETRIEVED` if the cell contains element.
        private val CELL_STATE_RETRIEVED = Any()
    }
}
