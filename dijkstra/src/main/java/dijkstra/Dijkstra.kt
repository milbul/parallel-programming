package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import kotlin.Comparator
import kotlin.concurrent.thread
import kotlinx.atomicfu.atomic

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = PriorityMultiQueue(workers, NODE_DISTANCE_COMPARATOR)
    q.add(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while (true) {
                val cur: Node? = q.poll()
                if (cur == null) {
                    if (q.size.value == 0) break else continue
                }
                for (e in cur.outgoingEdges) {
                    val newDist = cur.distance + e.weight
                    while (true) {
                        val curDist = e.to.distance
                        if (curDist <= newDist) {
                            break
                        }
                        if (e.to.casDistance(curDist, newDist)) {
                            q.add(e.to)
                            break
                        }
                    }
                }
                q.size.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

private class PriorityMultiQueue(val workers: Int, comparator: Comparator<Node>) {
    val qs: MutableList<PriorityQueue<Node>> = Collections.nCopies(workers, PriorityQueue(comparator))
    val size = atomic(0)
    val random = Random(0)

    fun poll(): Node? {
        var i1 = random.nextInt(workers)
        var i2 = random.nextInt(workers)
        while (i1 == i2) {
            i1 = random.nextInt(workers)
            i2 = random.nextInt(workers)
        }
        synchronized(qs[i1]) {
            synchronized(qs[i2]) {
                if (qs[i1].peek() == null) {
                    return qs[i2].peek()
                }
                if (qs[i2].peek() == null) {
                    return qs[i1].peek()
                }
                return if (qs[i1].peek().distance < qs[i2].peek().distance) {
                    qs[i1].poll()
                } else {
                    qs[i2].poll()
                }
            }
        }
    }

    fun add(element: Node) {
        val i = random.nextInt(workers)
        synchronized(qs[i]) {
            qs[i].add(element)
        }
        size.incrementAndGet()
    }
}