package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.Timer;

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
import components.MediaComponents;
import components.SideMenuComponents;

import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JSlider;

/**
 * This is the main class, where the GUI and the player is assembled
 * This class also includes the implementations of file broswers
 * @author Kaimin Li
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
	public static File videoFile;
	public static File mp3File;
	public final JLabel mp3Label;
	public static Player frame;
	protected Timer t;
	protected int offsetTime;
	/**
	 * Launch the application. Set up the GUI
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

	/**
	 * Main menu frame, contains most of the GUI and the media player
	 * The file browsing functionalities and the video player are implemented here
	 */
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
						SideMenuComponents.sideMenu.getAddComBtn().setEnabled(true);
						SideMenuComponents.sideMenu.getAddOffsetBtn().setEnabled(true);
					}
				}
			}
		});
		btnBrowseVideo.setBounds(33, 9, 168, 25);
		contentPane.add(btnBrowseVideo);

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
					SideMenuComponents.sideMenu.getPlayMp3Btn().setEnabled(true);
					if (videoFile != null) {
						SideMenuComponents.sideMenu.getAddComBtn().setEnabled(true);
						SideMenuComponents.sideMenu.getAddOffsetBtn().setEnabled(true);
					}
				}
			}
		});
		btnBrowseMp.setBounds(750, 183, 135, 40);
		contentPane.add(btnBrowseMp);
		
		//Helpful label for textbox
		JLabel textBoxHelpLabel = new JLabel("Enter text here for text to speech");
		textBoxHelpLabel.setForeground(Color.WHITE);
		textBoxHelpLabel.setBounds(750, 14, 304, 15);
		contentPane.add(textBoxHelpLabel);

	}
	/**
	 * Add media components into the player
	 * @param mediaComp
	 */
	private void addMediaComps(MediaComponents mediaComp){
		contentPane.add(mediaComp.getRewindBtn());
		contentPane.add(mediaComp.getFFBtn());
		contentPane.add(mediaComp.getPlayBtn());
		contentPane.add(mediaComp.getMuteBtn());
		contentPane.add(mediaComp.getVolSlider());
		contentPane.add(mediaComp.getSpeedLbl());
		contentPane.add(mediaComp.getSpeedSlider());
	}
	
	/**
	 * Add side menu components
	 * @param sideMenu
	 */
	private void addSideMenuComps(SideMenuComponents sideMenu){
		contentPane.add(sideMenu.getListenBtn());
		contentPane.add(sideMenu.getCharLbl());
		contentPane.add(sideMenu.getScrollPane());
		contentPane.add(sideMenu.getPlayMp3Btn());
		contentPane.add(sideMenu.getCreateMp3Btn());
		contentPane.add(sideMenu.getAddComBtn());
		contentPane.add(sideMenu.getAddOffsetBtn());
		contentPane.add(sideMenu.getOffsetLbl());
		contentPane.add(sideMenu.getOverwriteBox());
	}
	
}
