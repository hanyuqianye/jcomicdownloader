/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader;

import jcomicdownloader.tools.*;
import jcomicdownloader.module.*;
import jcomicdownloader.enums.*;

import javax.swing.table.*;
import java.util.*;

public class DataTableModel extends DefaultTableModel {
    /**
 *
 * @author user
 */
    public DataTableModel(Vector columnNames, int rowCount) {
        super( columnNames, rowCount) ;
    }
    public boolean isCellEditable(int row, int col) { // set unchanged item
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if ( getColumnClass( col ).toString().matches( ".*oolean.*" ) ) {
            // only could change value of checkbox
            return true;
        } else {
            return false;
        }
    }

    public Class getColumnClass(int c) { // boolean turn into checkbox
        return getValueAt( 0, c ).getClass();
    }

    public Object getValueAt( int row, int col ) {
        if ( col == DownTableEnum.CHECKS ) // choice or not ( "true" or "false"
            return (Object)( getHowManyTrue( Common.getSeparateStrings( String.valueOf( super.getValueAt( row, col ) ) ) ) );
            //return super.getValueAt( row, col );
        else if ( col == DownTableEnum.VOLUMES ) // show how many volumes in the main page
            return (Object)( Common.getSeparateStrings( String.valueOf( super.getValueAt( row, col ) ) ).length );
        else
            return super.getValueAt( row, col );
	}

    public Object getRealValueAt( int row, int col ) { // for volumes(col==3) and check(col==4)
        return super.getValueAt( row, col );
	}

	private int getHowManyTrue( String[] strings ) {
	   int count = 0;
	   for ( String str : strings )
	       if ( str.equals( "true" ) )
	           count ++;
	   return count;
	}
}
