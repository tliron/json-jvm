/**
 * Based on code from JSON.org. The original code came with the following
 * notice:
 * <p>
 * Copyright (c) 2002 JSON.org
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * The Software shall be used for Good, not Evil.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.threecrickets.jvm.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Decoded JSON text to objects.
 */
public class JsonDecoder
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param implementation
	 *        The implementation
	 * @param reader
	 *        The reader
	 * @param allowTransform
	 *        Whether to allow transformations
	 */
	public JsonDecoder( JsonImplementation implementation, Reader reader, boolean allowTransform )
	{
		this.implementation = implementation;
		this.reader = reader.markSupported() ? reader : new BufferedReader( reader );
		this.allowTransform = allowTransform;
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.column = 1;
		this.line = 1;
	}

	//
	// Operations
	//

	/**
	 * Decode an object <i>or</i> an array.
	 * 
	 * @return An object or an array
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	public Object decode() throws JsonSyntaxError, IOException
	{
		char next = nextClean();
		if( next == '{' )
		{
			back();
			return decodeObject();
		}
		else if( next == '[' )
		{
			back();
			return decodeArray();
		}
		else
			throw new JsonSyntaxError( "JSON text must begin with either a '{' or a '['", line, column );
	}

	/**
	 * Decode an object.
	 * 
	 * @return An object
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	public Object decodeObject() throws JsonSyntaxError, IOException
	{
		Object object = implementation.createObject();
		char c;
		String key;

		if( nextClean() != '{' )
			throw new JsonSyntaxError( "A JSON object text must begin with '{'", line, column );
		for( ;; )
		{
			c = nextClean();
			switch( c )
			{
				case 0:
					throw new JsonSyntaxError( "A JSON object text must end with '}'", line, column );
				case '}':
					return transform( object );
				default:
					back();
					key = nextValue().toString();
			}

			// The key is followed by ':'. We will also tolerate '=' or '=>'.

			c = nextClean();
			if( c == '=' )
			{
				if( next() != '>' )
					back();
			}
			else if( c != ':' )
				throw new JsonSyntaxError( "Expected a ':' after a key", line, column );
			implementation.putInObject( object, key, transform( nextValue() ) );

			// Pairs are separated by ','. We will also tolerate ';'.

			switch( nextClean() )
			{
				case ';':
				case ',':
					if( nextClean() == '}' )
						return transform( object );
					back();
					break;
				case '}':
					return transform( object );
				default:
					throw new JsonSyntaxError( "Expected a ',' or '}'", line, column );
			}
		}
	}

	/**
	 * Decode an array.
	 * 
	 * @return An array
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	public Object decodeArray() throws JsonSyntaxError, IOException
	{
		ArrayList<Object> list = new ArrayList<Object>();
		char c = nextClean();
		char q;

		if( c == '[' )
			q = ']';
		else if( c == '(' )
			q = ')';
		else
			throw new JsonSyntaxError( "A JSON array text must start with '['", line, column );
		if( nextClean() == ']' )
			return transform( collectionToArray( list ) );
		back();
		for( ;; )
		{
			if( nextClean() == ',' )
			{
				back();
				list.remove( list.size() - 1 );
			}
			else
			{
				back();
				list.add( transform( nextValue() ) );
			}
			c = nextClean();
			switch( c )
			{
				case ';':
				case ',':
					if( nextClean() == ']' )
						return transform( collectionToArray( list ) );
					back();
					break;
				case ']':
				case ')':
					if( q != c )
						throw new JsonSyntaxError( "Expected a '" + new Character( q ) + "'", line, column );
					return transform( collectionToArray( list ) );
				default:
					throw new JsonSyntaxError( "Expected a ',' or ']'", line, column );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final JsonImplementation implementation;

	private final Reader reader;

	private final boolean allowTransform;

	private int line;

	private int column;

	private int index;

	private char previous;

	private boolean usePrevious;

	private boolean eof;

	private boolean end()
	{
		return eof && !usePrevious;
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability, so
	 * that you can test for a digit or letter before attempting to parse the
	 * next number or identifier.
	 * 
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 */
	private void back() throws JsonSyntaxError
	{
		if( usePrevious || index <= 0 )
			throw new JsonSyntaxError( "Stepping back two steps is not supported", line, column );
		this.index -= 1;
		this.column -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Get the next character in the source string.
	 * 
	 * @return The next character, or 0 if past the end of the source string
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	private char next() throws JsonSyntaxError, IOException
	{
		int c;
		if( this.usePrevious )
		{
			this.usePrevious = false;
			c = this.previous;
		}
		else
		{
			c = this.reader.read();

			if( c <= 0 )
			{ // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
		if( this.previous == '\r' )
		{
			this.line += 1;
			this.column = c == '\n' ? 0 : 1;
		}
		else if( c == '\n' )
		{
			this.line += 1;
			this.column = 0;
		}
		else
			this.column += 1;
		this.previous = (char) c;
		return this.previous;
	}

	/**
	 * Get the next n characters.
	 * 
	 * @param n
	 *        The number of characters to take
	 * @return The next characters
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	private String next( int n ) throws JsonSyntaxError, IOException
	{
		if( n == 0 )
			return "";

		char[] buffer = new char[n];
		int pos = 0;

		while( pos < n )
		{
			buffer[pos] = next();
			if( end() )
				throw new JsonSyntaxError( "Substring bounds error", line, column );
			pos += 1;
		}
		return new String( buffer );
	}

	/**
	 * Get the next char in the string, skipping whitespace.
	 * 
	 * @return The next character, or 0 if there are no more characters
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	private char nextClean() throws JsonSyntaxError, IOException
	{
		for( ;; )
		{
			char c = next();
			if( c == 0 || c > ' ' )
				return c;
		}
	}

	/**
	 * Return the characters up to the next close quote character. Backslash
	 * processing is done. The formal JSON format does not allow strings in
	 * single quotes, but an implementation is allowed to accept them.
	 * 
	 * @param quote
	 *        The quoting character, either <code>"</code>&nbsp;<small>(double
	 *        quote)</small> or <code>'</code>&nbsp;<small>(single
	 *        quote)</small>.
	 * @return The next string
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	private String nextString( char quote ) throws JsonSyntaxError, IOException
	{
		char c;
		StringBuffer sb = new StringBuffer();
		for( ;; )
		{
			c = next();
			switch( c )
			{
				case 0:
				case '\n':
				case '\r':
					throw new JsonSyntaxError( "Unterminated string", line, column );
				case '\\':
					c = next();
					switch( c )
					{
						case 'b':
							sb.append( '\b' );
							break;
						case 't':
							sb.append( '\t' );
							break;
						case 'n':
							sb.append( '\n' );
							break;
						case 'f':
							sb.append( '\f' );
							break;
						case 'r':
							sb.append( '\r' );
							break;
						case 'u':
							sb.append( (char) Integer.parseInt( next( 4 ), 16 ) );
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							sb.append( c );
							break;
						default:
							throw new JsonSyntaxError( "Illegal escape", line, column );
					}
					break;
				default:
					if( c == quote )
						return sb.toString();
					sb.append( c );
			}
		}
	}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 * 
	 * @return An object.
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	private Object nextValue() throws JsonSyntaxError, IOException
	{
		char c = nextClean();
		String s;

		switch( c )
		{
			case '"':
			case '\'':
				return nextString( c );
			case '{':
				back();
				return decodeObject();
			case '[':
			case '(':
				back();
				return decodeArray();
		}

		// Handle unquoted text. This could be the values true, false, or null,
		// or it can be a number. An implementation (such as this one) is
		// allowed to also accept non-standard forms. Accumulate characters
		// until we reach the end of the text or a formatting character.

		StringBuffer sb = new StringBuffer();
		while( c >= ' ' && ",:]}/\\\"[{;=#".indexOf( c ) < 0 )
		{
			sb.append( c );
			c = next();
		}
		back();

		s = sb.toString().trim();
		if( s.equals( "" ) )
			throw new JsonSyntaxError( "Missing value", line, column );

		return stringToValue( s );
	}

	private Object stringToValue( String s )
	{
		if( s.equals( "" ) )
			return implementation.createString( s );
		if( s.equalsIgnoreCase( "true" ) )
			return Boolean.TRUE;
		if( s.equalsIgnoreCase( "false" ) )
			return Boolean.FALSE;
		if( s.equalsIgnoreCase( "null" ) )
			return null;

		// If it might be a number, try converting it. We support the
		// non-standard 0x- convention. If a number cannot be produced, then the
		// value will just be a string. Note that the 0x-, plus, and implied
		// string conventions are non-standard. A JSON parser may accept
		// non-JSON forms as long as it accepts all correct JSON forms.

		char b = s.charAt( 0 );
		if( ( b >= '0' && b <= '9' ) || b == '.' || b == '-' || b == '+' )
		{
			if( b == '0' && s.length() > 2 && ( s.charAt( 1 ) == 'x' || s.charAt( 1 ) == 'X' ) )
			{
				try
				{
					// Tal Liron's patch: allow for long hexes! Why not?
					Long myLong = Long.parseLong( s.substring( 2 ), 16 );
					if( myLong.longValue() == myLong.intValue() )
						return implementation.createInteger( myLong.intValue() );
					else
						return implementation.createLong( myLong );
				}
				catch( Exception ignore )
				{
				}
			}
			try
			{
				if( s.indexOf( '.' ) > -1 || s.indexOf( 'e' ) > -1 || s.indexOf( 'E' ) > -1 )
					return implementation.createDouble( Double.valueOf( s ) );
				else
				{
					Long myLong = new Long( s );
					if( myLong.longValue() == myLong.intValue() )
						return implementation.createInteger( myLong.intValue() );
					else
						return implementation.createLong( myLong );
				}
			}
			catch( Exception ignore )
			{
			}
		}

		return implementation.createString( s );
	}

	private Object collectionToArray( Collection<Object> collection )
	{
		Object array = implementation.createArray( collection.size() );
		int index = 0;
		for( Object item : collection )
			implementation.setInArray( array, index++, item );
		return array;
	}

	private Object transform( Object object )
	{
		if( allowTransform )
		{
			for( JsonTransformer transformer : implementation.getTransformers() )
			{
				Object r = transformer.transform( object, implementation );
				if( r != null )
					return r;
			}
		}
		return object;
	}
}
