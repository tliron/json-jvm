/**
 * Copyright 2010-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jvm.json.nashorn;

import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.objects.NativeString;
import jdk.nashorn.internal.objects.annotations.Function;
import jdk.nashorn.internal.runtime.ConsString;
import jdk.nashorn.internal.runtime.NumberToString;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.Undefined;
import jdk.nashorn.internal.runtime.arrays.ArrayData;

import com.threecrickets.jvm.json.JsonException;
import com.threecrickets.jvm.json.JsonImplementation;
import com.threecrickets.jvm.json.nashorn.util.NashornTokener;
import com.threecrickets.jvm.json.util.JavaScriptUtil;
import com.threecrickets.jvm.json.util.Literal;

/**
 * Conversion between native Nashorn values and JSON. Extensible using a
 * {@link NashornJsonExtender}.
 * <p>
 * Recognizes Rhino's {@link NativeArray}, {@link NativeString},
 * {@link ConsString}, {@link Undefined}, {@link ScriptObject} and
 * {@link Function}.
 * <p>
 * Also recognizes JVM types: {@link Map}, {@link Collection}, {@link Date},
 * {@link Pattern} and primitives.
 * 
 * @author Tal Liron
 */
public class NashornJsonImplementation implements JsonImplementation
{
	//
	// Construction
	//

	public NashornJsonImplementation()
	{
		this( null );
	}

	public NashornJsonImplementation( NashornJsonExtender jsonExtender )
	{
		// Force a NoClassDefFoundError if Nashorn is not available
		ScriptObject.class.getClass();

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
		NashornTokener tokener = new NashornTokener( json );
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
			ArrayData arrayData = ( (NativeArray) object ).getArray();
			int length = (int) arrayData.length();

			for( int i = 0; i < length; i++ )
			{
				Object value = arrayData.getObject( i );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					arrayData.set( i, converted, false );
			}
		}
		else if( object instanceof ScriptObject )
		{
			ScriptObject scriptObject = (ScriptObject) object;

			Object r = jsonExtender.from( scriptObject, true );
			if( r != null )
				return r;

			// Convert regular Nashorn object

			for( String key : scriptObject.getOwnKeys( true ) )
			{
				Object value = scriptObject.get( key );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					scriptObject.put( key, converted, false );
			}
		}

		return object;
	}

	public Object createTokener( Reader reader )
	{
		return new NashornTokener( reader );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final NashornJsonExtender jsonExtender;

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
			s.append( NumberToString.stringFor( (Double) object ) );
		else if( ( object instanceof Number ) || ( object instanceof Boolean ) )
			s.append( object );
		else if( object instanceof Collection )
			encodeCollection( s, (Collection<?>) object, allowCode, depth );
		else if( object instanceof Map )
			encodeMap( s, (Map<?, ?>) object, allowCode, depth );
		else if( object instanceof NativeArray )
			encodeNativeArray( s, (NativeArray) object, allowCode, depth );
		else if( ( object instanceof String ) || ( object instanceof NativeString ) || ( object instanceof ConsString ) )
		{
			s.append( '\"' );
			s.append( JavaScriptUtil.escape( object.toString() ) );
			s.append( '\"' );
		}
		else if( object instanceof ScriptFunction )
		{
			s.append( '\"' );
			s.append( JavaScriptUtil.escape( ( (ScriptFunction) object ).toSource() ) );
			s.append( '\"' );
		}
		else if( object instanceof ScriptObject )
		{
			encodeScriptObject( s, (ScriptObject) object, allowCode, depth );
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

		ArrayData data = nativeArray.getArray();
		long length = data.length();
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				Object value = data.getObject( i );

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

	private void encodeScriptObject( StringBuilder s, ScriptObject scriptObject, boolean javaScript, int depth )
	{
		s.append( '{' );

		String[] keys = scriptObject.getOwnKeys( true );
		int length = keys.length;
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				String key = keys[i];
				Object value = scriptObject.get( key );

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
