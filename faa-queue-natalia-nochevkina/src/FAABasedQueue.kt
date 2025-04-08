import java.util.concurrent.atomic.*

/**
 * @author Nochevkina Natalia
 *
 */
class FAABasedQueue<E> : Queue<E> {
    private val enqIdx = AtomicLong(0)
    private val deqIdx = AtomicLong(0)
    private val tail: AtomicReference<Segment?>
    private val head: AtomicReference<Segment?>

    init {
        val segment = Segment(0)
        tail = AtomicReference(segment)
        head = AtomicReference(segment)
    }


    override fun enqueue(element: E) {
        do {
            val index = enqIdx.getAndIncrement().toInt()
        } while (! compAndSet(index, null, element))
    }

    @Suppress("UNCHECKED_CAST")
    override fun dequeue(): E? {
        while (true) {
            if (!shouldTryToDequeue()) return null
            val index = deqIdx.getAndIncrement().toInt()
            if (compAndSet(index, null, POISONED)) continue
            val result = getEl(index) as E
            setEl(index, null)
            return result
        }
    }

    private fun shouldTryToDequeue(): Boolean {
        while(true) {
            val curEnqIdx = enqIdx.get()
            val curDeqIdx = deqIdx.get()
            if (curEnqIdx != enqIdx.get()) continue
            return curDeqIdx < curEnqIdx
        }
    }

    private fun getSegment(idx: Int): AtomicReference<Segment?> {
        val number = idx / SEGMENT_SIZE
        var segment = tail
        while (segment.get()?.id != number.toLong()) {
            segment = segment.get()!!.next()
        }
        return segment
    }

    private fun localIndex(idx: Int): Int {
        return idx % SEGMENT_SIZE
    }

    private fun compAndSet(idx: Int, exp: Any?, set: Any?): Boolean {
        return getSegment(idx).get()!!.cells.compareAndSet(localIndex(idx), exp, set)
    }

    private fun getEl(idx: Int): Any? {
        return getSegment(idx).get()!!.cells.get(localIndex(idx))
    }

    private fun setEl(idx: Int, el: Any?) {
        return getSegment(idx).get()!!.cells.set(localIndex(idx), el)
    }

    private val POISONED = Any()
}

private class Segment(val id: Long) {
    val next = AtomicReference<Segment?>(null)
    val cells = AtomicReferenceArray<Any?>(SEGMENT_SIZE)
    fun next(): AtomicReference<Segment?> {
        next.compareAndSet(null, Segment(id + 1))
        return next
    }
}

// DO NOT CHANGE THIS CONSTANT
private const val SEGMENT_SIZE = 2
