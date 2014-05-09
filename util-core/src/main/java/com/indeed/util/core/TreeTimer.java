package com.indeed.util.core;

import java.util.Map;
import java.util.Stack;
import java.util.LinkedHashMap;

import javax.annotation.Nonnull;

/**
 * Class to help keep a set of timings.  Call push() with a name when timer should start, then call pop() when the timer should stop.  If another push() is called prior to a pop
 * the timing values begin to nest.  If the same name is pushed/popped several times the times accumulate
 * To dump all the timing information call toString()
 *
 * @author ahudson
 */
public class TreeTimer {
    Stack<Long> timeStack = new Stack<Long>();
    Stack<Node> nodeStack = new Stack<Node>();
    Node root = new Node();

    private static class Node {
        long time = 0;
        Map<String, Node> children = null;
        Node getChild(String s) {
            if (children == null) children = new LinkedHashMap<String, Node>();
            Node child = children.get(s);
            if (child == null) {
                child = new Node();
                children.put(s, child);
            }
            return child;
        }
    }

    public void push(String s) {
        Node node;
        if (nodeStack.isEmpty()) {
            node = root;
        } else {
            node = nodeStack.peek();
        }
        nodeStack.push(node.getChild(s));
        timeStack.push(System.currentTimeMillis());
    }

    public int pop() {
        if (!nodeStack.isEmpty()) {
            Node node = nodeStack.pop();
            long start = timeStack.pop();
            long duration = System.currentTimeMillis() - start;
            node.time += duration;
            return (int) duration;
        }
        return -1;
    }

    // used for aligning output for prettier printing
    private static long findMaxTime(Node n) {
        long max = Long.MIN_VALUE;
        for (Map.Entry<String, Node> entry : n.children.entrySet()) {
            max = Math.max(max, entry.getValue().time);
        }
        return max;
    }

    private static void printNode(int indent, Node n, StringBuilder ret) {
        if (n.children == null) return;
        long max = findMaxTime(n);
        int width = String.valueOf(max).length();
        for (Map.Entry<String, Node> entry: n.children.entrySet()) {
            ret.append(TreeTimer.leftpad(String.valueOf(entry.getValue().time), width + indent));
            ret.append("ms ").append(entry.getKey()).append("\n");
            printNode(indent + width + 3, entry.getValue(), ret);
        }
    }

    /**
     * Left-pads a String with spaces so it is length <code>n</code>.  If the String
     * is already at least length n, no padding is done.
     */
    @Nonnull
    private static String leftpad(@Nonnull String s, int n) {
        return leftpad(s, n, ' ');
    }

    /**
     * Left-pads a String with the specific padChar so it is length <code>n</code>.  If the String
     * is already at least length n, no padding is done.
     */
    @Nonnull
    private static String leftpad(@Nonnull String s, int n, char padChar) {
        int diff = n - s.length();
        if (diff <= 0) {
            return s;
        }
        StringBuilder buf = new StringBuilder(n);

        for (int i = 0; i < diff; ++i) {
            buf.append(padChar);
        }
        buf.append(s);
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        printNode(2, root, ret);
        return ret.toString();
    }
}
