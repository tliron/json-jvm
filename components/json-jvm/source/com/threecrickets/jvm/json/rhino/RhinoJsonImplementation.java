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

package com.threecrickets.jvm.json.rhino;

import java.util.Collection;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.threecrickets.jvm.json.BaseJsonImplementation;
import com.threecrickets.jvm.json.JsonEncoder;
import com.threecrickets.jvm.json.JsonTransformer;
import com.threecrickets.jvm.json.generic.GenericJsonImplementation;

/**
 * A JSON implementation for the
 * <a href="https://github.com/mozilla/rhino">Rhino JavaScript engine</a>. Uses
 * {@link ScriptableObject} for JSON objects and {@link NativeArray} for JSON
 * arrays, as well as Rhino-specific primitives.
 * 
 * @author Tal Liron
 */
public class RhinoJsonImplementation extends BaseJsonImplementation
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
		encoders.add( new WrapperEncoder() );
		// Make sure ScriptableEncoder is last
		encoders.add( new ScriptableEncoder() );
	}

	public static void addTransformers( Collection<JsonTransformer> transformers )
	{
		transformers.add( new WrapperTransformer() );
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
		return "Rhino";
	}

	public Object createObject()
	{
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newObject( scope );
	}

	public void putInObject( Object object, String key, Object value )
	{
		Scriptable scriptable = (Scriptable) object;
		scriptable.put( key, scriptable, value );
	}

	public Object createArray( int length )
	{
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newArray( scope, length );
	}

	public void setInArray( Object object, int index, Object value )
	{
		Scriptable scriptable = (Scriptable) object;
		scriptable.put( index, scriptable, value );
	}

	public Object createString( String value )
	{
		// NativeString is private in Rhino, so we create it indirectly
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newObject( scope, "String", new Object[]
		{
			value
		} );
	}

	public Object createDouble( double value )
	{
		// NativeNumber is private in Rhino, so we create it indirectly
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newObject( scope, "Number", new Object[]
		{
			value
		} );
	}

	public Object createInteger( int value )
	{
		// NativeNumber is private in Rhino, so we create it indirectly
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newObject( scope, "Number", new Object[]
		{
			value
		} );
	}

	public Object createLong( long value )
	{
		// NativeNumber is private in Rhino, so we create it indirectly
		Context context = Context.getCurrentContext();
		Scriptable scope = ScriptRuntime.getTopCallScope( context );
		return context.newObject( scope, "Number", new Object[]
		{
			value
		} );
	}
}
