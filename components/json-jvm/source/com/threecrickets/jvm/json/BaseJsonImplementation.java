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

package com.threecrickets.jvm.json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.threecrickets.jvm.json.generic.NullEncoder;

/**
 * A convenient base class for JSON implementations with common functionality.
 * <p>
 * Note that you do <i>not</i> have to extend this class for your
 * implementation, you just need to implement {@link JsonImplementation}.
 * 
 * @author Tal Liron
 */
public abstract class BaseJsonImplementation implements JsonImplementation
{
	//
	// JsonImplementation
	//

	public void initialize()
	{
	}

	public int getPriority()
	{
		return 0;
	}

	public JsonContext createContext( Appendable out, boolean expand, boolean allowCode, int depth )
	{
		return new JsonContext( this, out, expand, allowCode, depth );
	}

	public JsonDecoder createDecoder( Reader reader, boolean allowTransform )
	{
		return new JsonDecoder( this, reader, allowTransform );
	}

	public Collection<JsonEncoder> getEncoders()
	{
		return Collections.unmodifiableCollection( encoders );
	}

	public JsonEncoder getFallbackEncoder()
	{
		return fallbackEncoder;
	}

	public Collection<JsonTransformer> getTransformers()
	{
		return Collections.unmodifiableCollection( transformers );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final ArrayList<JsonEncoder> encoders = new ArrayList<JsonEncoder>();

	protected final ArrayList<JsonTransformer> transformers = new ArrayList<JsonTransformer>();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final JsonEncoder fallbackEncoder = new NullEncoder();
}
