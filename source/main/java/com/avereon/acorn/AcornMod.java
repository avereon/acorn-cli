package com.avereon.acorn;

import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;
import com.avereon.zenna.icon.AcornIcon;

public class AcornMod extends Mod {

	public static final String STYLESHEET = "acorn.css";

	private AcornAssetType design2dAssetType;

	public AcornMod() {
		super();
	}

	@Override
	public void startup() throws Exception {
		super.startup();
		registerIcon( getCard().getArtifact(), new AcornIcon() );

		registerAssetType( design2dAssetType = new AcornAssetType( this ) );
		ToolRegistration design2dEditorRegistration = new ToolRegistration( this, AcornTool.class );
		design2dEditorRegistration.setName( "Acorn Counting Tool" );
		registerTool( design2dAssetType, design2dEditorRegistration );
	}

	@Override
	public void shutdown() throws Exception {
		unregisterTool( design2dAssetType, AcornTool.class );
		unregisterAssetType( design2dAssetType );

		unregisterIcon( getCard().getArtifact(), new AcornIcon() );
		super.shutdown();
	}

}
