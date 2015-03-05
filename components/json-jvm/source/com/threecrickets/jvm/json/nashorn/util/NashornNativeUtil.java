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

package com.threecrickets.jvm.json.nashorn.util;

import java.util.Date;
import java.util.regex.Pattern;

import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.objects.NativeDate;
import jdk.nashorn.internal.objects.NativeNumber;
import jdk.nashorn.internal.objects.NativeRegExp;
import jdk.nashorn.internal.objects.NativeString;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;

/**
 * Conversion between native Nashorn values and JVM equivalents.
 * 
 * @author Tal Liron
 */
public class NashornNativeUtil
{
	//
	// Static operations
	//

	public static ScriptObject to( Number number )
	{
		return toNumber( number );
	}

	public static NativeString to( String value )
	{
		return (NativeString) NativeString.constructor( true, null, value );
	}

	public static NativeDate to( Date date )
	{
		return (NativeDate) NativeDate.construct( true, null, date.getTime() );
	}

	public static NativeRegExp toRegExp( String source, String optionsString )
	{
		return (NativeRegExp) NativeRegExp.constructor( true, null, source, optionsString );
	}

	public static NativeRegExp to( Pattern pattern )
	{
		String regex = pattern.toString();

		// Note: JVM pattern does not support a "g" flag

		int flags = pattern.flags();
		String options = "";
		if( ( flags & Pattern.CASE_INSENSITIVE ) != 0 )
			options += 'i';
		if( ( flags & Pattern.MULTILINE ) != 0 )
			options += 'm';

		return toRegExp( regex, options );
	}

	public static NativeNumber toNumber( Object value )
	{
		return (NativeNumber) NativeNumber.constructor( true, null, value );
	}

	public static ScriptFunction toFunction( Object value )
	{
		// TODO
		return null;
	}

	public static Object from( ScriptObject scriptObject )
	{
		if( scriptObject instanceof NativeDate )
		{
			Double time = NativeDate.getTime( scriptObject );
			return new Date( time.longValue() );
		}
		else if( scriptObject instanceof NativeString )
			return scriptObject.toString();

		return null;
	}

	public static String[] from( NativeRegExp nativeRegExp )
	{
		Object source = nativeRegExp.get( "source" );

		Object isGlobal = nativeRegExp.get( "global" );
		Object isIgnoreCase = nativeRegExp.get( "ignoreCase" );
		Object isMultiLine = nativeRegExp.get( "multiline" );

		// Note: JVM pattern does not support a "g" flag. Also, compiling
		// the pattern here is a waste of time.
		//
		// int flags = 0;
		// if( ( isIgnoreCase instanceof Boolean ) && ( ( (Boolean)
		// isIgnoreCase ).booleanValue() ) )
		// flags |= Pattern.CASE_INSENSITIVE;
		// if( ( isMultiLine instanceof Boolean ) && ( ( (Boolean)
		// isMultiLine ).booleanValue() ) )
		// flags |= Pattern.MULTILINE;
		// return Pattern.compile( source.toString(), flags );

		String options = "";
		if( ( isGlobal instanceof Boolean ) && ( ( (Boolean) isGlobal ).booleanValue() ) )
			options += "g";
		if( ( isIgnoreCase instanceof Boolean ) && ( ( (Boolean) isIgnoreCase ).booleanValue() ) )
			options += "i";
		if( ( isMultiLine instanceof Boolean ) && ( ( (Boolean) isMultiLine ).booleanValue() ) )
			options += "m";

		return new String[]
		{
			source.toString(), options
		};
	}

	public static ScriptObject newObject()
	{
		return Global.newEmptyInstance();
	}

	public static NativeArray newArray( int length )
	{
		return (NativeArray) NativeArray.construct( false, null, length );
	}

	public static ScriptObject wrap( Object value )
	{
		return (ScriptObject) Global.toObject( value );
	}
}
