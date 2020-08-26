import com.avereon.acorn.AcornMod;

module com.avereon.acorn {

	requires com.avereon.xenon;
	requires java.management;

	opens com.avereon.acorn.bundles;
	opens com.avereon.acorn.settings;

	exports com.avereon.acorn to com.avereon.xenon;

	provides com.avereon.xenon.Mod with AcornMod;
}