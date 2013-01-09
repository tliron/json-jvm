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
 * Conversion between native Rhino objects and JSON, with support for <a
 * href="http://www.mongodb.org/display/DOCS/Mongo+Extended+JSON">MongoDB's
 * extended JSON format</a>.
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
	 * <p>
	 * Can optionally recognize MongoDB's extended JSON: {$oid:'objectid'},
	 * {$binary:'base64',$type:'hex'}, {$ref:'collection',$id:'objectid'},
	 * {$date:timestamp}, {$regex:'pattern',$options:'options'} and
	 * {$long:'integer'}, {$function:'source'}.
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
	 * Recursively convert from native JavaScript, a few JVM types and BSON
	 * types to extended JSON.
	 * <p>
	 * Recognizes JavaScript objects, arrays, Date objects, RegExp objects,
	 * Function objects and primitives.
	 * <p>
	 * Recognizes JVM types: java.util.Map, java.util.Collection,
	 * java.util.Date, java.util.regex.Pattern and java.lang.Long.
	 * <p>
	 * Recognizes BSON types: ObjectId, Binary and DBRef.
	 * <p>
	 * Note that java.lang.Long will be converted only if necessary in order to
	 * preserve its value when converted to a JavaScript Number object.
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
	 * Recursively convert from native JavaScript, a few JVM types and BSON
	 * types to extended JSON.
	 * <p>
	 * Recognizes JavaScript objects, arrays, Date objects, RegExp objects,
	 * Function objects and primitives.
	 * <p>
	 * Recognizes JVM types: java.util.Map, java.util.Collection,
	 * java.util.Date, java.util.regex.Pattern and java.lang.Long.
	 * <p>
	 * Recognizes BSON types: ObjectId, Binary and DBRef.
	 * <p>
	 * Note that java.lang.Long will be converted only if necessary in order to
	 * preserve its value when converted to a JavaScript Number object.
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
	 * Recursively convert from native JavaScript, a few JVM types and BSON
	 * types to extended JSON.
	 * <p>
	 * Recognizes JavaScript objects, arrays, Date objects, RegExp objects,
	 * Function objects and primitives.
	 * <p>
	 * Recognizes JVM types: java.util.Map, java.util.Collection,
	 * java.util.Date, java.util.regex.Pattern and java.lang.Long.
	 * <p>
	 * Recognizes BSON types: ObjectId, Binary and DBRef.
	 * <p>
	 * Note that java.lang.Long will be converted only if necessary in order to
	 * preserve its value when converted to a JavaScript Number object.
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
	 * Recursively converts MongoDB's extended JSON to native JavaScript or
	 * native BSON types.
	 * <p>
	 * Converts {$date:timestamp} objects to JavaScript Date objects and
	 * {$regex:'pattern',$options:'options'} to JavaScript RegExp objects.
	 * <p>
	 * The following BSON types are supported: {$oid:'objectid'},
	 * {$binary:'base64',$type:'hex'} and {$ref:'collection',$id:'objectid'}.
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
