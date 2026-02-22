package com.github.tth05.minecraftnbtintellijplugin.snbt;

import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.NBTTagType;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class SNbtSerializer {

	private static final Pattern SIMPLE_KEY = Pattern.compile("^[a-zA-Z0-9._+-]+$");

	public static String serialize(NBTTagTreeNode node) {
		StringBuilder sb = new StringBuilder();
		writeValue(node, sb);
		return sb.toString();
	}

	public static String serializePretty(NBTTagTreeNode node) {
		StringBuilder sb = new StringBuilder();
		writePrettyValue(node, sb, 0);
		sb.append("\n");
		return sb.toString();
	}

	private static void writePrettyValue(NBTTagTreeNode node, StringBuilder sb, int indent) {
		switch (node.getType()) {
			case COMPOUND:
				writePrettyCompound(node, sb, indent);
				break;
			case LIST:
				writePrettyList(node, sb, indent);
				break;
			case BYTE_ARRAY:
				writePrettyTypedArray(node, sb, indent, "B");
				break;
			case INT_ARRAY:
				writePrettyTypedArray(node, sb, indent, "I");
				break;
			case LONG_ARRAY:
				writePrettyTypedArray(node, sb, indent, "L");
				break;
			case BYTE:
				sb.append(node.getValue()).append("b");
				break;
			case SHORT:
				sb.append(node.getValue()).append("s");
				break;
			case INT:
				sb.append(node.getValue());
				break;
			case LONG:
				sb.append(node.getValue()).append("L");
				break;
			case FLOAT:
				sb.append(node.getValue()).append("f");
				break;
			case DOUBLE:
				sb.append(node.getValue()).append("d");
				break;
			case STRING:
				writeQuotedString((String) node.getValue(), sb);
				break;
		}
	}

	private static void writePrettyCompound(NBTTagTreeNode node, StringBuilder sb, int indent) {
		sb.append("{");
		if (node.getChildCount() == 0) {
			sb.append("}");
			return;
		}
		sb.append("\n");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",\n");
			first = false;
			NBTTagTreeNode child = (NBTTagTreeNode) children.nextElement();
			appendIndent(sb, indent + 1);
			writeKey(child.getName(), sb);
			sb.append(": ");
			writePrettyValue(child, sb, indent + 1);
		}
		sb.append("\n");
		appendIndent(sb, indent);
		sb.append("}");
	}

	private static void writePrettyList(NBTTagTreeNode node, StringBuilder sb, int indent) {
		sb.append("[");
		if (node.getChildCount() == 0) {
			sb.append("]");
			return;
		}
		sb.append("\n");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",\n");
			first = false;
			appendIndent(sb, indent + 1);
			writePrettyValue((NBTTagTreeNode) children.nextElement(), sb, indent + 1);
		}
		sb.append("\n");
		appendIndent(sb, indent);
		sb.append("]");
	}

	private static void writePrettyTypedArray(NBTTagTreeNode node, StringBuilder sb, int indent, String prefix) {
		sb.append("[").append(prefix).append(";");
		if (node.getChildCount() == 0) {
			sb.append("]");
			return;
		}
		sb.append("\n");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",\n");
			first = false;
			appendIndent(sb, indent + 1);
			NBTTagTreeNode child = (NBTTagTreeNode) children.nextElement();
			sb.append(child.getValue());
			switch (prefix) {
				case "B":
					sb.append("b");
					break;
				case "L":
					sb.append("L");
					break;
			}
		}
		sb.append("\n");
		appendIndent(sb, indent);
		sb.append("]");
	}

	private static void appendIndent(StringBuilder sb, int level) {
		for (int i = 0; i < level; i++)
			sb.append("  ");
	}

	private static void writeValue(NBTTagTreeNode node, StringBuilder sb) {
		switch (node.getType()) {
			case COMPOUND:
				writeCompound(node, sb);
				break;
			case LIST:
				writeList(node, sb);
				break;
			case BYTE_ARRAY:
				writeTypedArray(node, sb, "B");
				break;
			case INT_ARRAY:
				writeTypedArray(node, sb, "I");
				break;
			case LONG_ARRAY:
				writeTypedArray(node, sb, "L");
				break;
			case BYTE:
				sb.append(node.getValue()).append("b");
				break;
			case SHORT:
				sb.append(node.getValue()).append("s");
				break;
			case INT:
				sb.append(node.getValue());
				break;
			case LONG:
				sb.append(node.getValue()).append("L");
				break;
			case FLOAT:
				sb.append(node.getValue()).append("f");
				break;
			case DOUBLE:
				sb.append(node.getValue()).append("d");
				break;
			case STRING:
				writeQuotedString((String) node.getValue(), sb);
				break;
		}
	}

	private static void writeCompound(NBTTagTreeNode node, StringBuilder sb) {
		sb.append("{");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",");
			first = false;
			NBTTagTreeNode child = (NBTTagTreeNode) children.nextElement();
			writeKey(child.getName(), sb);
			sb.append(":");
			writeValue(child, sb);
		}
		sb.append("}");
	}

	private static void writeList(NBTTagTreeNode node, StringBuilder sb) {
		sb.append("[");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",");
			first = false;
			writeValue((NBTTagTreeNode) children.nextElement(), sb);
		}
		sb.append("]");
	}

	private static void writeTypedArray(NBTTagTreeNode node, StringBuilder sb, String prefix) {
		sb.append("[").append(prefix).append(";");
		Enumeration<TreeNode> children = node.children();
		boolean first = true;
		while (children.hasMoreElements()) {
			if (!first)
				sb.append(",");
			first = false;
			NBTTagTreeNode child = (NBTTagTreeNode) children.nextElement();
			sb.append(child.getValue());
			switch (prefix) {
				case "B":
					sb.append("b");
					break;
				case "L":
					sb.append("L");
					break;
			}
		}
		sb.append("]");
	}

	private static void writeKey(String key, StringBuilder sb) {
		if (SIMPLE_KEY.matcher(key).matches()) {
			sb.append(key);
		} else {
			writeQuotedString(key, sb);
		}
	}

	private static void writeQuotedString(String value, StringBuilder sb) {
		sb.append("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\' || c == '"')
				sb.append('\\');
			sb.append(c);
		}
		sb.append("\"");
	}
}
