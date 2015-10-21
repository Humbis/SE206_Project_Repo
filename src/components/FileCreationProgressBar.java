package components;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

public class FileCreationProgressBar extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public FileCreationProgressBar() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 85);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar, BorderLayout.CENTER);
		
		JLabel progbarLbl = new JLabel("Please wait while the file is being generated...");
		contentPane.add(progbarLbl, BorderLayout.NORTH);
	}

}
