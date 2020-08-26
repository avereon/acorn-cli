package com.avereon.acorn;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.*;

public class AcornAssetType extends AssetType {

	private static final String uriPattern = "acorn:acorn";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public AcornAssetType( ProgramProduct product ) {
		super( product, "acorn" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean assetNew( Program program, Asset asset ) throws AssetException {
		asset.setModified( false );
		return true;
	}

	@Override
	public boolean assetOpen( Program program, Asset asset ) throws AssetException {
		asset.setModified( false );
		return true;
	}

}
