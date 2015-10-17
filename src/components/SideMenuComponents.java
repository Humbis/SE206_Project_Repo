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
import vidivox.ListenDoInBackground;
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
import components.MediaComponents;

import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.JCheckBox;

public class SideMenuComponents {
	//protected JButton btnAddCom;
	//protected JButton btnAudioOffset;
	protected JButton btnListen;
	//protected final JTextArea txtArea;
	//protected JButton btnCreateMp;
	//private JLabel lblChars;
	//protected File videoFile;
	//protected File mp3File;
	//private boolean isMp3Playing = false;
	//private boolean isOverwrite = false;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();
	
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

				ListenDoInBackground ListenWorker = new ListenDoInBackground(Player.frame);

				//execute SwingWorker
				ListenWorker.execute();
			}
		});

		btnListen.setBounds(750, 131, 135, 40);
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
	
	//getters
	public JButton getListenBtn(){
		return btnListen;
	}
}
