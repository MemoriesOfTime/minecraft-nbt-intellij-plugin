package com.github.tth05.minecraftnbtintellijplugin.snbt;

import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.NBTTagType;

/**
 * Recursive descent parser for Stringified NBT (SNBT) format.
 */
public class SNbtParser {

	private final String input;
	private int pos;

	public SNbtParser(String input) {
		this.input = input;
		this.pos = 0;
	}

	/**
	 * Parses a complete SNBT value and returns it as a tree node with an empty name.
	 */
	public NBTTagTreeNode parse() {
		skipWhitespace();
		NBTTagTreeNode node = parseValue("");
		skipWhitespace();
		if (pos < input.length())
			throw new SNbtParseException("Unexpected trailing content", pos);
		return node;
	}

	/**
	 * Parses a named value in the format "key:value" — used for pasting into Compound nodes.
	 */
	public NBTTagTreeNode parseNamedValue() {
		skipWhitespace();
		String key = parseKey();
		skipWhitespace();
		expect(':');
		skipWhitespace();
		NBTTagTreeNode node = parseValue(key);
		skipWhitespace();
		if (pos < input.length())
			throw new SNbtParseException("Unexpected trailing content", pos);
		return node;
	}

	private NBTTagTreeNode parseValue(String name) {
		if (pos >= input.length())
			throw new SNbtParseException("Unexpected end of input", pos);

		char c = input.charAt(pos);
		if (c == '{')
			return parseCompound(name);
		if (c == '[')
			return parseListOrArray(name);
		if (c == '"' || c == '\'')
			return new NBTTagTreeNode(NBTTagType.STRING, name, parseQuotedString());
		return parseUnquotedValue(name);
	}

	private NBTTagTreeNode parseCompound(String name) {
		expect('{');
		NBTTagTreeNode node = new NBTTagTreeNode(NBTTagType.COMPOUND, name, null);
		skipWhitespace();
		if (pos < input.length() && input.charAt(pos) == '}') {
			pos++;
			return node;
		}
		while (true) {
			skipWhitespace();
			String key = parseKey();
			skipWhitespace();
			expect(':');
			skipWhitespace();
			node.add(parseValue(key));
			skipWhitespace();
			if (pos >= input.length())
				throw new SNbtParseException("Expected ',' or '}'", pos);
			if (input.charAt(pos) == '}') {
				pos++;
				return node;
			}
			expect(',');
		}
	}

	private NBTTagTreeNode parseListOrArray(String name) {
		expect('[');
		skipWhitespace();

		// Check for typed arrays: [B;...], [I;...], [L;...]
		if (pos + 1 < input.length() && input.charAt(pos + 1) == ';') {
			char typeChar = input.charAt(pos);
			if (typeChar == 'B' || typeChar == 'I' || typeChar == 'L') {
				pos += 2; // skip type char and semicolon
				return parseTypedArray(name, typeChar);
			}
		}

		// Regular list
		NBTTagTreeNode node = new NBTTagTreeNode(NBTTagType.LIST, name, null);
		skipWhitespace();
		if (pos < input.length() && input.charAt(pos) == ']') {
			pos++;
			node.setValue("0 elements");
			return node;
		}
		int index = 0;
		while (true) {
			skipWhitespace();
			node.add(parseValue("[" + index + "]"));
			index++;
			skipWhitespace();
			if (pos >= input.length())
				throw new SNbtParseException("Expected ',' or ']'", pos);
			if (input.charAt(pos) == ']') {
				pos++;
				node.setValue(index + " elements");
				return node;
			}
			expect(',');
		}
	}

	private NBTTagTreeNode parseTypedArray(String name, char typeChar) {
		NBTTagType arrayType;
		NBTTagType elementType;
		switch (typeChar) {
			case 'B':
				arrayType = NBTTagType.BYTE_ARRAY;
				elementType = NBTTagType.BYTE;
				break;
			case 'I':
				arrayType = NBTTagType.INT_ARRAY;
				elementType = NBTTagType.INT;
				break;
			case 'L':
				arrayType = NBTTagType.LONG_ARRAY;
				elementType = NBTTagType.LONG;
				break;
			default:
				throw new SNbtParseException("Unknown array type: " + typeChar, pos);
		}

		NBTTagTreeNode node = new NBTTagTreeNode(arrayType, name, null);
		skipWhitespace();
		if (pos < input.length() && input.charAt(pos) == ']') {
			pos++;
			node.setValue("0 elements");
			return node;
		}
		int index = 0;
		while (true) {
			skipWhitespace();
			String raw = readUnquotedString();
			// Strip type suffix (b/B, L, etc.)
			String stripped = stripTypeSuffix(raw, typeChar);
			Object value = parseTypedNumber(stripped, elementType);
			node.add(new NBTTagTreeNode(elementType, "[" + index + "]", value));
			index++;
			skipWhitespace();
			if (pos >= input.length())
				throw new SNbtParseException("Expected ',' or ']'", pos);
			if (input.charAt(pos) == ']') {
				pos++;
				node.setValue(index + " elements");
				return node;
			}
			expect(',');
		}
	}

	private String stripTypeSuffix(String raw, char typeChar) {
		if (raw.isEmpty())
			return raw;
		char last = raw.charAt(raw.length() - 1);
		switch (typeChar) {
			case 'B':
				if (last == 'b' || last == 'B')
					return raw.substring(0, raw.length() - 1);
				break;
			case 'L':
				if (last == 'l' || last == 'L')
					return raw.substring(0, raw.length() - 1);
				break;
		}
		return raw;
	}

	private Object parseTypedNumber(String raw, NBTTagType type) {
		try {
			switch (type) {
				case BYTE:
					return Byte.parseByte(raw);
				case INT:
					return Integer.parseInt(raw);
				case LONG:
					return Long.parseLong(raw);
				default:
					throw new SNbtParseException("Unsupported array element type", pos);
			}
		} catch (NumberFormatException e) {
			throw new SNbtParseException("Invalid number: " + raw, pos);
		}
	}

	private NBTTagTreeNode parseUnquotedValue(String name) {
		String raw = readUnquotedString();
		if (raw.isEmpty())
			throw new SNbtParseException("Expected a value", pos);

		// Check boolean
		if ("true".equalsIgnoreCase(raw))
			return new NBTTagTreeNode(NBTTagType.BYTE, name, (byte) 1);
		if ("false".equalsIgnoreCase(raw))
			return new NBTTagTreeNode(NBTTagType.BYTE, name, (byte) 0);

		// Try type-suffixed values
		if (raw.length() > 1) {
			char last = raw.charAt(raw.length() - 1);
			String numPart = raw.substring(0, raw.length() - 1);
			try {
				switch (last) {
					case 'b':
					case 'B':
						return new NBTTagTreeNode(NBTTagType.BYTE, name, Byte.parseByte(numPart));
					case 's':
					case 'S':
						return new NBTTagTreeNode(NBTTagType.SHORT, name, Short.parseShort(numPart));
					case 'l':
					case 'L':
						return new NBTTagTreeNode(NBTTagType.LONG, name, Long.parseLong(numPart));
					case 'f':
					case 'F':
						return new NBTTagTreeNode(NBTTagType.FLOAT, name, Float.parseFloat(numPart));
					case 'd':
					case 'D':
						return new NBTTagTreeNode(NBTTagType.DOUBLE, name, Double.parseDouble(numPart));
				}
			} catch (NumberFormatException ignored) {
			}
		}

		// Try int
		try {
			return new NBTTagTreeNode(NBTTagType.INT, name, Integer.parseInt(raw));
		} catch (NumberFormatException ignored) {
		}

		// Try double (bare number with decimal point)
		try {
			if (raw.contains("."))
				return new NBTTagTreeNode(NBTTagType.DOUBLE, name, Double.parseDouble(raw));
		} catch (NumberFormatException ignored) {
		}

		// Fall back to string
		return new NBTTagTreeNode(NBTTagType.STRING, name, raw);
	}

	private String parseKey() {
		if (pos >= input.length())
			throw new SNbtParseException("Expected a key", pos);
		char c = input.charAt(pos);
		if (c == '"' || c == '\'')
			return parseQuotedString();
		return readUnquotedString();
	}

	private String parseQuotedString() {
		char quote = input.charAt(pos);
		pos++;
		StringBuilder sb = new StringBuilder();
		while (pos < input.length()) {
			char c = input.charAt(pos);
			if (c == '\\' && pos + 1 < input.length()) {
				pos++;
				sb.append(input.charAt(pos));
			} else if (c == quote) {
				pos++;
				return sb.toString();
			} else {
				sb.append(c);
			}
			pos++;
		}
		throw new SNbtParseException("Unterminated string", pos);
	}

	private String readUnquotedString() {
		int start = pos;
		while (pos < input.length() && isUnquotedChar(input.charAt(pos)))
			pos++;
		return input.substring(start, pos);
	}

	private boolean isUnquotedChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
				(c >= '0' && c <= '9') || c == '_' || c == '-' ||
				c == '.' || c == '+';
	}

	private void skipWhitespace() {
		while (pos < input.length() && Character.isWhitespace(input.charAt(pos)))
			pos++;
	}

	private void expect(char expected) {
		if (pos >= input.length() || input.charAt(pos) != expected)
			throw new SNbtParseException("Expected '" + expected + "'", pos);
		pos++;
	}
}
