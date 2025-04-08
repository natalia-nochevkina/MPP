import java.util.concurrent.atomic.*
import kotlin.math.*

/**
 * @author Nochevkina Natalia
 */
class FAABasedQueueSimplified<E> : Queue<E> {
    private val infiniteArray = AtomicReferenceArray<Any?>(100) // conceptually infinite array
    private val enqIdx = AtomicLong(0)
    private val deqIdx = AtomicLong(0)

    override fun enqueue(element: E) {
        do {
            val index = enqIdx.getAndIncrement().toInt()
        } while (! infiniteArray.compareAndSet(index, null, element))
    }

    @Suppress("UNCHECKED_CAST")
    override fun dequeue(): E? {
        while (true) {
            if (!shouldTryToDequeue()) return null
            val index = deqIdx.getAndIncrement().toInt()
            if (infiniteArray.compareAndSet(index, null, POISONED)) continue
            val result = infiniteArray.get(index) as E
            infiniteArray.set(index, null)
            return result
        }
    }

    override fun validate() {
        for (i in 0 until min(deqIdx.get().toInt(), enqIdx.get().toInt())) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `deqIdx = ${deqIdx.get()}` at the end of the execution"
            }
        }
        for (i in max(deqIdx.get().toInt(), enqIdx.get().toInt()) until infiniteArray.length()) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `enqIdx = ${enqIdx.get()}` at the end of the execution"
            }
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
}

private val POISONED = Any()
