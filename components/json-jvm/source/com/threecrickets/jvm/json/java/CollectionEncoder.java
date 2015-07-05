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

package com.threecrickets.jvm.json.java;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.threecrickets.jvm.json.JsonContext;
import com.threecrickets.jvm.json.JsonEncoder;

public class CollectionEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof Collection;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		@SuppressWarnings("unchecked")
		Collection<Object> collection = (Collection<Object>) object;

		context.out.append( '[' );

		if( !collection.isEmpty() )
		{
			context.newline();

			for( Iterator<Object> i = collection.iterator(); i.hasNext(); )
			{
				Object value = i.next();

				context.indentNested();
				context.nest().encode( value );

				if( i.hasNext() )
					context.comma();
			}

			context.newline();
			context.indent();
		}

		context.out.append( ']' );
	}
}
