package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {

    private final Exist head = new Exist(Integer.MIN_VALUE, new Exist(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        retry:
        while (true) {
            Window w = new Window();
            w.cur = head;
            w.next = (Exist) w.cur.next.getValue();
            while (w.next.x < x) {
                Node node = w.next.next.getValue();
                if (node instanceof Removed) {
                    Exist next = ((Removed) node).node;
                    if (!w.cur.next.compareAndSet(w.next, next)) {
                        continue retry;
                    }
                    w.next = next;
                } else {
                    w.cur = w.next;
                    w.next = (Exist) node;
                }
            }
            return w;
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x == x && w.next.next.getValue() instanceof Exist) {
                return false;
            } else {
                if (w.cur.next.compareAndSet(w.next, new Exist(x, w.next))) {
                    return true;
                }
            }
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x != x) {
                return false;
            }
            Node node = w.next.next.getValue();
            if (node instanceof Removed) {
                return false;
            }
            Node rem = new Removed((Exist) node);
            if (w.next.next.compareAndSet(node, rem)) {
                w.cur.next.compareAndSet(w.next, node);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        return w.next.x == x && !(w.next.next.getValue() instanceof Removed);
    }

    private class Window {
        Exist cur, next;
    }

    private interface Node {
    }

    private class Exist implements Node {
        AtomicRef<Node> next;
        int x;

        Exist(int x, Exist next) {
            this.next = new AtomicRef<Node>(next);
            this.x = x;
        }
    }

    private class Removed implements Node {
        final Exist node;

        Removed(Exist node) {
            this.node = node;
        }
    }
}