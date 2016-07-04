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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.threecrickets.jvm.json.generic.GenericJsonImplementation;

/**
 * Conversion to and from JSON text and native objects.
 * 
 * @author Tal Liron
 */
public class Json
{
	//
	// Static attributes
	//

	/**
	 * The implementation used by the static methods in this class.
	 * <p>
	 * By default, it is the implementation for the current Scripturian
	 * {@link com.threecrickets.scripturian.LanguageAdapter}. If there is none
	 * available, the dummy {@link GenericJsonImplementation} will be used.
	 * <p>
	 * You can override this behavior and set a specific implementation using
	 * {@link #setImplementation(JsonImplementation)}.
	 * 
	 * @return The implementation
	 */
	public static JsonImplementation getImplementation()
	{
		JsonImplementation implementation = Json.implementation;
		if( implementation != null )
			return implementation;
		else
		{
			implementation = implementations.get( getLanguageAdapterName() );
			if( implementation == null )
				implementation = new GenericJsonImplementation();
			return implementation;
		}
	}

	/**
	 * Sets the implementation to be used by the static methods in this class.
	 * If set to null (the default) will use the implementation for the current
	 * Scripturian {@link com.threecrickets.scripturian.LanguageAdapter}
	 * 
	 * @param implementation
	 *        The new implementation or null
	 */
	public static void setImplementation( JsonImplementation implementation )
	{
		Json.implementation = implementation;
	}

	/**
	 * All available implementations.
	 * 
	 * @return The implementations
	 */
	public static Collection<JsonImplementation> getImplementations()
	{
		return Collections.unmodifiableCollection( implementations.values() );
	}

	//
	// Static operations
	//

	/**
	 * Encodes the object into compact JSON.
	 * 
	 * @param object
	 *        The object to encode
	 * @return An implementation-specific string
	 */
	public static Object to( Object object )
	{
		return to( object, false );
	}

	/**
	 * Encodes the object into JSON.
	 * 
	 * @param object
	 *        The object to encode
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @return An implementation-specific string
	 */
	public static Object to( Object object, boolean expand )
	{
		return to( object, expand, false );
	}

	/**
	 * Encodes the object into JSON.
	 * 
	 * @param object
	 *        The object to encode
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @param allowCode
	 *        Whether to allow programming language code (non-standard JSON)
	 * @return An implementation-specific string
	 */
	public static Object to( Object object, boolean expand, boolean allowCode )
	{
		StringWriter out = new StringWriter();
		try
		{
			to( object, expand, allowCode, out );
		}
		catch( IOException x )
		{
			// There should never be exceptions with a StringWriter
		}
		return getImplementation().createString( out.toString() );
	}

	/**
	 * Encodes the object into JSON.
	 * 
	 * @param object
	 *        The object to encode
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @param allowCode
	 *        Whether to allow programming language code (non-standard JSON)
	 * @param out
	 *        Where to write the JSON
	 * @throws IOException
	 *         In case of a write error
	 */
	public static void to( Object object, boolean expand, boolean allowCode, Appendable out ) throws IOException
	{
		JsonContext context = getImplementation().createContext( out, expand, allowCode, 0 );
		context.encode( object );
	}

	/**
	 * Decodes JSON into implementation-specific objects. Supports both JSON
	 * objects and arrays. Do not allow transformations.
	 * 
	 * @param text
	 *        The JSON text
	 * @return An implementation-specific object or an array
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 */
	public static Object from( String text ) throws JsonSyntaxError
	{
		return from( text, false );
	}

	/**
	 * Decodes JSON into implementation-specific objects. Supports both JSON
	 * objects and arrays.
	 * 
	 * @param text
	 *        The JSON text
	 * @param allowTransform
	 *        Whether to allow transformations
	 * @return An implementation-specific object or an array
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 */
	public static Object from( String text, boolean allowTransform ) throws JsonSyntaxError
	{
		try
		{
			return from( new StringReader( text ), allowTransform );
		}
		catch( IOException x )
		{
			// There should never be exceptions with a StringReader
			return null;
		}
	}

	/**
	 * Decodes JSON into implementation-specific objects. Supports both JSON
	 * objects and arrays.
	 * 
	 * @param reader
	 *        The reader
	 * @param allowTransform
	 *        Whether to allow transformations
	 * @return An implementation-specific object or an array
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 * @throws IOException
	 *         In case of a read error
	 */
	public static Object from( Reader reader, boolean allowTransform ) throws JsonSyntaxError, IOException
	{
		JsonDecoder decoder = createDecoder( reader, allowTransform );
		return decoder.decode();
	}

	/**
	 * Creates a JSON decoder that decodes into implementation-specific objects.
	 * 
	 * @param reader
	 *        The reader
	 * @param allowTransform
	 *        Whether to allow transformations
	 * @return A decoder.
	 */
	public static JsonDecoder createDecoder( Reader reader, boolean allowTransform )
	{
		return getImplementation().createDecoder( reader, allowTransform );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static volatile JsonImplementation implementation;

	private static final Map<String, JsonImplementation> implementations = new HashMap<String, JsonImplementation>();

	/**
	 * The name of the current Scripturian
	 * {@link com.threecrickets.scripturian.LanguageAdapter}.
	 * 
	 * @return The language adapter name
	 */
	private static String getLanguageAdapterName()
	{
		try
		{
			return (String) com.threecrickets.scripturian.ExecutionContext.getCurrent().getAdapter().getAttributes().get( com.threecrickets.scripturian.LanguageAdapter.NAME );
		}
		catch( NoClassDefFoundError x )
		{
			return null;
		}
	}

	static
	{
		ServiceLoader<JsonImplementation> implementationLoader = ServiceLoader.load( JsonImplementation.class, Json.class.getClassLoader() );
		for( Iterator<JsonImplementation> i = implementationLoader.iterator(); i.hasNext(); )
		{
			JsonImplementation implementation;
			try
			{
				implementation = i.next();
			}
			catch( Throwable x )
			{
				// Probably a ClassNotFoundException
				continue;
			}
			JsonImplementation existing = implementations.get( implementation.getName() );
			if( ( existing == null ) || ( implementation.getPriority() > existing.getPriority() ) )
			{
				implementation.initialize();
				implementations.put( implementation.getName(), implementation );
			}
		}
	}
}
