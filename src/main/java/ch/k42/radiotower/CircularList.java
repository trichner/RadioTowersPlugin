package ch.k42.radiotower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 01.01.2015.
 *
 * @author Thomas
 */
public class CircularList<T>{

    private Node<T> head = null;

    public Node<T> head(){
        return head;
    }

    public Node<T> push(T element){
        Node<T> node;
        if(head==null){
            head= new Node<>(element);
            node = head;
        }else {
            node = head.push(element);
        }
        return node;
    }

    public Node<T> remove(T element){
        Node<T> removed = null;
        if(head==null){
            // nothing to remove, list is empty
        }else if (head.value.equals(element)) {
            removed = head;
            head = head.next;
        } else {
            removed = head.remove(element);
        }
        return removed;
    }

    public Node<T> next(Node<T> node){
        if(node.next==null){
            return head;
        }else{
            return node.next;
        }
    }

    public T next(T element){
        Node<T> node = find(element);
        if(node==null){
            //NODE NOT FOUND!!! FIXME
        }
        return next(find(element)).value();
    }

    public List<T> toList() {
        List<T> list;
        if(head==null){
           list = Collections.emptyList();
        }else{
            list = head.toList();
        }
        return list;
    }

    public int size(){
        int size = 0;
        if(head!=null){
            size = head.size();
        }
        return size;
    }

    public boolean contains(T element){
        return find(element)!=null;
    }

    public Node<T> find(T element){
        if(head==null){
            return null;
        }
        return head.find(element);
    }

    public static class Node<T>{
        public Node(T value) {
            this.value = value;
        }

        private T value;
        private Node<T> next = null;

        public Node<T> push(T value){
            if(this.value.equals(value)){
                return this;
            }if(this.next==null) {
                this.next = new Node<>(value);
                return this.next;
            }else {
                return this.next.push(value);
            }
        }
        public Node<T> remove(T value){
            if(this.next == null){
                return null;
            }else if(this.next.value.equals(value)) {
                Node<T> removed = this.next;
                this.next = this.next.next;
                return removed;
            }else {
                return this.next.remove(value);
            }
        }

        public List<T> toList() {
            if(this.next==null) {
                List<T> list = new ArrayList<>();
                list.add(this.value);
                return list;
            }else{
                List<T> list = this.next.toList();
                list.add(this.value);
                return list;
            }
        }

        public int size(){
            if(this.next==null){
                return 1;
            }else {
                int size = this.next.size();
                return size+1;
            }
        }

        public Node<T> find(T element){
            if(this.value.equals(element)){
                return this;
            }else if(this.next==null){
                return null;
            }else {
                return this.next.find(element);
            }
        }

        public T value() {
            return value;
        }
    }
}
