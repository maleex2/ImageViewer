import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * ImageViewer is the main class of the image viewer application. It builds and
 * displays the application GUI and initialises all other components.
 * 
 * To start the application, create an object of this class.
 * 
 * @author Michael Kölling and David J. Barnes.
 * @version 3.1
 */
public class ImageViewer {
	// static fields:
	private static final String VERSION = "Version 3.1";
	private static JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

	// fields:
	private JFrame frame;
	private ImagePanel imagePanel;
	private JLabel filenameLabel;
	private JLabel statusLabel;
	private JButton smallerButton;
	private JButton largerButton;
	private JButton undoButton;
	private JButton reloadButton;
	private JButton redoButton;
	private OFImage currentImage;
	private OFImage reloadImage;
	private OFImage leftImage;
	List<OFImage> smallerArrayList = new ArrayList<>();
	List<OFImage> undoArrayList = new ArrayList<>();
	private List<Filter> filters;
	List<OFImage> redoArrayList = new ArrayList<>();
	List<OFImage> rotateArrayList = new ArrayList<>();
	List<OFImage> leftArrayList = new ArrayList<>();
	int rotated = 4;

	private JMenu menuFilter;
	private JMenu menuEdit;
	int rotatedLeft = 4;
	private JButton rightButton;
	private JButton leftButton;
	private Component option;

	/**
	 * Create an ImageViewer and display its GUI on screen.
	 */
	public ImageViewer() {
		currentImage = null;
		filters = createFilters();
		makeFrame();
	}

	// ---- implementation of menu functions ----

	/**
	 * Open function: open a file chooser to select a new image file, and then
	 * display the chosen image.
	 */
	private void openFile() {
		int returnVal = fileChooser.showOpenDialog(frame);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return; // cancelled
		}
		File selectedFile = fileChooser.getSelectedFile();
		currentImage = ImageFileManager.loadImage(selectedFile);
		reloadImage = currentImage;

		if (currentImage == null) { // image file was not a valid image
			JOptionPane.showMessageDialog(frame, "The file was not in a recognized image file format.",
					"Image Load Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		imagePanel.setImage(currentImage);
		setButtonsEnabled(true); // after opening a picture, this methods enable everything
		setMenusEnabled(true); // that has been disabled before that.
		setUndoButton(false);
		setRedoButton(false);
		showFilename(selectedFile.getPath());
		showStatus("File loaded.");
		frame.pack();

		undoArrayList.add(currentImage); // we put every change in arrayList and access it fairly easy.
	}


	/**
	 * Close function: close the current image.
	 */

	private void close() {
		currentImage = null;
		imagePanel.clearImage();
		showFilename(null);
		setButtonsEnabled(false);
		setMenusEnabled(false);
		
	}

	/**
	 * Save As function: save the current image to a file.
	 */
	private void saveAs() {
		if (currentImage != null) {
			int returnVal = fileChooser.showSaveDialog(frame);

			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return; // cancelled
			}
			File selectedFile = fileChooser.getSelectedFile();
			ImageFileManager.saveImage(currentImage, selectedFile);

			showFilename(selectedFile.getPath());
		}
	}

	/**
	 * Quit function: quit the application.
	 */
	private void quit() {
		System.exit(0);
	}

	/**
	 * Apply a given filter to the current image.
	 * 
	 * @param filter
	 *            The filter object to be applied.
	 */
	private void applyFilter(Filter filter) {
		if (currentImage != null) {
			int width = currentImage.getWidth();
			int height = currentImage.getHeight();
			OFImage newImage = new OFImage(width, height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newImage.setPixel(x, y, currentImage.getPixel(x, y));
				}
			}
			
			filter.apply(newImage);
			frame.repaint();
			showStatus("Applied: " + filter.getName());
			
		
			/////   help lines for undo()  ///////

			// copy pixel data into new image
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newImage.setPixel(x, y, newImage.getPixel(x, y));
				}
			}
		
			
			imagePanel.setImage(newImage);
			currentImage = newImage;
			undoArrayList.add(currentImage);
			setUndoButton(true);
			frame.pack();

		} else {
			showStatus("No image loaded.");
		}
	}
	

	/**
	 * 'About' function: show the 'about' box.
	 */
	private void showAbout() {
		JOptionPane.showMessageDialog(frame, "ImageViewer\n" + VERSION, "About ImageViewer",
				JOptionPane.INFORMATION_MESSAGE);
	}


	private void undo() {
		if (undoArrayList.get(undoArrayList.size() - 1) != null) { // since we move the elements from undoArrayList to redoArrayList,
			//setUndoButton(false);	// the undoArrayList will be empty after we move everything.
			showStatus("Undo not available.");
		}
		else
			setUndoButton(true);{
		currentImage = undoArrayList.get(undoArrayList.size() - 2); // if the arrayList isn't empty, 
		imagePanel.setImage(currentImage);  // get the before last element and delete the last.
		//setButtonsEnabled(true);
		showStatus("Undo done.");
		redoArrayList.add(undoArrayList.get(undoArrayList.size() - 1));
		setRedoButton(true);
		undoArrayList.remove(undoArrayList.size() - 1);
		//frame.pack();

	}
	}

	private void redo() {
		if (redoArrayList.size() == 0) {
			setRedoButton(false);
			showStatus("Redo not available.");

		}
		else {
			
		currentImage = redoArrayList.get(redoArrayList.size() - 1);
		imagePanel.setImage(currentImage);
		showStatus("Redo done.");
		undoArrayList.add(redoArrayList.get(redoArrayList.size() - 1));
		redoArrayList.remove(redoArrayList.size() - 1);
		//frame.pack();
	}}

	private void reload() {
		
		int result = JOptionPane.showConfirmDialog(option, "You are about to deleted your changes. Do you want to proceed?", 
			       "Woah!", JOptionPane.INFORMATION_MESSAGE);
		
		if (result == JOptionPane.OK_OPTION){
			
		
		
		currentImage = reloadImage;
		imagePanel.setImage(reloadImage);
		undoArrayList.add(currentImage);
		frame.pack();
		showStatus("You Just Deleted Your Changes. You can undo tho ;)");
	}
		else
			return;
	}
	
	
	/**
	 * Make the current picture larger.
	 */
	private void makeLarger() {
		if (currentImage != null) {
			if (smallerArrayList.size() == 0) { // check if the image has been shrunken before trying to make it larger
												// this way we get the image from this special array and do not make lose the image.
				if (currentImage != null) {
					// create new image with double size
					int width = currentImage.getWidth();
					int height = currentImage.getHeight();
					OFImage newImage = new OFImage(width * 2, height * 2);

					// copy pixel data into new image
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {
							Color col = currentImage.getPixel(x, y);
							newImage.setPixel(x * 2, y * 2, col);
							newImage.setPixel(x * 2 + 1, y * 2, col);
							newImage.setPixel(x * 2, y * 2 + 1, col);
							newImage.setPixel(x * 2 + 1, y * 2 + 1, col);
						}
					}

					currentImage = newImage;
					imagePanel.setImage(currentImage);
					undoArrayList.add(currentImage);
					setUndoButton(true);
					//frame.pack();
				}
			} else {
				currentImage = smallerArrayList.get(smallerArrayList.size() - 1); // if smaller() has been evoked,
				smallerArrayList.remove(smallerArrayList.size() - 1); // we are here and so get the shrunken image
				undoArrayList.add(currentImage);
				imagePanel.setImage(currentImage);
				setUndoButton(true);
				//frame.pack();
			}
		} else {
			showStatus("No image loaded.");
		}
	}

	/**
	 * Make the current picture smaller.
	 */
	private void makeSmaller() {
		if (currentImage != null) {
												// before the method core we
			smallerArrayList.add(currentImage); // add the image to smallerRarraylist so we can access it with makelarger
			int width = currentImage.getWidth() / 2;
			int height = currentImage.getHeight() / 2;
			OFImage newImage = new OFImage(width, height);

			// copy pixel data into new image
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newImage.setPixel(x, y, currentImage.getPixel(x * 2, y * 2));
				}
			}

			currentImage = newImage;
			imagePanel.setImage(currentImage);
			undoArrayList.add(currentImage);
			setUndoButton(true);
			//frame.pack();
		} else {
			showStatus("No image loaded.");
		}
	}

	public void rotateRight() {
		if (undoArrayList.get(undoArrayList.size() - 1) != null) {
			if (rotated % 2== 0) {
				
				OFImage forRotateRight = undoArrayList.get(undoArrayList.size() - 1);
				int width = forRotateRight.getWidth();
				int height = forRotateRight.getHeight();
				OFImage newImage = new OFImage(height,width);
				for (int x = 0; x < width; x++)
					for (int y = 0; y < height; y++)
						newImage.setPixel(height-1-y, x, forRotateRight.getPixel(x,y));// -1 because of the starting index from zero
				currentImage = newImage;                    
				imagePanel.setImage(currentImage);
				undoArrayList.add(currentImage);
				setUndoButton(true);
				rotated++;
				//frame.pack();
			}

			else{
				rotateClockwiseAfterRight();

			}
			

		}
	}
	
	private void rotateClockwiseAfterRight() { // helping to complete rotateRight cycles since rotate from right doesn't seem to work
		OFImage forRotate = undoArrayList.get(undoArrayList.size() - 2);
		if (forRotate != null) {
			int width = forRotate.getWidth();
			int height = forRotate.getHeight();
			OFImage newImage = new OFImage(width, height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newImage.setPixel(x, height-1-y, forRotate.getPixel(x, y)); // same as rotateRight but don't bother with width
				}
			}
			currentImage = newImage;
			imagePanel.setImage(currentImage);
			//frame.pack();
			undoArrayList.add(currentImage);
			setUndoButton(true);
			rotated++; 
			//frame.pack();
		}
	}

	

	private void rotate180() { // just rotate to 180 degrees
		if(rotated%2==0) {

			int width = currentImage.getWidth();
			int height = currentImage.getHeight();
			OFImage newImage = new OFImage(width, height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newImage.setPixel(x, height - y - 1, currentImage.getPixel(x, y));
				}
			}
			currentImage = newImage;
			imagePanel.setImage(currentImage);
			frame.pack();
			undoArrayList.add(currentImage);
			setUndoButton(true);
			rotated++;
			//frame.pack();
		} else {
			showStatus("This function can only be used with non-rotated images or images rotated to 180 degrees");
			JOptionPane.showMessageDialog(frame, "This function can only be used with non-rotated images or images rotated to 180 degrees.");
		}
	}
	

	private void rotateLeft1() {  // act like normal 90 degrees rotator to the right
	if (undoArrayList.get(undoArrayList.size() - 1) != null) {
		//90
			OFImage forRotateRight = leftArrayList.get(leftArrayList.size() - 1);
			int width = forRotateRight.getWidth();
			int height = forRotateRight.getHeight();
			OFImage newImage = new OFImage(height,width);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					newImage.setPixel(height-1-y, width-1-x, forRotateRight.getPixel(x,y));
				}
			}
			leftImage=newImage;
			leftArrayList.add(leftImage);
			
	}
	}
	
	private void rotateLeft2() { // act like 180 rotator
		//180
			OFImage forRotate = undoArrayList.get(undoArrayList.size() - 1);
			if (forRotate != null) {
				int width = forRotate.getWidth();
				int height = forRotate.getHeight();
				OFImage newImage = new OFImage(width, height);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						newImage.setPixel(x, height - y - 1, forRotate.getPixel(x, y));
					}
				}
			
			leftImage=newImage;
			leftArrayList.add(leftImage);
			}
	}
	
	private void rotateLeft() {
		rotateLeft2();
		rotateLeft1();
		
		currentImage = leftArrayList.get(leftArrayList.size() - 1);
		imagePanel.setImage(currentImage);
		frame.pack();
		undoArrayList.add(currentImage);
		setUndoButton(true);
		rotated++;
		//frame.pack();
		
	
	}
	
	



	// ---- support methods ----

	/**
	 * Show the file name of the current image in the fils display label. 'null' may
	 * be used as a parameter if no file is currently loaded.
	 * 
	 * @param filename
	 *            The file name to be displayed, or null for 'no file'.
	 */
	private void showFilename(String filename) {
		if (filename == null) {
			filenameLabel.setText("No file displayed.");
		} else {
			filenameLabel.setText("File: " + filename);
		}
	}

	/**
	 * Show a message in the status bar at the bottom of the screen.
	 * 
	 * @param text
	 *            The status message.
	 */
	private void showStatus(String text) {
		statusLabel.setText(text);
	}

	/**
	 * Enable or disable all toolbar buttons.
	 * 
	 * @param status
	 *            'true' to enable the buttons, 'false' to disable.
	 */
	private void setButtonsEnabled(boolean status) {
		smallerButton.setEnabled(status);
		largerButton.setEnabled(status);
		redoButton.setEnabled(status);
		undoButton.setEnabled(status);
		reloadButton.setEnabled(status);
		rightButton.setEnabled(status);
		leftButton.setEnabled(status);
	}
	private void setMenusEnabled(boolean status) {
		menuFilter.setEnabled(status);
		menuEdit.setEnabled(status);
	}
	
	private void setUndoButton(boolean status) {
		undoButton.setEnabled(status);
	}
	
	private void setRedoButton(boolean status) {
		redoButton.setEnabled(status);
	}


	/**
	 * Create a list with all the known filters.
	 * 
	 * @return The list of filters.
	 */
	private List<Filter> createFilters() {
		List<Filter> filterList = new ArrayList<>();
		filterList.add(new DarkerFilter("Darker"));
		filterList.add(new LighterFilter("Lighter"));
		filterList.add(new ThresholdFilter("Threshold"));
		filterList.add(new InvertFilter("Invert"));
		filterList.add(new SolarizeFilter("Solarize"));
		filterList.add(new SmoothFilter("Smooth"));
		filterList.add(new PixelizeFilter("Pixelize"));
		filterList.add(new MirrorFilter("Mirror"));
		filterList.add(new GrayScaleFilter("Grayscale"));
		filterList.add(new EdgeFilter("Edge Detection"));
		filterList.add(new FishEyeFilter("Fish Eye"));
		filterList.add(new BlueFilter("Blue Extract"));//new

		return filterList;
	}

	// ---- Swing stuff to build the frame and all its components and menus ----

	/**
	 * Create the Swing frame and its content.
	 */
	private void makeFrame() {
		frame = new JFrame("ImageViewer");
		JPanel contentPane = (JPanel) frame.getContentPane(); ///////////////////////
		contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		makeMenuBar(frame);
        

        
       
        
        
		// Specify the layout manager with nice spacing
		contentPane.setLayout(new BorderLayout(10, 10));

		// Create the image pane in the center
		imagePanel = new ImagePanel();
		imagePanel.setBorder(new EtchedBorder());
		contentPane.add(imagePanel, BorderLayout.CENTER);

		// Create two labels at top and bottom for the file name and status messages
		filenameLabel = new JLabel();
		contentPane.add(filenameLabel, BorderLayout.NORTH);

		statusLabel = new JLabel(VERSION);
		contentPane.add(statusLabel, BorderLayout.SOUTH);

		
		
		
		
		
		
		// Create the toolbar with the buttons
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new GridLayout(10, 5));
		
		
		
		smallerButton = new JButton("Smaller");
		smallerButton.addActionListener(e -> makeSmaller());
		toolbar.add(smallerButton);

		largerButton = new JButton("Larger");
		largerButton.addActionListener(e -> makeLarger());
		toolbar.add(largerButton);

		
		rightButton = new JButton("Rotate Right");
		rightButton.addActionListener(e -> rotateRight());
		toolbar.add(rightButton);
		
		leftButton = new JButton("Rotate Left");
		leftButton.addActionListener(e -> rotateLeft());
		toolbar.add(leftButton);

		
		
		toolbar.setSize(5,5);
		JPanel flow = new JPanel();
		flow.add(toolbar);

		contentPane.add(flow, BorderLayout.EAST);
		
		
		JPanel toolbar2 = new JPanel();
		
		undoButton = new JButton("Undo");
		undoButton.addActionListener(e -> undo());
		toolbar2.add(undoButton);

		redoButton = new JButton("Redo");
		redoButton.addActionListener(e -> redo());
		toolbar2.add(redoButton);
		
		
		reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> reload());
		toolbar2.add(reloadButton);
		
		
		

		// Add toolbar into panel with flow layout for spacing
		JPanel flow2 = new JPanel();
		flow2.add(toolbar2);
		toolbar2.setSize(3,1);
		contentPane.add(flow2, BorderLayout.WEST);
		
		
		
		
		

		// building is done - arrange the components
		showFilename(null);
		setButtonsEnabled(false);
		frame.pack();

		// place the frame at the center of the screen and show
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();///////
		frame.setLocation(d.width / 2 - frame.getWidth() / 2, d.height / 2 - frame.getHeight() / 2);
		frame.setVisible(true);
	}

	/**
	 * Create the main frame's menu bar.
	 * 
	 * @param frame
	 *            The frame that the menu bar should be added to.
	 */

	
	
	private void makeMenuBar(JFrame frame)
    {
        final int SHORTCUT_MASK =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        
        JMenu menu;
        JMenuItem item;
        
        // create the File menu
        menu = new JMenu("File");
        menubar.add(menu);
        
        item = new JMenuItem("Open...");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_MASK));
            item.addActionListener(e -> openFile());
        menu.add(item);

        item = new JMenuItem("Close");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, SHORTCUT_MASK));
            item.addActionListener(e -> close());
        menu.add(item);
        menu.addSeparator();

        item = new JMenuItem("Save As...");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_MASK));
            item.addActionListener(e -> saveAs());
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Quit");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_MASK));
            item.addActionListener(e -> quit());
        menu.add(item);

        
        menuEdit = new JMenu("Edit");
		menubar.add(menuEdit);
		menuEdit.setEnabled(false);
		
		item = new JMenuItem("Undo");
		item.addActionListener(e -> undo());
		menuEdit.add(item);

		item = new JMenuItem("redo");
		item.addActionListener(e -> redo());
		menuEdit.add(item);



		item = new JMenuItem("Rotate180");
		item.addActionListener(e -> rotate180());
		menuEdit.add(item);


        // create the Filter menu
        menuFilter = new JMenu("Filter");
        menubar.add(menuFilter);
        menuFilter.setEnabled(false);
        
        for(Filter filter : filters) {
            item = new JMenuItem(filter.getName());
            item.addActionListener(e -> applyFilter(filter));
             menuFilter.add(item);
         }

        // create the Help menu
        menu = new JMenu("Help");
        menubar.add(menu);
        
        item = new JMenuItem("About ImageViewer...");
            item.addActionListener(e -> showAbout());
        menu.add(item);

    }
}


