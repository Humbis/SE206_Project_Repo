package components;

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
import vidivox.Player;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JTextArea;
import components.DocumentSizeFilter;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.JCheckBox;

/**
 * This is the class containing all major components to do with the media player. 
 * This includes rewind, ff , play/pause as well as some sliders
 * This class does not include labels and file browsing functionality
 * @author Kaimin Li
 *
 */
public class MediaComponents {
	protected JButton rewind;
	protected JButton play;
	protected JButton fastForward;
	protected JButton mute;
	protected final JSlider volSlider;
	protected final JSlider speedSlider;
	volatile private boolean mouseDown = false;
	protected final JLabel speedLabel;
	
	public MediaComponents(){
		//Reverse button
		rewind = new JButton("<<");
		rewind.setForeground(SystemColor.text);
		rewind.setBackground(Color.GRAY);
		rewind.addMouseListener(new MouseAdapter() {
			//hold down to reverse, release to stop reversing
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = true;
					initVidControlThread("R");	//calls method that make a thread for this to keep GUI responsive
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = false;	//the thread will check this to determine when to stop
				}
			}
		});
		rewind.setBounds(33, 471, 54, 25);
		
		//fastforward button
		fastForward = new JButton(">>");
		fastForward.setForeground(SystemColor.text);
		fastForward.setBackground(Color.GRAY);
		fastForward.addMouseListener(new MouseAdapter() {
			//If held down it will FF, stops FFing when released.
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = true;
					initVidControlThread("FF");	//calls method that make a thread for this to keep GUI responsive
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = false;	//the thread will check this to determine when to stop
				}
			}
		});
		fastForward.setBounds(205, 471, 54, 25);
		
		//Play and pause button
		play = new JButton("Play/Pause");
		play.setForeground(SystemColor.text);
		play.setBackground(Color.GRAY);
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//check if video is playing and choose action accordingly
				if(Player.video.isPlaying()){
					Player.video.pause();
				}else{
					Player.video.play();
				}
			}
		});
		play.setBounds(90, 471, 113, 25);
		
		//Simple mute button
		mute = new JButton("Mute");
		mute.setForeground(SystemColor.text);
		mute.setBackground(Color.GRAY);
		mute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Player.video.mute();
			}
		});
		mute.setBounds(261, 471, 70, 25);
		
		//Volume slider
		volSlider = new JSlider();
		volSlider.setValue(100);
		volSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int vol = volSlider.getValue();	//get slider value
				Player.video.setVolume(vol);	//set the video volume
			}
		});
		volSlider.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Volume", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
		volSlider.setForeground(Color.BLACK);
		volSlider.setBackground(Color.DARK_GRAY);
		volSlider.setBounds(345, 471, 219, 25);
		
		//Label that shows the current playing speed
		speedLabel = new JLabel("1.00");
		speedLabel.setForeground(Color.WHITE);
		speedLabel.setBounds(997, 481, 70, 15);
		
		//Slider to control the player speed, up to 2 times and 0.25 times
		speedSlider = new JSlider();
		speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				float rate = (float) (0.25 *speedSlider.getValue());
				Player.video.setRate(rate);
				speedLabel.setText(Float.toString(rate));
			}
		});
		speedSlider.setSnapToTicks(true);
		speedSlider.setValue(4);
		speedSlider.setForeground(Color.WHITE);
		speedSlider.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Video Speed", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
		speedSlider.setMaximum(16);
		speedSlider.setMinimum(1);
		speedSlider.setBackground(Color.DARK_GRAY);
		speedSlider.setBounds(759, 471, 226, 25);
	}
	
	
	/*
	 * check method to ensure concurrency when multiple events are fired
	 * This is just in case other events are fired while fastforwarding or reversing (highly unlikely)
	 */
	volatile private boolean isRunning = false;
	private synchronized boolean checkAndMark() {
		if (isRunning) return false;
		isRunning = true;
		return true;
	}
	
	//FF and rewind thread
	private void initVidControlThread(final String arg){	                    
		if (checkAndMark()) {	//don't start another thread if this one is still running
			new Thread() {
				public void run() {
					do {
						//Check which button was pressed
						if(arg.equals("FF")){
							Player.video.skip(15);
						}else{
							Player.video.skip(-15);
						}
						try {
							sleep(1);	//Slight delay to prevent big jumps
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					} while (mouseDown); //do until released
					isRunning = false;	//no longer running
				}
			}.start();	
		}
	}
	
	//Getters for the main player
	public JButton getRewindBtn(){
		return rewind;
	}
	public JButton getFFBtn(){
		return fastForward;
	}
	public JButton getPlayBtn(){
		return play;
	}
	public JButton getMuteBtn(){
		return mute;
	}
	public JSlider getVolSlider(){
		return volSlider;
	}
	public JLabel getSpeedLbl(){
		return speedLabel;
	}
	public JSlider getSpeedSlider(){
		return speedSlider;
	}
}
