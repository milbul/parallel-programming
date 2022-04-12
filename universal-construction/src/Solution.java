/**
 * @author : Bulkina Milena
 */
    public class Solution implements AtomicCounter {
    // объявите здесь нужные вам поля
    private final Node head = new Node(0);
    private final ThreadLocal<Node> cur = ThreadLocal.withInitial(() -> head);

    public int getAndAdd(int x) {
        // напишите здесь код
        while (true) {
            int old = cur.get().val;
            Node node = new Node(old + x);
            cur.set(cur.get().next.decide(node));
            if (cur.get() == node) {
                return old;
            }
        }
    }

    // вам наверняка потребуется дополнительный класс
    private static class Node {
        private final int val;
        private final Consensus<Node> next = new Consensus<>();

        Node(int val) {
            this.val = val;
        }
    }

}
