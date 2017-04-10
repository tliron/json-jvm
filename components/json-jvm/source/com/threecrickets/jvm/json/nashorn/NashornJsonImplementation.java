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

package com.threecrickets.jvm.json.nashorn;

import java.util.Collection;

import com.threecrickets.jvm.json.BaseJsonImplementation;
import com.threecrickets.jvm.json.JsonEncoder;
import com.threecrickets.jvm.json.JsonTransformer;
import com.threecrickets.jvm.json.generic.GenericJsonImplementation;

import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.objects.NativeNumber;
import jdk.nashorn.internal.objects.NativeString;
import jdk.nashorn.internal.runtime.ScriptObject;

/**
 * A JSON implementation for the
 * <a href="http://openjdk.java.net/projects/nashorn/">Nashorn JavaScript
 * engine</a>. Uses {@link ScriptObject} for JSON objects and
 * {@link NativeArray} for JSON arrays, as well as Nashorn-specific primitives.
 * 
 * @author Tal Liron
 */
public class NashornJsonImplementation extends BaseJsonImplementation
{
	//
	// Static operations
	//

	public static void addEncoders( Collection<JsonEncoder> encoders )
	{
		encoders.add( new ConsStringEncoder() );
		encoders.add( new NativeArrayEncoder() );
		encoders.add( new NativeBooleanEncoder() );
		encoders.add( new NativeNumberEncoder() );
		encoders.add( new NativeStringEncoder() );
		encoders.add( new ScriptObjectEncoder() );
		encoders.add( new ScriptObjectMirrorEncoder() );
	}

	public static void addTransformers( Collection<JsonTransformer> transformers )
	{
		transformers.add( new ScriptObjectMirrorTransformer() );
	}

	//
	// JsonImplementation
	//

	public void initialize()
	{
		addEncoders( encoders );
		GenericJsonImplementation.addEncoders( encoders );
		addTransformers( transformers );
	}

	public String getName()
	{
		return "Nashorn";
	}

	public Object createObject()
	{
		return Global.newEmptyInstance();
	}

	public void putInObject( Object object, String key, Object value )
	{
		( (ScriptObject) object ).put( key, value, true );
	}

	public Object createArray( int length )
	{
		return NativeArray.construct( true, null, length );
	}

	public void setInArray( Object object, int index, Object value )
	{
		( (NativeArray) object ).set( index, value, 0 );
	}

	public Object createString( String value )
	{
		return NativeString.constructor( true, null, value );
	}

	public Object createDouble( double value )
	{
		return NativeNumber.constructor( true, null, value );
	}

	public Object createInteger( int value )
	{
		return Global.toObject( value );
	}

	public Object createLong( long value )
	{
		return Global.toObject( value );
	}
}
