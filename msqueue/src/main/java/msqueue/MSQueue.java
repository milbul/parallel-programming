package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(Integer.MIN_VALUE, new AtomicRef<Node>(null));
        this.head = new AtomicRef<>(dummy);
        this.tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x, new AtomicRef<Node>(null));
        while (true) {
            Node curTail = tail.getValue();
            AtomicRef<Node> tailNext = curTail.next;
            if (tailNext.compareAndSet(null, newTail)) {
                tail.compareAndSet(curTail, newTail);
                return;
            } else {
                tail.compareAndSet(curTail, curTail.next.getValue());
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node curHead = head.getValue();
            Node curTail = tail.getValue();
            Node headNext = curHead.next.getValue();
            if (headNext == null) {
                return Integer.MIN_VALUE;
            }
            if (curHead == curTail) {
                tail.compareAndSet(curTail, curTail.next.getValue());
            } else if (head.compareAndSet(curHead, headNext)) {
                return headNext.x;
            }
        }
    }

    @Override
    public int peek() {
        Node curHead = head.getValue();
        Node curTail = tail.getValue();
        Node curHeadNext = curHead.next.getValue();
        if (curHead == curTail) {
            return Integer.MIN_VALUE;
        }
        return curHeadNext.x;
    }

    private class Node {
        final int x;
        AtomicRef<Node> next;

        Node(int x, AtomicRef<Node> next) {
            this.x = x;
            this.next = next;
        }
    }
}