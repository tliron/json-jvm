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

package com.threecrickets.jvm.json;

import java.util.Collection;

public interface JsonImplementation
{
	/**
	 * Initialize the implementation.
	 */
	public void initialize();

	/**
	 * The name of this implementation.
	 * 
	 * @return The name of this implementation
	 */
	public String getName();

	/**
	 * The priority of this implementation. Higher numbers mean higher priority.
	 * 
	 * @return The priority of this implementation
	 */
	public int getPriority();

	public JsonContext createContext( Appendable out, boolean expand, boolean allowCode, int depth );

	public Collection<JsonEncoder> getEncoders();

	public JsonEncoder getFallbackEncoder();

	public Collection<JsonTransformer> getTransformers();

	public Object createObject();

	public void putInObject( Object object, String key, Object value );

	public Object createArray( int length );

	public void setInArray( Object object, int index, Object value );

	public Object createString( String value );

	public Object createDouble( double value );

	public Object createInteger( int value );

	public Object createLong( long value );
}
