package userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import userManagement.User;
import utilities.PassTableModel;

/**
 * Class for creating a JPanel for Viewing and Entering entity data
 * 
 * @author MDWhite
 */
public class EntityMgrPanel extends JPanel
{
	private PassProFrame ppf;
	private JLabel nameLabel;
	private JTextField nameTF;
	private JLabel webLabel;
	private JTextField webTF;
	private JLabel usrLabel;
	private JTextField usrTF;
	private JLabel passLabel;
	private JTextField passTF;
	private JMenuItem editWeb;
	private JTable tab;
	private PassTableModel tabmod;
	private User user;
	private JPopupMenu popup;
	
	
	/**
	 * Constructor
	 * 
	 * @param user
	 * @param f Frame associated with this object
	 */
	public EntityMgrPanel(User user, PassProFrame f)
	{
		this.user = user;
		ppf = f;
		
		init();
	}
	
	/**
	 * Initializes everything for this Panel
	 */
	public void init()
	{
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Add New Entity"));
		mainPanel.add(createNamePanel());
		mainPanel.add(createEntityDataPanel());
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(createTablePanel());
		add(mainPanel);
	}
	
	/**
	 * Creates Panel for the user to enter entity name and website 
	 * 
	 * @return name Panel
	 */
	private JPanel createNamePanel()
	{
		JPanel namePanel = new JPanel();
		
		namePanel.setLayout(new GridLayout(2,3,0,5));
		namePanel.setPreferredSize(new Dimension(0,65));
		nameLabel = new JLabel(" Entity Name: ", JLabel.RIGHT);
		nameLabel.setForeground(Color.RED);
		nameTF = new JTextField(20);
		webLabel = new JLabel(" Website (optional): ", JLabel.RIGHT);
		webTF = new JTextField(20);

		namePanel.add(nameLabel);
		namePanel.add(nameTF);
		namePanel.add(new JLabel(""));
		namePanel.add(webLabel);
		namePanel.add(webTF);
		namePanel.add(new JLabel(""));
		
		return namePanel;
	}
	
	/**
	 * Creates panel for user to enter a username/password combo and a button for action
	 * 
	 * @return entDataPanel
	 */
	private JPanel createEntityDataPanel()
	{
		JPanel entDataPanel = new JPanel();
		entDataPanel.setLayout(new BoxLayout(entDataPanel, BoxLayout.PAGE_AXIS));
		entDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		
		JPanel enterPanel = new JPanel();
		enterPanel.setPreferredSize(new Dimension(0,30));
		enterPanel.setLayout(new BoxLayout(enterPanel, BoxLayout.LINE_AXIS));
		enterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		usrLabel = new JLabel(" Username: ");
		usrLabel.setForeground(Color.RED);
		usrTF = new JTextField(15);
		passLabel = new JLabel("Password:  ");
		passLabel.setForeground(Color.RED);
		passTF = new JTextField(15);
		passTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doCreateEntity();
			}
		});
		
		enterPanel.add(usrLabel);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		enterPanel.add(usrTF);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		enterPanel.add(passLabel);
		enterPanel.add(passTF);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		// Button time
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
		btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 7));
		JButton addBtn = new JButton("Add Entity");
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doCreateEntity();
			}
		});
		
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(addBtn);
		btnPanel.add(Box.createRigidArea(new Dimension(10,0)));
		entDataPanel.add(enterPanel);
		entDataPanel.add(Box.createRigidArea(new Dimension(0,10)));
		entDataPanel.add(btnPanel);
		entDataPanel.add(Box.createVerticalGlue());
		
		return entDataPanel;
	}
	
	/**
	 * Create the panel that holds the JTable
	 * 
	 * @return tablePanel
	 */
	private JPanel createTablePanel()
	{
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
		tablePanel.setMinimumSize(new Dimension(300,200));
		
		// GET the entity metadata, sort and get it ready for table
		String[] columns = {"Entity Name", "Website"};
		HashMap<String, String[]> ar = user.getEntitiesMetadata();
		ArrayList<String> names = new ArrayList<String>(ar.keySet());
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		
		// Add a rows to the vector
		for (String name: names)
		{
			Vector<String> row = new Vector<String>();
			row.add(name);
			row.add(ar.get(name)[1]);
			rows.add(row);
		}
		
		tabmod = new PassTableModel(columns, rows);
		tab = new JTable(tabmod);
//		tab.getModel().addTableModelListener(new TableModelListener() {
//			public void tableChanged(TableModelEvent e)
//			{
//				int row = e.getFirstRow();
//				int column = e.getColumn();
//		        TableModel model = (TableModel)e.getSource();
//		        //String columnName = model.getColumnName(column);
//		        //Object data = model.getValueAt(row, column);
//			}
//		});
		
		// Popup for right click on the table
		popup = new JPopupMenu();
        JMenuItem launch = new JMenuItem("Launch Website");
        launch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doLaunch();
			}
		});
        JMenuItem del = new JMenuItem("Delete Selected Entities");
        del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doDelete();
			}
		});
        
    	editWeb = new JMenuItem("Edit Selected Website");
        editWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doEditWeb();
			}
		});
        
        popup.add(launch);
        popup.add(del);
        popup.add(editWeb);
		
        // Add a tablemouselistener to allow clicking on the table rows and get a popup menu
		tab.addMouseListener(new MouseAdapter() {
	    	public void mouseReleased(MouseEvent e) 
	        {
	            if (e.getModifiers() == InputEvent.BUTTON3_MASK)
	            {
		            int rowindex = tab.getSelectedRow();
		            
		            if (rowindex < 0)
		            {
		                return;
		            }
		            
		            if (e.getComponent() instanceof JTable )
		            {
		            	// Only show menu if there is at least one row in the table
		            	if (!tabmod.isEmpty)
		            	{
		            		// Only show editWeb if they have a single row selected
			                if (tab.getSelectedRows().length == 1)
			                {
			                	editWeb.setEnabled(true);
			                }
			                else
			                {
			                	editWeb.setEnabled(false);
			                }
			                
			                popup.show(e.getComponent(), e.getX(), e.getY());
		            	}
		            }
	            }
	        }
		});
		
		// Center the Name Column
		tab.setRowHeight(25);
		tab.setAutoCreateRowSorter(true);
		tab.getRowSorter().toggleSortOrder(0);
		
		// Allows us to set the font, size, alignment, and other cool stuff of cells
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        dtcr.setFont(new Font(null, Font.BOLD, 12));
        tab.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        tab.getColumnModel().getColumn(1).setCellRenderer(dtcr);
		
        // Add the scroll pane, set the scroll bar to always visible
		JScrollPane scrollPane = new JScrollPane(tab);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20,20,20,20), 
								BorderFactory.createLineBorder(Color.BLACK)));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tablePanel.add(scrollPane);
		
		return tablePanel;
	}
	
	/**
	 * Creates a new entity using the values the user has entered from the GUI
	 */
	private void doCreateEntity()
	{
		user.updateTimer();
		
		//TODO - Currently we check only that they have something in these fields, but we probably should do more
		if (!nameTF.getText().equals("") && !usrTF.getText().equals("") && !passTF.getText().equals(""))
		{
			String name = nameTF.getText().toUpperCase();
			String web = webTF.getText();
			
			// Make sure we don't already have an entity by this name
			if (user.getEntity(name) != null)
			{
				JOptionPane.showMessageDialog(null, "Entity " + name + " Already Exists!");
				return;
			}
			
			// Send off to the user class to do all the work
			user.createNewEntity(name, web, usrTF.getText(), passTF.getText());
			doCleanup(name, web);
		}
	}
	
	/**
	 * Launches the selected entity websites in the user's default browser
	 */
	private void doLaunch()
	{
		user.updateTimer();
		
		int[] rows = tab.getSelectedRows();
		
		for (int i = 0; i < rows.length; i++)
		{
			String website = (String)tab.getValueAt(rows[i], 1);
			
			try
			{
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(website));
			}
			catch (IOException e)
			{
				System.out.println(e.getStackTrace());
			}
		}
	}
	
	/**
	 * Deletes whatever entities are selected and also all their data
	 */
	private void doDelete()
	{
		user.updateTimer();
		
		// Ask for confirmation before deleting all data for an entity
		Object[] options = {"Continue", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
			    "Are you sure you want to delete all the selected Entities?  \n" +
			    "This will delete all the password history and data \n" +
			    "associated with the entity and CANNOT be undone!",
			    "Are You Sure You Want to Delete?",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[1]);
		
		// Go For it!
		if (n == 0)
		{
			// Get selected rows and add them to a list
			int[] rows = tab.getSelectedRows();
			ArrayList<String> list = new ArrayList<String>();
			
			for (int i = 0; i < rows.length; i++)
			{
				list.add((String)tab.getValueAt(rows[i], 0));
			}
			
			UpdUsrPwPanel upPanel = ppf.getUPPanel();
			for (String name: list)
			{
				// Delete the entity first from the user, then from the table
				user.deleteEntity(name);
				tabmod.deleteRow(name);
				
				// Call across and remove the entity names from the selection box on the UpdUsrPwPanel
				upPanel.removeEntity(name);
			}
		}
	}
	
	/**
	 * Launches a new window for the user to edit the website in
	 */
	private void doEditWeb()
	{
		user.updateTimer();
		
		int row = tab.getSelectedRow();
		String name = (String)tab.getValueAt(row, 0);
		String website = (String)tab.getValueAt(row, 1);
		
		String s = (String)JOptionPane.showInputDialog(
                null,
                "Update or enter new Entity Website:\n",
                "Update Website",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                website);
		
		if ((s != null) && (s.length() > 0))
		{
			// Update the website info in all our code and stored files
		    user.updateWebsite(name, s);
		    tabmod.setValueAt(s, name, 1);
		}
	}
	
	/**
	 * Cleanup of stuff we want to do before the user is ready for it again
	 * 
	 * @param name of the entity
	 * @param web website
	 */
	public void doCleanup(String name, String web)
	{
		nameTF.setText("");
		nameTF.requestFocus();
		webTF.setText("");
		usrTF.setText("");
		passTF.setText("");
		
		Vector<String> data = new Vector<String>();
		data.add(name);
		data.add(web);
		tabmod.insertRow(data);
		ppf.getUPPanel().addEntity(name);
	}
	
	
	public static void main(String[] args)
	{
		new EntityMgrPanel(null, null).setVisible(true);
	}
}
