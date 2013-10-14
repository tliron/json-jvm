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

package com.threecrickets.jvm.json.rhino;

import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.threecrickets.jvm.json.JsonException;
import com.threecrickets.jvm.json.JsonImplementation;
import com.threecrickets.jvm.json.rhino.util.RhinoTokener;
import com.threecrickets.jvm.json.util.JavaScriptUtil;
import com.threecrickets.jvm.json.util.Literal;

/**
 * Conversion between native Rhino values and JSON. Extensible using a
 * {@link RhinoJsonExtender}.
 * <p>
 * Recognizes Rhino's {@link NativeArray}, {@link NativeJavaObject},
 * org.mozilla.javascript.NativeString, {@link ConsString}, {@link Undefined},
 * {@link ScriptableObject} and {@link Function}.
 * <p>
 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
 * {@link Pattern} and primitives.
 * 
 * @author Tal Liron
 */
public class RhinoJsonImplementation implements JsonImplementation
{
	//
	// Construction
	//

	public RhinoJsonImplementation()
	{
		this( null );
	}

	public RhinoJsonImplementation( RhinoJsonExtender jsonExtender )
	{
		this.jsonExtender = jsonExtender;
	}

	//
	// JsonImplementation
	//

	public Object from( String json ) throws JsonException
	{
		return from( json, false );
	}

	public Object from( String json, boolean extendedJSON ) throws JsonException
	{
		RhinoTokener tokener = new RhinoTokener( json );
		Object object = tokener.createNative();
		if( extendedJSON )
			object = fromExtendedJSON( object );
		return object;
	}

	public String to( Object object )
	{
		return to( object, false, false );
	}

	public String to( Object object, boolean indent )
	{
		return to( object, indent, false );
	}

	public String to( Object object, boolean indent, boolean allowCode )
	{
		StringBuilder s = new StringBuilder();
		encode( s, object, allowCode, indent, indent ? 0 : -1 );
		return s.toString();
	}

	public Object fromExtendedJSON( Object object )
	{
		if( jsonExtender == null )
			return object;

		if( object instanceof NativeArray )
		{
			NativeArray nativeArray = (NativeArray) object;
			int length = (int) nativeArray.getLength();

			for( int i = 0; i < length; i++ )
			{
				Object value = ScriptableObject.getProperty( nativeArray, i );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					ScriptableObject.putProperty( nativeArray, i, converted );
			}
		}
		else if( object instanceof ScriptableObject )
		{
			ScriptableObject scriptableObject = (ScriptableObject) object;

			Object r = jsonExtender.from( scriptableObject, true );
			if( r != null )
				return r;

			// Convert regular Rhino object

			Object[] ids = scriptableObject.getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = ScriptableObject.getProperty( scriptableObject, key );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					ScriptableObject.putProperty( scriptableObject, key, converted );
			}
		}

		return object;
	}

	public Object createTokener( Reader reader )
	{
		return new RhinoTokener( reader );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final RhinoJsonExtender jsonExtender;

	private void encode( StringBuilder s, Object object, boolean allowCode, boolean indent, int depth )
	{
		if( indent )
			indent( s, depth );

		if( jsonExtender != null )
		{
			Object r = jsonExtender.to( object, false, allowCode );
			if( r != null )
			{
				if( r instanceof Literal )
					s.append( ( (Literal) r ).toString( depth ) );
				else
					encode( s, r, indent, allowCode, depth );
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

			encode( s, ( (NativeJavaObject) object ).unwrap(), allowCode, false, depth );
		}
		else if( object instanceof Collection )
			encodeCollection( s, (Collection<?>) object, allowCode, depth );
		else if( object instanceof Map )
			encodeMap( s, (Map<?, ?>) object, allowCode, depth );
		else if( object instanceof NativeArray )
			encodeNativeArray( s, (NativeArray) object, allowCode, depth );
		else if( object instanceof ScriptableObject )
		{
			ScriptableObject scriptableObject = (ScriptableObject) object;
			String className = scriptableObject.getClassName();
			if( className.equals( "String" ) )
			{
				// Unpack NativeString (private class) or ConsString

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( scriptableObject.toString() ) );
				s.append( '\"' );
			}
			else if( className.equals( "Function" ) )
			{
				// Trying to encode functions can result in stack overflows...

				s.append( '\"' );
				s.append( JavaScriptUtil.escape( scriptableObject.toString() ) );
				s.append( '\"' );
			}
			else
				encodeScriptableObject( s, scriptableObject, allowCode, depth );
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

	private void encodeNativeArray( StringBuilder s, NativeArray nativeArray, boolean javaScript, int depth )
	{
		s.append( '[' );

		long length = nativeArray.getLength();
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				Object value = ScriptableObject.getProperty( nativeArray, i );

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

	private void encodeScriptableObject( StringBuilder s, ScriptableObject scriptableObject, boolean javaScript, int depth )
	{
		s.append( '{' );

		Object[] ids = scriptableObject.getAllIds();
		int length = ids.length;
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				String key = ids[i].toString();
				Object value = ScriptableObject.getProperty( scriptableObject, key );

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
