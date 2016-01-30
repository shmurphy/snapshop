/*
 * TCSS 305
 * Assignment 4 - SnapShop
 */

package gui;


import filters.EdgeDetectFilter;
import filters.EdgeHighlightFilter;
import filters.Filter;
import filters.FlipHorizontalFilter;
import filters.FlipVerticalFilter;
import filters.GrayscaleFilter;
import filters.SharpenFilter;
import filters.SoftenFilter;
import image.PixelImage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Graphical User Interface for a program that can display and change images.
 * 
 * @author Shannon Murphy
 * @version 2 February 2015
 */
public final class SnapShopGUI {   
    // constants for GUI size and GUI location
    /** A ToolKit. */
    private static final Toolkit KIT = Toolkit.getDefaultToolkit();

    /** The Dimension of the screen. */
    private static final Dimension SCREEN_SIZE = KIT.getScreenSize();

    /** The width of the screen. */
    private static final int SCREEN_WIDTH = SCREEN_SIZE.width;

    /** The height of the screen. */
    private static final int SCREEN_HEIGHT = SCREEN_SIZE.height;
    
    /** A Factor for scaling the width of the location of the GUI. */
    private static final int SCALE = 3;
    
    /** A Factor for scaling the height of the location of the GUI. */
    private static final int HEIGHT_SCALE = 6;
    
    // instance fields for the components of the GUI
    
    /** The frame for this application's GUI. */
    private final JFrame myFrame;
    
    /** The file chooser for the application. */
    private final JFileChooser myChooser;
    
    /** A Collection of filter buttons. */
    private final Map<Integer, JButton> myFilterButtons;
    
    /** A Collection of filters. */
    private final Map<String, Filter> myFilters;
    
    /** The Open button. */
    private JButton myOpen;
    
    /** The Close button. */
    private JButton myClose;
    
    /** The SaveAs button. */
    private JButton mySave;
    
    /** The Undo button. */
    private JButton myUndo;
    
    /** JLabel to be used to display the image. */
    private JLabel myImageLabel;
    
    /** To determine whether an image is open already. */
    private boolean myImageOpen;
    
    /** JPanel for where the image is placed. */
    private JPanel myImagePanel;
    
    /** The current Image. */
    private PixelImage myImage;
    
    /** Temporary File to hold the image before a filter is applied. */
    private File myUndoFile;
   
    /**
     * Constructor to initialize fields.
     */
    public SnapShopGUI() {
        myFrame = new JFrame("TCSS 305 SnapShop");
        myChooser = new JFileChooser("H:\\workspace\\shmurphy-snapshop");
        myImageLabel = new JLabel();
        myFilterButtons = new TreeMap<Integer, JButton>();       
        myFilters = new TreeMap<String, Filter>();
        createFilters();        
        createButtons();     
        myImageOpen = false;
        myImagePanel = new JPanel();
        myImage = null; // ignore null warning
    } 

    /**
     * Sets up and displays the GUI for this application.
     */
    public void start() {
        myFrame.setSize(SCREEN_WIDTH / SCALE, SCREEN_HEIGHT / SCALE);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = dim.width / SCALE - myFrame.getSize().width / SCALE;
        final int y = dim.height / 2 - myFrame.getSize().height / HEIGHT_SCALE;
        centerFrame(x, y);        
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);      
        setupComponents();
        myFrame.pack();
        myFrame.setVisible(true);     
    }
    
    /**
     * Sets up the components in this frame.
     */
    private void setupComponents() {        
        final JPanel filterPanel = new JPanel(); // default LayoutManager is FlowLayout()
        final JPanel filePanel = new JPanel();  
        
        addFilterButtons(filterPanel);
        addFileButtons(filePanel);
        
        setButtonStatus(false);
        myOpen.setEnabled(true); // enable open
        
        myFrame.add(filterPanel, BorderLayout.NORTH);
        myFrame.add(filePanel, BorderLayout.SOUTH); 
        
        myOpen.addActionListener(new OpenListener()); 
        myClose.addActionListener(new CloseListener()); 
        mySave.addActionListener(new SaveAsListener()); 
        myUndo.addActionListener(new UndoListener());
    }     
    
    /**
     * Helper method for the constructor to initialize the File modifier buttons.
     */
    private void createButtons() {
        myOpen = new JButton("Open...");
        mySave = new JButton("Save As...");
        myClose = new JButton("Close Image");
        myUndo = new JButton("Undo");
    }
    
    /**
     * Helper method for the constructor to add each Filter to the myFilters TreeMap.
     */
    private void createFilters() { 
        myFilters.put("edgeD", new EdgeDetectFilter());
        myFilters.put("edgeH", new EdgeHighlightFilter());
        myFilters.put("flipH", new FlipHorizontalFilter());
        myFilters.put("flipV", new FlipVerticalFilter());
        myFilters.put("grayscale", new GrayscaleFilter());
        myFilters.put("sharpen", new SharpenFilter());
        myFilters.put("soften", new SoftenFilter());
    }

    /**
     * Adds the file buttons (open, save as, close, and undo) to the file panel at the bottom 
     * of the JFrame.
     * 
     * @param thePanel the panel the buttons are added to.
     */
    private void addFileButtons(final JPanel thePanel) {
        thePanel.add(myOpen);
        thePanel.add(mySave);
        thePanel.add(myClose);
        thePanel.add(myUndo);
    }
    
    /**
     * Adds the filter buttons to the filter panel at the top of the JFrame.
     * 
     * @param thePanel the panel the buttons are added to.
     */
    private void addFilterButtons(final JPanel thePanel) {
        final Iterator<Map.Entry<String, Filter>> itr = myFilters.entrySet().iterator();
        int i = 0;
        while (itr.hasNext()) {
            final Map.Entry<String, Filter> pairs = (Map.Entry<String, Filter>) itr.next();
            final Filter filter = (Filter) pairs.getValue();

            final JButton filterButton = createFilterButton(filter);
            thePanel.add(filterButton);
            myFilterButtons.put(i, filterButton);
            i++;           
        } 
    }
    
    /**
     * Creates a button for each type of filter. 
     * 
     * @param theFilter the filter to be applied.
     * @return a new button with an ActionListener to apply the appropriate filter.
     */
    private JButton createFilterButton(final Filter theFilter) {
        final JButton button = new JButton(theFilter.getDescription());
        
        /**
         * ActionListener to filter the image, based on which button is clicked. Replaces
         * the old image with the new filtered image on the image panel.
         * Also saves a copy of the image before the filter is applied to be used for the undo
         * function.
         */
        class FilterListener implements ActionListener {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                // save the current image to use for the undo button
                try {                    
                    myImage.save(myUndoFile);
                } catch (final IOException e) {
                    JOptionPane.showMessageDialog(null, "Cannot save this file!", 
                                      "Can't save", JOptionPane.WARNING_MESSAGE);
                }
                theFilter.filter(myImage);
                myUndo.setEnabled(true);
                myFrame.remove(myImagePanel);
                myFrame.validate();
                myFrame.repaint();
                myImagePanel = new JPanel();
                myFrame.add(myImagePanel, BorderLayout.CENTER);
                centerImagePanel();
                myFrame.validate();
            }
        }
        button.addActionListener(new FilterListener());
        return button;
    }  
    
    /**
     * Centers the JFrame on the screen based on the passed X and Y coordinates.
     * 
     * @param theX the x-coordinate for the location.
     * @param theY the y-coordinate for the location.
     */
    private void centerFrame(final int theX, final int theY) {
        myFrame.setLocation(theX, theY);
    }
    
    /**
     * Sets the buttons to either be enabled or disabled. Excludes open because its
     * status may be different than the rest of the buttons. 
     * 
     * @param theStatus true if the buttons need to be enabled, false if disabled.
     */
    private void setButtonStatus(final boolean theStatus) {
        myClose.setEnabled(theStatus);
        mySave.setEnabled(theStatus);
        myUndo.setEnabled(theStatus);
        final Iterator<Map.Entry<Integer, JButton>> itr = myFilterButtons.entrySet().
                                                                               iterator();
        while (itr.hasNext()) {
            final Map.Entry<Integer, JButton> pairs = (Map.Entry<Integer, JButton>) itr.next();
            final JButton buttons = (JButton) pairs.getValue();
            buttons.setEnabled(theStatus);
        }       
    }
    
    /**
     * Centers the JPanel that contains the image so that it stays centered even if the user
     * resizes the JFrame.
     */
    private void centerImagePanel() {
        myImagePanel.setLayout(new BoxLayout(myImagePanel, BoxLayout.X_AXIS));
        myImagePanel.add(Box.createHorizontalGlue());
        myImagePanel.add(myImageLabel);
        myImagePanel.add(Box.createHorizontalGlue());
    }
    
    /**
     * ActionListener for the "Open..." button to open an image file and display it.
     */
    class OpenListener implements ActionListener {       
        @Override
        public void actionPerformed(final ActionEvent theEvent) {             
            final int result = myChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = myChooser.getSelectedFile();
                myUndoFile = new File(myChooser.getSelectedFile() + "(1)");
                try {
                    myImage = PixelImage.load(selectedFile);
                } catch (final IOException e) {
                    JOptionPane.showMessageDialog(null, "The selected file did not "
                                  + "contain an image!", "Error!", 
                                                  JOptionPane.WARNING_MESSAGE);
                }
                
                myImageLabel = new JLabel(new ImageIcon(myImage));
                
                if (myImageOpen) {   
                    myFrame.remove(myImagePanel);
                    myFrame.validate();
                    myFrame.repaint();
                    myFrame.pack();
                    
                    myImagePanel = new JPanel();
                    myImageLabel = new JLabel(new ImageIcon(myImage));
                } 
                myFrame.add(myImagePanel, BorderLayout.CENTER);
                myImageOpen = true;
                setButtonStatus(true);
                myUndo.setEnabled(false);
                centerImagePanel();
                
                myFrame.setSize(SCREEN_WIDTH / SCALE, SCREEN_HEIGHT / SCALE);
                final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                final int x = dim.width / SCALE - myFrame.getSize().width / SCALE;
                final int y = dim.height / SCALE - myFrame.getSize().height / SCALE;
                centerFrame(x, y);

                myFrame.pack();
            }
        }
    }
    
    /**
     * ActionListener for the close button to close the image.
     */
    class CloseListener implements ActionListener {       
        @Override
        public void actionPerformed(final ActionEvent theEvent) {
            myFrame.remove(myImagePanel);
            myFrame.validate();
            myFrame.repaint();

            myFrame.setSize(SCREEN_WIDTH / SCALE, SCREEN_HEIGHT / SCALE);
            final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            final int x = dim.width / SCALE - myFrame.getSize().width / SCALE;
            final int y = dim.height / 2 - myFrame.getSize().height / HEIGHT_SCALE;
            centerFrame(x, y);
            setButtonStatus(false);
            myFrame.pack();
        }
    }
    
    /**
     * ActionListener for the save as button to save the image.
     */
    class SaveAsListener implements ActionListener {       
        @Override
        public void actionPerformed(final ActionEvent theEvent) {
            final int result = myChooser.showSaveDialog(null);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = myChooser.getSelectedFile();
                try {
                    myImage.save(selectedFile);
                } catch (final IOException e) {
                    JOptionPane.showMessageDialog(null, "You cannot save this file!", 
                                      "Can't save image", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    
    /**
     * ActionListener for the undo button to undo the most recent change to the image.
     */
    class UndoListener implements ActionListener {       
        @Override
        public void actionPerformed(final ActionEvent theEvent) {
            try {
                myImage = PixelImage.load(myUndoFile);
            } catch (final IOException e) {
                JOptionPane.showMessageDialog(null, "The file did not "
                              + "contain an image", "Error", 
                                              JOptionPane.WARNING_MESSAGE);
            }
            myFrame.remove(myImagePanel);
            myImagePanel.remove(myImageLabel);
            
            myImagePanel = new JPanel(new GridLayout());
            myImageLabel = new JLabel(new ImageIcon(myImage));
            myImagePanel.add(myImageLabel, BorderLayout.CENTER);
 
            myFrame.add(myImagePanel, BorderLayout.CENTER);
            myUndo.setEnabled(false);
            myFrame.validate();
            myFrame.repaint();
        }
    }
}

