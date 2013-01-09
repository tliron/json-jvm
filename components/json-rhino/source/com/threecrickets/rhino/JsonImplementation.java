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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.threecrickets.rhino.internal.JsonException;
import com.threecrickets.rhino.internal.JsonTokener;
import com.threecrickets.rhino.util.JavaScriptUtil;
import com.threecrickets.rhino.util.Literal;

/**
 * Conversion between native Rhino objects and JSON, with support for <a
 * href="http://www.mongodb.org/display/DOCS/Mongo+Extended+JSON">MongoDB's
 * extended JSON format</a>.
 * <p>
 * This class can be used directly in Rhino.
 * 
 * @author Tal Liron
 */
public class JsonImplementation
{
	//
	// Construction
	//

	public JsonImplementation()
	{
		extendedJson = null;
	}

	public JsonImplementation( JsonExtender extendedJson )
	{
		this.extendedJson = extendedJson;
	}

	//
	// Operations
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
	public Object from( String json ) throws JsonException
	{
		return from( json, false );
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
	public Object from( String json, boolean extendedJSON ) throws JsonException
	{
		JsonTokener tokener = new JsonTokener( json );
		Object object = tokener.createNative();
		if( extendedJSON )
			object = fromExtendedJSON( object );
		return object;
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
	public String to( Object object )
	{
		return to( object, false, false );
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
	public String to( Object object, boolean indent )
	{
		return to( object, indent, false );
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
	public String to( Object object, boolean indent, boolean javaScript )
	{
		StringBuilder s = new StringBuilder();
		encode( s, object, javaScript, indent, indent ? 0 : -1 );
		return s.toString();
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
	public Object fromExtendedJSON( Object object )
	{
		if( object instanceof NativeArray )
		{
			NativeArray array = (NativeArray) object;
			int length = (int) array.getLength();

			for( int i = 0; i < length; i++ )
			{
				Object value = ScriptableObject.getProperty( array, i );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					ScriptableObject.putProperty( array, i, converted );
			}
		}
		else if( object instanceof ScriptableObject )
		{
			ScriptableObject scriptable = (ScriptableObject) object;

			if( extendedJson != null )
			{
				Object r = extendedJson.from( scriptable, true );
				if( r != null )
					return r;
			}

			// Convert regular Rhino object

			Object[] ids = scriptable.getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = ScriptableObject.getProperty( scriptable, key );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					ScriptableObject.putProperty( scriptable, key, converted );
			}
		}

		return object;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final JsonExtender extendedJson;

	private void encode( StringBuilder s, Object object, boolean javaScript, boolean indent, int depth )
	{
		if( indent )
			indent( s, depth );

		if( extendedJson != null )
		{
			Object r = extendedJson.to( object, false, javaScript );
			if( r != null )
			{
				if( r instanceof Literal )
					s.append( ( (Literal) r ).toString( depth ) );
				else
					encode( s, r, indent, javaScript, depth );
				return;
			}
		}

		if( ( object == null ) || ( object instanceof Undefined ) )
			s.append( "null" );
		else if( object instanceof Double )
			s.append( ScriptRuntime.numberToString( (Double) object, 10 ) );
		else if( ( object instanceof Number ) || ( object instanceof Boolean ) )
			s.append( object );
		else if( object instanceof NativeJavaObject )
		{
			// This happens either because the developer purposely created a
			// Java object, or because it was returned from a Java call and
			// wrapped by Rhino.

			encode( s, ( (NativeJavaObject) object ).unwrap(), javaScript, false, depth );
		}
		else if( object instanceof Collection )
			encodeCollection( s, (Collection<?>) object, javaScript, depth );
		else if( object instanceof Map )
			encodeMap( s, (Map<?, ?>) object, javaScript, depth );
		else if( object instanceof NativeArray )
			encodeNativeArray( s, (NativeArray) object, javaScript, depth );
		else if( object instanceof ScriptableObject )
		{
			ScriptableObject scriptable = (ScriptableObject) object;
			String className = scriptable.getClassName();
			if( className.equals( "String" ) )
			{
				// Unpack NativeString

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( object.toString() ) );
				s.append( '\"' );
			}
			else if( className.equals( "Function" ) )
			{
				// Trying to encode functions can result in stack overflows...

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( object.toString() ) );
				s.append( '\"' );
			}
			else
				encodeScriptableObject( s, scriptable, javaScript, depth );
		}
		else
		{
			s.append( '\"' );
			s.append( JavaScriptUtil.escape( object.toString() ) );
			s.append( '\"' );
		}
	}

	private void encodeCollection( StringBuilder s, Collection<?> collection, boolean javaScript, int depth )
	{
		s.append( '[' );

		Iterator<?> i = collection.iterator();
		if( i.hasNext() )
		{
			if( depth > -1 )
				s.append( '\n' );

			while( true )
			{
				Object value = i.next();

				encode( s, value, javaScript, true, depth > -1 ? depth + 1 : -1 );

				if( i.hasNext() )
				{
					s.append( ',' );
					if( depth > -1 )
						s.append( '\n' );
				}
				else
					break;
			}

			if( depth > -1 )
			{
				s.append( '\n' );
				indent( s, depth );
			}
		}

		s.append( ']' );
	}

	private void encodeMap( StringBuilder s, Map<?, ?> map, boolean javaScript, int depth )
	{
		s.append( '{' );

		Iterator<?> i = map.entrySet().iterator();
		if( i.hasNext() )
		{
			if( depth > -1 )
				s.append( '\n' );

			while( true )
			{
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
				String key = entry.getKey().toString();
				Object value = entry.getValue();

				if( depth > -1 )
					indent( s, depth + 1 );

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( key ) );
				s.append( "\":" );

				if( depth > -1 )
					s.append( ' ' );

				encode( s, value, javaScript, false, depth > -1 ? depth + 1 : -1 );

				if( i.hasNext() )
				{
					s.append( ',' );
					if( depth > -1 )
						s.append( '\n' );
				}
				else
					break;
			}

			if( depth > -1 )
			{
				s.append( '\n' );
				indent( s, depth );
			}
		}

		s.append( '}' );
	}

	private void encodeNativeArray( StringBuilder s, NativeArray array, boolean javaScript, int depth )
	{
		s.append( '[' );

		long length = array.getLength();
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				Object value = ScriptableObject.getProperty( array, i );

				encode( s, value, javaScript, true, depth > -1 ? depth + 1 : -1 );

				if( i < length - 1 )
				{
					s.append( ',' );
					if( depth > -1 )
						s.append( '\n' );
				}
			}

			if( depth > -1 )
			{
				s.append( '\n' );
				indent( s, depth );
			}
		}

		s.append( ']' );
	}

	private void encodeScriptableObject( StringBuilder s, ScriptableObject object, boolean javaScript, int depth )
	{
		s.append( '{' );

		Object[] ids = object.getAllIds();
		int length = ids.length;
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				String key = ids[i].toString();
				Object value = ScriptableObject.getProperty( object, key );

				if( depth > -1 )
					indent( s, depth + 1 );

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( key ) );
				s.append( "\":" );

				if( depth > -1 )
					s.append( ' ' );

				encode( s, value, javaScript, false, depth > -1 ? depth + 1 : -1 );

				if( i < length - 1 )
				{
					s.append( ',' );
					if( depth > -1 )
						s.append( '\n' );
				}
			}

			if( depth > -1 )
			{
				s.append( '\n' );
				indent( s, depth );
			}
		}

		s.append( '}' );
	}

	private static void indent( StringBuilder s, int depth )
	{
		for( int i = depth - 1; i >= 0; i-- )
			s.append( "  " );
	}
}
