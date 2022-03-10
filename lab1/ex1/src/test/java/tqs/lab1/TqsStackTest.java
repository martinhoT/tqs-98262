package tqs.lab1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TqsStackTest {
    
    private TqsStack<Object> stack;

    @BeforeEach
    void beforeEach() {
        stack = new TqsStack<>();
    }

    @AfterEach
    void afterEach() {}



    @Test
    void empty() {
        assertTrue(stack.isEmpty());
    }
    
    @Test
    void size0() {
        assertEquals(stack.size(), 0);
    }

    @DisplayName("After n pushes to an empty stack, n > 0, the stack is not empty and its size is n.")
    @Test
    void nPush() {
        int n = 10;
        for (int i = 0; i < n; i++)
            stack.push(0);
        
        assertFalse(stack.isEmpty());
        assertEquals(stack.size(), n);
    }

    @Test
    void xPushPop() {
        int x = 10;
        stack.push(x);

        assertEquals(stack.pop(), x);
    }

    @Test
    void xPushPeek() {
        int x = 10;
        stack.push(x);

        assertEquals(stack.size(), 1);
        assertEquals(stack.peek(), x);
        assertEquals(stack.size(), 1);
    }

    @Test
    void nPop() {
        int n = 10;
        for (int i = 0; i < n; i++)
            stack.push(0);

        assertEquals(stack.size(), n);

        for (int i = 0; i < n; i++)
            stack.pop();

        assertTrue(stack.isEmpty());
        assertEquals(stack.size(), 0);
    }

    @Test
    void popEmpty() {
        assertThrows(NoSuchElementException.class, () -> stack.pop());
    }

    @Test
    void peekEmpty() {
        assertThrows(NoSuchElementException.class, () -> stack.peek());
    }

    @Test
    void pushFull() {
        int n = 10;
        stack.setCapacity(n);
        for (int i = 0; i < n; i++)
            stack.push(0);

        assertThrows(IllegalStateException.class, () -> stack.push(0));
    }
}
