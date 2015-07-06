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

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.Context;

/**
 * A JSON encoder for Nashorn's native {@link ScriptObjectMirror}.
 * <p>
 * In some cases, the mirror can be unwrapped, in which case this encoder will
 * delegate to the encoder for that wrapped object. If it cannot be unwrapped,
 * it will encode as either a JSON object or JSON array as appropriate.
 * 
 * @author Tal Liron
 */
public class ScriptObjectMirrorEncoder implements JsonEncoder
{
	//
	// JsonEncoder
	//

	public boolean canEncode( Object object, JsonContext context )
	{
		return object instanceof ScriptObjectMirror;
	}

	public void encode( Object object, JsonContext context ) throws IOException
	{
		ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) object;

		Object wrapped = ScriptObjectMirror.unwrap( scriptObjectMirror, Context.getGlobal() );
		if( !( wrapped instanceof ScriptObjectMirror ) )
		{
			context.encode( wrapped );
			return;
		}

		if( scriptObjectMirror.isArray() )
		{
			context.out.append( '[' );

			int length = scriptObjectMirror.size();
			if( length > 0 )
			{
				context.newline();

				for( int i = 0; i < length; i++ )
				{
					Object value = scriptObjectMirror.getSlot( i );

					context.indentNested();
					context.nest().encode( value );

					if( i < length - 1 )
						context.comma();
				}

				context.newline();
				context.indent();
			}

			context.out.append( ']' );
		}
		else
		{
			context.out.append( '{' );

			String[] keys = scriptObjectMirror.getOwnKeys( true );
			int length = keys.length;
			if( length > 0 )
			{
				context.newline();

				for( int i = 0; i < length; i++ )
				{
					String key = keys[i];
					Object value = scriptObjectMirror.get( key );

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
}
