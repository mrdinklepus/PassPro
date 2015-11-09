package utilities;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * Model for holding data in the tables
 * 
 * @author MDWhite
 */
public class PassTableModel extends AbstractTableModel
{
	private String[] columnNames;
	private Vector<Vector<String>> data;
	private Vector<String> emptyRow = new Vector<String>();
	public boolean isEmpty;
	
	/**
	 * Constructor
	 * 
	 * @param cols
	 * @param rows
	 */
	public PassTableModel(String[] cols, Vector<Vector<String>> rows)
	{
		columnNames = cols;
		data = rows;
		
		for (int i = 0; i < cols.length; i++)
		{
			emptyRow.add("");
		}
		
		if (data.isEmpty())
		{
			data.add(emptyRow);
			isEmpty = true;
		}
	}
	
	public int getColumnCount()
	{
		return columnNames.length;
	}
	
	public int getRowCount()
	{
		return data.size();
	}
	
	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	
	public Object getValueAt(int row, int col)
	{
		return data.get(row).get(col);
	}
	
	/*
	* JTable uses this method to determine the default renderer/
	* editor for each cell.  If we didn't implement this method,
	* then the last column would contain text ("true"/"false"),
	* rather than a check box.
	*/
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
	
	/*
	* Don't need to implement this method unless your table's
	* editable.
	*/
	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		if (col < 2) {
		return false;
		} else {
		return true;
		}
	}
	
	/**
	 * Set the value at a specific row and column
	 * 
	 * @param value
	 * @param row
	 * @param col
	 */
	public void setValueAt(String value, String key, int col) 
	{
		for (Vector<String> row:data)
		{
			if (row.get(0).equals(key))
			{
				row.setElementAt(value, col);
				fireTableCellUpdated(data.indexOf(row), col);
				break;
			}
		}
	}
	
	/**
	 * Inserts a row into the table
	 * 
	 * @param rowData
	 */
	public void insertRow(Vector<String> rowData)
	{
		data.add(rowData);
		
		if (isEmpty)
		{
			data.removeElementAt(0);
			isEmpty = false;
		}
		
		fireTableDataChanged();
	}
	
	/**
	 * Delete a row from the table
	 * 
	 * @param rowKey
	 */
	public void deleteRow(String rowKey)
	{
		int rowIndex = -1;
		for (Vector<String> row: data)
		{
			if (row.get(0).equals(rowKey))
			{
				rowIndex = data.indexOf(row);
			}
		}
		
		if (data.size() == 1)
		{
			data.add(emptyRow);
			isEmpty = true;
		}
		
		if (rowIndex >= 0)
		{
			data.removeElementAt(rowIndex);
			fireTableDataChanged();
		}
	}
	
	/**
	 * Replace data in the table
	 * 
	 * @param data
	 */
	public void setTable(Vector<Vector<String>> data)
	{
		this.data = data;
		
		if (this.data.isEmpty())
		{
			data.add(emptyRow);
			isEmpty = true;
		}
		else
		{
			isEmpty = false;
		}
		
		fireTableDataChanged();
	}
}
