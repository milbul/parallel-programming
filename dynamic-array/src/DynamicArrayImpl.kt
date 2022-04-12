import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.atomicfu.AtomicRef
import java.util.concurrent.atomic.AtomicInteger

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))

    override fun get(index: Int): E {
        if (index >= size) {
            throw IllegalArgumentException()
        }
        return core.value.array[index].value!!.value
    }

    override fun put(index: Int, element: E) {
        if (index >= size) {
            throw IllegalArgumentException()
        }
        while (true) {
            val prev = core.value.array[index].value
            if (!prev!!.removed && core.value.array[index].compareAndSet(prev, Node(element, false))) {
                return
            }
        }
    }

    override fun pushBack(element: E) {
        while (true) {
            val coreVal = core.value
            val prevSize = coreVal.sz.get()
            if (coreVal.sz.get() == coreVal.size) {
                ensureCapacity(coreVal)
                continue
            }
            if (coreVal.array[prevSize].compareAndSet(null, Node(element, false))) {
                coreVal.sz.getAndIncrement()
                return
            }
        }
    }

    private fun ensureCapacity(prevCore: Core<E>) {
        val curCore = Core<E>(prevCore.size * 2)
        curCore.sz.set(prevCore.size)
        if (prevCore.next.compareAndSet(null, curCore)) {
            for (i in 0 until prevCore.size) {
                while (true) {
                    val element = prevCore.array[i].value ?: continue
                    if (prevCore.array[i].compareAndSet(element, Node(element.value, true))) {
                        curCore.array[i].value = element
                        break
                    }
                }
            }
            core.value = curCore
        }
    }

    override val size: Int
        get() {
            return core.value.sz.get()
        }
}

class Core<E>(val size: Int) {
    val array = atomicArrayOfNulls<Node<E>>(size)
    val next: AtomicRef<Core<E>?> = atomic(null)
    val sz = AtomicInteger(0)
}

class Node<E>(
    val value: E,
    val removed: Boolean,
)

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME