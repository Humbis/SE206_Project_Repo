package audioFunctions;

import java.io.IOException;

import javax.swing.SwingWorker;

import components.SideMenuComponents;

/**
 * This is the swingWorker for the festival process tts
 * This ensures GUI concurrency while the text is playing.
 * @author Kaimin Li
 */
public class ListenDoInBackground extends SwingWorker<Void, Void>{
	
	@Override
	protected Void doInBackground() throws Exception {
		//run festival in bash from the entered text
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "echo " + SideMenuComponents.sideMenu.getTextArea().getText() + " | festival --tts");

		try {
			//begin process and wait for process to complete
			Process process = builder.start();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	//after speech is done, re enable listen button
	@Override
	protected void done(){
		SideMenuComponents.sideMenu.getListenBtn().setEnabled(true);
	}
}
