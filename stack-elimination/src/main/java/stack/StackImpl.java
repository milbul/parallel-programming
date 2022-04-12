package stack;

import kotlinx.atomicfu.AtomicRef;
import kotlinx.atomicfu.AtomicArray;
import java.util.Random;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private final int ARRAY_SIZE = 50;
    private final int SPIN_WAIT = 30;
    private final int AROUND = 7;
    private final Random random = new Random();

    private final AtomicRef<Node> head = new AtomicRef<>(null);
    private final AtomicArray<Integer> eliminationArray = new AtomicArray<>(ARRAY_SIZE);

    @Override
    public void push(int x) {
        int ind = random.nextInt(ARRAY_SIZE);
        int l = Math.max(0, ind - AROUND);
        int r = Math.min(ind + AROUND, ARRAY_SIZE);
        for (int i = l; i < r; i++) {
            Integer val = x;
            if (eliminationArray.get(i).compareAndSet(null, val)) {
                for (int wait = 0; wait < SPIN_WAIT; wait++) {
                    if (eliminationArray.get(i).getValue() == null) {
                        return;
                    }
                }
                if (eliminationArray.get(i).compareAndSet(val, null)) {
                    break;
                }
                return;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            if (head.compareAndSet(curHead, new Node(x, curHead))) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        int ind = random.nextInt(ARRAY_SIZE);
        int l = Math.max(0, ind - AROUND);
        int r = Math.min(ind + AROUND, ARRAY_SIZE);
        for (int i = l; i < r; i++) {
            Integer val = eliminationArray.get(i).getValue();
            if (val != null && eliminationArray.get(i).compareAndSet(val, null)) {
                return val;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            if (curHead == null) {
                return Integer.MIN_VALUE;
            }
            if (head.compareAndSet(curHead, curHead.next.getValue())) {
                return curHead.x;
            }
        }
    }
}
