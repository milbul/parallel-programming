import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()
    private val random = Random()
    private val workers = 4
    private val operations = atomicArrayOfNulls<Operation<E?>>(workers)

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        val op = Operation<E?>(Op.POLL, null)
        allOp(op)
        return op.value.get()
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        val op = Operation<E?>(Op.PEEK, null)
        allOp(op)
        return op.value.get()
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        val op = Operation<E?>(Op.ADD, element)
        allOp(op)
    }

    private fun applyFunc(op: Operation<E?>) {
        if (op.done.get()) return
        when (op.type) {
            Op.ADD -> {
                q.add(op.value.get())
            }
            Op.PEEK -> {
                op.value.set(q.peek())
            }
            Op.POLL -> {
                op.value.set(q.poll())
            }
        }
        op.done.set(true)
    }

    private fun allOp(op: Operation<E?>) {
        while (true) {
            val i = random.nextInt(workers)
            if (operations[i].compareAndSet( null, op)) {
                while (true) {
                    if (lock()) {
                        applyFunc(op)
                        combine()
                        unlock()
                    }
                    if (op.done.get()) {
                        operations[i].value = null
                        return
                    }
                }
            }
        }
    }

    private fun combine() {
        for (i in 0 until workers) {
            val op = operations[i].value
            if (op != null) {
                applyFunc(op)
            }
        }
    }

    private val lock = atomic(false)

    private fun lock(): Boolean {
        return lock.compareAndSet(false, true)
    }

    private fun unlock() {
        lock.value = false
    }
}

enum class Op {
    POLL, PEEK, ADD
}

class Operation<E>(op: Op, value: E) {
    val type = op
    val value = AtomicReference(value)
    val done = AtomicBoolean(false)
}