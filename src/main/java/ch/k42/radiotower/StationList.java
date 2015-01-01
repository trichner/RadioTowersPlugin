package ch.k42.radiotower;

/**
 * Created on 01.01.2015.
 *
 * @author Thomas
 */
public class StationList<T> {

    private Entry<T> head = null;
    private Entry<T> tail = null;

    public Entry<T> head(){
        return head;
    }

    public Entry<T> push(T element){
        Entry<T> entry = new Entry<>(element);
        if(head==null){
            head=entry;
            tail=entry;
            entry.next=entry;
        }else if(tail==null){
            throw new RuntimeException("LinkedList is in invalid state!");
        }else{
            tail.next = entry;
            tail = entry;
        }
        return entry;
    }

    public Entry<T> remove(T element){
        if(head.value==element){
            if(head==tail){
                Entry<T> removed = head;

                head=null;
                tail=null;
                return removed;
            }else {
                Entry<T> removed = head;
                head = head.next;
                tail.next = head;
                return removed;
            }
        }else {
            return removeEntry(element,head);
        }
    }

    public Entry<T> removeEntry(T element,Entry<T>  entry){
        if(entry.next==head){
            return null;
        }

        if(entry.next.value==element){
            Entry<T> removed = entry.next;
            entry.next = entry.next.next;
            return removed;
        }

        return removeEntry(element,entry.next);
    }

    public static class Entry<T>{
        public Entry(T value) {
            this.value = value;
        }

        private T value;
        private Entry<T> next;
    }
}
