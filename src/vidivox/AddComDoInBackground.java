package vidivox;

import java.io.File;

import javax.swing.SwingWorker;

public class AddComDoInBackground extends SwingWorker<Void, Void>{
	//Fields include the actual player (for accessing files) and the output name
	private Player player;
	private String comOutName;
	private int time = 0;
	private boolean overwrite;
	public AddComDoInBackground(Player p, String n, int t, boolean o){
		player = p;
		comOutName = n;
		time = t;
		overwrite = o;
	}
	@Override
	protected Void doInBackground() throws Exception {
		//FFMPEG commands to split audio from video, combine the two audios and re attache the audio and video  
		//Time is used to offset the audio based on user selection
		ProcessBuilder empty = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t " + time + " -q:a 9 -acodec libmp3lame empty.mp3");
		ProcessBuilder offset = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i \"concat:empty.mp3|" + player.mp3File.getAbsolutePath() + "\" -c copy output.mp3");
		ProcessBuilder splitter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + player.videoFile.getAbsolutePath() + " -i output.mp3 -filter_complex amix=inputs=2:duration=first temp.mp3");
		ProcessBuilder combiner = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i temp.mp3 -i " + player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");
		
		//Need to create a empty audio file the length of the offset
		Process offsetGen = empty.start();
		offsetGen.waitFor();
		//concatenate this with the audio file that the user selected
		Process offsetApply = offset.start();
		offsetApply.waitFor();
		if(overwrite == true){
			//If overwrite is true, do not extract video audio, instead overwrite it with the input audio
			ProcessBuilder overwriter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i output.mp3 -i " + player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");
			Process overwrite = overwriter.start();
			overwrite.waitFor();
		}else{
			//Strip the audio from the video then combine it with the user selected audio 
			Process split = splitter.start();
			split.waitFor();
			//Combine this back with the video
			Process combine = combiner.start();
			combine.waitFor();
		}
		return null;
	}

	@Override
	protected void done(){
		//remove the temporary mp3 files that was created
		try {
			File delT = new File("temp.mp3");
			delT.delete();
			File delO = new File("output.mp3");
			delO.delete();
			File del = new File("empty.mp3");
			del.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (player.videoFile != null) {
			player.btnAddCom.setEnabled(true);
		}
	}
}
