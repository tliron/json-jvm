/**
 * Copyright 2010-2016 Three Crickets LLC.
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
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

public interface JsonImplementationOld
{
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
	 * Recursively convert from JSON into native values.
	 * <p>
	 * Creates native dicts, arrays and primitives.
	 * 
	 * @param json
	 *        The JSON string
	 * @return A native object or array
	 * @throws JsonSyntaxError
	 *         In case of a JSON conversion error
	 */
	public Object from( String json ) throws JsonSyntaxError;

	/**
	 * Recursively convert from JSON into native values.
	 * <p>
	 * Creates native dicts, arrays and primitives.
	 * 
	 * @param json
	 *        The JSON string
	 * @param extendedJSON
	 *        Whether to convert extended JSON objects
	 * @return A native object or array
	 * @throws JsonSyntaxError
	 *         In case of a JSON conversion error
	 */
	public Object from( String json, boolean extendedJSON ) throws JsonSyntaxError;

	/**
	 * Recursively convert from native values to JSON.
	 * <p>
	 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
	 * {@link Pattern} and primitives.
	 * 
	 * @param object
	 *        A native object or array
	 * @return The JSON string
	 */
	public String to( Object object );

	/**
	 * Recursively convert from native values to JSON.
	 * <p>
	 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
	 * {@link Pattern} and primitives.
	 * 
	 * @param object
	 *        A native object or array
	 * @param indent
	 *        Whether to indent the JSON for human readability
	 * @return The JSON string
	 */
	public String to( Object object, boolean indent );

	/**
	 * Recursively convert from native values to JSON.
	 * <p>
	 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
	 * {@link Pattern} and primitives.
	 * 
	 * @param object
	 *        A native object or array
	 * @param indent
	 *        Whether to indent the JSON for human readability
	 * @param allowCode
	 *        True to allow language code (this will break JSON compatibility!)
	 * @return The JSON string
	 */
	public String to( Object object, boolean indent, boolean allowCode );

	/**
	 * Recursively converts extended JSON to native values.
	 * 
	 * @param object
	 *        A native object or array
	 * @return The converted object or the original
	 */
	public Object fromExtendedJSON( Object object );

	/**
	 * Creates a tokener.
	 * 
	 * @param reader
	 *        The reader
	 * @return A new tokener
	 */
	public Object createTokener( Reader reader );
}