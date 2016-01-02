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

package com.threecrickets.jvm.json.nashorn;

import com.threecrickets.jvm.json.JsonImplementation;
import com.threecrickets.jvm.json.JsonTransformer;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.Context;

/**
 * Transformer for a Nashorn's native {@link ScriptObjectMirror}. Unwraps and
 * delegates to the transformer for that wrapped object.
 * <p>
 * ScriptObjectMirrors should not normally result from JSON decoding, because we
 * construct our ScriptObjects directly, however the transformer is provided for
 * the sake of completion.
 * 
 * @author Tal Liron
 */
public class ScriptObjectMirrorTransformer implements JsonTransformer
{
	//
	// JsonTransformer
	//

	public Object transform( Object object, JsonImplementation implementation )
	{
		if( object instanceof ScriptObjectMirror )
		{
			ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) object;

			Object wrapped = ScriptObjectMirror.unwrap( scriptObjectMirror, Context.getGlobal() );
			if( !( wrapped instanceof ScriptObjectMirror ) )
			{
				for( JsonTransformer transformer : implementation.getTransformers() )
				{
					Object r = transformer.transform( wrapped, implementation );
					if( r != null )
						return r;
				}
			}
		}

		return null;
	}
}
