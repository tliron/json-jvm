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

import org.mozilla.javascript.Wrapper;

import com.threecrickets.jvm.json.JsonImplementation;
import com.threecrickets.jvm.json.JsonTransformer;

/**
 * Transformer for a Rhino's {@link Wrapper}. Unwraps and delegates to the
 * transformer for that wrapped object.
 * <p>
 * Wrappers should not normally result from JSON decoding, because we construct
 * our ScriptableObjects directly, however the transformer is provided for the
 * sake of completion.
 * 
 * @author Tal Liron
 */
public class WrapperTransformer implements JsonTransformer
{
	//
	// JsonTransformer
	//

	public Object transform( Object object, JsonImplementation implementation )
	{
		if( object instanceof Wrapper )
		{
			Wrapper wrapper = (Wrapper) object;

			Object wrapped = wrapper.unwrap();
			for( JsonTransformer transformer : implementation.getTransformers() )
			{
				Object r = transformer.transform( wrapped, implementation );
				if( r != null )
					return r;
			}
		}

		return null;
	}
}
