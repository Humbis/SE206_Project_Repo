package vidivox;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.SwingWorker;
import vidivox.Player;
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
			//create mp3 file
			ProcessBuilder makeWav = new ProcessBuilder("/bin/bash", "-c", "echo " + Player.frame.txtArea.getText() + " | text2wave -o " + output +".wav");
			ProcessBuilder convert = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -y -i " + output + ".wav -f mp3 "+ output+".mp3");
			try {
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
			player.mp3File = new File(mp3url);
			player.mp3Label.setText(player.mp3File.getName());
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
		
		if (player.videoFile != null) {
			player.btnAddCom.setEnabled(true);
		}
	}
}
