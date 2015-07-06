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

package com.threecrickets.jvm.json.nashorn;

import java.io.IOException;

import com.threecrickets.jvm.json.JsonContext;
import com.threecrickets.jvm.json.JsonEncoder;

import jdk.nashorn.internal.runtime.ScriptObject;

/**
 * A JSON encoder for Nashorn's native {@link ScriptObject}.
 * 
 * @author Tal Liron
 */
public class ScriptObjectEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof ScriptObject;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		ScriptObject scriptObject = (ScriptObject) object;

		context.out.append( '{' );

		String[] keys = scriptObject.getOwnKeys( true );
		int length = keys.length;
		if( length > 0 )
		{
			context.newline();

			for( int i = 0; i < length; i++ )
			{
				String key = keys[i];
				Object value = scriptObject.get( key );

				context.indentNested();
				context.quoted( key );
				context.colon();
				context.nest().encode( value );

				if( i < length - 1 )
					context.comma();
			}

			context.newline();
			context.indent();
		}

		context.out.append( '}' );
	}
}
