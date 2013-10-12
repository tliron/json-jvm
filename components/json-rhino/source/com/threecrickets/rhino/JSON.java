/**
 * Copyright 2010-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.rhino;

import org.mozilla.javascript.ScriptableObject;

import com.threecrickets.rhino.internal.JsonException;

/**
 * Conversion between native Rhino (JavaScript) objects and JSON.
 * <p>
 * This class can be used directly in Rhino.
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
	 * Recursively convert from JSON into native JavaScript values.
	 * <p>
	 * Creates JavaScript objects, arrays and primitives.
	 * 
	 * @param json
	 *        The JSON string
	 * @return A JavaScript object or array
	 * @throws JsonException
	 */
	public static Object from( String json ) throws JsonException
	{
		return getImplementation().from( json );
	}

	/**
	 * Recursively convert from JSON into native JavaScript values.
	 * <p>
	 * Creates JavaScript objects, arrays and primitives.
	 * 
	 * @param json
	 *        The JSON string
	 * @param extendedJSON
	 *        Whether to convert extended JSON objects
	 * @return A JavaScript object or array
	 * @throws JsonException
	 */
	public static Object from( String json, boolean extendedJSON ) throws JsonException
	{
		return getImplementation().from( json, extendedJSON );
	}

	/**
	 * Recursively convert from native JavaScript to JSON.
	 * 
	 * @param object
	 *        A native JavaScript object
	 * @return The JSON string
	 * @see #fromExtendedJSON(Object)
	 */
	public static String to( Object object )
	{
		return getImplementation().to( object );
	}

	/**
	 * Recursively convert from native JavaScript to JSON.
	 * 
	 * @param object
	 *        A native JavaScript object
	 * @param indent
	 *        Whether to indent the JSON for human readability
	 * @return The JSON string
	 * @see #fromExtendedJSON(Object)
	 */
	public static String to( Object object, boolean indent )
	{
		return getImplementation().to( object, indent );
	}

	/**
	 * Recursively convert from native JavaScript to JSON.
	 * 
	 * @param object
	 *        A native JavaScript object
	 * @param indent
	 *        Whether to indent the JSON for human readability
	 * @param javaScript
	 *        True to allow JavaScript literals (these will break JSON
	 *        compatibility!)
	 * @return The JSON string
	 * @see #fromExtendedJSON(Object)
	 */
	public static String to( Object object, boolean indent, boolean javaScript )
	{
		return getImplementation().to( object, indent, javaScript );
	}

	/**
	 * Recursively converts extended JSON to native JavaScript types.
	 * 
	 * @param object
	 *        A native JavaScript object or array
	 * @return The converted object or the original
	 * @see JsonExtender#from(ScriptableObject, boolean)
	 */
	public static Object fromExtendedJSON( Object object )
	{
		return getImplementation().fromExtendedJSON( object );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static volatile JsonImplementation implementation = new JsonImplementation();
}
