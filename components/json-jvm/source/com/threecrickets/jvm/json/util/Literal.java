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

package com.threecrickets.jvm.json.util;

/**
 * A container for literal text during conversions.
 * 
 * @author Tal Liron
 */
public class Literal
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param value
	 *        The value
	 */
	public Literal( String value )
	{
		this.value = value;
	}

	//
	// Attributes
	//

	/**
	 * The literal value.
	 */
	public final String value;

	//
	// Operations
	//

	/**
	 * For multiline literals, indent all lines after the first.
	 * 
	 * @param depth
	 *        The indentation depth
	 * @return The indented literal
	 */
	public String toString( int depth )
	{
		String[] lines = value.split( "\n" );
		if( lines.length == 1 )
			return value;

		StringBuilder r = new StringBuilder();
		for( int i = depth - 1; i >= 0; i-- )
			r.append( "  " );
		String prefix = r.toString();

		r = new StringBuilder();
		for( int i = 0, length = lines.length; i < length; i++ )
		{
			if( i > 0 )
				r.append( prefix );
			r.append( lines[i] );
			if( i < length - 1 )
				r.append( '\n' );
		}
		return r.toString();
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return value;
	}
}
