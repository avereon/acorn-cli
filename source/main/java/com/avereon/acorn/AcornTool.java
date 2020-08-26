package com.avereon.acorn;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.util.Lambda;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;

public class AcornTool extends ProgramTool {

	private static final Timer timer = new Timer(true);

	public AcornTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setIcon( "acorn" );

		Method getSystemCpuLoad;
		ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
		for( Method method : bean.getClass().getMethods() ) {
			if( "getSystemCpuLoad".equals( method.getName() ) ) {
				timer.schedule( Lambda.timerTask( () -> {
					try {
						Object result = method.invoke( bean );
						System.out.println( "CPU load=" + result );
					} catch( IllegalAccessException e ) {
						e.printStackTrace();
					} catch( InvocationTargetException e ) {
						e.printStackTrace();
					}
				} ), 0, 1000 );
			}
		}

	}

}
