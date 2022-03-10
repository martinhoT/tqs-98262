package tqs.lab1;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TqsStack<T> implements Stack<T> {

    private List<T> stack;
    private int capacity;

    public TqsStack() {
        stack = new ArrayList<>();
        capacity = Integer.MAX_VALUE;
    }

    @Override
    public void push(T t) {
        if (stack.size() >= capacity)
            throw new IllegalStateException();
        else
            stack.add(t);
    }

    @Override
    public T pop() {
        if (stack.size() == 0)
            throw new NoSuchElementException();
        return stack.remove(stack.size()-1);
    }

    @Override
    public T peek() {
        if (stack.size() == 0)
            throw new NoSuchElementException();
        return stack.get(stack.size()-1);
    }

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public boolean isEmpty() {
        return stack.size() == 0;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
}
