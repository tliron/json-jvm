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

import java.io.IOException;

import org.mozilla.javascript.Scriptable;

import com.threecrickets.jvm.json.JsonContext;
import com.threecrickets.jvm.json.JsonEncoder;
import com.threecrickets.jvm.json.util.JsonUtil;

/**
 * A JSON encoder for Rhino's NativeNumber (the class is private in Rhino).
 * 
 * @author Tal Liron
 */
public class NativeNumberEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return ( object instanceof Scriptable ) && ( (Scriptable) object ).getClassName().equals( "Number" );
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		Scriptable nativeNumber = (Scriptable) object;
		Double number = (Double) nativeNumber.getDefaultValue( Double.class );
		context.out.append( JsonUtil.numberToString( number ) );
	}
}
