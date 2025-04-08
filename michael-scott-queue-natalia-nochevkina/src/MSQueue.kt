import java.util.concurrent.atomic.*

/**
 * @author Nochevkina Natalia
 */
class MSQueue<E> : Queue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>

    init {
        val dummy = Node<E>(null)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        val newTail = Node(element)
        while (true) {
            val tailCurr = tail.get()
            if (tailCurr.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(tailCurr, newTail)
                return
            } else {
                tail.compareAndSet(tailCurr, tailCurr.next.get())
            }
        }
    }

    override fun dequeue(): E? {
        while (true) {
            val headCurr = head.get()
            val tailCurr = tail.get()
            val nextHead = headCurr.next.get()
            if (headCurr == head.get()){
                if (headCurr == tailCurr) {
                    nextHead ?: return null
                    tail.compareAndSet(tailCurr, nextHead)
                } else if (head.compareAndSet(headCurr, nextHead)) {
                    val el = nextHead?.element
                    nextHead?.element = null
                    return el
                }
            }
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    override fun validate() {
        check(tail.get().next.get() == null) {
            "At the end of the execution, `tail.next` must be `null`"
        }
        check(head.get().element == null) {
            "At the end of the execution, the dummy node shouldn't store an element"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}
