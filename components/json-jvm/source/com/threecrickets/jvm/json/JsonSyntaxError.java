/**
 * Copyright 2010-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jvm.json;

/**
 * Exception for JSON syntax errors.
 * 
 * @author Tal Liron
 * @see JsonDecoder
 */
public class JsonSyntaxError extends Exception
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        The exception message
	 * @param line
	 *        The line number
	 * @param column
	 *        The column number
	 */
	public JsonSyntaxError( String message, int line, int column )
	{
		super( message );
		this.line = line;
		this.column = column;
	}

	//
	// Attributes
	//

	/**
	 * The line number.
	 * 
	 * @return The line number
	 */
	public int getLine()
	{
		return line;
	}

	/**
	 * The column number.
	 * 
	 * @return The column number
	 */
	public int getColumn()
	{
		return column;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 0;

	private final int line;

	private final int column;
}
