/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/11/1
 ----------------------------------------------------------------------------------------------------
 ChangeLog:

----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.table;

import jcomicdownloader.enums.*;
import javax.swing.table.*;
import java.util.*;

/**
 *
 * 定義記錄頁面的表格
 */
public class RecordTableModel extends DefaultTableModel {

    public RecordTableModel(Vector columnNames, int rowCount) {
        super( columnNames, rowCount) ;
    }
    public boolean isCellEditable(int row, int col) { // set unchanged item
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return false;
    }

    public Class getColumnClass(int c) { // boolean turn into checkbox
        return getValueAt( 0, c ).getClass();
    }

    public Object getValueAt( int row, int col ) {
        return super.getValueAt( row, col );
    }

}

