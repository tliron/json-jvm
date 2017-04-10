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

import org.mozilla.javascript.ConsString;

import com.threecrickets.jvm.json.JsonContext;
import com.threecrickets.jvm.json.JsonEncoder;
import com.threecrickets.jvm.json.generic.CharSequenceEncoder;

/**
 * A JSON encoder for Rhino's native {@link ConsString}.
 * <p>
 * Here only for completion: actually, {@link CharSequenceEncoder} would also be
 * able to encode a {@link ConsString}.
 * 
 * @author Tal Liron
 */
public class ConsStringEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof ConsString;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		context.quoted( (ConsString) object );
	}
}
