package com.company;

import java.util.Random;
import java.util.function.Function;

/**
 * Реализация простейшего бинарного дерева
 */
public class SimpleBinaryTree<T> implements BinaryTree<T> {

    protected class SimpleTreeNode implements BinaryTree.TreeNode<T> {
        public T value;
        public SimpleTreeNode left;
        public SimpleTreeNode right;

        public SimpleTreeNode(T value, SimpleTreeNode left, SimpleTreeNode right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public SimpleTreeNode(T value) {
            this(value, null, null);
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public TreeNode<T> getLeft() {
            return left;
        }

        @Override
        public TreeNode<T> getRight() {
            return right;
        }
    }

    protected SimpleTreeNode root = null;

    protected Function<String, T> fromStrFunc;
    protected Function<T, String> toStrFunc;

    public SimpleBinaryTree(Function<String, T> fromStrFunc, Function<T, String> toStrFunc) {
        this.fromStrFunc = fromStrFunc;
        this.toStrFunc = toStrFunc;
    }

    public SimpleBinaryTree(Function<String, T> fromStrFunc) {
        this(fromStrFunc, Object::toString);
    }

    public SimpleBinaryTree() {
        this(null);
    }

    @Override
    public TreeNode<T> getRoot() {
        return root;
    }

    public void clear() {
        root = null;
    }

    private T fromStr(String s) throws Exception {
        s = s.trim();
        if (s.length() > 0 && s.charAt(0) == '"') {
            s = s.substring(1);
        }
        if (s.length() > 0 && s.charAt(s.length() - 1) == '"') {
            s = s.substring(0, s.length() - 1);
        }
        if (fromStrFunc == null) {
            throw new Exception("Не определена функция конвертации строки в T");
        }
        return fromStrFunc.apply(s);
    }

    private static class IndexWrapper {
        public int index = 0;
    }

    private void skipSpaces(String bracketStr, IndexWrapper iw) {
        while (iw.index < bracketStr.length() && Character.isWhitespace(bracketStr.charAt(iw.index))) {
            iw.index++;
        }
    }

    private T readValue(String bracketStr, IndexWrapper iw) throws Exception {
        // пропуcкаем возможные пробелы
        skipSpaces(bracketStr, iw);
        if (iw.index >= bracketStr.length()) {
            return null;
        }
        int from = iw.index;
        boolean quote = bracketStr.charAt(iw.index) == '"';
        if (quote) {
            iw.index++;
        }
        while (iw.index < bracketStr.length() && (
                    quote && bracketStr.charAt(iw.index) != '"' ||
                    !quote && !Character.isWhitespace(bracketStr.charAt(iw.index)) && "(),".indexOf(bracketStr.charAt(iw.index)) < 0
               )) {
            iw.index++;
        }
        if (quote && bracketStr.charAt(iw.index) == '"') {
            iw.index++;
        }
        String valueStr = bracketStr.substring(from, iw.index);
        T value = fromStr(valueStr);
        skipSpaces(bracketStr, iw);
        return value;
    }

    private SimpleTreeNode fromBracketStr(String bracketStr, IndexWrapper iw) throws Exception {
        T parentValue = readValue(bracketStr, iw);
        SimpleTreeNode parentNode = new SimpleTreeNode(parentValue);
        if (bracketStr.charAt(iw.index) == '(') {
            iw.index++;
            skipSpaces(bracketStr, iw);
            if (bracketStr.charAt(iw.index) != ',') {
                parentNode.left = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) == ',') {
                iw.index++;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                parentNode.right = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                throw new Exception(String.format("Ожидалось ')' [%d]", iw.index));
            }
            iw.index++;
        }

        return parentNode;
    }

    public void fromBracketNotation(String bracketStr) throws Exception {
        IndexWrapper iw = new IndexWrapper();
        SimpleTreeNode root = fromBracketStr(bracketStr, iw);
        if (iw.index < bracketStr.length()) {
            throw new Exception(String.format("Ожидался конец строки [%d]", iw.index));
        }
        this.root = root;
    }

    public String createRandomTree (int minValue, int maxValue, int maxHeight) throws Exception {
        Random random = new Random();

        int height = random.nextInt(maxHeight) + 1;

        StringBuilder futureTree = new StringBuilder();

        futureTree.append(random.nextInt(maxValue - minValue + 1) + minValue);

        int leftP = 0;
        int rightP = 0;

        while (leftP != 1 && rightP != 1) {
            leftP = random.nextInt(2);
            rightP = random.nextInt(2);
        }

        createByRecurse(futureTree, minValue, maxValue, leftP, rightP, height);

        return String.valueOf(futureTree);
    }

    private void createByRecurse(StringBuilder futureTree, int minValue, int maxValue, int leftP, int rightP, int height) {
        Random random = new Random();
        if (height == -1) {
            return;
        }

        if ((leftP == 1) && (rightP == 1)) {
            height--;
            int first = random.nextInt(maxValue - minValue + 1) + minValue;
            int second = random.nextInt(maxValue - minValue + 1) + minValue;

            futureTree.append("(");
            if (first < second) {
                futureTree.append(first);

                createByRecurse(futureTree, minValue, maxValue, random.nextInt(2), random.nextInt(2), height);

                futureTree.append(",");
                futureTree.append(second);

            } else {
                futureTree.append(second);

                createByRecurse(futureTree, minValue, maxValue, random.nextInt(2), random.nextInt(2), height);

                futureTree.append(",");
                futureTree.append(first);

            }

            createByRecurse(futureTree, minValue, maxValue, random.nextInt(2), random.nextInt(2), height);

            futureTree.append(")");
        } else if ((leftP == 0) && (rightP == 1)) {
            height--;

            futureTree.append("(");
            futureTree.append(",");
            futureTree.append(random.nextInt(maxValue - minValue + 1) + minValue);

            createByRecurse(futureTree, minValue, maxValue, random.nextInt(2), random.nextInt(2), height);

            futureTree.append(")");
        } else if ((leftP == 1) && (rightP == 0)){
            height--;

            futureTree.append("(");
            futureTree.append(random.nextInt(maxValue - minValue + 1) + minValue);

            createByRecurse(futureTree, minValue, maxValue, random.nextInt(2), random.nextInt(2), height);

            futureTree.append(")");
        } else if ((leftP == 0) && (rightP == 0)) {
            height--;
        }
    }
}
