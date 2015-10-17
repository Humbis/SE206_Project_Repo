package vidivox;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.SwingWorker;

import components.SideMenuComponents;
import vidivox.Player;

/**
 * This is the swingWorker responsible for generating mp3 files based on the text entered
 * This ensures GUI concurrency while the mp3 file is being generated
 * @author Kaimin Li
 */
public class CreateMp3DoInBackground extends SwingWorker<Void,Void> {
	private String output;
	private Player player;
	
	public CreateMp3DoInBackground(Player p, String output){
		this.output = output;
		this.player = p;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		if (output != null){
			//create wav file, then convert the format to mp3
			ProcessBuilder makeWav = new ProcessBuilder("/bin/bash", "-c", "echo " + SideMenuComponents.sideMenu.getTextArea().getText() + " | text2wave -o " + output +".wav");
			ProcessBuilder convert = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -y -i " + output + ".wav -f mp3 "+ output+".mp3");
			try {
				//begin the process
				Process process = makeWav.start();
				process.waitFor();
				Process converse = convert.start();
				converse.waitFor();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void done(){
		//set the newly create file as the selected mp3 file
		URI mp3url;
		try { 
			//create URI from the path of the mp3 created (in the current directory)
			mp3url = new URI("file:///"+System.getProperty("user.dir")+"/"+output+".mp3");
			Player.mp3File = new File(mp3url);
			player.mp3Label.setText(Player.mp3File.getName());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		//remove the wav file that was created
		try {
			File del = new File(output+".wav");
			del.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (Player.videoFile != null) {
			//enable buttons when both video and audio are selected
			SideMenuComponents.sideMenu.getAddComBtn().setEnabled(true);
			SideMenuComponents.sideMenu.getAddOffsetBtn().setEnabled(true);
		}
	}
}
