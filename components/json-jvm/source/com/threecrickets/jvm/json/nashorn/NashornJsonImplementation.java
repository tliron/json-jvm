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

package com.threecrickets.jvm.json.nashorn;

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
import jdk.nashorn.internal.runtime.Property;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.Undefined;
import jdk.nashorn.internal.runtime.arrays.ArrayData;

import com.threecrickets.jvm.json.JsonException;
import com.threecrickets.jvm.json.JsonImplementation;
import com.threecrickets.jvm.json.nashorn.util.JsonTokener;
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
		JsonTokener tokener = new JsonTokener( json );
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

	public String to( Object object, boolean indent, boolean javaScript )
	{
		StringBuilder s = new StringBuilder();
		encode( s, object, javaScript, indent, indent ? 0 : -1 );
		return s.toString();
	}

	public Object fromExtendedJSON( Object object )
	{
		if( jsonExtender == null )
			return object;

		if( object instanceof NativeArray )
		{
			ArrayData array = ( (NativeArray) object ).getArray();
			int length = (int) array.length();

			for( int i = 0; i < length; i++ )
			{
				Object value = array.getObject( i );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					array.set( i, converted, false );
			}
		}
		else if( object instanceof ScriptObject )
		{
			ScriptObject script = (ScriptObject) object;

			Object r = jsonExtender.from( script, true );
			if( r != null )
				return r;

			// Convert regular Nashorn object

			for( Iterator<String> i = script.propertyIterator(); i.hasNext(); )
			{
				String key = i.next();
				Object value = script.get( key );
				Object converted = fromExtendedJSON( value );
				if( converted != value )
					script.put( key, converted, false );
			}
		}

		return object;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final NashornJsonExtender jsonExtender;

	private void encode( StringBuilder s, Object object, boolean javaScript, boolean indent, int depth )
	{
		if( indent )
			indent( s, depth );

		if( jsonExtender != null )
		{
			Object r = jsonExtender.to( object, false, javaScript );
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
			s.append( NumberToString.stringFor( (Double) object ) );
		else if( ( object instanceof Number ) || ( object instanceof Boolean ) )
			s.append( object );
		else if( object instanceof Collection )
			encodeCollection( s, (Collection<?>) object, javaScript, depth );
		else if( object instanceof Map )
			encodeMap( s, (Map<?, ?>) object, javaScript, depth );
		else if( object instanceof NativeArray )
			encodeNativeArray( s, (NativeArray) object, javaScript, depth );
		else if( ( object instanceof NativeString ) || ( object instanceof ConsString ) )
		{
			s.append( '\"' );
			s.append( JavaScriptUtil.escape( object.toString() ) );
			s.append( '\"' );
		}
		else if( object instanceof ScriptFunction )
		{
			s.append( '\"' );
			s.append( JavaScriptUtil.escape( object.toString() ) );
			s.append( '\"' );
		}
		else if( object instanceof ScriptObject )
			encodeScriptableObject( s, (ScriptObject) object, javaScript, depth );
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

		ArrayData data = array.getArray();
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

	private void encodeScriptableObject( StringBuilder s, ScriptObject object, boolean javaScript, int depth )
	{
		s.append( '{' );

		Property[] properties = object.getMap().getProperties();
		int length = properties.length;
		if( length > 0 )
		{
			if( depth > -1 )
				s.append( '\n' );

			for( int i = 0; i < length; i++ )
			{
				String key = properties[i].getKey();
				Object value = object.get( key );

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
