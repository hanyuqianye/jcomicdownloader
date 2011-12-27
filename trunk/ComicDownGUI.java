/*
 * JComicDownloader
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Version  : v2.12
Last Modified : 2011/12/27
----------------------------------------------------------------------------------------------------
ChangeLog:
 * 2.12: 1. 新增對www.bengou.com的支援。
 * 2.11: 1. 新增對www.kangdm.com的支援。
 *      2. 增加搜尋此本漫畫的右鍵選單。
 *      3. 增加取消勾選『分析後下載圖檔』時的提醒視窗。
 *      4. 修復manhua.178.com擷取網頁時出錯的問題。（應該都可以正常下載了）
 *      5. 修復重試後無法下載中間漏頁的問題。（ex. 5.jpg 7.jpg 8.jpg，中間遺漏6.jpg）
 * 2.10: 1. 新增對manhua.178.com的支援。（仍有些問題，測試中）
 *      2. 增加任務完成音效的選項。
 *      3. 修改黑底介面的訊息文字顯示顏色（藍色 -> 黃色）。
 *      4. 修復kuku解析少數圖片網址時後面多出">"的問題。
 *      5. 修復沒有設定瀏覽圖片程式便無法開啟網頁的問題。
 * 2.09: 1. 新增對www.kkkmh.com/的支援。
 *      2. 新增對6comic.com的支援。
 *      3. 增加開啟原始網頁的右鍵選單。
 *      4. 修復顯示加入單集的訊息後仍抓取網址的問題。
 *      5. 修復Linux系統下無法在同目錄讀取set.ini的問題。
 *      6. 修復Linux系統下無法正常使用JTattoo介面的問題。
 *      7. 拿掉對comic.92wy.com的支援。（關站了......）
 * 2.08: 1. 增加額外的JTattoo介面選項（共增加11組介面可供選擇）。
 *      2. 修復xindm解析錯誤的bug。
 *      3. 修復部份89890解析錯誤的bug。
 * 2.07: 1. 新增對comic.sfacg.com的支援。
 *      2. 增加baidu頁面下載時的選項。（至貼圖結束為止或解析全部頁面）
 *      3. 修改紀錄檔機制，下載清單和書籤清單會寫出檔案多次，避免因不正常關閉造成記錄遺失。
 * 2.06: 1. 新增對baidu的支援。
 *      2. 修復集數名稱數字格式化的bug。
 * 2.05: 1. 修改選項視窗，明確顯示失敗重傳次數和連線逾時時間（因為linux系統下無法看到刻度）。
 *      2. 修復Linux系統下無法開啟檔案的bug。
 *      3. 修復無法開啟壓縮檔的bug。（預設開啟圖片和壓縮檔為同個程式）
 *      4. 修復暫存資料夾路徑無法改變的bug。
 *      5. 修復部分集數名稱解析失敗的bug。（數字規格化改由前面開始找）
 * 2.04: 1. 增加選擇紀錄檔和暫存資料夾的選項。
 *      2. 修改下拉式介面選單的渲染機制，使其可改變字型。 
 *      3. 修改集數名稱命名機制，將裡面的數字格式化（ex. 第3回 -> 第003回），以方便排序。
 *      4. 增加選擇字型時可以預覽字型的功能。
 *      5. 修復部份CC漫畫解析錯誤的bug。
 * 
 * 2.03: 1. 新增對www.iibq.com的支援。
 *      2. 增加可用外部程式開啟漫畫的選項。
 *      3. 增加逾時倒數時間設定的選項。
 *      4. 增加下載失敗後重新嘗試次數(retryTimes)的選項
 *      5. 修復nanadm有些第一集無法解析的bug。
 *      6. 因應dmeden轉換位址進行解析修正（dmeden.net <-> www.dmeden.com）
 *      7. 因應改版後的mangaFox進行解析修正
 *      8. 修改選項視窗為多面板介面。
 * 2.02: 1. 新增對www.citymanga.com的支援。
 *      2. 修復kuku網址轉碼部份發生錯誤的bug。
 *      3. 修復92wy部分集數無法讀取的bug。
 *      4. 修復mangaFox部份集數命名重疊的bug。
 * 
 * 2.01: 1. 在訊息視窗中加入支援列表的資訊。
 *      2. 在選項視窗中加入可以預設全選集數的選項。
 *      3. 修改下載機制，不下載青蛙圖（檔案大小10771 bytes）。
 *      4. 修復NANA無法解析粗體字集數名稱的bug。
 *      5. 修正Google圖片搜尋中部份非英文關鍵字沒有正確解析為資料夾名稱的bug。
 * 2.0 : 1. 新增對www.nanadm.com的支援。
 *      2. 修復Google圖片搜尋批次每張圖下載十秒後就逾時的bug。
 *      3. 修復書籤和紀錄表格改變外觀順序後無法對應的bug。
 *      4. 修復下載任務置頂或置底卻沒有改變下載順序的bug。
 *      5. 修復下載表格改變外觀順序後發生錯誤的bug。（作法就是禁止改變下載表格的外觀順序......）

 * 1.19: 1. 修正後已支援『顯示更多結果』後面的圖。
 *      2. 修改下載機制，遇到非正常連線直接放棄，加快速度。
 * 1.18: 1. 新增新增對google圖片搜尋的支援(僅支援前237張)。
 * 1.17: 1. 按下載按鈕後會回到下載任務頁面。
 *      2. 修復集數名稱後面數字會消失的bug。
=======
 * 1.17: 1. 按下載按鈕後會回到下載任務頁面。
 *      2. 修復集數名稱後面數字會消失的bug。
>>>>>>> .r66
 * 1.16: 1. 新增新增對comic.92wy.com的支援。
 *      2. 新增對EX的支援。
 *      3. 增加將下載任務置頂與置底的右鍵選單。
 *      4. 增加紀錄檔存放資料夾選項（set.ini裡面的recordFileDirectory項目）
 *      5. 修改選項視窗，使之勾選自動刪除就要連帶勾選自動壓縮。
 *      6. 修復部分網站無法立即跳至最後一張圖片開始下載的bug。
 *      7. 修復記錄檔讀取失敗會無法開啟的bug。
 * 1.15: 1. 新增對xindm.cn的支援。
 *      2. 增加網址列的右鍵選單（貼上網址）。
 *      3. 修復"坂"無對應繁體字的bug。
 * 1.14: 1. 新增對manmankan.com的支援。
 *      2. 增加可選擇字型和字體大小的選項。
 *      3. 修改集數選擇視窗（choiceFrame）的關閉功能，允許按右上角的『X』來關閉。
 *      4. 修復若沒有下載成功仍會產生空壓縮檔的bug。
 *      5. 修復official.html無法刪除的bug。 
 * 1.13: 1. 新增對mangafox.com的支援。
 *      2. 修復jumpcn.com.cn因置換伺服器而解析錯誤的問題。
 * 1.12: 1. 增加對jumpcn.com的支援。
 *      2. 增加兩個下載任務區的右鍵選單：『刪除所有未勾選任務』和『刪除所有已完成任務』。
 *      3. 修復刪除單一下載任務會造成網址錯誤的bug。
 *      4. 修復tempDirectory最後出現兩個斜線的bug。
 *      5. 修復8comic的圖庫選擇集數時無法感知是否已經存在於資料夾的bug。
 *      6. 修復kuku的美食的俘虜解析網址錯誤的bug。
 *      7. 讓部分網站邊解析邊下載，降低實際下載前的解析時間。
 * 1.11: 1. 新增對dmeden.net的支援。
 *      2. 將volumeTitle傳入RunModule，可避免重新解析單集名稱不一致的問題。
 *      3. 選取集數視窗中，已經存在於資料夾的集數會呈淺灰色。
 *      4. 修復記錄表格無法點選刪除的bug。
 * 1.10: 1. 加入www.jumpcn.com.cn的支援
 *      2. 修復一些1.09版的bug
 * 1.09: 1. 加入書籤和記錄兩個新頁面。
 *      2. 修復右鍵表單出現亂碼的bug。
 *      3. 拿掉多餘標題字尾。
 * 1.08: 1. 新增對8comic的支援，包含免費漫畫區和圖庫區。
 *      2. 下載圖檔時若發現圖檔大小只有21或22kb，則懷疑連接到盜連警示圖片，停一秒後重新連線一次。
 *      3. 若logFrame有開啟，就會自動讓logFrame輸出資訊。
 *      4. 修復在取得最新版本資訊時無法點擊其他按鈕的bug。
 *      5. 修復選取集數後按『下載』仍跳出選取集數視窗的bug。
 * 1.07: 修復EH無法下載會出現警告頁面Content Warning的問題

----------------------------------------------------------------------------------------------------

 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader;

import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.table.*;
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
import javax.swing.JFileChooser.*;

/**
 * @author surveyorK
 * @version 1.14
 * user 主介面，同時監聽window、mouse、button和textField。
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
    JTabbedPane tabbedPane; // 裡面放三個頁面（任務、書籤、紀錄）
    public static TrayIcon trayIcon; // 系統列圖示
    private JPopupMenu trayPopup;
    private JMenuItem trayShowItem;  // 開啟主介面
    private JMenuItem trayStartItem; // 開始任務
    private JMenuItem trayStopItem;  // 停止任務
    private JMenuItem trayExitItem;  // 離開
    private JPopupMenu urlFieldPopup; // 網址列的右鍵選單
    private JMenuItem pasteSystemClipboardItem; // 網址列的右鍵選單項目一
    private JPopupMenu downloadTablePopup;
    private int downloadTablePopupRow; // 觸發downloadTablePopup的所在列
    private JMenuItem tableSearchDownloadComic;  // 以瀏覽器開啟搜尋下載漫畫的搜尋頁面
    private JMenuItem tableSearchBookmarkComic;  // 以瀏覽器開啟搜尋書籤漫畫的搜尋頁面
    private JMenuItem tableSearchRecordComic;  // 以瀏覽器開啟搜尋記錄漫畫的搜尋頁面
    private JMenuItem tableOpenDownloadURL;  // 以瀏覽器開啟漫畫網址
    private JMenuItem tableOpenDownloadFile;  // 開啟下載檔案
    private JMenuItem tableOpenDownloadDirectoryItem;  // 開啟下載資料夾
    private JMenuItem tableAddBookmarkFromDownloadItem;  // 加入到書籤
    private JMenuItem tableRechoiceVolumeItem;  // 重新選擇集數
    private JMenuItem tableDeleteMissionItem;  // 刪除任務
    private JMenuItem tableDeleteAllUnselectedMissionItem;  // 刪除所有未勾選的任務
    private JMenuItem tableDeleteAllDoneMissionItem;  // 刪除所有已經完成的任務
    private JMenuItem tableMoveToRoofItem;  // 將此任務置頂
    private JMenuItem tableMoveToFloorItem;  // 將此任務置底
    private JPopupMenu bookmarkTablePopup;
    private int bookmarkTablePopupRow; // 觸發downloadTablePopup的所在列
    private JMenuItem tableOpenBookmarkURL;  // 開啟漫畫網址
    private JMenuItem tableOpenBookmarkFile;  // 開啟書籤檔案
    private JMenuItem tableOpenBookmarkDirectoryItem;  // 開啟書籤資料夾
    private JMenuItem tableAddMissionFromBookmarkItem;  // 加入到任務
    private JMenuItem tableDeleteBookmarkItem;  // 刪除書籤
    private JPopupMenu recordTablePopup;
    private int recordTablePopupRow; // 觸發downloadTablePopup的所在列
    private JMenuItem tableOpenRecordURL;  // 開啟記錄漫畫網址
    private JMenuItem tableOpenRecordFile;  // 開啟記錄檔案
    private JMenuItem tableOpenRecordDirectoryItem;  // 開啟記錄資料夾
    private JMenuItem tableAddBookmarkFromRecordItem;  // 加入到書籤
    private JMenuItem tableAddMissionFromRecordItem;  // 加入到任務
    private JMenuItem tableDeleteRecordItem; // 刪除記錄
    public static LogFrame logFrame; // show log, for debug
    public JTable downTable;
    public JTable bookmarkTable;
    public JTable recordTable;
    public static JLabel stateBar;
    public static DownloadTableModel downTableModel;
    public static BookmarkTableModel bookmarkTableModel;
    public static RecordTableModel recordTableModel;
    public static String[] downTableUrlStrings;
    public static String[] nowSelectedCheckStrings;
    public static int[][] downTableRealChoiceOrder;
    public static String defaultSkinClassName;
    // non-GUI component
    private String[] args;
    private static String resourceFolder;
    private StringBuffer messageString;
    private Run mainRun;

    public ComicDownGUI() {
        super( "JComicDownloader  v2.12" );

        minimizeEvent();
        initTrayIcon();

        mainFrame = this; // for change look and feel

        if ( Common.isUnix() ) {
            defaultSkinClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        } else {
            defaultSkinClassName = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        }

        resourceFolder = "resource" + Common.getSlash();

        downTableUrlStrings = new String[1000]; // 若任務超過1000個可能就會出錯...
        downTableRealChoiceOrder = new int[2000][]; // 最多1000個任務，每個任務最多2000集... ex.柯南七百集了...

        // 建立logFrame視窗，是否開啟則視預設值而定
        //javax.swing.SwingUtilities.invokeLater( new Runnable() { sdf

        //    public void run() {
        Thread logFrameThread = new Thread( new Runnable() {

            public void run() {
                logFrame = new LogFrame();

                if ( SetUp.getOpenDebugMessageWindow() ) { // 由logFrame輸出資訊
                    logFrame.setVisible( true );
                    Debug.commandDebugMode = false;
                } else { // 由cmd輸出資訊
                    logFrame.setVisible( false );
                    Debug.commandDebugMode = true;
                }

            }
        } );
        logFrameThread.start();

        counter();  // 以code頁面記錄開啟次數（好玩測試看看）

        messageString = new StringBuffer( "" );

        setUpUIComponent();
        setUpeListener();
        setVisible( true );

        checkSkin(); // 檢查skin是否由外部jar支援，若是外部skin且沒有此jar，則下載

    }

    // 檢查skin是否由外部jar支援，若是外部skin且沒有此jar，則下載
    private void checkSkin() {
        if ( SetUp.getSkinClassName().matches( "com.jtattoo.plaf.*" ) && !new File( Common.getNowAbsolutePath() + "JTattoo.jar" ).exists() ) {
            new CommonGUI().downloadJTattoo(); // 下載JTattoo.jar
        }
    }

    private void setUpUIComponent() {
        setSize( 640, 480 );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) ); // 設置左上角圖示

        addWindowFocusListener( this ); // 用來監測主視窗情形，若取得焦點就在輸入欄貼上剪貼簿網址

        Container contentPane = getContentPane();

        setButton( contentPane );
        setText( contentPane );

        // default skin: Windows uses Nimbus skin, Ubuntu uses GTK skin
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
    private void setSkin() {
        setSkin( SetUp.getSkinClassName() );
    }

    private void setSkin( String skinClassName ) {
        Common.debugPrintln( "設置" + skinClassName + "介面" );
        try {
            CommonGUI.setLookAndFeelByClassName( skinClassName );
        } catch ( Exception ex ) {
            Common.errorReport( "無法使用" + skinClassName + "介面 !!" );

            // 若無法配置指定的skin，就用預設的
            CommonGUI.setLookAndFeelByClassName( defaultSkinClassName );
        }
        SwingUtilities.updateComponentTreeUI( this );
    }

    // 設置主介面上的主要按鈕
    private void setButton( Container contentPane ) {
        button = new JButton[7];
        String buttonPic;
        String buttonText;

        buttonPanel = new JPanel();
        buttonPanel.setLayout( new GridLayout( 1, button.length ) );

        button[ButtonEnum.ADD] = getButton( "加入", "add.png" );
        button[ButtonEnum.DOWNLOAD] = getButton( "下載", "download.png" );
        button[ButtonEnum.STOP] = getButton( "停止", "stop.png" );
        button[ButtonEnum.CLEAR] = getButton( "清除", "clear.png" );
        button[ButtonEnum.OPTION] = getButton( "選項", "option.png" );
        button[ButtonEnum.INFORMATION] = getButton( "資訊", "information.png" );
        button[ButtonEnum.EXIT] = getButton( "離開", "exit.png" );

        button[ButtonEnum.ADD].setToolTipText( "解析網址列的網址，解析後可選擇欲下載集數並加入任務" );
        button[ButtonEnum.DOWNLOAD].setToolTipText( "若網址列有網址，則解析後加入任務並開始下載；若網址列沒有網址，則開始下載目前的任務清單" );
        button[ButtonEnum.STOP].setToolTipText( "停止下載，中斷進行中的任務" );
        button[ButtonEnum.CLEAR].setToolTipText( "清除目前的任務清單（若一次無法清空且按多次）" );
        button[ButtonEnum.OPTION].setToolTipText( "功能設定與調整（粗體字為預設功能）" );
        button[ButtonEnum.INFORMATION].setToolTipText( "相關提示與訊息" );
        button[ButtonEnum.EXIT].setToolTipText( "關閉本程式" );

        for ( int count = 0 ; count < button.length ; count++ ) {
            button[count].setHorizontalTextPosition( SwingConstants.CENTER );
            button[count].setVerticalTextPosition( SwingConstants.BOTTOM );
            buttonPanel.add( button[count] );
            button[count].addActionListener( this );
        }

        contentPane.add( buttonPanel, BorderLayout.NORTH );
    }

    // 設置主介面上的網址輸入框
    private void setText( Container contentPane ) {
        urlField = new JTextField( "請複製欲下載的漫畫頁面網址，此輸入欄會自動捕捉" );
        urlField.setFont( SetUp.getDefaultFont( 3 ) );
        urlField.addMouseListener( this );
        urlField.setToolTipText( "請輸入漫畫作品的主頁面或單集頁面網址" );

        setUrlFieldJPopupMenu(); // 設置右鍵彈出選單

        Document doc = urlField.getDocument();
        doc.addDocumentListener( this ); // check the string in urlField on time

        // set white space in up, down, left and right
        JPanel urlPanel = new CommonGUI().getCenterPanel( urlField );

        textPanel = new JPanel( new BorderLayout() );
        textPanel.add( urlPanel, BorderLayout.NORTH );
        setTabbedPane( textPanel );

        contentPane.add( textPanel, BorderLayout.CENTER );
    }

    // 設置主介面上的任務清單
    private void setDownloadTable( JPanel textPanel ) {
        downTable = getDownloadTable();//new JTable( new DataTable());
        //downTable.setPreferredScrollableViewportSize( new Dimension( 450, 120 ) );
        downTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );

        JScrollPane downScrollPane = new JScrollPane( downTable );
        //JPanel downPanel = new CommonGUI().getCenterPanel( downScrollPane );

        setDownloadTableJPopupMenu(); // 設置右鍵彈出選單

        textPanel.add( downScrollPane, BorderLayout.CENTER );
    }

    // 設置主介面上的書籤頁面
    private void setBookmarkTable( JPanel textPanel ) {
        bookmarkTable = getBookmarkTable();//new JTable( new DataTable());
        //downTable.setPreferredScrollableViewportSize( new Dimension( 450, 120 ) );
        bookmarkTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        bookmarkTable.setFillsViewportHeight( true );
        bookmarkTable.setAutoCreateRowSorter( true );

        JScrollPane bookmarkScrollPane = new JScrollPane( bookmarkTable );
        //JPanel downPanel = new CommonGUI().getCenterPanel( downScrollPane );

        setBookmarkTableJPopupMenu(); // 設置右鍵彈出選單

        textPanel.add( bookmarkScrollPane, BorderLayout.CENTER );
    }

    private void setRecordTable( JPanel textPanel ) {
        recordTable = getRecordTable();//new JTable( new DataTable());
        //downTable.setPreferredScrollableViewportSize( new Dimension( 450, 120 ) );
        recordTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        recordTable.setFillsViewportHeight( true );
        recordTable.setAutoCreateRowSorter( true );

        JScrollPane recordScrollPane = new JScrollPane( recordTable );
        //JPanel downPanel = new CommonGUI().getCenterPanel( downScrollPane );

        setRecordTableJPopupMenu(); // 設置右鍵彈出選單

        textPanel.add( recordScrollPane, BorderLayout.CENTER );
    }

    private void setTabbedPane( JPanel textPanel ) {

        tabbedPane = new JTabbedPane();

        //JComponent panel1 = makeTextPanel("Panel #1");
        JPanel downTablePanel = new JPanel( new GridLayout( 1, 1 ) ); // 不規範為gridLayout就會固定大小...
        setDownloadTable( downTablePanel );
        tabbedPane.addTab( " 下載任務  ", new CommonGUI().getImageIcon( "tab_download.png" ),
                downTablePanel, "所有欲下載的任務都會出現在此處，可依序下載" );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_1 );

        JPanel bookmarkTablePanel = new JPanel( new GridLayout( 1, 1 ) ); // 不規範為gridLayout就會固定大小...
        setBookmarkTable( bookmarkTablePanel );
        tabbedPane.addTab( " 我的書籤  ", new CommonGUI().getImageIcon( "tab_bookmark.png" ),
                bookmarkTablePanel, "希望持續追蹤的漫畫可加入到此處" );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_2 );

        JPanel recordTablePanel = new JPanel( new GridLayout( 1, 1 ) ); // 不規範為gridLayout就會固定大小...
        setRecordTable( recordTablePanel );
        tabbedPane.addTab( " 任務記錄  ", new CommonGUI().getImageIcon( "tab_record.png" ),
                recordTablePanel, "所有曾經加入到下載任務的漫畫都會記錄在這邊，可由『選項』來選擇持續記錄或關閉後清空" );
        tabbedPane.setMnemonicAt( 2, KeyEvent.VK_3 );

        textPanel.add( tabbedPane, BorderLayout.CENTER );
    }

    protected JComponent makeTextPanel( String text ) {
        JPanel panel = new JPanel( false );
        JLabel filler = new JLabel( text );
        filler.setHorizontalAlignment( JLabel.CENTER );
        panel.setLayout( new GridLayout( 1, 1 ) );
        panel.add( filler );
        return panel;
    }

    public static Vector<String> getDownloadColumns() {
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

    public static Vector<String> getBookmarkColumns() {
        Vector<String> columnName = new Vector<String>();
        columnName.add( "編號" );
        columnName.add( "漫畫名稱" );
        columnName.add( "漫畫網址" );
        columnName.add( "加入日期" );
        columnName.add( "評論註解" );

        return columnName;
    }

    public static Vector<String> getRecordColumns() {
        Vector<String> columnName = new Vector<String>();
        columnName.add( "編號" );
        columnName.add( "漫畫名稱" );
        columnName.add( "漫畫網址" );
        columnName.add( "加入日期" );

        return columnName;
    }

    private void setUrlFieldJPopupMenu() {

        pasteSystemClipboardItem = new JMenuItem( "貼上網址" ); // 開啟下載資料夾
        pasteSystemClipboardItem.addActionListener( this );

        urlFieldPopup = new JPopupMenu();
        urlFieldPopup.add( pasteSystemClipboardItem );
        urlField.add( urlFieldPopup ); // 必須指定父元件，否則會拋出NullPointerException
    }

    private void setDownloadTableJPopupMenu() {
        tableSearchDownloadComic = new JMenuItem( "搜尋這本漫畫" ); // 以瀏覽器開啟搜尋此本漫畫的搜尋頁面
        tableSearchDownloadComic.addActionListener( this );
        tableOpenDownloadURL = new JMenuItem( "開啟網頁" ); // 以瀏覽器開啟漫畫網址
        tableOpenDownloadURL.addActionListener( this );
        tableOpenDownloadFile = new JMenuItem( "開啟檔案" ); // 開啟下載資料夾
        tableOpenDownloadFile.addActionListener( this );
        tableOpenDownloadDirectoryItem = new JMenuItem( "開啟資料夾" ); // 開啟下載資料夾
        tableOpenDownloadDirectoryItem.addActionListener( this );
        tableAddBookmarkFromDownloadItem = new JMenuItem( "加入到書籤" ); // 開啟下載資料夾
        tableAddBookmarkFromDownloadItem.addActionListener( this );
        tableRechoiceVolumeItem = new JMenuItem( "重新選擇集數" );  // 重新選擇集數
        tableRechoiceVolumeItem.addActionListener( this );
        tableDeleteMissionItem = new JMenuItem( "刪除此任務" );  // 刪除任務
        tableDeleteMissionItem.addActionListener( this );
        tableDeleteAllUnselectedMissionItem = new JMenuItem( "刪除所有未勾選任務" );  // 刪除所有未勾選的任務
        tableDeleteAllUnselectedMissionItem.addActionListener( this );
        tableDeleteAllDoneMissionItem = new JMenuItem( "刪除所有已完成任務" );  // 刪除所有已經完成的任務
        tableDeleteAllDoneMissionItem.addActionListener( this );
        tableMoveToRoofItem = new JMenuItem( "將此列任務置頂" );  // 刪除所有已經完成的任務
        tableMoveToRoofItem.addActionListener( this );
        tableMoveToFloorItem = new JMenuItem( "將此列任務置底" );  // 刪除所有已經完成的任務
        tableMoveToFloorItem.addActionListener( this );

        downloadTablePopup = new JPopupMenu();
        downloadTablePopup.add( tableAddBookmarkFromDownloadItem );
        downloadTablePopup.add( tableOpenDownloadDirectoryItem );
        downloadTablePopup.add( tableOpenDownloadFile );
        downloadTablePopup.add( tableOpenDownloadURL );
        downloadTablePopup.add( tableSearchDownloadComic );
        downloadTablePopup.add( tableRechoiceVolumeItem );
        downloadTablePopup.add( tableDeleteMissionItem );
        downloadTablePopup.add( tableDeleteAllUnselectedMissionItem );
        downloadTablePopup.add( tableDeleteAllDoneMissionItem );
        downloadTablePopup.add( tableMoveToRoofItem );
        downloadTablePopup.add( tableMoveToFloorItem );

        downTable.add( downloadTablePopup ); // 必須指定父元件，否則會拋出NullPointerException
    }

    private void setBookmarkTableJPopupMenu() {
        tableSearchBookmarkComic = new JMenuItem( "搜尋這本漫畫" ); // 以瀏覽器開啟搜尋此本漫畫的搜尋頁面
        tableSearchBookmarkComic.addActionListener( this );
        tableOpenBookmarkURL = new JMenuItem( "開啟網頁" ); // 開啟書籤漫畫網址
        tableOpenBookmarkURL.addActionListener( this );
        tableOpenBookmarkDirectoryItem = new JMenuItem( "開啟資料夾" ); // 開啟下載資料夾
        tableOpenBookmarkDirectoryItem.addActionListener( this );
        tableOpenBookmarkFile = new JMenuItem( "開啟檔案" ); // 開啟下載資料夾
        tableOpenBookmarkFile.addActionListener( this );
        tableAddMissionFromBookmarkItem = new JMenuItem( "加入到下載任務" ); // 加入到下載任務
        tableAddMissionFromBookmarkItem.addActionListener( this );
        tableDeleteBookmarkItem = new JMenuItem( "刪除此書籤" );  // 刪除書籤
        tableDeleteBookmarkItem.addActionListener( this );

        bookmarkTablePopup = new JPopupMenu();
        bookmarkTablePopup.add( tableAddMissionFromBookmarkItem );
        bookmarkTablePopup.add( tableOpenBookmarkDirectoryItem );
        bookmarkTablePopup.add( tableOpenBookmarkFile );
        bookmarkTablePopup.add( tableOpenBookmarkURL );
        bookmarkTablePopup.add( tableSearchBookmarkComic );
        bookmarkTablePopup.add( tableDeleteBookmarkItem );

        bookmarkTable.add( bookmarkTablePopup ); // 必須指定父元件，否則會拋出NullPointerException
    }

    private void setRecordTableJPopupMenu() {
        tableSearchRecordComic = new JMenuItem( "搜尋這本漫畫" ); // 以瀏覽器開啟搜尋此本漫畫的搜尋頁面
        tableSearchRecordComic.addActionListener( this );
        tableOpenRecordURL = new JMenuItem( "開啟網頁" ); // 開啟記錄漫畫網址
        tableOpenRecordURL.addActionListener( this );
        tableOpenRecordFile = new JMenuItem( "開啟檔案" ); // 開啟下載檔案
        tableOpenRecordFile.addActionListener( this );
        tableOpenRecordDirectoryItem = new JMenuItem( "開啟資料夾" ); // 開啟下載資料夾
        tableOpenRecordDirectoryItem.addActionListener( this );
        tableAddMissionFromRecordItem = new JMenuItem( "加入到下載任務" ); // 加入到下載任務
        tableAddMissionFromRecordItem.addActionListener( this );
        tableAddBookmarkFromRecordItem = new JMenuItem( "加入到書籤" ); // 開啟下載資料夾
        tableAddBookmarkFromRecordItem.addActionListener( this );
        tableDeleteRecordItem = new JMenuItem( "刪除此記錄" );  // 刪除記錄
        tableDeleteRecordItem.addActionListener( this );

        recordTablePopup = new JPopupMenu();
        recordTablePopup.add( tableAddMissionFromRecordItem );
        recordTablePopup.add( tableAddBookmarkFromRecordItem );
        recordTablePopup.add( tableOpenRecordDirectoryItem );
        recordTablePopup.add( tableOpenRecordFile );
        recordTablePopup.add( tableOpenRecordURL );
        recordTablePopup.add( tableSearchRecordComic );
        recordTablePopup.add( tableDeleteRecordItem );

        recordTable.add( recordTablePopup ); // 必須指定父元件，否則會拋出NullPointerException
    }

    private JTable getDownloadTable() {
        downTableModel = Common.inputDownTableFile();//new DataTableModel( getDownloadColumns(), 0 );
        JTable table = new JTable( downTableModel ) {

            protected String[] columnToolTips = { "此欄位滑鼠左鍵點兩下可刪除該列任務",
                "此欄位若沒有勾選就不會進行下載",
                "此欄位滑鼠左鍵點兩下可開啟該列任務的下載資料夾",
                "此欄位滑鼠左鍵點兩下可重新選取該列任務的下載集數",
                "此欄位滑鼠左鍵點兩下可重新選取該列任務的下載集數",
                "此欄位可顯示目前的下載進度，滑鼠左鍵點兩下以預設瀏覽程式開啟",
                null
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

        table.setPreferredScrollableViewportSize( new Dimension( 400, 170 ) );
        table.setFillsViewportHeight( true );
        //table.setAutoCreateRowSorter( true ); // allow resort
        table.getSelectionModel().addListSelectionListener( new RowListener() );
        table.addMouseListener( this );


        // 取得這個table的欄位模型
        TableColumnModel cModel = table.getColumnModel();

        // 配置每個欄位的寬度比例（可隨視窗大小而變化）
        cModel.getColumn( DownTableEnum.ORDER ).setPreferredWidth( (int) (this.getWidth() * 0.14) );
        cModel.getColumn( DownTableEnum.YES_OR_NO ).setPreferredWidth( (int) (this.getWidth() * 0.14) );
        cModel.getColumn( DownTableEnum.TITLE ).setPreferredWidth( (int) (this.getWidth() * 0.6) );
        cModel.getColumn( DownTableEnum.VOLUMES ).setPreferredWidth( (int) (this.getWidth() * 0.14) );
        cModel.getColumn( DownTableEnum.CHECKS ).setPreferredWidth( (int) (this.getWidth() * 0.14) );
        cModel.getColumn( DownTableEnum.STATE ).setPreferredWidth( (int) (this.getWidth() * 0.3) );
        cModel.getColumn( DownTableEnum.URL ).setPreferredWidth( (int) (this.getWidth() * 0.02) );

        return table;
    }

    private JTable getBookmarkTable() {
        bookmarkTableModel = Common.inputBookmarkTableFile();//new DataTableModel( getDownloadColumns(), 0 );
        JTable table = new JTable( bookmarkTableModel ) {

            protected String[] columnToolTips = { "此欄位滑鼠左鍵點兩下可刪除該列書籤",
                "此欄位滑鼠左鍵點兩下可開啟該列書籤的下載資料夾",
                "此欄位滑鼠左鍵點兩下可將該列書籤加入到下載任務清單中",
                "此欄位顯示該列漫畫加入書籤的系統時間，滑鼠左鍵點兩下以預設瀏覽程式開啟",
                "此欄位滑鼠左鍵點兩下可自由編輯該列任務的注解"
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

        table.setPreferredScrollableViewportSize( new Dimension( 400, 170 ) );
        table.setFillsViewportHeight( true );
        table.setAutoCreateRowSorter( true ); // allow resort
        table.getSelectionModel().addListSelectionListener( new RowListener() );
        table.addMouseListener( this );


        // 取得這個table的欄位模型
        TableColumnModel cModel = table.getColumnModel();

        // 配置每個欄位的寬度比例（可隨視窗大小而變化）
        cModel.getColumn( BookmarkTableEnum.ORDER ).setPreferredWidth( (int) (this.getWidth() * 0.07) );
        cModel.getColumn( BookmarkTableEnum.TITLE ).setPreferredWidth( (int) (this.getWidth() * 0.25) );
        cModel.getColumn( BookmarkTableEnum.DATE ).setPreferredWidth( (int) (this.getWidth() * 0.25) );
        cModel.getColumn( BookmarkTableEnum.URL ).setPreferredWidth( (int) (this.getWidth() * 0.38) );
        cModel.getColumn( BookmarkTableEnum.COMMENT ).setPreferredWidth( (int) (this.getWidth() * 0.27) );

        return table;
    }

    private JTable getRecordTable() {
        recordTableModel = Common.inputRecordTableFile();//new DataTableModel( getDownloadColumns(), 0 );
        JTable table = new JTable( recordTableModel ) {

            protected String[] columnToolTips = { "此欄位滑鼠左鍵點兩下可刪除該列記錄",
                "此欄位滑鼠左鍵點兩下可開啟該列記錄的下載資料夾",
                "此欄位滑鼠左鍵點兩下可將該列漫畫加入到下載任務清單中",
                "此欄位顯示該列漫畫在當初加入任務的系統時間，滑鼠左鍵點兩下以預設瀏覽程式開啟"
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

        table.setPreferredScrollableViewportSize( new Dimension( 400, 170 ) );
        table.setFillsViewportHeight( true );
        table.setAutoCreateRowSorter( true ); // allow resort
        table.getSelectionModel().addListSelectionListener( new RowListener() );
        table.addMouseListener( this );

        // 取得這個table的欄位模型
        TableColumnModel cModel = table.getColumnModel();

        // 配置每個欄位的寬度比例（可隨視窗大小而變化）
        cModel.getColumn( RecordTableEnum.ORDER ).setPreferredWidth( (int) (this.getWidth() * 0.07) );
        cModel.getColumn( RecordTableEnum.TITLE ).setPreferredWidth( (int) (this.getWidth() * 0.32) );
        cModel.getColumn( RecordTableEnum.DATE ).setPreferredWidth( (int) (this.getWidth() * 0.25) );
        cModel.getColumn( RecordTableEnum.URL ).setPreferredWidth( (int) (this.getWidth() * 0.43) );

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
    public void windowGainedFocus( WindowEvent e ) {
        SystemClipBoard clip = new SystemClipBoard();
        String clipString = clip.getClipString();

        if ( !clipString.equals( Common.prevClipString ) ) {
            //Common.debugPrint( "取得系統剪貼簿內容: " + clipString + "  " );

            if ( Common.isLegalURL( clipString ) ) {
                urlField.setText( clipString ); // 當取得焦點時自動貼網址到輸入框中
                Common.prevClipString = clipString;

                if ( SetUp.getAutoAddMission() ) {
                    String[] tempArgs = { clipString };
                    parseURL( tempArgs, false, false, 0 );
                }
            }
        } else {
            urlField.setText( "" ); // 之前貼過的就不要再顯示了
        }
    }

    public void windowLostFocus( WindowEvent e ) {
    }

    private void minimizeEvent() {
        this.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );

        this.addWindowListener( new WindowAdapter() {

            public void windowClosing( WindowEvent e ) {
                if ( Common.isWindows() ) {
                    setState( Frame.ICONIFIED ); // 縮小後可觸發windowIconified()而縮入系統列
                } else {
                    // 因為在ubuntu縮小收進系統框後無法再顯示視窗
                    // 只能用取消顯示再產生system tray
                    setVisible( false );
                    minimizeToTray();
                }
            }

            public void windowIconified( WindowEvent e ) {
                if ( SystemTray.isSupported() && Common.isWindows() ) {
                    setVisible( false );
                    minimizeToTray();
                } else {
                    // ubuntu底下縮小收進系統框後無法取出，
                    // 因此在非windows環境，按縮小就只給縮小功能
                    setState( Frame.ICONIFIED );
                }
            }
        } );
    }

    public void minimizeToTray() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add( this.trayIcon );
        } catch ( AWTException ex ) {
            System.err.println( "無法加入系統工具列圖示" );
            ex.printStackTrace();
        }
    }

    // 設置縮小到系統框的圖示
    private void initTrayIcon() {
        //Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("resource\\system_tray_ico.gif"));
        Image image = new CommonGUI().getImage( "main_icon.png" );

        trayExitItem = new JMenuItem( "離開此程式" );
        trayExitItem.addActionListener( this );
        trayStartItem = new JMenuItem( "開始任務" );
        trayStartItem.addActionListener( this );
        trayStopItem = new JMenuItem( "停止任務" );
        trayStopItem.addActionListener( this );
        trayShowItem = new JMenuItem( "開啟主介面" );
        trayShowItem.addActionListener( this );

        trayPopup = new JPopupMenu();
        trayPopup.add( trayExitItem );
        trayPopup.add( trayStopItem );
        trayPopup.add( trayStartItem );
        trayPopup.add( trayShowItem );

        //trayIcon = new TrayIcon( image, "JComicDownloader", trayPopup );

        if ( image != null ) {
            trayIcon = new TrayIcon( image, "JComicDownloader", null );
            trayIcon.addMouseListener( this );
        } else {
            trayIcon = null;
        }

    }

    // --------- urlField Event -------------
    public void insertUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) {
            ex.printStackTrace();
        }

        //messageArea.append( webSite );
    }

    public void removeUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) {
            ex.printStackTrace();
        }
    }

    public void changedUpdate( DocumentEvent event ) {
        Document doc = event.getDocument();
        try {
            args = doc.getText( 0, doc.getLength() ).split( "\\s+" );
        } catch ( BadLocationException ex ) {
            ex.printStackTrace();
        }
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

        if ( event.getSource() == urlField && event.getButton() == MouseEvent.BUTTON3 ) {
            showUrlFieldPopup( event );
            System.out.print( "." );
        }

        if ( event.getSource() == downTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) {
            showDownloadPopup( event );
        } else if ( event.getSource() == bookmarkTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) {
            showBookmarkPopup( event );
        } else if ( event.getSource() == recordTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) {
            showRecordPopup( event );
        }
    }

    public void mouseExited( MouseEvent event ) {
    }

    public void mouseEntered( MouseEvent event ) {
    }

    public void mouseReleased( MouseEvent event ) {
        if ( event.getSource() == downTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) {
            showDownloadPopup( event );
        } else if ( event.getSource() == bookmarkTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) {
            showBookmarkPopup( event );
        } else if ( event.getSource() == recordTable && tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) {
            showRecordPopup( event );
        }

        if ( event.getSource() == urlField && event.getButton() == MouseEvent.BUTTON3 ) {
            showUrlFieldPopup( event );
        }

        if ( event.getSource() == trayIcon && event.getButton() == MouseEvent.BUTTON3 ) {
            if ( event.isPopupTrigger() && !trayPopup.isVisible() ) {
                trayPopup.setLocation( event.getX() + 10, event.getY() );
                trayPopup.setInvoker( trayPopup );
                trayPopup.setVisible( true );
            } else {
                trayPopup.setVisible( false );
            }
        }

    }

    // 在任務列點擊左鍵兩下會跳出重新選擇集數的視窗，
    // 點擊右鍵一下會跳出是否刪除此任務的訊息視窗。
    public void mouseClicked( MouseEvent event ) {

        if ( (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0
                && event.getClickCount() == 2
                && tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) {
            int row = event.getY() / downTable.getRowHeight();
            int col = downTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();

            if ( event.getSource() == downTable
                    && row < Common.missionCount && row >= 0 ) {

                // 任何時候都能開啟下載資料夾
                if ( col == DownTableEnum.TITLE ) {
                    openDownloadDirectory( row );
                } else if ( col == DownTableEnum.STATE ) {
                    openDownloadFile( row );
                }

                // 正在下載的時候不能重選集數，也不能刪除任務
                if ( col == DownTableEnum.VOLUMES
                        || col == DownTableEnum.CHECKS ) {
                    if ( !Flag.downloadingFlag ) {
                        rechoiceVolume( row );
                    } else {
                        JOptionPane.showMessageDialog( this, "目前正下載中，無法重新選擇集數",
                                "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    }
                } else if ( col == DownTableEnum.ORDER ) {
                    if ( !Flag.downloadingFlag ) {
                        deleteMission( row );
                    } else {
                        JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                                "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    }
                }
            }
        }

        if ( (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0
                && event.getClickCount() == 2
                && tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) {
            int row = event.getY() / bookmarkTable.getRowHeight();
            int col = bookmarkTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();

            if ( event.getSource() == bookmarkTable
                    && row < Common.bookmarkCount && row >= 0 ) {

                // 任何時候都能開啟下載資料夾
                if ( col == BookmarkTableEnum.TITLE ) {
                    openDownloadDirectory( row );
                } else if ( col == BookmarkTableEnum.DATE ) {
                    openDownloadFile( row );
                }

                // 正在下載的時候不能重選集數，也不能刪除任務
                if ( col == BookmarkTableEnum.URL ) {
                    addMission( row );
                } else if ( col == BookmarkTableEnum.ORDER ) {
                    deleteBookmark( row );
                }
            }
        }

        if ( (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0
                && event.getClickCount() == 2
                && tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) {
            int row = event.getY() / recordTable.getRowHeight();
            int col = recordTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();

            if ( event.getSource() == recordTable
                    && row < Common.recordCount && row >= 0 ) {

                // 任何時候都能開啟下載資料夾
                if ( col == RecordTableEnum.TITLE ) {
                    openDownloadDirectory( row );
                } else if ( col == RecordTableEnum.DATE ) {
                    openDownloadFile( row );
                }

                // 正在下載的時候不能重選集數，也不能刪除任務
                if ( col == RecordTableEnum.URL ) {
                    addMission( row );
                } else if ( col == RecordTableEnum.ORDER ) {
                    deleteRecord( row );
                }
            }
        }

    }

    private void addMission( int row ) { // 從書籤或紀錄加入任務
        String[] tempArgs = new String[1];

        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) {
            row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            tempArgs[0] = bookmarkTableModel.getValueAt( row, BookmarkTableEnum.URL ).toString();
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) {
            row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            tempArgs[0] = recordTableModel.getValueAt( row, RecordTableEnum.URL ).toString();
        } else {
            Common.errorReport( "不可能從書籤和記錄以外的地方加入任務！" );
        }

        urlField.setText( tempArgs[0] );
        parseURL( tempArgs, false, false, 0 );
    }

    private void rechoiceVolume( int row ) { // 重新選擇集數
        row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        System.out.println( downTableModel.getRealValueAt( row, DownTableEnum.CHECKS ).toString() );
        ComicDownGUI.nowSelectedCheckStrings = Common.getSeparateStrings(
                String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.CHECKS ) ) );
        Common.debugPrintln( "重新解析位址（為了重選集數）：" + downTableUrlStrings[row] );
        parseURL( new String[] { downTableUrlStrings[row] }, false, true, row );
    }

    // 從beginIndex開始，後面的全部往前挪一格
    private void stringsMoveOneForward( String[] strings, int beginIndex ) {
        for ( int i = beginIndex ; strings[i + 1] != null ; i++ ) {
            strings[i] = strings[i + 1];
        }
    }

    private void moveMissionToRoof( int row ) { // 將第row列任務置頂
        row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        String urlString = downTableUrlStrings[row]; // 先將此列任務另存

        downTable.setValueAt( 1, row, DownTableEnum.ORDER );
        for ( int i = row - 1 ; i >= 0 ; i-- ) {
            downTableUrlStrings[i + 1] = downTableUrlStrings[i]; // row列之前的往後遞移一位
            i = downTable.convertRowIndexToModel( i ); // 顯示的列 -> 實際的列
            //int orderValue = Integer.parseInt( downTable.getValueAt( i, DownTableEnum.ORDER ).toString() );
            downTable.setValueAt( i + 2, i, DownTableEnum.ORDER );
        }
        downTableUrlStrings[0] = urlString; // 再將之前另存任務存入第一個位置

        int missionAmount = downTableModel.getRowCount();
        downTableModel.moveRow( row, row, 0 );

    }

    private void moveMissionToFloor( int row ) { // 將第row列任務置底
        row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        int missionAmount = downTableModel.getRowCount();
        String urlString = downTableUrlStrings[row]; // 先將此列任務另存

        downTable.setValueAt( missionAmount, row, DownTableEnum.ORDER );
        for ( int i = row + 1 ; i < missionAmount ; i++ ) {
            i = downTable.convertRowIndexToModel( i ); // 顯示的列 -> 實際的列
            downTableUrlStrings[i - 1] = downTableUrlStrings[i]; // row列之後的往前遞移一位
            downTable.setValueAt( i, i, DownTableEnum.ORDER );
        }
        downTableUrlStrings[missionAmount - 1] = urlString; // 再將之前另存任務存入第一個位置

        downTableModel.moveRow( row, row, missionAmount - 1 );
    }

    private void deleteMission( int row ) { // 刪除第row列任務
        row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        String title = String.valueOf( downTableModel.getRealValueAt(
                row, DownTableEnum.TITLE ) );

        String message = "是否要在任務清單中刪除" + title + " ?";

        int choice = JOptionPane.showConfirmDialog( this, message, "提醒訊息", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            Common.missionCount--;
            downTableModel.removeRow( row );
            stringsMoveOneForward( downTableUrlStrings, row ); // 儲存URL的字串陣列從row開始都往前挪一格
        }
    }

    private void deleteAllUnselectedMission() { // 刪除所有未勾選的任務
        String message = "是否要在任務清單中刪除所有未勾選的任務 ?";

        int choice = JOptionPane.showConfirmDialog( this, message, "詢問視窗", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            int nowRow = 0;
            while ( nowRow < downTableModel.getRowCount() ) {
                if ( downTableModel.getValueAt( nowRow, DownTableEnum.YES_OR_NO ).toString().equals( "false" ) ) {
                    downTableModel.removeRow( nowRow );
                    stringsMoveOneForward( downTableUrlStrings, nowRow ); // 儲存URL的字串陣列從row開始都往前挪一格
                    Common.missionCount--;
                } else {
                    nowRow++; // 沒有符合情況才往後搜尋
                }
            }
            repaint();
        }
    }

    private void deleteAllDoneMission() { // 刪除所有已經完成的任務
        String message = "是否要在任務清單中刪除所有已經完成的任務 ?";

        int choice = JOptionPane.showConfirmDialog( this, message, "提醒訊息", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            int nowRow = 0;
            while ( nowRow < downTableModel.getRowCount() ) {
                if ( downTableModel.getValueAt( nowRow, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                    downTableModel.removeRow( nowRow );
                    stringsMoveOneForward( downTableUrlStrings, nowRow ); // 儲存URL的字串陣列從row開始都往前挪一格
                    Common.missionCount--;
                } else {
                    nowRow++; // 沒有符合情況才往後搜尋
                }
            }
            repaint();
        }
    }

    private void deleteBookmark( int row ) { // 刪除第row列書籤
        row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        String title = String.valueOf( bookmarkTableModel.getValueAt(
                row, BookmarkTableEnum.TITLE ) );
        String message = "是否要在書籤中刪除" + title + " ?";
        int choice = JOptionPane.showConfirmDialog( this, message, "提醒訊息", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            Common.bookmarkCount--;
            bookmarkTableModel.removeRow( row );
        }
    }

    private void deleteRecord( int row ) { // 刪除第row列記錄
        row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        String title = String.valueOf( recordTableModel.getValueAt(
                row, RecordTableEnum.TITLE ) );
        String message = "是否要在記錄中刪除" + title + " ?";
        int choice = JOptionPane.showConfirmDialog( this, message, "提醒訊息", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) { // agree to remove the title in the download list
            Common.recordCount--;
            recordTableModel.removeRow( row );
        }
    }

    private void addBookmark( int row ) {  // 將第row列任務加入到書籤中
        row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
        String title = "";
        String url = "";
        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) { // 從任務清單加入
            title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            url = downTableUrlStrings[row];
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) { // 從紀錄清單加入
            title = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.TITLE ) );
            url = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.URL ) );
        }

        Common.debugPrintln( "加入到書籤：" + title + " " + url );
        bookmarkTableModel.addRow( CommonGUI.getBookmarkDataRow(
                ++Common.bookmarkCount,
                title,
                url ) );

        Common.outputBookmarkTableFile( bookmarkTableModel ); // 每加入書籤便寫入書籤記錄檔一次。
    }

    private void searchDownloadComic( int row ) {  // 以瀏覽器開啟第row列任務的下載檔案網址
        String title = "";
        String url = "";

        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) { // 從任務清單開啟
            row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            url = downTableUrlStrings[row];
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) { // 從書籤清單開啟
            row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.TITLE ) );
            url = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.URL ) );
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) { // 從記錄清單開啟
            row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.TITLE ) );
            url = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.URL ) );
        }

        Common.debugPrintln( "瀏覽器開啟自訂搜尋引擎，並以『" + title + "』作為關鍵字進行搜尋" );

        new RunBrowser().runBroswer( getKeywordSearchURL( title ) );
    }

    private void openDownloadURL( int row ) {  // 以瀏覽器開啟第row列任務的下載檔案網址
        String title = "";
        String url = "";

        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) { // 從任務清單開啟
            row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            url = downTableUrlStrings[row];
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) { // 從書籤清單開啟
            row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.TITLE ) );
            url = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.URL ) );
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) { // 從記錄清單開啟
            row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.TITLE ) );
            url = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.URL ) );
        }

        Common.debugPrintln( "以預設瀏覽器開啟" + title + "的原始網頁" );

        new RunBrowser().runBroswer( url );
    }

    private void openDownloadFile( int row ) {  // 開啟第row列任務的下載檔案
        String title = "";
        String url = "";

        if ( SetUp.getOpenPicFileProgram().matches( "" ) ) {
            String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
            String colorString = "blue";
            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                colorString = "yellow";
            }

            JOptionPane.showMessageDialog( this,
                    "<html>尚未設定開啟程式，請前往<font color=" + colorString + ">選項 -> 瀏覽</font>做設定</html>",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) { // 從任務清單開啟
            row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            url = downTableModel.getRealValueAt( row, DownTableEnum.URL ).toString();
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) { // 從書籤清單開啟
            row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.TITLE ) );
            url = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.URL ) );
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) { // 從記錄清單開啟
            row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.TITLE ) );
            url = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.URL ) );
        }

        Common.debugPrintln( "以外部程式開啟" + title + "的下載資料夾或壓縮檔" );

        if ( url.matches( "(?s).*e-hentai(?s).*" ) || url.matches( "(?s).*exhentai(?s).*" ) ) {
            String cmd = SetUp.getOpenZipFileProgram();
            String path = "";

            if ( new File( SetUp.getOriginalDownloadDirectory() + title + ".zip" ).exists() ) {
                path = SetUp.getOriginalDownloadDirectory() + title + ".zip";
                Common.debugPrintln( "開啟命令：" + cmd + " " + path );

                if ( Common.isWindows() ) {
                    runUnansiCmd( cmd, path );
                } else {
                    try {
                        
                        String[] cmds = new String[] { cmd, path };
                        Runtime.getRuntime().exec( cmds, null, new File( Common.getNowAbsolutePath() ) );
                    } catch ( IOException ex ) {
                        Logger.getLogger( ComicDownGUI.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                }
            } else {
                path = SetUp.getOriginalDownloadDirectory() + title + Common.getSlash();

                if ( !new File( path ).exists() ) {
                    String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
                    String colorString = "blue";
                    if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                        colorString = "yellow";
                    }

                    JOptionPane.showMessageDialog( this, "<html><font color=" + colorString + ">"
                            + path + "</font>" + "不存在，無法開啟</html>",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    return;
                }

                if ( Common.isWindows() ) {
                    Common.debugPrintln( "開啟命令：" + cmd + " " + path );
                    runUnansiCmd( cmd, path );
                } else {
                    String[] picList = new File( path ).list();
                    String firstPicFileInFirstVolume = picList[0];
                    path += firstPicFileInFirstVolume;

                    Common.debugPrintln( "開啟命令：" + cmd + " " + path );

                    try {
                        String[] cmds = new String[] { cmd, path };
                        Runtime.getRuntime().exec( cmds, null, new File( Common.getNowAbsolutePath() ) );
                    } catch ( IOException ex ) {
                        Logger.getLogger( ComicDownGUI.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                }
            }

        } else {
            if ( Common.isWindows() ) {
                runUnansiCmd( SetUp.getOpenPicFileProgram(),
                        SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() );
            } else {
                runCmd( SetUp.getOpenPicFileProgram(),
                        SetUp.getOriginalDownloadDirectory() + title );
            }
        }

    }

    // 非windows系統時的操作
    private void runCmd( String program, String file ) {
        String path = file;
        String cmd = program;

        // 檔案不存在就只顯示訊息而不繼續操作
        if ( !new File( file ).exists() ) {
            String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
            String colorString = "blue";
            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                colorString = "yellow";
            }

            JOptionPane.showMessageDialog( this, "<html><font color=" + colorString + ">"
                    + file + "</font>" + "不存在，無法開啟</html>",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        String[] fileList = new File( file ).list();
        System.out.println( file );

        String firstZipFileName = "";
        boolean existZipFile = false;
        for ( int i = 0 ; i < fileList.length ; i++ ) {
            System.out.println( "FILE: " + fileList[i] );
            if ( fileList[i].matches( "(?s).*\\.zip" ) ) {
                firstZipFileName = fileList[i];
                existZipFile = true;
                break;
            }
        }


        if ( existZipFile ) {
            // 資料夾內存在壓縮檔
            path = file + Common.getSlash() + firstZipFileName;
        } else {
            String[] picList = new File( file + Common.getSlash() + fileList[0] ).list();
            String firstPicFileInFirstVolume = picList[0];
            path = file + Common.getSlash() + fileList[0]
                    + Common.getSlash() + firstPicFileInFirstVolume;
        }

        Common.debugPrintln( "開啟命令：" + cmd + path );

        try {
            String[] cmds = new String[] { cmd, path };
            Runtime.getRuntime().exec( cmds, null, new File( Common.getNowAbsolutePath() ) );
            //Runtime.getRuntime().exec(cmd + path);

        } catch ( IOException ex ) {
            Logger.getLogger( ComicDownGUI.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    // 解決非ANSI字會變成？而無法使用程式開啟的問題
    // 出處：http://stackoverflow.com/questions/1876507/java-runtime-exec-on-windows-fails-with-unicode-in-arguments
    private void runUnansiCmd( String program, String file ) {
        if ( !new File( file ).exists() ) {
            String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
            String colorString = "blue";
            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                colorString = "yellow";
            }

            JOptionPane.showMessageDialog( this, "<html><font color=" + colorString + ">"
                    + file + "</font>" + "不存在，無法開啟</html>",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        String[] cmd = new String[] { program, file };
        Map<String, String> newEnv = new HashMap<String, String>();
        newEnv.putAll( System.getenv() );
        String[] i18n = new String[cmd.length + 2];
        i18n[0] = "cmd";
        i18n[1] = "/C";
        i18n[2] = cmd[0];
        for ( int counter = 1 ; counter < cmd.length ; counter++ ) {
            String envName = "JENV_" + counter;
            i18n[counter + 2] = "%" + envName + "%";
            newEnv.put( envName, cmd[counter] );
        }
        cmd = i18n;

        ProcessBuilder pb = new ProcessBuilder( cmd );
        Map<String, String> env = pb.environment();
        env.putAll( newEnv );
        try {
            final Process p = pb.start();
        } catch ( IOException ex ) {
            Logger.getLogger( ComicDownGUI.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private void openDownloadDirectory( int row ) {  // 開啟第row列任務的下載資料夾
        String title = "";
        String url = "";

        if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) { // 從任務清單開啟
            row = downTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( downTableModel.getRealValueAt( row, DownTableEnum.TITLE ) );
            url = downTableModel.getRealValueAt( row, DownTableEnum.URL ).toString();
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) { // 從書籤清單開啟
            row = bookmarkTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.TITLE ) );
            url = String.valueOf( bookmarkTableModel.getValueAt( row, BookmarkTableEnum.URL ) );
        } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) { // 從記錄清單開啟
            row = recordTable.convertRowIndexToModel( row ); // 顯示的列 -> 實際的列
            title = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.TITLE ) );
            url = String.valueOf( recordTableModel.getValueAt( row, RecordTableEnum.URL ) );
        }

        Common.debugPrintln( "開啟" + title + "的下載資料夾" );

        if ( url.matches( "(?s).*e-hentai(?s).*" ) || url.matches( "(?s).*exhentai(?s).*" ) ) {
            if ( Common.isWindows() ) {
                if ( new File( SetUp.getOriginalDownloadDirectory() + title + ".zip" ).exists() ) {
                    runUnansiCmd( "explorer /select, ", SetUp.getOriginalDownloadDirectory() + title + ".zip" );
                } else if ( new File( SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() ).exists() ) {
                    runUnansiCmd( "explorer /select, ", SetUp.getOriginalDownloadDirectory() + title );
                } else {
                    runUnansiCmd( "explorer ", SetUp.getOriginalDownloadDirectory() );
                }
            } else {
                runCmd( "nautilus ", SetUp.getOriginalDownloadDirectory() );
            }
        } else if ( Common.isWindows() ) {
            runUnansiCmd( "explorer ", SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() );
        } else {
            runCmd( "nautilus ", SetUp.getOriginalDownloadDirectory() + title + Common.getSlash() );
        }
    }

    private void showDownloadPopup( MouseEvent event ) {
        downloadTablePopupRow = event.getY() / downTable.getRowHeight();
        if ( downloadTablePopupRow < Common.missionCount && downloadTablePopupRow >= 0 ) {
            if ( event.isPopupTrigger() ) {
                downloadTablePopup.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
    }

    private void showBookmarkPopup( MouseEvent event ) {
        bookmarkTablePopupRow = event.getY() / bookmarkTable.getRowHeight();
        if ( bookmarkTablePopupRow < Common.bookmarkCount && bookmarkTablePopupRow >= 0 ) {
            if ( event.isPopupTrigger() ) {
                bookmarkTablePopup.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
    }

    private void showRecordPopup( MouseEvent event ) {
        recordTablePopupRow = event.getY() / recordTable.getRowHeight();
        if ( recordTablePopupRow < Common.recordCount && recordTablePopupRow >= 0 ) {
            if ( event.isPopupTrigger() ) {
                recordTablePopup.show( event.getComponent(), event.getX(), event.getY() );
            }
        }
    }

    private void showUrlFieldPopup( MouseEvent event ) {
        //if ( event.isPopupTrigger() ) {
        System.out.print( "|" );
        System.out.println( event.getX() + "," + event.getY() );
        urlFieldPopup.show( event.getComponent(), event.getX() + 15, event.getY() );
        //}
    }

    // --------------  button event   -----------------
    // 開始進行下載任務（若downloadAfterChoice為true，則先等待選擇集數完畢後，才開始下載。）
    public void startDownloadList( final boolean downloadAfterChoice ) {
        Thread downThread = new Thread( new Runnable() {

            public void run() {
                Common.debugPrintln( "進入下載主函式中" );

                if ( downloadAfterChoice ) {
                    Common.downloadLock = true;
                    Common.debugPrintln( "進入選擇集數，等待中..." );

                    synchronized ( ComicDownGUI.mainFrame ) { // lock main frame
                        while ( Common.downloadLock ) {
                            try {
                                ComicDownGUI.mainFrame.wait();
                            } catch ( InterruptedException ex ) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    Common.debugPrintln( "選擇集數完畢，結束等待" );
                }

                for ( int i = 0 ; i < Common.missionCount && Run.isAlive ; i++ ) {
                    if ( downTableModel.getValueAt( i, DownTableEnum.YES_OR_NO ).toString().equals( "false" )
                            || downTableModel.getValueAt( i, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                        Common.processPrintln( "跳過 " + downTableModel.getValueAt( i, DownTableEnum.TITLE ).toString() );
                        continue;
                    }

                    // ex http://xxx  http://xxx  ...
                    String[] urlStrings = Common.getSeparateStrings(
                            downTableModel.getValueAt( i, DownTableEnum.URL ).toString() );

                    // ex. true  false  false ...
                    String[] checkStrings = Common.getSeparateStrings(
                            String.valueOf( downTableModel.getRealValueAt( i, DownTableEnum.CHECKS ) ) );

                    // volume title
                    String[] volumeStrings = Common.getSeparateStrings(
                            String.valueOf( downTableModel.getRealValueAt( i, DownTableEnum.VOLUMES ) ) );

                    int downloadCount = 0;
                    for ( int j = 0 ; j < urlStrings.length && Run.isAlive ; j++ ) {
                        if ( checkStrings[j].equals( "true" ) ) {
                            Flag.allowDownloadFlag = true;

                            String nowState = "下載進度：" + downloadCount + " / " + Common.getTrueCountFromStrings( checkStrings );
                            downTableModel.setValueAt( nowState, i, DownTableEnum.STATE );
                            // 啟動下載
                            Run mainRun = new Run( urlStrings[j], volumeStrings[j], downTableModel.getValueAt( i, DownTableEnum.TITLE ).toString(),
                                    RunModeEnum.DOWNLOAD_MODE );
                            mainRun.run();
                            downloadCount++;
                        }
                    }

                    if ( Run.isAlive ) {
                        if ( Flag.downloadErrorFlag ) {
                            downTableModel.setValueAt( "下載錯誤", i, DownTableEnum.STATE );
                            Flag.downloadErrorFlag = false; // 歸初始值
                        } else {
                            downTableModel.setValueAt( "下載完畢", i, DownTableEnum.STATE );
                        }
                        String title = String.valueOf( downTableModel.getRealValueAt( i, DownTableEnum.TITLE ) );

                        if ( SetUp.getShowDoneMessageAtSystemTray() ) {
                            trayIcon.displayMessage( "JComicDownloader Message", title + "下載完畢! ", TrayIcon.MessageType.INFO );
                            if ( SetUp.getPlaySingleDoneAudio() ) {
                                Common.playSingleDoneAudio(); // 播放單一任務完成音效
                            }
                        }
                    } else {
                        downTableModel.setValueAt( "下載中斷", i, DownTableEnum.STATE );
                        trayIcon.setToolTip( "下載中斷" );
                    }

                    Common.outputDownTableFile( downTableModel ); // 每處理一個任務就寫出下載任務記錄檔一次
                }
                if ( Run.isAlive ) {
                    stateBar.setText( Common.missionCount + "個任務全部下載完畢! " );
                    if ( SetUp.getPlayAllDoneAudio() ) {
                        Common.playAllDoneAudio(); // 播放全部任務完成音效
                    }
                    if ( SetUp.getShowDoneMessageAtSystemTray() ) {
                        trayIcon.displayMessage( "JComicDownloader Message", Common.missionCount + "個任務全部下載完畢! ", TrayIcon.MessageType.INFO );
                    }
                    trayIcon.setToolTip( "JComicDownloader" );
                    Flag.allowDownloadFlag = false;

                    System.gc(); // 下載完就建議JAVA做垃圾回收
                }
            }
        } );

        Common.downloadThread = downThread;
        downThread.start();
    }

    // 開始下載urlTextField目前位址指向的集數
    public void startDownloadURL( final String[] newArgs ) {
        Thread downThread = new Thread( new Runnable() {

            public void run() {
                // download comic from url
                Flag.allowDownloadFlag = true; // allow the action of download
                Common.processPrintln( "開始單集下載" );
                stateBar.setText( "  開始單集下載" );
                Run singleRun = new Run( newArgs, RunModeEnum.DOWNLOAD_MODE );
                singleRun.start();
                try {
                    singleRun.join();
                } catch ( InterruptedException ex ) {
                    ex.printStackTrace();
                }
                Flag.allowDownloadFlag = false;
            }
        } );

        Common.downloadThread = downThread;
        downThread.start();
    }

    // 跳出選擇集數的視窗
    public void runChoiceFrame( boolean modifySelected, int modifyRow, String title, String urlString ) {
        //SwingUtilities.invokeLater( new Runnable(){ public void run() {
        //String urlString = urlField.getText();

        ChoiceFrame choiceFrame;
        if ( modifySelected ) {
            choiceFrame = new ChoiceFrame( "重新選擇欲下載的集數", true, modifyRow, title, urlString );
        } else {
            choiceFrame = new ChoiceFrame( title, urlString );
        }

        String[] volumeStrings = choiceFrame.getVolumeStrings();
        String[] checkStrings = choiceFrame.getCheckStrings();

        args = null;
        urlField.setText( "" );
        repaint();

    }

    private void clearMission() { // 清空下載任務
        int downListCount = downTableModel.getRowCount();
        while ( downTableModel.getRowCount() > 1 ) {
            downTableModel.removeRow( downTableModel.getRowCount() - 1 );
            Common.missionCount--;
        }
        if ( Common.missionCount > 0 ) {
            downTableModel.removeRow( 0 );
        }
        repaint(); // 重繪

        Common.missionCount = 0;
        Common.processPrint( "全部下載任務清空" );
        stateBar.setText( "全部下載任務清空" );
        trayIcon.setToolTip( "JComicDownloader" );
    }

    private void clearBookmark() { // 清空書籤
        int bookmarkListCount = bookmarkTableModel.getRowCount();
        while ( bookmarkTableModel.getRowCount() > 1 ) {
            bookmarkTableModel.removeRow( bookmarkTableModel.getRowCount() - 1 );
            Common.bookmarkCount--;
        }
        if ( Common.bookmarkCount > 0 ) {
            bookmarkTableModel.removeRow( 0 );
        }
        repaint(); // 重繪

        Common.bookmarkCount = 0;
        Common.processPrint( "全部書籤清空" );
        stateBar.setText( "全部書籤清空" );
        trayIcon.setToolTip( "JComicDownloader" );
    }

    private void clearRecord() {
        int recordListCount = recordTableModel.getRowCount();
        while ( recordTableModel.getRowCount() > 1 ) {
            recordTableModel.removeRow( recordTableModel.getRowCount() - 1 );
            Common.recordCount--;
        }
        if ( Common.recordCount > 0 ) {
            recordTableModel.removeRow( 0 );
        }
        repaint(); // 重繪

        Common.recordCount = 0;
        Common.processPrint( "全部記錄清空" );
        stateBar.setText( "全部記錄清空" );
        trayIcon.setToolTip( "JComicDownloader" );
    }

    // 分析位址，分析完後開始下載。
    public void parseURL( final String[] newArgs,
            final boolean allowDownload,
            final boolean modifySelected,
            final int modifyRow ) {  // call by add button and download button
        Thread praseThread = new Thread( new Runnable() {

            public void run() {

                Common.urlIsUnknown = false; // 解決一次不認識之後就都不認識的bug
                if ( newArgs == null || newArgs[0].equals( "" ) ) {
                    if ( !allowDownload ) {
                        stateBar.setText( "  沒有輸入網址 !!" );
                    } else {
                        if ( Common.missionCount > 0 ) {
                            Flag.parseUrlFlag = false; // 分析結束
                            startDownloadList( false ); // download all selected comic
                        } else {
                            stateBar.setText( "  沒有下載任務也沒有輸入網址 !!" );
                        }
                    }
                } else if ( !Common.isLegalURL( newArgs[0] ) ) { // url is illegal
                    stateBar.setText( "  網址錯誤，請輸入正確的網址 !!" );
                } else {
                    stateBar.setText( "  解析網址中" );
                    if ( Common.withGUI() ) {
                        trayIcon.setToolTip( "解析網址中" );
                    }

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

                    } catch ( InterruptedException ex ) {
                        ex.printStackTrace();
                    }

                    title = mainRun.getTitle();
                    Common.debugPrintln( "選擇集數前解析得到的title：" + title );
                    if ( Common.urlIsUnknown ) {
                        stateBar.setText( "  無法解析此網址 !!" );
                    } else if ( Common.isMainPage ) { // args is main page
                        runChoiceFrame( modifySelected, modifyRow, title, tempArgs[0] );

                        if ( allowDownload ) {
                            Flag.parseUrlFlag = false; // 分析結束
                            startDownloadList( true ); // download all selected comic
                        }
                    } else { // args is single page
                        if ( !allowDownload ) {
                            stateBar.setText( "  單集頁面無法加入下載佇列 !!" );
                        } else {
                            stateBar.setText( "  正在下載單一集數" );
                            Flag.parseUrlFlag = false; // 分析結束
                            startDownloadURL( tempArgs ); // download single comic on textfield
                        }
                    }

                }

                Flag.parseUrlFlag = false; // 分析結束
            }
        } );

        praseThread.start();
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event.getSource() == urlField ) {
        }

        if ( event.getSource() == tableSearchDownloadComic ) {
            searchDownloadComic( downloadTablePopupRow );
        } else if ( event.getSource() == tableSearchBookmarkComic ) {
            searchDownloadComic( bookmarkTablePopupRow );
        } else if ( event.getSource() == tableSearchRecordComic ) {
            searchDownloadComic( recordTablePopupRow );
        }

        if ( event.getSource() == tableOpenDownloadFile ) {
            openDownloadFile( downloadTablePopupRow );
        } else if ( event.getSource() == tableOpenBookmarkFile ) {
            openDownloadFile( bookmarkTablePopupRow );
        } else if ( event.getSource() == tableOpenRecordFile ) {
            openDownloadFile( recordTablePopupRow );
        }
        if ( event.getSource() == tableOpenDownloadURL ) {
            openDownloadURL( downloadTablePopupRow );
        } else if ( event.getSource() == tableOpenBookmarkURL ) {
            openDownloadURL( bookmarkTablePopupRow );
        } else if ( event.getSource() == tableOpenRecordURL ) {
            openDownloadURL( recordTablePopupRow );
        } else if ( event.getSource() == tableOpenDownloadDirectoryItem ) {
            openDownloadDirectory( downloadTablePopupRow );
        } else if ( event.getSource() == tableOpenBookmarkDirectoryItem ) {
            openDownloadDirectory( bookmarkTablePopupRow );
        } else if ( event.getSource() == tableOpenRecordDirectoryItem ) {
            openDownloadDirectory( recordTablePopupRow );
        }

        if ( event.getSource() == pasteSystemClipboardItem ) { // 貼上剪貼簿網址
            String clipString = new SystemClipBoard().getClipString();
            urlField.setText( clipString );
        }

        if ( event.getSource() == tableAddBookmarkFromDownloadItem ) {
            addBookmark( downloadTablePopupRow );
        } else if ( event.getSource() == tableAddBookmarkFromRecordItem ) {
            addBookmark( recordTablePopupRow );
        }

        if ( event.getSource() == tableAddMissionFromBookmarkItem ) {
            addMission( bookmarkTablePopupRow );
        } else if ( event.getSource() == tableAddMissionFromRecordItem ) {
            addMission( recordTablePopupRow );
        }

        if ( event.getSource() == tableRechoiceVolumeItem ) {
            if ( !Flag.downloadingFlag ) {
                rechoiceVolume( downloadTablePopupRow );
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法重新選擇集數",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }
        if ( event.getSource() == tableDeleteMissionItem ) {
            if ( !Flag.downloadingFlag ) {
                deleteMission( downloadTablePopupRow );
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }
        if ( event.getSource() == tableDeleteAllUnselectedMissionItem ) {
            if ( !Flag.downloadingFlag ) {
                deleteAllUnselectedMission();
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }
        if ( event.getSource() == tableDeleteAllDoneMissionItem ) {
            if ( !Flag.downloadingFlag ) {
                deleteAllDoneMission();
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法刪除任務",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }
        if ( event.getSource() == tableMoveToRoofItem ) {
            if ( !Flag.downloadingFlag ) {
                moveMissionToRoof( downloadTablePopupRow );
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法移動任務的順序位置",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }
        if ( event.getSource() == tableMoveToFloorItem ) {
            if ( !Flag.downloadingFlag ) {
                moveMissionToFloor( downloadTablePopupRow );
            } else {
                JOptionPane.showMessageDialog( this, "目前正下載中，無法移動任務的順序位置",
                        "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            }
        }

        if ( event.getSource() == tableDeleteBookmarkItem ) {
            deleteBookmark( bookmarkTablePopupRow );
        }

        if ( event.getSource() == tableDeleteRecordItem ) {
            deleteRecord( recordTablePopupRow );
        }

        if ( event.getSource() == trayShowItem ) {
            setVisible( true );
            setState( Frame.NORMAL );
            SystemTray.getSystemTray().remove( trayIcon );
        }

        if ( event.getSource() == button[ButtonEnum.ADD] ) { // button of add
            logFrame.redirectSystemStreams(); // start to log message
            testDownload(); // 測試此網站的下載是否正常

            String urlString = urlField.getText();
            parseURL( args, false, false, 0 );
            args = null;
        }
        if ( event.getSource() == button[ButtonEnum.DOWNLOAD]
                || event.getSource() == trayStartItem ) { // button of Download
            if ( Flag.downloadingFlag || Flag.parseUrlFlag ) { // 目前正分析網址或下載中，不提供直接下載服務
                JOptionPane.showMessageDialog( this, "目前正下載中，不提供直接下載，請按「加入」來加入下載任務。", "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            } else {
                logFrame.redirectSystemStreams(); // start to log message
                Run.isAlive = true;
                stateBar.setText( "開始下載中..." );
                tabbedPane.setSelectedIndex( TabbedPaneEnum.MISSION ); // 按下載就會回到下載任務頁面
                Flag.parseUrlFlag = true; // 開始分析
                parseURL( args, true, false, 0 );
                args = null;
            }
        }
        if ( event.getSource() == button[ButtonEnum.STOP]
                || event.getSource() == trayStopItem ) { // button of stop
            Run.isAlive = false; // forbid download work
            Flag.allowDownloadFlag = Flag.downloadingFlag = Flag.parseUrlFlag = false;
            stateBar.setText( "所有下載任務停止" );
            trayIcon.setToolTip( "JComicDownloader" );

        }
        if ( event.getSource() == button[ButtonEnum.OPTION] ) { // button of Option
            // 用javax.swing.SwingUtilities.invokeLater反而很頓......
            new Thread( new Runnable() {

                public void run() {
                    new OptionFrame();
                }
            } ).start();
        }
        if ( event.getSource() == button[ButtonEnum.INFORMATION] ) { // button of Information
            new Thread( new Runnable() {

                public void run() {
                    final InformationFrame frame = new InformationFrame();

                    new Thread( new Runnable() {

                        public void run() {
                            frame.setNewestVersion(); // 更新版本資訊
                        }
                    } ).start();
                }
            } ).start();
        }
        if ( event.getSource() == button[ButtonEnum.CLEAR] ) { // button of CLEAR
            int choice = JOptionPane.showConfirmDialog( this, "請問是否要將目前內容全部清空？",
                    "提醒訊息", JOptionPane.YES_NO_OPTION );

            if ( choice == JOptionPane.YES_OPTION ) {
                if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.MISSION ) {
                    clearMission();
                } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.BOOKMARK ) {
                    clearBookmark();
                } else if ( tabbedPane.getSelectedIndex() == TabbedPaneEnum.RECORD ) {
                    clearRecord();
                }
            }

        }
        if ( event.getSource() == button[ButtonEnum.EXIT]
                || event.getSource() == trayExitItem ) { // button of Exit
            int choice = JOptionPane.showConfirmDialog( this, "請問是否要關閉JComicDownloader？",
                    "提醒訊息", JOptionPane.YES_NO_OPTION );

            if ( choice == JOptionPane.YES_OPTION ) {
                // 輸出下載任務清單，下次開啟時會自動載入

                exit();
            }
        }
    }

    // 結束程式之前要做的事情
    public static void exit() {
        SetUp.writeSetFile(); // 將目前的設定存入設定檔（set.ini）

        Common.outputDownTableFile( downTableModel );
        Common.outputBookmarkTableFile( bookmarkTableModel );
        Common.outputRecordTableFile( recordTableModel );

        Run.isAlive = false;
        Common.debugPrintln( "刪除所有暫存檔案" );
        Common.deleteFolder( SetUp.getTempDirectory() ); // 刪除暫存檔
        Common.debugPrintln( "Exit JComicDownloader ... " );

        System.exit( 0 );
    }

    private void whlie( boolean b ) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    //addWindowListener(new WindowAdapter(){
    private class RowListener implements ListSelectionListener {

        public void valueChanged( ListSelectionEvent event ) {
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

    // 取得自訂搜尋引擎的關鍵字搜尋頁面網址
    private String getKeywordSearchURL( String keyword ) {
        String url = "http://www.google.com/cse?cx=002948535609514911011%3Als5mhwb6sqa&ie=UTF-8&q="
                + keyword
                + "&sa=%E6%90%9C%E5%B0%8B&hl=zh-TW&siteurl=www.google.com%2Fcse%2Fhome%3Fcx%3D002948535609514911011%3Als5mhwb6sqa%26hl%3Dzh-TW#gsc.tab=0&gsc.q="
                + keyword
                + "&gsc.page=1";

        return Common.getFixedChineseURL( url );
    }

    // 以code頁面記錄開啟次數（好玩測試看看）
    private void counter() {
        new Thread( new Runnable() {

            public void run() {
                try {
                    Thread.sleep( 3000 ); // 先等三秒
                } catch ( InterruptedException ex ) {
                    Logger.getLogger( ComicDownGUI.class.getName() ).log( Level.SEVERE, null, ex );
                }

                String counterURL = "http://jcomicdownloader.googlecode.com/files/count.txt";

                //Common.downloadFileByForce( counterURL, SetUp.getTempDirectory(), "counter.txt", false, null );
                //ComicDownGUI.stateBar.setText( "請貼上網址" );

                Common.urlIsOK( counterURL );
            }
        } ).start();
    }

    public void testDownload() {
        Thread downThread = new Thread( new Runnable() {

            public void run() {



                //playAudio();

                /*
                String message = "old: " + Common.getNowAbsolutePathOld() + "\n" +
                "fixed: " + Common.getNowAbsolutePath();
                JOptionPane.showMessageDialog( ComicDownGUI.mainFrame,
                message, "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                
                 */
                //Run.isAlive = true;
                String picURL = "http://up1.emland.net/img.php?url=photo/27464/27465/46781292144946.jpg";
                String pageURL = "http://www.kangdm.com/comic/10256/";
                String testURL = "http://comic.veryim.com/manhua/zuishangys/";

                String cookie = null;//Common.getCookieString( pageURL );
                //cookie = cookie.replaceAll( "20-", "21-" );
                System.out.println( cookie );
                //cookie = "tyb=No; his=1324039671%7C%C8%AB1%BE%ED%7C179745%7C%BE%CD%CA%C7%B2%BB%D0%ED%B0%AE%BA%DC%B4%F3%7C201012%2F179746%7C%7C1324336783%7C%B5%DA1%BE%ED%7C212218%7C%C8%A8%C1%A6%B5%C4%D3%CE%CF%B7%7C201112%2F212222";

                //Common.downloadFile( pageURL, "", "test.html", false, cookie );
                Common.downloadFile( picURL, "", "test.jpg", false, cookie );

                //String[] cookies = Common.getCookieStrings( pageURL );

                System.out.println( "OVER" );

            }
        } );
        //downThread.start();
    }

    private JButton getButton( String string, String picName ) {
        JButton button = new JButton( string, new CommonGUI().getImageIcon( picName ) );
        button.setFont( SetUp.getDefaultFont( 5 ) );

        return button;
    }
}
