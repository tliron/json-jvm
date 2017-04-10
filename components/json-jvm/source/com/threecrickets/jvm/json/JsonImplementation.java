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

import java.io.Reader;
import java.util.Collection;

/**
 * Implements JSON conversion for a specific environment.
 * 
 * @author Tal Liron
 * @see Json
 */
public interface JsonImplementation
{
	/**
	 * Initialize the implementation.
	 */
	public void initialize();

	/**
	 * The name of this implementation.
	 * 
	 * @return The name of this implementation
	 */
	public String getName();

	/**
	 * The priority of this implementation. Higher numbers mean higher priority.
	 * 
	 * @return The priority of this implementation
	 */
	public int getPriority();

	/**
	 * Creates a context for encoding.
	 * <p>
	 * Note that some implementations may return a custom subclass of
	 * {@link JsonContext} if they require more information to be stored in the
	 * context.
	 * 
	 * @param out
	 *        Where to write the JSON
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @param allowCode
	 *        Whether to allow programming language code (non-standard JSON)
	 * @param depth
	 *        The indentation depth level
	 * @return A context
	 */
	public JsonContext createContext( Appendable out, boolean expand, boolean allowCode, int depth );

	/**
	 * Creates a JSON decoder that decoded into implementation-specific objects.
	 * 
	 * @param reader
	 *        The reader
	 * @param allowTransform
	 *        Whether to allow transformations
	 * @return A decoder
	 */
	public JsonDecoder createDecoder( Reader reader, boolean allowTransform );

	/**
	 * The encoders used by this implementation.
	 * 
	 * @return The encoders
	 */
	public Collection<JsonEncoder> getEncoders();

	/**
	 * The encoder to use if no other is found.
	 * 
	 * @return The fallback encoder
	 */
	public JsonEncoder getFallbackEncoder();

	/**
	 * The transformers used by this implementation.
	 * 
	 * @return The transformers
	 */
	public Collection<JsonTransformer> getTransformers();

	/**
	 * Creates an implementation-specific object, equivalent to a JSON "{...}".
	 * 
	 * @return An implementation-specific object
	 */
	public Object createObject();

	/**
	 * Updates entries in an implementation-specific object, as created by
	 * {@link #createObject()}.
	 * 
	 * @param object
	 *        The implementation-specific object
	 * @param key
	 *        The key
	 * @param value
	 *        The value
	 */
	public void putInObject( Object object, String key, Object value );

	/**
	 * Creates an implementation-specific array, equivalent to a JSON "[...]".
	 * 
	 * @param length
	 *        The array's length
	 * @return An implementation-specific array
	 */
	public Object createArray( int length );

	/**
	 * Sets items in an implementation-specific array, as created by
	 * {@link #createArray(int)}.
	 * 
	 * @param object
	 *        The implementation-specific array
	 * @param index
	 *        The index
	 * @param value
	 *        The value
	 */
	public void setInArray( Object object, int index, Object value );

	/**
	 * Creates an implementation-specific string.
	 * 
	 * @param value
	 *        The string
	 * @return An implementation-specific string
	 */
	public Object createString( String value );

	/**
	 * Creates an implementation-specific double.
	 * 
	 * @param value
	 *        The double
	 * @return An implementation-specific double
	 */
	public Object createDouble( double value );

	/**
	 * Creates an implementation-specific integer.
	 * 
	 * @param value
	 *        The integer
	 * @return An implementation-specific integer
	 */
	public Object createInteger( int value );

	/**
	 * Creates an implementation-specific long.
	 * 
	 * @param value
	 *        The long
	 * @return An implementation-specific long
	 */
	public Object createLong( long value );
}
