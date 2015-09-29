package vidivox;

import java.io.File;

import javax.swing.SwingWorker;

public class AddComDoInBackground extends SwingWorker<Void, Void>{
	//Fields include the actual player (for accessing files) and the output name
	private Player player;
	private String comOutName;
	public AddComDoInBackground(Player p, String n){
		player = p;
		comOutName = n;
	}
	@Override
	protected Void doInBackground() throws Exception {
		//FFMPEG commands to split audio from video, combine the two audios and re attache the audio and video  
		ProcessBuilder splitter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + player.videoFile.getAbsolutePath() + " -i " + player.mp3File.getAbsolutePath() + " -filter_complex amix=inputs=2:duration=first temp.mp3");
		ProcessBuilder combiner = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i temp.mp3 -i " + player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");

		Process split = splitter.start();
		split.waitFor();
		Process combine = combiner.start();
		combine.waitFor();
		return null;
	}

	@Override
	protected void done(){
		//remove the mp3 file that was created
		try {
			File del = new File("temp.mp3");
			del.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (player.videoFile != null) {
			player.btnAddCom.setEnabled(true);
		}
	}
}
