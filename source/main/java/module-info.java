module com.avereon.acorn {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.zevra;
	requires java.management;
	exports com.avereon.acorn;

}
