package io.github.gastaldi;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

class TreeNode implements Comparable<TreeNode>{

    final String name;
    final Collection<TreeNode> children;

    public TreeNode(String name, Collection<TreeNode> children) {
        this.name = name;
        this.children = children;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }


    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);
        buffer.append('\n');
        if (children != null)
            for (Iterator<TreeNode> it = children.iterator(); it.hasNext(); ) {
                TreeNode next = it.next();
                if (it.hasNext()) {
                    next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
                } else {
                    next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
                }
            }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TreeNode treeNode = (TreeNode) o;
        return name.equals(treeNode.name) && Objects.equals(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override public int compareTo(TreeNode o) {
        return name.compareTo(o.name);
    }
}
