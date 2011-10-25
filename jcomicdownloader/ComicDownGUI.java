/*
 * JComicDownloader
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Version  : v1.07
 Last Modified : 2011/10/25
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 * 
 * 1.07: 修復EH無法下載會出現警告頁面Content Warning的問題

----------------------------------------------------------------------------------------------------

 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader;

import jcomicdownloader.tools.*;
import jcomicdownloader.module.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.frame.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import javax.swing.table.*;
import java.lang.Thread;

import javax.swing.JFileChooser.*;

/**
 * @author user 主介面，同時監聽window、mouse、button和textField。
 * */
public class ComicDownGUI extends JFrame implements ActionListener,
                                                         DocumentListener,
                                                         MouseListener,
                                                         WindowFocusListener {

    public static JFrame mainFrame; // for change look and feel

    // GUI component
    private BorderLayout layout;
    private JPanel buttonPanel, textPanel;
    private JButton button[];
    private JTextArea messageArea;
    private JTextField urlField;
    private JLabel urlLabel, logoLabel;

    public static TrayIcon trayIcon; // 系統列圖示
    private PopupMenu trayPopup;
    private MenuItem trayShowItem;  // 開啟主界面
    private MenuItem trayStartItem; // 開始任務
    private MenuItem trayStopItem;  // 停止任務
    private MenuItem trayExitItem;  // 離開

    private PopupMenu tablePopup;
    private int tablePopupRow; // 觸發tablePopup的所在列
    private MenuItem tableOpenDirectoryItem;  // 開啟下載資料夾
    private MenuItem tableRechoiceVolumeItem;  // 重新選擇集數
    private MenuItem tableDeleteMissionItem;  // 刪除任務
 
    public static LogFrame logFrame; // show log, for debug

    public JTable downTable;
    public static JLabel stateBar;
    public static DataTableModel downTableModel;
    public static String[] downTableUrlStrings;
    public static String[] nowSelectedCheckStrings;
    public static int [][] downTableRealChoiceOrder;

    private static String defaultSkinClassName;

    // non-GUI component
    private String[] args;
    private static String resourceFolder;
    private StringBuffer messageString;
    private Run mainRun;

    public ComicDownGUI() {
        super( "JComicDownloader  v1.07" );

        minimizeEvent();
        initTrayIcon();

        mainFrame = this; // for change look and feel
        defaultSkinClassName = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        resourceFolder = "resource" + Common.getSlash();

        downTableUrlStrings = new String[400]; // 若任務超過400個可能就會出錯...
        downTableRealChoiceOrder = new int [400][]; // 最多四百個任務，每個任務最多500集...

        // 建立logFrame視窗，是否開啟則視預設值而定
        javax.swing.SwingUtilities.invokeLater( new Runnable() { public void run() {
            logFrame = new LogFrame();
            logFrame.setVisible( SetUp.getOpenDebugMessageWindow() );
        } } );

        messageString = new StringBuffer( "" );

        setUpUIComponent();
        setUpeListener();
        setVisible( true );
    }

    private void setUpUIComponent() {
        setSize( 640, 480 );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) ); // 設置左上角圖示
        
        addWindowFocusListener( this ); // 用來監測主視窗情形，若取得焦點就在輸入欄貼上剪貼簿網址

        Container contentPane = getContentPane();

        setButton( contentPane );
        setText( contentPane );

        setSkin( SetUp.getSkinClassName() );

        stateBar = new JLabel( "請貼上網址" );
        stateBar.setHorizontalAlignment( SwingConstants.LEFT );
        stateBar.setBorder( BorderFactory.createEtchedBorder() );
        stateBar.setToolTipText( "可顯示程式執行流程與目前下載進度" );

        contentPane.add( stateBar, BorderLayout.SOUTH );

    }

    public static String getDefaultSkinClassName() {
        return defaultSkinClassName;
    }
    public static void setDefaultSkinClassName( String className ) {
        defaultSkinClassName = className;
    }


    /**
     * 改成defaultSkinClassName名稱的版面
     * */
    private void setSkin( String defaultSkinClassName ) {
        // default skin: Windows uses Nimbus skin, Ubuntu uses GTK skin
        CommonGUI.setLookAndFeelByClassName( SetUp.getSkinClassName() );
        SwingUtilities.updateComponentTreeUI( this );
    }

    // 設置主界面上的主要按鈕
    private void setButton( Container contentPane ) {
        button = new JButton[7];
        String buttonPic;
        String buttonText;

        buttonPanel = new JPanel();
        buttonPanel.setLayout( new GridLayout( 1, button.length ) );

        button[ButtonEnum.ADD] = new JButton( new CommonGUI().getButtonText( "加入" ),
                                 new CommonGUI().getImageIcon( "add.png" ) );
        button[ButtonEnum.DOWNLOAD] = new JButton( new CommonGUI().getButtonText( "下載" ),
                                 new CommonGUI().getImageIcon( "download.png" ) );
        button[ButtonEnum.STOP] = new JButton( new CommonGUI().getButtonText( "停止" ),
                                 new CommonGUI().getImageIcon( "stop.png" ) );
        button[ButtonEnum.CLEAR] = new JButton( new CommonGUI().getButtonText( "清除" ),
                                 new CommonGUI().getImageIcon( "clear.png" ) );
        button[ButtonEnum.OPTION] = new JButton( new CommonGUI().getButtonText( "選項" ),
                                 new CommonGUI().getImageIcon( "option.png" ) );
        button[ButtonEnum.INFORMATION] = new JButton( new CommonGUI().getButtonText( "資訊" ),
                                 new CommonGUI().getImageIcon( "information.png" ) );
        button[ButtonEnum.EXIT] = new JButton( new CommonGUI().getButtonText( "離開" ),
                                 new CommonGUI().getImageIcon( "exit.png" ) );

        button[ButtonEnum.ADD].setToolTipText( "解析網址列的網址，解析後可選擇欲下載集?並加入任務" );
        button[ButtonEnum.DOWNLOAD].setToolTipText( "若網址列有網址，則解析後加入任務並開始下載；若網址列沒有網址，則開始下載目前的任務清單" );
        button[ButtonEnum.STOP].setToolTipText( "停止下載，中斷進行中的任務" );
        button[ButtonEnum.CLEAR].setToolTipText( "清除目前的任務清單（若一次無法清空且按多次）" );
        button[ButtonEnum.OPTION].setToolTipText( "功能設定與調整（粗體字為預設功能）" );
        button[ButtonEnum.INFORMATION].setToolTipText( "相關提示與訊息" );
        button[ButtonEnum.EXIT].setToolTipText( "關閉本程式" );

        for ( int count = 0; count < button.length; count ++ ) {
            button[count].setHorizontalTextPosition( SwingConstants.CENTER );
            button[count].setVerticalTextPosition( SwingConstants.BOTTOM );
            buttonPanel.add( button[count] );
            button[count].addActionListener( this );
        }

        contentPane.add( buttonPanel, BorderLayout.NORTH );
    }

    // 設置主界面上的網址輸入框
    private void setText( Container contentPane ) {
        urlField = new JTextField( "請複製欲下載的漫畫頁面網址，此輸入欄會自動捕捉" );
        urlField.setFont( new Font( "新細明體", Font.PLAIN, 22 ) );
        urlField.addMouseListener( this );
        urlField.setToolTipText( "請輸入漫畫作品的主頁面或單集頁面網址" );

        Document doc = urlField.getDocument();
        doc.addDocumentListener( this ); // check the string in urlField on time

        // set white space in up, down, left and right
        JPanel urlPanel = new CommonGUI().getCenterPanel( urlField );

        textPanel = new JPanel( new BorderLayout() );
        textPanel.add( urlPanel, BorderLayout.NORTH );
        setTable( textPanel );

        contentPane.add( textPanel, BorderLayout.CENTER );
    }

    // 設置主界面上的任務清單
    private void setTable( JPanel textPanel ) {
        downTable = getDefaultJTable();//new JTable( new DataTable());
        //downTable.setPreferredScrollableViewportSize( new Dimension( 450, 120 ) );
        downTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        downTable.setFillsViewportHeight( true );
        downTable.setAutoCreateRowSorter( true );

        JScrollPane downScrollPane = new JScrollPane( downTable );
        JPanel downPanel = new CommonGUI().getCenterPanel( downScrollPane );

        setTablePopupMenu(); // 設置右鍵彈出選單

        textPanel.add( downPanel, BorderLayout.CENTER );
    }

    public static Vector<String> getDefaultColumns() {
        Vector<String> columnName = new Vector<String>();
        columnName.add( "下載順序" );
        columnName.add( "是否下載" );
        columnName.add( "漫畫名稱" );
        columnName.add( "總共集數" );
        columnName.add( "勾選集數" );
        columnName.add( "目前狀態" );
        columnName.add( "網址解析" );

        return columnName;
    }

    private void setTablePopupMenu() {

        tableOpenDirectoryItem = new MenuItem( "開啟資料夾" ); // 開啟下載資料夾
        tableOpenDirectoryItem.addActionListener( this );
        tableRechoiceVolumeItem = new MenuItem( "重新選擇集數" );  // 重新選擇集數
        tableRechoiceVolumeItem.addActionListener( this );
        tableDeleteMissionItem = new MenuItem( "刪除此任務" );  // 刪除任務
        tableDeleteMissionItem.addActionListener( this );

        tablePopup = new PopupMenu();
        tablePopup.add( tableOpenDirectoryItem );
        tablePopup.add( tableRechoiceVolumeItem );
        tablePopup.add( tableDeleteMissionItem );

        downTable.add( tablePopup ); // 必須指定父元件，否則會拋出NullPointerException
    }

    private JTable getDefaultJTable() {
        downTableModel = Common.inputDownTableFile();//new DataTableModel( getDefaultColumns(), 0 );
        JTable table = new JTable( downTableModel );

        table.setPreferredScrollableViewportSize( new Dimension( 400, 170 ) );
        table.setFillsViewportHeight( true );
        table.setAutoCreateRowSorter( true ); // allow resort
        table.getSelectionModel().addListSelectionListener(new RowListener());
        table.addMouseListener( this );


        // 取得這個table的欄位模型
        TableColumnModel cModel = table.getColumnModel();

        // 配置每個欄位的寬度比例（可隨視窗大小而變化）
        cModel.getColumn( DownTableEnum.ORDER ).setPreferredWidth( (int) ( this.getWidth() * 0.16 ) );
        cModel.getColumn( DownTableEnum.YES_OR_NO ).setPreferredWidth( (int) ( this.getWidth() * 0.16 ) );
        cModel.getColumn( DownTableEnum.TITLE ).setPreferredWidth( (int) ( this.getWidth() * 0.6 ) );
        cModel.getColumn( DownTableEnum.VOLUMES ).setPreferredWidth( (int) ( this.getWidth() * 0.16 ) );
        cModel.getColumn( DownTableEnum.CHECKS ).setPreferredWidth( (int) ( this.getWidth() * 0.16 ) );
        cModel.getColumn( DownTableEnum.STATE ).setPreferredWidth( (int) ( this.getWidth() * 0.3 ) );
        cModel.getColumn( DownTableEnum.URL ).setPreferredWidth( (int) ( this.getWidth() * 0.02 ) );

        return table;
    }

    private void showNotifyMessage( String message ) {

    }

    private void setUpeListener() {
        //setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {
        Common.debugPrintln( "JComicDownloader start ..." );
        SetUp set = new SetUp();
        set.readSetFile(); // 讀入設置檔的設置參數
        new ComicDownGUI();
    }

    // --------- window event --------------
    
    public void windowGainedFocus(WindowEvent e){
        SystemClipBoard clip = new SystemClipBoard();
        
        String clipString = clip.getClipString();
        if ( Common.prevClipString != null ) { // 剛開啟不會去修改urlField的值
            if ( !clipString.equals( Common.prevClipString ) ) {
                //Common.debugPrint( "取得系統剪貼簿內容: " + clipString + "  " );
        
                if ( Common.isLegalURL( clipString ) ) {
                    urlField.setText( clipString ); // 當取得焦點時自動貼網址到輸入框中
                    Common.prevClipString = clipString;
                }
            }
            else
                urlField.setText( "" ); // 之前貼過的就不要再顯示了
        }
        else
            Common.prevClipString = ""; // 開啟之後就予值，啟動捕捉剪貼簿功能
    }
    public void windowLostFocus(WindowEvent e){
   
    }

    private void minimizeEvent() {
        this.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );

        this.addWindowListener( new WindowAdapter(){
            public void windowClosing( WindowEvent e ){
                if ( Common.isWindows() )
                    setState( Frame.ICONIFIED ); // 縮小後可觸發windowIconified()而縮入系統列
                else {
                    // 因為在ubuntu縮小收進系統框後無法再顯示視窗
                    // 只能用取消顯示再產生system tray
                    setVisible( false );
                    minimizeToTray();
                }
            }
            public void windowIconified( WindowEvent e ){
                if( SystemTray.isSupported() && Common.isWindows() ){
                    setVisible( false );
                    minimizeToTray();
                }
                else{
                    // ubuntu底下縮小收進系統框後無法取出，
                    // 因此在非windows環境，按縮小就只給縮小功能
                    setState( Frame.ICONIFIED );
               }
            }

        } );
    }

    public void minimizeToTray(){
        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add( this.trayIcon );
        } catch (AWTException ex) {
            System.err.println( "無法加入系統工具列圖示" );
            ex.printStackTrace();
        }
    }

    // 設置縮小到系統框的圖示
    private void initTrayIcon(){
        //Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("resource\\system_tray_ico.gif"));
        Image image = new CommonGUI().getImage( "main_icon.png" );

        trayExitItem = new MenuItem( "離開此程式" );
        trayExitItem.addActionListener( this );
        trayStartItem = new MenuItem( "開始任務" );
        trayStartItem.addActionListener( this );
        trayStopItem = new MenuItem( "停止任務" );
        trayStopItem.addActionListener( this );
        trayShowItem = new MenuItem( "開啟主界面" );
        trayShowItem.addActionListener( this );

        trayPopup = new PopupMenu();
        trayPopup.add( trayExitItem );
        trayPopup.add( trayStopItem );
        trayPopup.add( trayStartItem );
        trayPopup.add( trayShowItem );

        trayIcon = new TrayIcon( image, "JComicDownloader", trayPopup );
        trayIcon.addMouseListener( this );
   }

    // --------- urlField Event -------------

    public void insertUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) { ex.printStackTrace(); }

        //messageArea.append( webSite );
    }

    public void removeUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) { ex.printStackTrace(); }
    }

    public void changedUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) { ex.printStackTrace(); }
    }

    // -------------- mouse event   -----------------

    public void mousePressed( MouseEvent event ) {
        // when mouse move on the urlField, clear the text on urlField
        if ( event.getSource() == urlField ) {
            urlField.setText( "" );
        }

        if ( event.getSource() == trayIcon && event.getButton() == MouseEvent.BUTTON1 ) {
            setVisible( true );
            setState( Frame.NORMAL );
            SystemTray.getSystemTray().remove( trayIcon );
        }

        showPopup( event );
    }

    public void mouseExited( MouseEvent event ) {
    }
    public void mouseEntered( MouseEvent event ) {
    }
    public void mouseReleased( MouseEvent event ) {
        showPopup( event );
    }

    // 在任務列點擊左鍵兩下會跳出重新選擇集數的視窗，
    // 點擊右鍵一下會跳出是否刪除此任務的訊息視窗。
    public void mouseClicked( MouseEvent event ) {
        int row = event.getY() / downTable.getRowHeight();
        int col = downTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();


        if ( ( event.getModifiers() & MouseEvent.BUTTON1_MASK ) != 0 &&
             event.getClickCount() == 2 ) {
            if ( event.getSource() == downTable &&
                 row < Common.missionCount && row >= 0 ) {

                // 任何時候都能開啟下載資料夾
                if ( col == DownTableEnum.TITLE ) {
                    openDownloadDirectory( row );
                }

                // 正在下載的時候不能重選集數，也不能刪除任務
                if ( col == DownTableEnum.VOLUMES ||
                     col == DownTableEnum.CHECKS ) {
                    if ( !Flag.downloadingFlag )
                        rechoiceVolume( row );
                    else
                        JOptionPane.showMessageDialog( this, "目前正下載中，無法重新選擇集數",
                                               "提醒訊息", JOptionPane.INFORMATION_MESSAGE);
                }
                else if ( col == DownTableEnum.ORDER ) {
                    if ( !Flag.downloadingFlag )
                        deleteMission( row );
                    else
                        JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                                               "提醒訊息", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

    }



    private void rechoiceVolume( int row ) { // 重新選擇集數
        System.out.println( downTableModel.getRealValueAt( row, DownTableEnum.CHECKS ).toString() );
        ComicDownGUI.nowSelectedCheckStrings = Common.getSeparateStrings(
                                    String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.CHECKS ) ) );
        Common.debugPrintln( "重新解析位址（為了重選集數）：" + downTableUrlStrings[row] );
        parseURL( new String[]{downTableUrlStrings[row]}, false, true, row );
    }

    private void deleteMission( int row ) { // 刪除第row列任務
        String title = String.valueOf( downTableModel.getRealValueAt(
                                                 row, DownTableEnum.TITLE ) );
        String message = "是否要在下載任務中刪除" + title + " ?";
        int choice = JOptionPane.showConfirmDialog( this, message, "提醒訊息", JOptionPane.YES_NO_OPTION);

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            Common.missionCount --;
            downTableModel.removeRow( row );
        }
    }

    private void openDownloadDirectory( int row ) {  // 開啟第row列任務的下載資料夾
        try {
            String title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            Common.debugPrintln( "開啟" + title + "的下載資料夾" );
            if ( downTableModel.getRealValueAt( row, DownTableEnum.URL ).toString().matches( "(?s).*e-hentai(?s).*" ) )
                if ( Common.isWindows() )
                    Runtime.getRuntime().exec( "explorer " + SetUp.getOriginalDownloadDirectory() );
                else
                    Runtime.getRuntime().exec( "nautilus " + SetUp.getOriginalDownloadDirectory() );
            else
                if ( Common.isWindows() )
                    Runtime.getRuntime().exec( "explorer " + SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() );
                else
                    Runtime.getRuntime().exec( "nautilus " + SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() );
        } catch ( IOException ex ) { ex.printStackTrace(); }
    }

    private void showPopup( MouseEvent event ) {
        tablePopupRow = event.getY() / downTable.getRowHeight();
        if ( tablePopupRow < Common.missionCount && tablePopupRow >= 0 ) {
            if (event.isPopupTrigger()) {
                tablePopup.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
    }

    // --------------  button event   -----------------

    // 開始進行下載任務（若downloadAfterChoice為true，則先等待選擇集數完畢後，才開始下載。）
    public void startDownloadList( final boolean downloadAfterChoice ) {
        Thread downThread = new Thread( new Runnable() { public void run() {
            Common.debugPrintln( "進入下載主函式中" );

            if ( downloadAfterChoice ) {
                Common.downloadLock = true;
                Common.debugPrintln( "進入選擇集數，等待中..." );

                synchronized( ComicDownGUI.mainFrame ) { // lock main frame
                    while ( Common.downloadLock ) {
                        try { ComicDownGUI.mainFrame.wait(); }
                        catch ( InterruptedException ex ) { ex.printStackTrace(); }
                    }
                }
                Common.debugPrintln( "選擇集數完畢，結束等待" );
            }

            for ( int i = 0; i < Common.missionCount && Run.isAlive; i ++ ) {
                if ( downTableModel.getValueAt( i, DownTableEnum.YES_OR_NO ).toString().equals( "false" ) ||
                     downTableModel.getValueAt( i, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                    Common.processPrintln( "跳過 " + downTableModel.getValueAt( i, DownTableEnum.TITLE ).toString() );
                    continue;
                }
                
                // ex http://xxx  http://xxx  ...
                String[] urlStrings = Common.getSeparateStrings(
                                        downTableModel.getValueAt( i, DownTableEnum.URL ).toString() );

                // ex. true  false  false ...
                String[] checkStrings = Common.getSeparateStrings(
                                         String.valueOf( downTableModel.getRealValueAt( i, DownTableEnum.CHECKS ) ) );

                int downloadCount = 0;
                for ( int j = 0; j < urlStrings.length && Run.isAlive; j ++ ) {
                    if ( checkStrings[j].equals( "true" ) ) {
                        Flag.allowDownloadFlag = true;
						
                        String nowState = "進度：" + downloadCount + " / " + Common.getTrueCountFromStrings( checkStrings );
                        downTableModel.setValueAt( nowState, i, DownTableEnum.STATE );
                        // 啟動下載
                        System.out.println( "TTTTTTTTTTTTTTTTTTTTTTTTTTTT:  " + downTableModel.getValueAt( i, DownTableEnum.TITLE ).toString() );
                        Run mainRun = new Run( urlStrings[j], downTableModel.getValueAt( i, DownTableEnum.TITLE ).toString(), 
                                                    RunModeEnum.DOWNLOAD_MODE ); 
                        mainRun.run();
                        downloadCount ++;
                    }
                }

                if ( Run.isAlive ) {
                    downTableModel.setValueAt( "下載完畢", i, DownTableEnum.STATE );

                    String title = String.valueOf( downTableModel.getRealValueAt( i, DownTableEnum.TITLE ) );

                    if ( SetUp.getShowDoneMessageAtSystemTray() )
                        trayIcon.displayMessage( "JComicDownloader Message", title + "下載完畢! " , TrayIcon.MessageType.INFO );
                }
                else {
                    downTableModel.setValueAt( "下載中斷", i, DownTableEnum.STATE );
                    trayIcon.setToolTip( "下載中斷" );
                }
            }
            if ( Run.isAlive ) {
                stateBar.setText( Common.missionCount + "個任務全部下載完畢! " );
                if ( SetUp.getShowDoneMessageAtSystemTray() )
                    trayIcon.displayMessage( "JComicDownloader Message", Common.missionCount + "個任務全部下載完畢! " , TrayIcon.MessageType.INFO );
                trayIcon.setToolTip( "JComicDownloader" );
                Flag.allowDownloadFlag = false;

                System.gc(); // 下載完就建議JAVA做垃圾回收
            }
        } } );

        Common.downloadThread = downThread;
        downThread.start();
    }

    // 開始下載urlTextField目前位址指向的集數
    public void startDownloadURL( final String[] newArgs ) {
        Thread downThread = new Thread( new Runnable() { public void run() {
            // download comic from url
            Flag.allowDownloadFlag = true; // allow the action of download
            Common.processPrintln( "開始單集下載" );
            stateBar.setText( "  開始單集下載" );
            Run singleRun = new Run( newArgs, RunModeEnum.DOWNLOAD_MODE );
            singleRun.start();
            try {
                 singleRun.join();
            } catch ( InterruptedException ex ) { ex.printStackTrace(); }
            Flag.allowDownloadFlag = false;
        } } );

        Common.downloadThread = downThread;
        downThread.start();
    }

    // 跳出選擇集數的視窗
    public void runChoiceFrame( boolean modifySelected, int modifyRow, String title ) {
        //SwingUtilities.invokeLater( new Runnable(){ public void run() {
        String urlString = urlField.getText();

        ChoiceFrame choiceFrame;
        if ( modifySelected )
            choiceFrame = new ChoiceFrame( "重新選擇欲下載的集數", true, modifyRow, title, urlString );
        else
            choiceFrame = new ChoiceFrame( title, urlString );

        String[] volumeStrings = choiceFrame.getVolumeStrings();
        String[] checkStrings = choiceFrame.getCheckStrings();

        args = null;
        urlField.setText( "" );
        repaint();

    }

    // 分析位址，分析完後開始下載。
    public void parseURL( final String[] newArgs,
                          final boolean allowDownload,
                          final boolean modifySelected,
                          final int modifyRow ) {  // call by add button and download button
        Thread praseThread = new Thread( new Runnable(){ public void run() {
            Common.urlIsUnknown = false; // 解決一次不認識之後就都不認識的bug
            if ( newArgs == null || newArgs[0].equals( "" ) ) {
                if ( !allowDownload )
                    stateBar.setText( "  沒有輸入網址 !!" );
                else {
                    if ( Common.missionCount > 0 ) {
                        Flag.parseUrlFlag = false; // 分析結束
                        startDownloadList( false ); // download all selected comic
                    }
                    else
                        stateBar.setText( "  沒有下載任務也沒有輸入網址 !!" );
                }
            }
            else if ( !Common.isLegalURL( newArgs[0] ) ) { // url is illegal
                stateBar.setText( "  網址錯誤，請輸入正確的網址 !!" );
            }
            else {
                stateBar.setText( "  解析網址中" );
                if ( Common.withGUI() )
                    trayIcon.setToolTip( "解析網址中" );

                Flag.allowDownloadFlag = false;
                Run.isAlive = true; // open up the download work

                String[] tempArgs = Common.getCopiedStrings( newArgs );
                //args = null;

                //int runMode = allowDownload ? RunModeEnum.DOWNLOAD_MODE : RunModeEnum.PARSE_MODE;
                mainRun = new Run( tempArgs, RunModeEnum.PARSE_MODE );
                mainRun.start();
                String title = "";
                try {
                    mainRun.join();
                    
                } catch ( InterruptedException ex ) { ex.printStackTrace(); }
                
                title = mainRun.getTitle();
                Common.debugPrintln( "選擇集數前解析得到的title：" + title );
                if ( Common.urlIsUnknown )
                    stateBar.setText( "  無法解析此網址 !!" );
                else if ( Common.isMainPage ) { // args is main page
                    runChoiceFrame( modifySelected, modifyRow, title );

                    if ( allowDownload ) {
                        Flag.parseUrlFlag = false; // 分析結束
                        startDownloadList( true ); // download all selected comic
                    }
                }
                else { // args is single page
                    if ( !allowDownload )
                        stateBar.setText( "  單集頁面無法加入下載佇列 !!" );
                    else {
                        stateBar.setText( "  正在下載單一集數" );
                        Flag.parseUrlFlag = false; // 分析結束
                        startDownloadURL( tempArgs ); // download single comic on textfield
                    }
                }

            }

            Flag.parseUrlFlag = false; // 分析結束
        } } );

        praseThread.start();
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event.getSource() == urlField ) {

        }
        if ( event.getSource() == tableOpenDirectoryItem ) {
            openDownloadDirectory( tablePopupRow );
        }
        if ( event.getSource() == tableRechoiceVolumeItem ) {
            if ( !Flag.downloadingFlag )
                rechoiceVolume( tablePopupRow );
            else
                JOptionPane.showMessageDialog( this, "目前正下載中，無法重新選擇集數",
                                               "提醒訊息", JOptionPane.INFORMATION_MESSAGE);
        }
        if ( event.getSource() == tableDeleteMissionItem ) {
            if ( !Flag.downloadingFlag )
                deleteMission( tablePopupRow );
            else
                JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                                               "提醒訊息", JOptionPane.INFORMATION_MESSAGE);
        }

        if ( event.getSource() == trayShowItem ) {
            setVisible( true );
            setState( Frame.NORMAL );
            SystemTray.getSystemTray().remove( trayIcon );
        }

        if ( event.getSource() == button[ButtonEnum.ADD] ) { // button of add
            logFrame.redirectSystemStreams(); // start to log message

            parseURL( args, false, false, 0 );
            args = null;
        }
        if ( event.getSource() == button[ButtonEnum.DOWNLOAD] ||
             event.getSource() == trayStartItem ) { // button of Download
            if ( Flag.downloadingFlag || Flag.parseUrlFlag ) { // 目前正分析網址或下載中，不提供直接下載服務
                JOptionPane.showMessageDialog( this, "目前正下載中，不提供直接下載，請按「加入」來加入下載任務。", "提醒訊息", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                logFrame.redirectSystemStreams(); // start to log message
                Run.isAlive = true;
                stateBar.setText( "開始下載中..." );
                Flag.parseUrlFlag = true; // 開始分析
                parseURL( args, true, false, 0 );
                args = null;
            }
        }
        if ( event.getSource() == button[ButtonEnum.STOP] ||
             event.getSource() == trayStopItem ) { // button of stop
            Run.isAlive = false; // forbid download work
            Flag.allowDownloadFlag = Flag.downloadingFlag = Flag.parseUrlFlag = false;
            stateBar.setText( "所有下載任務停止" );
            trayIcon.setToolTip( "JComicDownloader" );
        }
        if ( event.getSource() == button[ButtonEnum.OPTION] ) { // button of Option
            //new Thread( new Runnable() { public void run() {
            javax.swing.SwingUtilities.invokeLater( new Runnable() { public void run() {
                    new OptionFrame();
                } } );
        }
        if ( event.getSource() == button[ButtonEnum.INFORMATION] ) { // button of Information
            javax.swing.SwingUtilities.invokeLater( new Runnable() { public void run() {
                    final InformationFrame frame = new InformationFrame();

                    javax.swing.SwingUtilities.invokeLater( new Runnable() { public void run() {
                        frame.setNewestVersion();
                    } } );
                } } );
        }
        if ( event.getSource() == button[ButtonEnum.CLEAR] ) { // button of CLEAR
            int downListCount = downTableModel.getRowCount();
            while ( downTableModel.getRowCount() > 1 ) {
                downTableModel.removeRow( downTableModel.getRowCount() - 1 );
                Common.missionCount --;
            }
            if ( Common.missionCount > 0 )
                downTableModel.removeRow( 0 );
            repaint(); // 重繪

            Common.missionCount = 0;
            Common.processPrint( "全部下載任務清空" );
            stateBar.setText( "全部下載任務清空" );
            trayIcon.setToolTip( "JComicDownloader" );
        }
        if ( event.getSource() == button[ButtonEnum.EXIT] ||
             event.getSource() == trayExitItem ) { // button of Exit
            int choice = JOptionPane.showConfirmDialog( this, "請問是否要關閉JComicDownloader？",
                                                        "提醒訊息", JOptionPane.YES_NO_OPTION);

            if ( choice == JOptionPane.YES_OPTION ) {
                // 輸出下載任務清單，下次開啟時會自動載入
                Common.outputDownTableFile( downTableModel );
                Run.isAlive = false;
                Common.debugPrintln( "刪除所有暫存檔案" );
                Common.deleteFolder( Common.tempDirectory ); // 刪除暫存檔
                Common.debugPrintln( "Exit JComicDownloader ... " );
                
                System.exit( 0 );
            }
        }
    }

    private void whlie(boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //addWindowListener(new WindowAdapter(){

    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            /* replace this with mouse event
            if (event.getValueIsAdjusting()) {
                return;
            }
            if ( Flag.downloadFlag )
                return;

            int row = downTable.getSelectionModel().getLeadSelectionIndex();
            int col = downTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
            //System.out.println( col );

            if ( row > 0 && col != 0 && col != 1 && col != 6 ) {
                //System.out.println( row + " : " + downTableUrlStrings[row] );

                ComicDownGUI.nowSelectedCheckStrings = Common.getSeparateStrings(
                                    String.valueOf( downTableModel.getRealValueAt(
                                            row, DownTableEnum.CHECKS ) ) );

                parseURL( new String[]{downTableUrlStrings[row]}, false, true, row );
            }
            */

        }
    }

}
