import com.avereon.acorn.AcornMod;

module com.avereon.acorn {

	requires com.avereon.xenon;

	opens com.avereon.acorn.bundles;
	opens com.avereon.acorn.settings;

	exports com.avereon.acorn to com.avereon.xenon;

	provides com.avereon.xenon.Mod with AcornMod;
}