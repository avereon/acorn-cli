package com.avereon.acorn;

import com.avereon.util.Log;
import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;
import com.avereon.zenna.icon.AcornIcon;

public class AcornMod extends Mod {

	public static final String STYLESHEET = "acorn.css";

	private static System.Logger log = Log.get();

	private AcornAssetType acornAssetType;

	public AcornMod() {
		super();
	}

	@Override
	public void startup() throws Exception {
		super.startup();
		registerIcon( getCard().getArtifact(), new AcornIcon() );

		registerAssetType( acornAssetType = new AcornAssetType( this ) );
		ToolRegistration design2dEditorRegistration = new ToolRegistration( this, AcornTool.class );
		design2dEditorRegistration.setName( "Acorn Counting Tool" );
		registerTool( acornAssetType, design2dEditorRegistration );
	}

	@Override
	public void shutdown() throws Exception {
		unregisterTool( acornAssetType, AcornTool.class );
		unregisterAssetType( acornAssetType );

		unregisterIcon( getCard().getArtifact(), new AcornIcon() );
		super.shutdown();
	}

}
