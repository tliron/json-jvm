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

package com.threecrickets.jvm.json.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threecrickets.jvm.json.BaseJsonImplementation;
import com.threecrickets.jvm.json.JsonEncoder;

/**
 * A JSON implementation using standard JVM types, specifically
 * {@link LinkedHashMap} for JSON objects and {@link ArrayList} for JSON arrays.
 * 
 * @author Tal Liron
 */
public class GenericJsonImplementation extends BaseJsonImplementation
{
	//
	// Static operations
	//

	public static void addEncoders( Collection<JsonEncoder> encoders )
	{
		encoders.add( new BooleanEncoder() );
		encoders.add( new CharSequenceEncoder() );
		encoders.add( new CollectionEncoder() );
		encoders.add( new MapEncoder() );
		encoders.add( new NullEncoder() );
		encoders.add( new NumberEncoder() );
	}

	//
	// JsonImplementation
	//

	public void initialize()
	{
		addEncoders( encoders );
	}

	public String getName()
	{
		return null;
	}

	public Object createObject()
	{
		return new LinkedHashMap<String, Object>();
	}

	@SuppressWarnings("unchecked")
	public void putInObject( Object object, String key, Object value )
	{
		( (Map<String, Object>) object ).put( key, value );
	}

	public Object createArray( int length )
	{
		return new ArrayList<Object>( length );
	}

	@SuppressWarnings("unchecked")
	public void setInArray( Object object, int index, Object value )
	{
		List<Object> list = (List<Object>) object;
		if( index == list.size() )
			list.add( value );
		else
			list.set( index, value );
	}

	public Object createString( String value )
	{
		return value;
	}

	public Object createDouble( double value )
	{
		return new Double( value );
	}

	public Object createInteger( int value )
	{
		return new Integer( value );
	}

	public Object createLong( long value )
	{
		return new Long( value );
	}
}
