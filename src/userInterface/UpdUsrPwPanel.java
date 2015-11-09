package userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.TableRowSorter;

import userManagement.User;
import utilities.PassTableModel;
import entityManagement.Entity;

/**
 * Panel for viewing and adding more username/password combinations
 * 
 * @author MDWhite
 */
public class UpdUsrPwPanel extends JPanel
{
	private PassProFrame ppf;
	private JComboBox nameList;
	private JLabel usrLabel;
	private JTextField usrTF;
	private JLabel passLabel;
	private JTextField passTF;
	private JTable tab;
	private JButton addBtn;
	private JButton launchBtn;
	private PassTableModel tabmod;
	private User user;
	private JPopupMenu popup;
	private Entity currentEntity;
	private Vector<Vector<String>> emptyVec = new Vector<Vector<String>>();
	
	/**
	 * Constructor
	 */
	public UpdUsrPwPanel()
	{
		init();
	}
	
	/**
	 * Constructor
	 * 
	 * @param user
	 * @param frame
	 */
	public UpdUsrPwPanel(User user, PassProFrame frame)
	{
		this.user = user;
		ppf = frame;
		init();
	}
	
	/**
	 * Initialize components
	 */
	public void init()
	{
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Add New Password"));
		mainPanel.add(createSelectPanel());
		mainPanel.add(createAddPanel());
		mainPanel.add(createTablePanel());
		add(mainPanel);
	}
	
	/**
	 * Create the panel for the user to select an entity
	 * 
	 * @return panel
	 */
	private JPanel createSelectPanel()
	{
		JPanel selectPanel = new JPanel();
		selectPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		// Get the list of all the entities and populate the drop down menu
		ArrayList<String> names = new ArrayList<String>(user.getEntitiesMetadata().keySet());
		Collections.sort(names);
		nameList = new JComboBox(names.toArray());
		nameList.insertItemAt(" ( Select an Entity ) ",0);
		nameList.setMaximumRowCount(10);
		nameList.setSelectedIndex(0);
		nameList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doChangeSelection();
			}
		});
		
		launchBtn = new JButton("Launch Website");
		launchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doLaunchWeb();
			}
		});
		launchBtn.setEnabled(false);
		selectPanel.add(nameList);
		selectPanel.add(Box.createRigidArea(new Dimension(5,0)));
		selectPanel.add(launchBtn);
		return selectPanel;
	}
	
	/**
	 * Create the panel for the text fields
	 * 
	 * @return panel
	 */
	private JPanel createAddPanel()
	{
		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));
		addPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		
		JPanel enterPanel = new JPanel();
		enterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		enterPanel.setLayout(new BoxLayout(enterPanel, BoxLayout.LINE_AXIS));
		enterPanel.setPreferredSize(new Dimension(0,30));
		usrLabel = new JLabel(" Username: ");
		usrLabel.setForeground(Color.RED);
		usrTF = new JTextField(15);
		usrTF.setEnabled(false);
		passLabel = new JLabel("Password:  ");
		passLabel.setForeground(Color.RED);
		passTF = new JTextField(15);
		passTF.setEnabled(false);
		passTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doAddCombo();
			}
		});
		
		enterPanel.add(usrLabel);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		enterPanel.add(usrTF);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		enterPanel.add(passLabel);
		enterPanel.add(passTF);
		enterPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		JPanel btnPanel = new JPanel();
		btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 7));
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
		addBtn = new JButton("Add New Password");
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doAddCombo();
			}
		});
		addBtn.setEnabled(false);
		
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(addBtn);
		btnPanel.add(Box.createRigidArea(new Dimension(10,0)));
		addPanel.add(enterPanel);
		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
		addPanel.add(btnPanel);
		addPanel.add(Box.createVerticalGlue());
		
		return addPanel;
	}
	
	/**
	 * Create panel that holds the table
	 * 
	 * @return panel
	 */
	private JPanel createTablePanel()
	{
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
		tablePanel.setMinimumSize(new Dimension(300,200));
		
		// GET the entity metadata, sort and get it ready for table
		String[] columns = {"PW Date", "Username", "Password"};
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		
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
        JMenuItem launch = new JMenuItem("Delete Selected Passwords");
        launch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doDeletePasswords();
			}
		});
        
        JMenuItem del = new JMenuItem("Clear All History");
        del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doDeleteAll();
			}
		});
        
        popup.add(launch);
        popup.add(del);
		
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
		            	if (!tabmod.isEmpty)
		            	{
			                popup.show(e.getComponent(), e.getX(), e.getY());
		            	}
		            }
	            }
	        }
		});
		
		// Center the Name Column
		tab.setRowHeight(23);
		tab.setAutoCreateRowSorter(true);
		DefaultRowSorter drs = new TableRowSorter(tabmod);
		drs.setComparator(0, new Comparator() {
			public int compare(Object arg0, Object arg1)
			{
				// Parse into dates so it will sort correctly
				Date date1 = currentEntity.getPwDate((String) arg0);
				Date date2 = currentEntity.getPwDate((String) arg1);
				
				return date1.compareTo(date2);
			}
		});
		tab.setRowSorter(drs);
		tab.getRowSorter().toggleSortOrder(0);
		tab.getRowSorter().toggleSortOrder(0);

		// Allows us to set the font, size, alignment, and other cool stuff of cells
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        dtcr.setFont(new Font(null, Font.BOLD, 12));
        tab.getColumnModel().getColumn(0).setCellRenderer(new DateRenderer());
        tab.getColumnModel().getColumn(1).setCellRenderer(dtcr);
        tab.getColumnModel().getColumn(2).setCellRenderer(dtcr);
		
        // Add the scroll pane, set the scroll bar to always visible
		JScrollPane scrollPane = new JScrollPane(tab);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20,20,20,20), 
								BorderFactory.createLineBorder(Color.BLACK,1)));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tablePanel.add(scrollPane);
		return tablePanel;
	}
	
	/**
	 * Populate the gui with the entity data of the selected entity
	 */
	private void doChangeSelection()
	{
		user.updateTimer();
		
		// Make sure there is more than 1 item in the list before changing
		if (nameList.getItemCount() > 1)
		{
			// Don't do the blank row
			if (nameList.getSelectedIndex() != 0)
			{
				String name = (String)nameList.getSelectedItem();
				currentEntity = user.getEntity(name);
				tabmod.setTable(currentEntity.getCombos());
				usrTF.setText(currentEntity.getCurrUsername());
				passTF.setText("");
				passTF.requestFocus();
				usrTF.setEnabled(true);
				passTF.setEnabled(true);
				addBtn.setEnabled(true);
				launchBtn.setEnabled(true);
			}
			else
			{
				tabmod.setTable(emptyVec);
				usrTF.setText("");
				passTF.setText("");
				usrTF.setEnabled(false);
				passTF.setEnabled(false);
				addBtn.setEnabled(false);
				launchBtn.setEnabled(false);
			}
			
			tab.getRowSorter().toggleSortOrder(0);
			tab.getRowSorter().toggleSortOrder(0);
		}
	}
	
	private void doLaunchWeb()
	{
		user.updateTimer();
		
		String name = (String)nameList.getSelectedItem();
		String website = user.getEntitiesMetadata().get(name)[1];
		
		try
		{
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(website));
		}
		catch (IOException e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	/**
	 * Delete the selected username/password info
	 */
	public void doDeletePasswords()
	{
		user.updateTimer();
		
		// Prompt confirmation for deleting data
		Object[] options = {"Continue", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
			    "Are you sure you want to delete all the selected passwords?  \n" +
			    "This cannot be undone!",
			    "Are You Sure You Want to Delete?",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[1]);
		
		// Delete away
		if (n == 0)
		{
			int[] rows = tab.getSelectedRows();
			ArrayList<String> list = new ArrayList<String>();
			
			// Put each name in an array list
			for (int i = 0; i < rows.length; i++)
			{
				list.add((String)tab.getValueAt(rows[i], 0));
			}
			
			// Loop through and delete one by one
			for (String time: list)
			{
				currentEntity.delUPCombo(time);
				tabmod.deleteRow(time);
			}
		}
	}
	
	/**
	 * Delete all the password data for this entity
	 */
	private void doDeleteAll()
	{
		user.updateTimer();
		
		// Prompt confirmation for deleting data
		Object[] options = {"Continue", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
			    "Are you sure you want to clear all the passwords?  \n" +
			    "This cannot be undone!",
			    "Are You Sure You Want to Delete?",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[1]);
		
		// Delete away
		if (n == 0)
		{
			ArrayList<String> list = new ArrayList<String>();
			
			// Put each name in an array list
			for (int i = 0; i < tab.getRowCount(); i++)
			{
				list.add((String)tab.getValueAt(i, 0));
			}
			
			// Loop through and delete one by one
			for (String time: list)
			{
				currentEntity.delUPCombo(time);
				tabmod.deleteRow(time);
			}
		}
	}
	
	/**
	 * Add the username/password combination
	 */
	public void doAddCombo()
	{
		user.updateTimer();
		
		// Check for empty text fields and add all data
		if (!usrTF.getText().equals("") && !passTF.getText().equals(""))
		{
			String currow = (String)tab.getValueAt(0, 0);
			Vector<String> info = currentEntity.addUPCombo(usrTF.getText(), passTF.getText());
			tabmod.insertRow(info);
			passTF.setText("");
			passTF.requestFocus();
			
			if (!user.getSaveHistory())
			{
				if (!currow.equals(""))
				{
					currentEntity.delUPCombo(currow);
					tabmod.deleteRow(currow);
				}
			}
		}
	}
	
	/**
	 * Remove an entity from the drop down list
	 * 
	 * @param name of the entity
	 */
	public void removeEntity(String name)
	{
		int size = nameList.getItemCount();
		
		// Loop through and remove from list when we match the name
		for (int i = 0; i < size; i++)
		{
			if (nameList.getItemAt(i).equals(name))
			{				
				nameList.removeItemAt(i);
				break;
			}
		}
		
		// If we have removed the last entity, disable the text fields
		if (size - 1 == 0)
		{
			usrTF.setEnabled(false);
			passTF.setEnabled(false);
		}
	}
	
	/**
	 * Add another entity to the drop down box
	 * 
	 * @param name of the new entity
	 */
	public void addEntity(String name)
	{
		int size = nameList.getItemCount();
		
		// If no items in the list, just insert
		if (size == 1)
		{
			nameList.insertItemAt(name, size);
		}
		else
		{
			// Loop through alphabetically and insert at the proper position
			for (int i = 1; i <= size; i++)
			{
				if (i != size)
				{
					if (name.compareTo((String)nameList.getItemAt(i)) < 0)
					{
						nameList.insertItemAt(name, i);
						break;
					}
				}
				else
				{
					nameList.insertItemAt(name, i);
				}
			}
		}
	}
	
	/**
	 * Resets the selection
	 */
	public void resetSelection()
	{
		int i = nameList.getSelectedIndex();
		nameList.setSelectedIndex(i);
	}
	
	class DateRenderer extends DefaultTableCellRenderer {

	    public DateRenderer() { super(); }

	    public void setValue(Object value) {
	    	if (currentEntity != null && value != null && value instanceof String)
	    	{
	    		Date date = currentEntity.getPwDate((String) value);
	    		setText(currentEntity.getDisplayedDateString(date));
	    	}
	    	else
	    	{
	    		setText("");
	    	}
	    }
	}
}
