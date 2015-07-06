/**
 * Copyright 2010-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jvm.json;

import java.io.IOException;

import com.threecrickets.jvm.json.util.JsonUtil;

/**
 * Stores information used for encoding JSON, and provides related utilities.
 * <p>
 * Note that implementations may extend this class in order to store additional
 * information. They may return their custom subclass in
 * {@link JsonImplementation#createContext(Appendable, boolean, boolean, int)}.
 * 
 * @author Tal Liron
 */
public class JsonContext
{
	//
	// Construction
	//

	/**
	 * Construct.
	 * 
	 * @param implementation
	 *        The implementation
	 * @param out
	 *        Where to write the JSON
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @param allowCode
	 *        Whether to allow programming language code (non-standard JSON)
	 * @param depth
	 *        The indentation depth level
	 */
	public JsonContext( JsonImplementation implementation, Appendable out, boolean expand, boolean allowCode, int depth )
	{
		this.implementation = implementation;
		this.out = out;
		this.expand = expand;
		this.allowCode = allowCode;
		this.depth = depth;
	}

	//
	// Attributes
	//

	/**
	 * The implementation.
	 */
	public final JsonImplementation implementation;

	/**
	 * Where to write the JSON.
	 */
	public final Appendable out;

	/**
	 * Whether to expand the JSON with newlines, indents, and spaces.
	 */
	public final boolean expand;

	/**
	 * The indenter for a single indentation depth level.
	 */
	public final String indenter = "  ";

	/**
	 * Whether to allow programming language code (non-standard JSON).
	 */
	public final boolean allowCode;

	/**
	 * The indentation depth level.
	 */
	public final int depth;

	//
	// Operations
	//

	public void encode( Object object ) throws IOException
	{
		for( JsonEncoder codec : implementation.getEncoders() )
		{
			if( codec.canEncode( object, this ) )
			{
				codec.encode( object, this );
				return;
			}
		}

		implementation.getFallbackEncoder().encode( object, this );
	}

	public JsonContext nest()
	{
		if( expand )
			return implementation.createContext( out, expand, allowCode, depth + 1 );
		else
			return this;
	}

	public void indent() throws IOException
	{
		if( expand )
			for( int i = depth - 1; i >= 0; i-- )
				out.append( indenter );
	}

	public void indentNested() throws IOException
	{
		if( expand )
			for( int i = depth; i >= 0; i-- )
				out.append( indenter );
	}

	public void newline() throws IOException
	{
		if( expand )
			out.append( '\n' );
	}

	public void comma() throws IOException
	{
		out.append( ',' );
		newline();
	}

	public void colon() throws IOException
	{
		if( expand )
			out.append( ": " );
		else
			out.append( ':' );
	}

	public void quoted( CharSequence string ) throws IOException
	{
		out.append( '\"' );
		out.append( JsonUtil.escapeCharSequence( string ) );
		out.append( '\"' );
	}
}
