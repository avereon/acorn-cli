module com.avereon.acorncli {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.zevra;
	requires java.management;
	exports com.avereon.acorncli;

}
