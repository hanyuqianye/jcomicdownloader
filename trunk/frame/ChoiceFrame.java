/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/4
----------------------------------------------------------------------------------------------------
ChangeLog:
2.01: 增加是否預設集數全選的選項
1.16: 讓EH和EX也能判斷是否已經下載。
1.14: 修改集數選擇視窗（choiceFrame）的關閉功能，允許按右上角的『X』來關閉。
1.11: 已經存在於資料夾的集數顯示淺色
1.09: 加入任務的同時也加入記錄
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.frame;

import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.table.DownloadTableModel;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.table.*;

/**
 *
 * 選取集數視窗
 */
public class ChoiceFrame extends JFrame implements TableModelListener {

    static final long serialVersionUID = 3345678;
    // about RadioButton
    private JRadioButton choiceAll, choiceNull;
    private ButtonGroup choiceGroup;
    // about Button
    private JButton confirmButton;
    private JButton cancelButton;
    public static JTable volumeTable;
    public DownloadTableModel volumeTableModel;
    private String[] columnNames; // store the colume names
    private static String[] volumeStrings; // store the volume strings
    private static String[] urlStrings; // store the volume url
    private static String[] checkStrings; // store the choice or not of volumes
    private int modifyRow;
    private boolean modifySelected;
    private String title; // 傳入作品名稱，避免連續下載中被混淆
    private String url; // 傳入位址，避免同時有兩個位址在解析會混淆
    public static JFrame choiceFrame; // use by other frame
    public JFrame thisFrame; // use by self

    public ChoiceFrame( String title, String url ) {
        this( "選擇欲下載的集數 [" + title + "]", false, 0, title, url );
    }

    public ChoiceFrame( String frameTitle, boolean modifySelected, int modifyRow, String title, String url ) {
        super( frameTitle );
        this.title = title;
        this.url = url;

        ChoiceFrame.choiceFrame = thisFrame = this; // for close the frame

        this.modifyRow = modifyRow;
        this.modifySelected = modifySelected;

        columnNames = new String[] { "是否下載", "標題" };
        volumeStrings = Common.getFileStrings( SetUp.getTempDirectory(), Common.tempVolumeFileName );
        urlStrings = Common.getFileStrings( SetUp.getTempDirectory(), Common.tempUrlFileName );
        checkStrings = new String[volumeStrings.length];

        if ( modifySelected ) {
            // 因為存入時是依當時顯示的順序，所以取得勾選集數的時候也要依當時順序為主
            checkStrings = getRealOrderCheckStrings( modifyRow, volumeStrings,
                    ComicDownGUI.nowSelectedCheckStrings );
        } else {
            // 依設定選擇是否預設全選
            String choiceString = SetUp.getChoiceAllVolume() ? "true" : "false";

            for ( int i = 0 ; i < volumeStrings.length ; i++ ) {
                checkStrings[i] = choiceString;
            }
        }

        if ( volumeStrings.length == 1 ) // 如果只有一集，就預設勾選下載
        {
            checkStrings[0] = "true";
        }

        setUpUIComponent();

        setVisible( true );
    }

    // 取得真實順序的挑選集數，才不會亂掉
    private String[] getRealOrderCheckStrings( int row, String[] volumeStrings, String[] checkStrings ) {
        String[] realOrderVolumeStrings = Common.getSeparateStrings( ComicDownGUI.downTableModel.getRealValueAt( row, DownTableEnum.VOLUMES ).toString() );
        String[] realOrderCheckStrings = new String[volumeStrings.length];

        for ( int i = 0 ; i < realOrderVolumeStrings.length ; i++ ) {
            for ( int j = 0 ; j < realOrderVolumeStrings.length ; j++ ) {
                if ( realOrderVolumeStrings[j].equals( volumeStrings[i] ) ) {
                    realOrderCheckStrings[j] = checkStrings[i];
                    break;
                }
            }
        }

        return realOrderCheckStrings;
    }

    private void setUpUIComponent() {
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

        setSize( 320, 470 );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) ); // 設置左上角圖示

        setRadioButtonUI( contentPane );
        setVolumeTableUI( contentPane );
        setButtonUI( contentPane );

        setUpeListener();
    }

    private void setRadioButtonUI( Container contentPane ) {
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
        radioPanel.setToolTipText( "下載順序由上而下，點擊上方『標題名稱』可改變集數的下載順序" );

        choiceAll = new JRadioButton( "全部選擇", false );
        choiceAll.addItemListener( new ItemHandler() );
        choiceNull = new JRadioButton( "全部取消", false );
        choiceNull.addItemListener( new ItemHandler() );

        choiceGroup = new ButtonGroup();
        choiceGroup.add( choiceAll );
        choiceGroup.add( choiceNull );

        radioPanel.add( choiceAll );
        radioPanel.add( choiceNull );

        contentPane.add( radioPanel, BorderLayout.NORTH );
    }

    private void setVolumeTableUI( Container contentPane ) {
        volumeTableModel = getDownloadTableModel();
        volumeTable = new JTable( volumeTableModel ) {

            protected String[] columnToolTips = { "希望下載哪一集就在同列的此欄位打勾",
                "顯示淺色代表已經下載過（不保證下載完整，請自行檢查）"
            };

            //Implement table header tool tips. 
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader( columnModel ) {

                    public String getToolTipText( MouseEvent e ) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX( p.x );
                        int realIndex = columnModel.getColumn( index ).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };

        volumeTable.setModel( volumeTableModel );
        volumeTable.setPreferredScrollableViewportSize( new Dimension( 400, 170 ) );
        volumeTable.setFillsViewportHeight( true );
        volumeTable.setAutoCreateRowSorter( true );

        volumeTableModel.addTableModelListener( this );

        setDefaultRenderer(); // 設置volumeTable上哪些集數要變色

        // 取得這個table的欄位模型
        TableColumnModel cModel = volumeTable.getColumnModel();

        // 配置每個欄位的寬度比例（可隨視窗大小而變化）
        cModel.getColumn( ChoiceTableEnum.YES_OR_NO ).setPreferredWidth( (int) (this.getWidth() * 0.25) );
        cModel.getColumn( ChoiceTableEnum.VOLUME_TITLE ).setPreferredWidth( (int) (this.getWidth() * 0.75) );


        JScrollPane volumeScrollPane = new JScrollPane( volumeTable );

        JPanel volumePanel = new CommonGUI().getCenterPanel( volumeScrollPane );
        volumePanel.setToolTipText( "下載順序由上而下，點擊上方『標題名稱』可改變集數的下載順序" );

        contentPane.add( volumePanel, BorderLayout.CENTER );

    }

    private void setDefaultRenderer() { // 設置volumeTable上哪些集數要變色
        GridTableRender cellRender = new GridTableRender( title, url, volumeTableModel );
        try {
            volumeTable.setDefaultRenderer( Class.forName( "java.lang.Object" ),
                    cellRender );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( ChoiceFrame.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private Vector<String> getDefaultColumns() {
        Vector<String> columnName = new Vector<String>();
        columnName.add( "是否下載" );
        columnName.add( "標題名稱" );

        return columnName;
    }

    private DownloadTableModel getDownloadTableModel() {
        DownloadTableModel tableModel = new DownloadTableModel( getDefaultColumns(), 0 );

        for ( int i = 0 ; i < volumeStrings.length ; i++ ) {
            tableModel.addRow( CommonGUI.getVolumeDataRow( checkStrings[i], volumeStrings[i] ) );
        }

        return tableModel;
    }

    private void setButtonUI( Container contentPane ) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );

        confirmButton = new JButton( "確定" );
        confirmButton.addActionListener( new ActionHandler() );
        cancelButton = new JButton( "取消" );
        cancelButton.addActionListener( new ActionHandler() );

        buttonPanel.add( confirmButton );
        buttonPanel.add( cancelButton );

        contentPane.add( buttonPanel, BorderLayout.SOUTH );
    }

    public static String[] getVolumeStrings() {
        return volumeStrings;
    }

    public static String[] getCheckStrings() {
        return checkStrings;
    }

    public static String[] getUrlStrings() {
        return urlStrings;
    }

    private void setUpeListener() {
        // do nothing when click X,
        // because it needs click buttons to unlock the downloadLock
        //setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );

        thisFrame.addWindowListener( new WindowAdapter() {

            public void windowClosing( WindowEvent e ) {
                notifyAllDownload();
                thisFrame.dispose();
            }
        } );
    }

    public void tableChanged( TableModelEvent event ) {
        //System.out.println( event.getColumn() + " " + event.getFirstRow() + " " + event.getLastRow() );
        checkStrings[event.getFirstRow()] = volumeTableModel.getValueAt( event.getFirstRow(), event.getColumn() ).toString();
        //System.out.println( checkStrings[event.getFirstRow()] + "#  " );

    }

    // 依最後顯示的集數順序來改變原本順序
    public static String[] changedStringsOrderToView( String[] strings ) {
        String[] tempStrings = new String[strings.length];

        for ( int i = 0 ; i < strings.length ; i++ ) {
            tempStrings[i] = strings[volumeTable.convertRowIndexToModel( i )];
        }

        return tempStrings;
    }

    // 儲存顯示的正確順序，這樣重新勾選的時候勾選集數才會一致
    public void storeVolumeRealOrder( int row, int totalVolume ) {
        ComicDownGUI.downTableRealChoiceOrder[row] = new int[totalVolume];
        for ( int i = 0 ; i < totalVolume ; i++ ) {
            ComicDownGUI.downTableRealChoiceOrder[row][i] = volumeTable.convertRowIndexToModel( i );
        }
    }

    // 在重新選擇集數時，取得真實順序
    public int getVolumeReadOrder( int row, int falseOrder ) {
        int realOrder = ComicDownGUI.downTableRealChoiceOrder[row][falseOrder];

        return realOrder;
    }

    // -------------  Listener  ---------------
    public void notifyAllDownload() { // unLock main frame
        synchronized ( ComicDownGUI.mainFrame ) {
            Common.debugPrintln( "解除downloadLock，允許下載" );
            Common.downloadLock = false;
            ComicDownGUI.mainFrame.notifyAll();
        }
    }

    private class ActionHandler implements ActionListener {

        public void actionPerformed( ActionEvent event ) {


            if ( event.getSource() == cancelButton ) {
                notifyAllDownload();
                thisFrame.dispose();
            } else if ( event.getSource() == confirmButton ) {
                String[] volumeStrings = ChoiceFrame.getVolumeStrings();
                String[] checkStrings = ChoiceFrame.getCheckStrings();
                String[] urlStrings = ChoiceFrame.getUrlStrings();


                // 依最後調整的順序為主
                volumeStrings = changedStringsOrderToView( volumeStrings );
                checkStrings = changedStringsOrderToView( checkStrings );
                urlStrings = changedStringsOrderToView( urlStrings );

                if ( modifySelected ) { // 只是重新選擇集數
                    ComicDownGUI.downTableModel.removeRow( modifyRow );
                    Vector<Object> dataVector = CommonGUI.getDownDataRow(
                            modifyRow + 1,
                            title,
                            volumeStrings,
                            checkStrings,
                            urlStrings );

                    storeVolumeRealOrder( modifyRow, volumeStrings.length ); // 存入真實集數順序

                    if ( Common.missionCount == 1 ) {
                        ComicDownGUI.downTableModel.addRow( dataVector );
                    } else {
                        ComicDownGUI.downTableModel.insertRow( modifyRow, dataVector );
                    }

                } else { // 加入新任務
                    storeVolumeRealOrder( Common.missionCount + 1, volumeStrings.length ); // 存入真實集數順序

                    ComicDownGUI.downTableModel.addRow( CommonGUI.getDownDataRow(
                            ++Common.missionCount,
                            title,
                            volumeStrings,
                            checkStrings,
                            urlStrings ) );

                    ComicDownGUI.recordTableModel.addRow( CommonGUI.getRecordDataRow(
                            ++Common.recordCount,
                            title,
                            new String( url ) ) );

                    // 只有在第一次選擇集數的時候才紀錄位址，之後重新選擇就可以利用這個位址了。
                    ComicDownGUI.downTableUrlStrings[Common.missionCount - 1] = new String( url );
                    //ComicDownGUI.recordTableUrlStrings[Common.recordCount-1] = new String( url );

                }

                //Common.preTitle = title;
                notifyAllDownload();

                thisFrame.dispose();
            }
        }
    }

    private class CheckBoxHandler implements ItemListener {

        public void itemStateChanged( ItemEvent event ) {
            if ( event.getStateChange() == ItemEvent.SELECTED ) {
            }
        }
    }

    private class ItemHandler implements ItemListener {

        public void itemStateChanged( ItemEvent event ) {
            if ( event.getSource() == choiceAll ) {
                if ( choiceAll.isSelected() ) {
                    for ( int i = 0 ; i < volumeTable.getRowCount() ; i++ ) {
                        volumeTable.setValueAt( true, i, ChoiceTableEnum.YES_OR_NO );
                    }

                    repaint(); // update data on time in the screen
                }
            }
            if ( event.getSource() == choiceNull ) {
                if ( choiceNull.isSelected() ) {
                    for ( int i = 0 ; i < volumeTable.getRowCount() ; i++ ) {
                        volumeTable.setValueAt( false, i, ChoiceTableEnum.YES_OR_NO );
                    }

                    repaint();
                }
            }

            if ( event.getStateChange() == ItemEvent.SELECTED ) {
            }

        }
    }
}

class GridTableRender extends DefaultTableCellRenderer {

    private String title; // 漫畫名稱
    private String url; // 漫畫位址
    DownloadTableModel tableModel; // 選擇表格的內容

    public GridTableRender( String title, String url, DownloadTableModel tableModel ) {
        super();
        this.title = title;
        this.url = url;
        this.tableModel = tableModel;
    }

    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column ) {
        Component cell =
                super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column );

        // if ( hasFocus ) {
        //     cell.setBackground( Color.green );
        //     cell.setForeground( Color.black );
        // } else {

        String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱

        if ( existsFileOnThisRow( ChoiceFrame.volumeTable.convertRowIndexToModel( row ) ) ) { // 若存在就顯示淺黑色
            //cell.setBackground( Color.gray );

            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                cell.setForeground( Color.black );
            }
            else
                cell.setForeground( Color.lightGray );
        } else { // 若不存在則顯示正常黑色
            //cell.setBackground( Color.white );
            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                cell.setForeground( Color.lightGray );
            }
            else
                cell.setForeground( Color.black );
        }

        // }
        return cell;

    }

    // 檢查第row列的單集漫畫是否已經存在於下載資料夾（只要有資料夾或壓縮檔都算）
    private boolean existsFileOnThisRow( int row ) {
        String volumeTitle = tableModel.getValueAt( row, ChoiceTableEnum.VOLUME_TITLE ).toString();

        File dirFile = null;
        File zipFile = null;
        if ( this.url.matches( "(?s).*hentai.org(?s).*" ) ) { // 讓EH和EX也能判斷是否已經下載
            dirFile = new File( SetUp.getOriginalDownloadDirectory() + volumeTitle );
            zipFile = new File( SetUp.getOriginalDownloadDirectory() + volumeTitle + ".zip" );
        } else {
            dirFile = new File( SetUp.getOriginalDownloadDirectory() + this.title + Common.getSlash() + volumeTitle );
            zipFile = new File( SetUp.getOriginalDownloadDirectory() + this.title + Common.getSlash() + volumeTitle + ".zip" );
        }

        if ( dirFile.exists() || zipFile.exists() ) {
            return true;
        } else {
            return false;
        }
    }
}
