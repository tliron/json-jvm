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

package com.threecrickets.jvm.json.rhino;

import java.io.IOException;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Wrapper;

import com.threecrickets.jvm.json.JsonContext;
import com.threecrickets.jvm.json.JsonEncoder;

/**
 * A JSON encoder for Rhino's {@link Wrapper}. Unwraps the object and delegates
 * to the appropriate encoder.
 * 
 * @author Tal Liron
 */
public class WrapperEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof NativeJavaObject;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		Wrapper wrapper = (Wrapper) object;
		Object wrapped = wrapper.unwrap();
		context.encode( wrapped );
	}
}
