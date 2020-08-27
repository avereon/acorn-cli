package com.avereon.acorn;

import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class AcornTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private static final Timer timer = new Timer( true );

	private final SystemCpuLoadCheck cpuLoadCheck;

	private final Consumer<Double> cpuLoadListener;

	private final Label result;

	private final ProgressIndicator progress;

	private Button button;

	private final Label message;

	private AcornChecker checker;

	public AcornTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		addStylesheet( AcornMod.STYLESHEET );
		getStyleClass().addAll( "acorn-tool" );
		setIcon( "acorn" );

		String acornText = product.rb().text( BundleKey.LABEL, "acorn" );
		String startText = product.rb().text( BundleKey.LABEL, "start" );
		String waitingText = product.rb().text( "message", "waiting-to-start" );

		cpuLoadCheck = new SystemCpuLoadCheck();
		cpuLoadListener = d -> log.log( Log.DEBUG, "cpu=" + d );

		result = new Label( "0" );
		result.getStyleClass().addAll( "result" );
		button = new Button( startText );
		//button.getStyleClass().addAll( "button" );
		progress = new ProgressBar( 0 );
		progress.getStyleClass().addAll( "progress" );
		message = new Label( waitingText );
		message.getStyleClass().addAll( "message" );

		button.setOnAction( e -> toggle() );

		VBox box = new VBox( result, progress, button );
		box.getStyleClass().addAll( "layout" );
		getChildren().add( box );
	}

	@Override
	protected void display() throws ToolException {
		cpuLoadCheck.addListener( cpuLoadListener );
		timer.schedule( cpuLoadCheck, 0, 1000 );
	}

	@Override
	protected void conceal() throws ToolException {
		cpuLoadCheck.cancel();
		cpuLoadCheck.removeListener( cpuLoadListener );
	}

	private void setScore( long score ) {
		Fx.run( () -> result.setText( String.valueOf( score ) ) );
	}

	private boolean isRunning() {
		return !(checker == null || checker.isDone());
	}

	private void toggle() {
		if( isRunning() ) {
			checker.cancel( true );
		} else {
			start();
		}
	}

	private void updateButtonText() {
		String startText = getProduct().rb().text( BundleKey.LABEL, "start" );
		String cancelText = getProduct().rb().text( BundleKey.LABEL, "cancel" );
		Fx.run( () -> button.setText( isRunning() ? cancelText : startText ) );
	}

	private void start() {
		checker = new AcornChecker();
		checker.register( TaskEvent.SUBMITTED, e -> {
			Fx.run( () -> progress.setProgress( 0 ) );
			updateButtonText();
		} );
		checker.register( TaskEvent.PROGRESS, e -> Fx.run( () -> progress.setProgress( e.getTask().getPercent() ) ) );
		checker.register( TaskEvent.SUCCESS, e -> Fx.run( () -> {
			try {
				setScore( checker.get() );
			} catch( InterruptedException | ExecutionException exception ) {
				log.log( Log.WARN, "Error computing acorn count", exception );
			}
		} ) );
		checker.register( TaskEvent.CANCEL, e -> Fx.run( () -> progress.setProgress( 0 ) ) );
		checker.register( TaskEvent.FINISH, e -> updateButtonText() );

		getProgram().getTaskManager().submit( checker );
	}

}
