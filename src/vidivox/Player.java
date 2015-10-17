package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.*;
import javax.swing.Timer;

import java.awt.Font;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JTextArea;
import components.DocumentSizeFilter;
import components.MediaComponents;
import components.SideMenuComponents;

import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.JCheckBox;

/*
 * This is the VIDIVOX Beta for Assignment 4
 * Authors: Kaimin Li, Aaron Zhong(prototype only)
 * upi: kli438
 */
public class Player extends JFrame {

	private static final long serialVersionUID = 1L;
	/*
	 * Instance fields useful throughout the player
	 * Some of the GUI are instance fields since they are changed in their SwingWorker classes
	 * Most of these instances are required for other classes to function
	 */
	public final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	public static EmbeddedMediaPlayer video = null;
	private JPanel contentPane;
	private boolean isMp3Playing = false;
	private boolean isOverwrite = false;
	protected File videoFile;
	protected File mp3File;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();
	private JLabel lblChars;
	protected final JLabel mp3Label;
	protected JButton btnAddCom;
	protected JButton btnAudioOffset;
	protected JButton btnListen;
	protected final JTextArea txtArea;
	protected JButton btnCreateMp;
	public static Player frame;
	protected Timer t;
	protected int offsetTime;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		//add vlc search path
		NativeLibrary.addSearchPath(
				RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
				);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new Player();
					MediaComponents m = new MediaComponents();
					SideMenuComponents s = new SideMenuComponents();
					frame.addMediaComps(m);
					frame.addSideMenuComps(s);
					frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	//Main menu frame, contains most of the GUI and the media player
	public Player() {
		//Define contentPane
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1079, 548);
		contentPane = new JPanel();
		contentPane.setForeground(Color.LIGHT_GRAY);
		contentPane.setBackground(Color.DARK_GRAY);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//panel for video player component
		JPanel playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBounds(33, 41, 699, 393);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		video = mediaPlayerComponent.getMediaPlayer();
		playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
		contentPane.add(playerPanel);

		
		//label for timer
		final JLabel timerLabel = new JLabel("0 sec");
		timerLabel.setForeground(Color.WHITE);
		timerLabel.setBounds(662, 476, 70, 15);
		contentPane.add(timerLabel);
		
		//Video slider that allows the user to drag to another time in the video
		final JSlider videoSlider = new JSlider();
		videoSlider.setMaximum(10000);
		videoSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				video.pause();
				t.stop();
			}
			@Override
			public void mouseReleased(MouseEvent e) {	
				float sliderPos = videoSlider.getValue()/10000.0f;
				video.setPosition(sliderPos);
				t.restart();
				video.play();
			}
		});
		videoSlider.setValue(0);
		videoSlider.setBackground(Color.DARK_GRAY);
		videoSlider.setBounds(33, 446, 699, 16);
		contentPane.add(videoSlider);

		//Timer used to check video time, and to update the video slider
		t = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(video.getMediaPlayerState().toString().equalsIgnoreCase("libvlc_Ended")){
					timerLabel.setText("End of Video");	//check for end of video
					//This doesn't actually display, timer refreshes to 0
				}else{
					timerLabel.setText((video.getTime()/1000)+ " sec");	//get video time
					videoSlider.setValue((int) (video.getPosition() * 10000.0f));
				}
			}
		}); 
		t.start();
		
		//video label, changes with user selection
		final JLabel videoLabel = new JLabel("No video chosen");
		videoLabel.setForeground(Color.WHITE);
		videoLabel.setBounds(228, 14, 506, 15);
		contentPane.add(videoLabel);

		//button for choosing a video to play
		JButton btnBrowseVideo = new JButton("Browse Video");
		btnBrowseVideo.setForeground(SystemColor.text);
		btnBrowseVideo.setBackground(Color.GRAY);
		btnBrowseVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Add file chooser as well as set a filter so that user only picks avi or mp4 files
				final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
				fileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("Video files (avi)", new String[] {"avi", "AVI"});
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){
					//play the file chosen
					videoFile = fileChooser.getSelectedFile();
					video.playMedia(videoFile.getAbsolutePath());
					videoLabel.setText(videoFile.getName());
					if (mp3File != null){
						btnAddCom.setEnabled(true);
						btnAudioOffset.setEnabled(true);
					}
				}
			}
		});
		btnBrowseVideo.setBounds(33, 9, 168, 25);
		contentPane.add(btnBrowseVideo);
		
		/*
		 * Side menu GUI components
		 * This includes all of the buttons for T2S, add commentary, picking start point for audio etc
		 */


		//Plays the selected mp3 file
		final JButton btnPlaymp3 = new JButton("Play Mp3");
		btnPlaymp3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//checks whether mp3 is playing or video is
				if(isMp3Playing == false){
					if(mp3File != null){
						isMp3Playing = true;
						btnPlaymp3.setText("Back to Video");	//change button name
						video.playMedia(mp3File.getAbsolutePath());
					}	
				}else{
					//click again to go back to video
					if(videoFile != null){
						isMp3Playing = false;
						btnPlaymp3.setText("Play Mp3");
						video.playMedia(videoFile.getAbsolutePath());
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
		contentPane.add(btnPlaymp3);
		
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
							CreateMp3DoInBackground maker = new CreateMp3DoInBackground(frame, output);
							maker.execute();
						}
					} else {
						CreateMp3DoInBackground maker = new CreateMp3DoInBackground(frame, output);
						maker.execute();
					}
					btnPlaymp3.setEnabled(true);
				}
			}
		});

		btnCreateMp.setBounds(905, 131, 142, 40);
		contentPane.add(btnCreateMp);

		//label for mp3 file
		mp3Label = new JLabel("No mp3 file chosen");
		mp3Label.setForeground(Color.WHITE);
		mp3Label.setBounds(750, 233, 297, 15);
		contentPane.add(mp3Label);
		
		
		//Browser for mp3 files
		JButton btnBrowseMp = new JButton("Browse mp3...");
		btnBrowseMp.setBackground(Color.GRAY);
		btnBrowseMp.setForeground(Color.WHITE);
		btnBrowseMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//create file chooser and filter (mp3)
				final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
				fileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("mp3 files", new String[] {"mp3","MP3"});
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){
					mp3File = fileChooser.getSelectedFile();
					mp3Label.setText(mp3File.getName());
					btnPlaymp3.setEnabled(true);
					if (videoFile != null) {
						btnAddCom.setEnabled(true);
						btnAudioOffset.setEnabled(true);
					}
				}
			}
		});
		btnBrowseMp.setBounds(750, 183, 135, 40);
		contentPane.add(btnBrowseMp);

		//Button to combined selected audio and video files
		btnAddCom = new JButton("Add Commentary\n");
		btnAddCom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pick a name for the output file
				final String comOutName = JOptionPane.showInputDialog("Enter New Video Name: ");
				File f = new File(comOutName+".avi");
				if(f.exists() && !f.isDirectory()) { 
					//ask if user would want to overwrite existing file
					int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.YES_OPTION){
						//generate swingworker instance
						AddComDoInBackground adder = new AddComDoInBackground(frame, comOutName, offsetTime, isOverwrite);
						adder.execute();
						JOptionPane.showMessageDialog(null, "Your new video is saved to " + System.getProperty("user.dir") + ". Please wait a while before browsing for the video.");
					}
				} else {
					//generate swingworker instance
					AddComDoInBackground adder = new AddComDoInBackground(frame, comOutName, offsetTime, isOverwrite);
					adder.execute();
					JOptionPane.showMessageDialog(null, "Your new video is saved to " + System.getProperty("user.dir") + ". Please wait a while before browsing for the video.");
				}
			}
		});
		btnAddCom.setBackground(Color.GRAY);
		btnAddCom.setForeground(Color.WHITE);
		btnAddCom.setFont(new Font("Dialog", Font.BOLD, 22));
		btnAddCom.setBounds(750, 260, 297, 100);
		btnAddCom.setEnabled(false);
		contentPane.add(btnAddCom);

		//Char count label to show how many characters the user can still enter
		lblChars = new JLabel("200/200");
		lblChars.setForeground(Color.WHITE);
		lblChars.setBounds(977, 115, 70, 15);
		contentPane.add(lblChars);
		//simple text area for the user to enter text
		txtArea = new JTextArea();
		txtArea.setWrapStyleWord(true);
		txtArea.setRows(5);
		txtArea.setToolTipText("Enter text for text to speech. ");
		txtArea.setFont(new Font("Dialog", Font.PLAIN, 15));
		txtArea.setLineWrap(true);
		txtArea.setBounds(551, 41, 302, 122);
		txtArea.setDocument(docfilt);
		contentPane.add(txtArea);
		
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

		//Allow text area to scroll
		JScrollPane scrollPane = new JScrollPane(txtArea);
		scrollPane.setBounds(750, 41, 297, 62);
		contentPane.add(scrollPane);

		//Label that displays the current audio offset
		final JLabel offsetLabel = new JLabel("Offset: 0 seconds");
		offsetLabel.setForeground(Color.WHITE);
		offsetLabel.setBounds(891, 408, 176, 15);
		contentPane.add(offsetLabel);
		
		//Audio offset option. This brings up a dialogue that asks for a offset time for the audio commentary.
		btnAudioOffset = new JButton("Audio Offset");
		btnAudioOffset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//get total video length
				int maxTime = (int) (video.getLength() / 1000.0f);
				//Switch to the audio file to get its length
				video.playMedia(mp3File.getAbsolutePath());
				video.stop();
				//get audio length and work out a range for the offset
				maxTime = (int) (maxTime - (video.getLength() / 1000.0f));
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
				video.playMedia(videoFile.getAbsolutePath());
				video.pause();
			}
		});
		btnAudioOffset.setForeground(Color.WHITE);
		btnAudioOffset.setBackground(Color.GRAY);
		btnAudioOffset.setBounds(754, 397, 131, 37);
		btnAudioOffset.setEnabled(false);
		contentPane.add(btnAudioOffset);
		
		
		//Checkbox that allows the users to overwrite the video's audio with the selected audio.
		final JCheckBox overwriteCheck = new JCheckBox("Overwrite Existing Audio");
		overwriteCheck.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				isOverwrite = overwriteCheck.isSelected();
			}
		});
		overwriteCheck.setForeground(Color.WHITE);
		overwriteCheck.setBackground(Color.DARK_GRAY);
		overwriteCheck.setBounds(849, 368, 198, 23);
		contentPane.add(overwriteCheck);
		
		JLabel textBoxHelpLabel = new JLabel("Enter text here for text to speech");
		textBoxHelpLabel.setForeground(Color.WHITE);
		textBoxHelpLabel.setBounds(750, 14, 304, 15);
		contentPane.add(textBoxHelpLabel);

	}
	//Add media components into the player
	private void addMediaComps(MediaComponents mediaComp){
		contentPane.add(mediaComp.getRewindBtn());
		contentPane.add(mediaComp.getFFBtn());
		contentPane.add(mediaComp.getPlayBtn());
		contentPane.add(mediaComp.getMuteBtn());
		contentPane.add(mediaComp.getVolSlider());
		contentPane.add(mediaComp.getSpeedLbl());
		contentPane.add(mediaComp.getSpeedSlider());
	}
	
	//Add side menu components
	private void addSideMenuComps(SideMenuComponents sideMenu){
		contentPane.add(sideMenu.getListenBtn());
	}
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
	
}
