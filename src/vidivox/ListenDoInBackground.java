package vidivox;

import java.io.IOException;

import javax.swing.SwingWorker;

public class ListenDoInBackground extends SwingWorker<Void, Void>{
	private Player player;
	
	public ListenDoInBackground(Player p){
		player = p;
	}
	@Override
	protected Void doInBackground() throws Exception {
		//run festival in bash from the entered text
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "echo " + player.txtArea.getText() + " | festival --tts");

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
		player.btnListen.setEnabled(true);
	}
}
