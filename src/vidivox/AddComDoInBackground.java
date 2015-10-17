package vidivox;

import java.io.File;

import javax.swing.SwingWorker;

import components.SideMenuComponents;

/**
 * This is the swingWorker for combining the audio with the video
 * This ensures GUI concurrency when the new video file is being generated
 * @author Kaimin Li
 */
public class AddComDoInBackground extends SwingWorker<Void, Void>{
	private String comOutName;
	private int time = 0;
	private boolean overwrite;
	public AddComDoInBackground(String n, int t, boolean o){
		comOutName = n;
		time = t;
		overwrite = o;
	}
	@Override
	protected Void doInBackground() throws Exception {
		//FFMPEG commands to split audio from video, combine the two audios and re attache the audio and video  
		//Time is used to offset the audio based on user selection
		ProcessBuilder empty = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t " + time + " -q:a 9 -acodec libmp3lame empty.mp3");
		ProcessBuilder offset = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i \"concat:empty.mp3|" + Player.mp3File.getAbsolutePath() + "\" -c copy output.mp3");
		ProcessBuilder splitter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + Player.videoFile.getAbsolutePath() + " -i output.mp3 -filter_complex amix=inputs=2:duration=first temp.mp3");
		ProcessBuilder combiner = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i temp.mp3 -i " + Player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");
		
		//Need to create a empty audio file the length of the offset
		Process offsetGen = empty.start();
		offsetGen.waitFor();
		//concatenate this with the audio file that the user selected
		Process offsetApply = offset.start();
		offsetApply.waitFor();
		if(overwrite == true){
			//If overwrite is true, do not extract video audio, instead overwrite it with the input audio
			ProcessBuilder overwriter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i output.mp3 -i " + Player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");
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
		
		if (Player.videoFile != null) {
			SideMenuComponents.sideMenu.getAddComBtn().setEnabled(true);
		}
	}
}
