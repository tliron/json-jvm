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

import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import com.threecrickets.jvm.json.nashorn.NashornJsonImplementation;
import com.threecrickets.jvm.json.rhino.RhinoJsonImplementation;

/**
 * Conversion between native JVM language objects and JSON.
 * <p>
 * This class can be used directly in JVM languages.
 * 
 * @author Tal Liron
 */
public class JSON
{
	//
	// Static attributes
	//

	public static JsonImplementation getImplementation()
	{
		return implementation;
	}

	public static void setImplementation( JsonImplementation implementation )
	{
		JSON.implementation = implementation;
	}

	//
	// Static operations
	//

	/**
	 * Recursively convert from JSON into native values.
	 * <p>
	 * Creates native dicts, arrays and primitives.
	 * 
	 * @param json
	 *        The JSON string
	 * @return A native object or array
	 * @throws JsonException
	 *         In case of a JSON conversion error
	 */
	public static Object from( String json ) throws JsonException
	{
		return getImplementation().from( json );
	}

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
	 * @throws JsonException
	 *         In case of a JSON conversion error
	 */
	public static Object from( String json, boolean extendedJSON ) throws JsonException
	{
		return getImplementation().from( json, extendedJSON );
	}

	/**
	 * Recursively convert from native values to JSON.
	 * <p>
	 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
	 * {@link Pattern} and primitives.
	 * 
	 * @param object
	 *        A native object or array
	 * @return The JSON string
	 * @see #fromExtendedJSON(Object)
	 */
	public static String to( Object object )
	{
		return getImplementation().to( object );
	}

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
	public static String to( Object object, boolean indent )
	{
		return getImplementation().to( object, indent );
	}

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
	public static String to( Object object, boolean indent, boolean allowCode )
	{
		return getImplementation().to( object, indent, allowCode );
	}

	/**
	 * Recursively converts extended JSON to native values.
	 * 
	 * @param object
	 *        A native object or array
	 * @return The converted object or the original
	 */
	public static Object fromExtendedJSON( Object object )
	{
		return getImplementation().fromExtendedJSON( object );
	}

	/**
	 * Creates a tokener.
	 * 
	 * @param reader
	 *        The reader
	 * @return A new tokener
	 */
	public static Object createTokener( Reader reader )
	{
		return getImplementation().createTokener( reader );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static volatile JsonImplementation implementation;

	static
	{
		try
		{
			implementation = new NashornJsonImplementation();
		}
		catch( NoClassDefFoundError x )
		{
			// Nashorn not available
			implementation = new RhinoJsonImplementation();
		}
		catch( UnsupportedClassVersionError x )
		{
			// Nashorn requires at least JVM 7
			implementation = new RhinoJsonImplementation();
		}
	}
}
