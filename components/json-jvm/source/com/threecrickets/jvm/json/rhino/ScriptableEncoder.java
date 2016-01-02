/**
 * Copyright 2010-2016 Three Crickets LLC.
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

/**
 * A JSON encoder for Rhino's native {@link Scriptable}.
 * 
 * @author Tal Liron
 */
public class ScriptableEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof Scriptable;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		Scriptable scriptable = (Scriptable) object;

		context.out.append( '{' );

		Object[] keys = scriptable.getIds();
		int length = keys.length;
		if( length > 0 )
		{
			context.newline();

			for( int i = 0; i < length; i++ )
			{
				String key = keys[i].toString();
				Object value = scriptable.get( key, scriptable );

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
