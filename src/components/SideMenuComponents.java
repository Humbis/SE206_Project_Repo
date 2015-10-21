package components;
import javax.swing.text.DefaultStyledDocument;

import audioFunctions.AddComDoInBackground;
import audioFunctions.CreateMp3DoInBackground;
import audioFunctions.ListenDoInBackground;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.event.*;
import java.awt.Font;

import vidivox.Player;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JTextArea;
import components.DocumentSizeFilter;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

/**
 * This is the class containing all components in the side menu of VIDIVOX.
 * This includes all text to speech as well as video audio combining functionalities
 * This class does NOT include the file browsing functions
 * @author Kaimin Li
 */
public class SideMenuComponents {
	protected JButton btnAddCom;
	protected JButton btnAudioOffset;
	protected JButton btnListen;
	protected final JTextArea txtArea;
	protected JButton btnCreateMp;
	protected final JButton btnPlaymp3;
	private JLabel lblChars;
	protected final JLabel offsetLabel;	
	private boolean isMp3Playing = false;
	private boolean isOverwrite = false;
	protected final JCheckBox overwriteCheck;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();
	protected int offsetTime;
	private JScrollPane scrollPane;
	public static SideMenuComponents sideMenu;
	
	/**
	 * Constructor that initialises the side menu components. This includes all audio functionalities.
	 */
	public SideMenuComponents(){
		//Button for listening to text entered
		btnListen = new JButton("Listen");
		btnListen.setEnabled(false);
		btnListen.setBackground(Color.GRAY);
		btnListen.setForeground(Color.WHITE);
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//disable listen button so speak only one thing at a time
				btnListen.setEnabled(false);

				ListenDoInBackground ListenWorker = new ListenDoInBackground();

				//execute SwingWorker
				ListenWorker.execute();
			}
		});

		btnListen.setBounds(750, 131, 135, 40);
		
		//Char count label to show how many characters the user can still enter
		lblChars = new JLabel("200/200");
		lblChars.setForeground(Color.WHITE);
		lblChars.setBounds(977, 115, 70, 15);
		//simple text area for the user to enter text
		txtArea = new JTextArea();
		txtArea.setWrapStyleWord(true);
		txtArea.setRows(5);
		txtArea.setToolTipText("Enter text for text to speech. ");
		txtArea.setFont(new Font("Dialog", Font.PLAIN, 15));
		txtArea.setLineWrap(true);
		txtArea.setBounds(551, 41, 302, 122);
		txtArea.setDocument(docfilt);
		
		//Allow text area to scroll
		scrollPane = new JScrollPane(txtArea);
		scrollPane.setBounds(750, 41, 297, 62);
		
		//set the maximum character to 200 so the festival voice doesn't die
		docfilt.setDocumentFilter(new DocumentSizeFilter(200));
		//add a listener to show user how many characters remaining
		docfilt.addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e) {
				charCount();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				charCount();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				charCount();
			}
		});
		
		//Plays the selected mp3 file
		btnPlaymp3 = new JButton("Play Mp3");
		btnPlaymp3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//checks whether mp3 is playing or video is
				if(isMp3Playing == false){
					if(Player.mp3File != null){
						isMp3Playing = true;
						btnPlaymp3.setText("Back to Video");	//change button name
						Player.video.playMedia(Player.mp3File.getAbsolutePath());
					}	
				}else{
					//click again to go back to video
					if(Player.videoFile != null){
						isMp3Playing = false;
						btnPlaymp3.setText("Play Mp3");
						Player.video.playMedia(Player.videoFile.getAbsolutePath());
					}else{
						isMp3Playing = false;
						btnPlaymp3.setText("Play Mp3");
					}
				}
			}
		});
		btnPlaymp3.setBackground(Color.GRAY);
		btnPlaymp3.setEnabled(false);
		btnPlaymp3.setForeground(Color.WHITE);
		btnPlaymp3.setBounds(905, 183, 142, 40);
		
		//Button to allow user to create an mp3 file from the text entered
		btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setEnabled(false);
		btnCreateMp.setBackground(Color.GRAY);
		btnCreateMp.setForeground(Color.WHITE);
		btnCreateMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ask user to enter desired output name
				final String output = JOptionPane.showInputDialog("Enter Mp3 Name: ");
				File f = new File(output+".mp3");
				if (output != null && output.length() > 0){
					if(f.exists() && !f.isDirectory()) { 
						//ask if user would want to overwrite existing file
						int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
						if (reply == JOptionPane.YES_OPTION){
							CreateMp3DoInBackground maker = new CreateMp3DoInBackground(Player.frame, output);
							maker.execute();
						}
					} else {
						CreateMp3DoInBackground maker = new CreateMp3DoInBackground(Player.frame, output);
						maker.execute();
					}
					btnPlaymp3.setEnabled(true);
				}
			}
		});

		btnCreateMp.setBounds(905, 131, 142, 40);
		
		//Button to combined selected audio and video files
		btnAddCom = new JButton("Add Commentary\n");
		btnAddCom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pick a name for the output file
				final String comOutName = JOptionPane.showInputDialog("Enter New Video Name: ");
				if(comOutName == null){
					return;
				}
				File f = new File(comOutName+".avi");
				if(f.exists() && !f.isDirectory()) { 
					//ask if user would want to overwrite existing file
					int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.YES_OPTION){
						//generate swingworker instance
						AddComDoInBackground adder = new AddComDoInBackground(comOutName, offsetTime, isOverwrite);
						adder.execute();
					}
				} else {
					//generate swingworker instance
					AddComDoInBackground adder = new AddComDoInBackground(comOutName, offsetTime, isOverwrite);
					adder.execute();
				}
			}
		});
		btnAddCom.setBackground(Color.GRAY);
		btnAddCom.setForeground(Color.WHITE);
		btnAddCom.setFont(new Font("Dialog", Font.BOLD, 22));
		btnAddCom.setBounds(750, 260, 297, 100);
		btnAddCom.setEnabled(false);
		
		//Label that displays the current audio offset
		offsetLabel = new JLabel("Offset: 0 seconds");
		offsetLabel.setForeground(Color.WHITE);
		offsetLabel.setBounds(891, 408, 176, 15);
		
		//Audio offset option. This brings up a dialogue that asks for a offset time for the audio commentary.
		btnAudioOffset = new JButton("Audio Offset");
		btnAudioOffset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//get total video length
				int maxTime = (int) (Player.video.getLength() / 1000.0f);
				//Switch to the audio file to get its length
				Player.video.playMedia(Player.mp3File.getAbsolutePath());
				Player.video.stop();
				//get audio length and work out a range for the offset
				maxTime = (int) (maxTime - (Player.video.getLength() / 1000.0f));
				//get user input for offset
				String timeStr = JOptionPane.showInputDialog("Please enter the offset (in seconds, Max = " + maxTime + ")");
				if(timeStr != null){	//don't parse if cancelled
					try{
						offsetTime = Integer.parseInt(timeStr);
						offsetLabel.setText("Offset: " + offsetTime + " seconds");
					}catch(NumberFormatException e){	//check for non numerical inputs
						JOptionPane.showMessageDialog(null, "Please enter a number");
					}
				}
				
				if(offsetTime > maxTime || offsetTime < 0){	//check for inputs greater than limit or negative
					JOptionPane.showMessageDialog(null, "The offset is either too big or negative! The audio file won't fit.");
					offsetTime = 0;		//reset offset to 0 to prevent errors
					offsetLabel.setText("Offset: " + offsetTime + " seconds");
					return;
				}	//go back to the video
				Player.video.playMedia(Player.videoFile.getAbsolutePath());
			}
		});
		btnAudioOffset.setForeground(Color.WHITE);
		btnAudioOffset.setBackground(Color.GRAY);
		btnAudioOffset.setBounds(754, 397, 131, 37);
		btnAudioOffset.setEnabled(false);
		
		//Checkbox that allows the users to overwrite the video's audio with the selected audio.
		overwriteCheck = new JCheckBox("Overwrite Existing Audio");
		overwriteCheck.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				isOverwrite = overwriteCheck.isSelected();
			}
		});
		overwriteCheck.setForeground(Color.WHITE);
		overwriteCheck.setBackground(Color.DARK_GRAY);
		overwriteCheck.setBounds(849, 368, 198, 23);
		
		sideMenu = this;
	}
	
	/**
	 * Counts the number of characters available to the user. This decreases every time the user enters a character
	 */
	private void charCount() {
		//Set a label to indicate how many characters remaining
		lblChars.setText((200 - docfilt.getLength())+"/200");
		//disable/reenable create mp3 button when text area is empty/non-empty
		if (docfilt.getLength() == 0){
			btnCreateMp.setEnabled(false);
			btnListen.setEnabled(false);
		} else {
			btnCreateMp.setEnabled(true);
			btnListen.setEnabled(true);
		}
	}
	
	//getters
	public JButton getListenBtn(){
		return btnListen;
	}
	public JTextArea getTextArea(){
		return txtArea;
	}
	public JLabel getCharLbl(){
		return lblChars;
	}
	public JScrollPane getScrollPane(){
		return scrollPane;
	}
	public JButton getPlayMp3Btn(){
		return btnPlaymp3;
	}
	public JButton getCreateMp3Btn(){
		return btnCreateMp;
	}
	public JButton getAddComBtn(){
		return btnAddCom;
	}
	public JButton getAddOffsetBtn(){
		return btnAudioOffset;
	}
	public JLabel getOffsetLbl(){
		return offsetLabel;
	}
	public JCheckBox getOverwriteBox(){
		return overwriteCheck;
	}
}
