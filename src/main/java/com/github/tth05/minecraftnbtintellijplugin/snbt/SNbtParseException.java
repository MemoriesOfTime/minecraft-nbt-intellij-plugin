package com.github.tth05.minecraftnbtintellijplugin.snbt;

public class SNbtParseException extends RuntimeException {

	private final int position;

	public SNbtParseException(String message, int position) {
		super(message + " at position " + position);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}
}
