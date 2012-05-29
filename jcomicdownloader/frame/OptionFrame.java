/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/25
----------------------------------------------------------------------------------------------------
ChangeLog:
2.11: 1. 增加取消勾選『分析後下載圖檔』時的提醒視窗。
2.10: 1. 增加任務完成音效的選項。
2.08: 1. 增加JTattoo介面選項。
2.05: 1. 修復無法開啟壓縮檔的bug。（預設開啟圖片和壓縮檔為同個程式）
 *    2. 修復暫存資料夾路徑無法改變的bug。
2.04: 1. 增加選擇紀錄檔和暫存資料夾的選項。
2. 修改下拉式介面選單的渲染機制，使其可改變字型。 
2.03: 1. 修改選項視窗為多面板介面。
2. 增加下載失敗重試次數的選項。
2.01: 1. 增加預設勾選全部集數的選項。 
1.16: 勾選自動刪除就要連帶勾選自動壓縮。
1.14: 增加可選擇字型和字體大小的選項
1.09: 加入是否保留記錄的選項
1.08: 讓logCheckBox來決定由cmd或由logFrame來輸出資訊
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.frame;

import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalTheme;
import jcomicdownloader.module.Run;

// 用於改變下拉式介面選單的渲染機制
/**

選項視窗
 */
public class OptionFrame extends JFrame implements MouseListener {
    // about language
    private Object[][] languages;
    private String[] languageStrings;
    //private UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
    private String[] languageClassNames; // 存放所有介面類別名稱
    private JLabel languageLabel;
    private JComboBox languageBox;
    
    // about skin
    private Object[][] skins;
    private String[] skinStrings;
    //private UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
    private String[] skinClassNames; // 存放所有介面類別名稱
    private JLabel skinLabel;
    private JComboBox skinBox;
    // about directory
    private JLabel dirLabel, tempDirLabel, recordDirLabel;
    private JTextField singleDoneAudioTextField, allDoneAudioTextField;
    private JLabel playSingleDoneAudioLabel, playAllDoneAudioLabel;
    private JTextField dirTextField, recordDirTextField;
    public static JTextField tempDirTextField;
    private JButton singleDoneAudioButton, allDoneAudioButton;
    private JButton defaultSingleDoneAudioButton, defaultAllDoneAudioButton;
    private JButton dirButton, chooseFontButton, tempDirButton, recordDirButton;
    private JCheckBox singleDoneAudioCheckBox, allDoneAudioCheckBox; // 是否要開啟音效
    private JCheckBox compressCheckBox; // about compress
    private JCheckBox deleteCheckBox;  // about delete
    private JCheckBox logCheckBox; // about log
    private JCheckBox keepRecordCheckBox; // 保留記錄
    private JCheckBox urlCheckBox; // about output the url file
    private JCheckBox downloadCheckBox; // about download the pic file
    private JCheckBox keepDoneCheckBox;  // 是否保持已完成任務到下次開啟
    private JCheckBox keepUndoneCheckBox;  // 是否保持未完成任務到下次開啟
    private JCheckBox trayMessageCheckBox;  // 縮小到系統框後是否顯示下載完成訊息
    private JCheckBox choiceAllVolumeCheckBox;  // 是否勾選全部集數
    private JTextField proxyServerTextField; // 輸入代理伺服器位址 ex. proxy.hinet.net
    private JTextField proxyPortTextField; // 輸入代理伺服器連接阜 ex. 80
    private JButton confirmButton;  // about confirm
    private JButton cencelButton;  // 取消按鈕
    private JButton defaultButton;  // 預設按鈕
    //private String defaultColor; // 預設的建議設定顏色
    public static JFrame thisFrame; // use by self
    private JSlider retryTimesSlider, timeoutSlider;
    private JTabbedPane tabbedPane;
    private String tabLogoName = "tab_option.png";
    private JLabel viewPicFileLabel;
    private JTextField viewPicFileTextField;
    private JLabel viewTextFileLabel;
    private JTextField viewTextFileTextField;
    private JButton viewPicFileButton;
    private JButton viewTextFileButton;
    private JLabel viewZipFileLabel;
    private JTextField viewZipFileTextField;
    private JButton viewZipFileButton;
    public static JLabel retryTimesLabel;
    public static JLabel timeoutLabel;
    protected JPanel wholePanel;
    private Dimension frameDimension;
    JCheckBox usingBackgroundPicOfMainFrameCheckBox,
            usingBackgroundPicOfInformationFrameCheckBox,
            usingBackgroundPicOfOptionFrameCheckBox,
            usingBackgroundPicOfChoiceFrameCheckBox;
    // 設置背景圖片和字體顏色搭配
    private JButton setBackgroundPicOfMainFrameButton;
    private JButton setBackgroundPicOfInformationFrameButton;
    private JButton setBackgroundPicOfOptionFrameButton;
    private JButton setBackgroundPicOfChoiceFrameButton;

    private JRadioButton zipRadioButtion, cbzRadioButtion;
    private JRadioButton txtRadioButtion, htmlRadioButtion;

    /**
    
    @author user
     */
    public OptionFrame() {
        super( "選項設定" );
        OptionFrame.thisFrame = this; // for close the frame

        setUpUIComponent();

        setVisible( true );
    }

    private void setUpUIComponent() {
        String picFileString = SetUp.getBackgroundPicPathOfOptionFrame();
        // 檢查背景圖片是否存在
        if ( SetUp.getUsingBackgroundPicOfOptionFrame()
                && !new File( picFileString ).exists() ) {
            CommonGUI.showMessageDialog( this, picFileString
                    + "\n背景圖片不存在，重新設定為原始佈景",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            SetUp.setUsingBackgroundPicOfOptionFrame( false );
        }

        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) {
            frameDimension = CommonGUI.getDimension( picFileString );
            int width = (int) frameDimension.getWidth() + CommonGUI.widthGapOfBackgroundPic;
            int height = (int) frameDimension.getHeight() + CommonGUI.heightGapOfBackgroundPic;
            setSize( width, height );
            setResizable( false );
        }
        else {
            setSize( 700, 380 );
            setResizable( true );
        }

        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( Common.mainIcon ) );
        setDefaultLookAndFeelDecorated( false ); // 讓標題欄可以隨look and feel改變


        Container contentPane;
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) {
            ((JPanel) getContentPane()).setOpaque( false );
            contentPane = CommonGUI.getImagePanel( picFileString );
            contentPane.setPreferredSize( frameDimension );
            getContentPane().add( contentPane, BorderLayout.CENTER );
        }
        else {
            contentPane = getContentPane();
        }
        contentPane.setLayout( new BorderLayout() );

        //defaultColor = "black";

        wholePanel = new JPanel( new GridLayout( 0, 1, 5, 5 ) );
        wholePanel.setOpaque( false );
        setTabbedPane( wholePanel );

        contentPane.add( new CommonGUI().getFixedTansparentLabel(), BorderLayout.NORTH ); // 最上方留白
        contentPane.add( wholePanel, BorderLayout.CENTER ); // 設置主要頁面內容
        contentPane.add( getConfirmPanel(), BorderLayout.SOUTH ); // 設置確定按鈕

        setUpeListener();
    }

    // 設置最下方的確定按鈕
    private JPanel getConfirmPanel() {
        confirmButton = getButton( "   確定   " );
        CommonGUI.setToolTip( confirmButton, "儲存目前的設定動作" );

        cencelButton = getButton( "   取消   " );
        CommonGUI.setToolTip( cencelButton, "取消目前的設定動作" );

        defaultButton = getButton( "   還原預設值   " );
        CommonGUI.setToolTip( defaultButton, "將所有的設定值還原回到原廠設定" );

        JPanel choicePanel = new JPanel( new GridLayout( 1, 2, 40, 40 ) );
        choicePanel.add( confirmButton );
        choicePanel.add( cencelButton );
        choicePanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel centerPanel = new CommonGUI().getCenterPanel( choicePanel, 10, 40 );

        return centerPanel;
    }

    private void setTabbedPane( JPanel panel ) {
        // 檔案相關、介面相關、連線相關、其他雜項

        tabbedPane = new JTabbedPane();
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            tabbedPane.setOpaque( false );
            tabbedPane.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
        }

        JPanel fileTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 3, 1 ) ) );
        setFileTablePanel( fileTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "檔案" ), null, fileTablePanel,
                CommonGUI.getToolTipString( "有關於檔案存放的設定" ) );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_1 );

        JPanel connectionTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setConnectionTablePanel( connectionTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "連線" ), null, connectionTablePanel,
                CommonGUI.getToolTipString( "有關於連線下載的設定" ) );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_2 );

        JPanel missionTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setMissionTablePanel( missionTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "任務" ), null,
                missionTablePanel, CommonGUI.getToolTipString( "有關於下載任務的設定" ) );
        tabbedPane.setMnemonicAt( 2, KeyEvent.VK_3 );

        JPanel interfaceTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setInterfaceTablePanel( interfaceTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "介面" ), null, interfaceTablePanel,
                CommonGUI.getToolTipString( "有關於視窗介面的設定" ) );
        tabbedPane.setMnemonicAt( 3, KeyEvent.VK_4 );

        JPanel viewTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setViewTablePanel( viewTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "瀏覽" ), null, viewTablePanel,
                CommonGUI.getToolTipString( "有關於開啟圖片或壓縮檔的設定" ) );
        tabbedPane.setMnemonicAt( 3, KeyEvent.VK_4 );

        JPanel audioTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setAudioTablePanel( audioTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "音效" ), null, audioTablePanel,
                CommonGUI.getToolTipString( "有關於開啟圖片或壓縮檔的設定" ) );
        tabbedPane.setMnemonicAt( 4, KeyEvent.VK_5 );

        JPanel backgroundTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setBackgroundTablePanel( backgroundTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "佈景" ), null, backgroundTablePanel,
                CommonGUI.getToolTipString( "有關於背景圖片的設定" ) );
        tabbedPane.setMnemonicAt( 5, KeyEvent.VK_6 );

        JPanel otherTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setOtherTablePanel( otherTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "其他" ), null, otherTablePanel,
                CommonGUI.getToolTipString( "有關於其他雜七雜八的設定" ) );
        tabbedPane.setMnemonicAt( 6, KeyEvent.VK_7 );



        panel.add( tabbedPane, BorderLayout.CENTER );
    }

    private String getTabeHtmlFontString( String tabName ) {
        tabName = Common.getStringUsingDefaultLanguage( tabName ); // 使用預設語言 
        
        int htmlFontSize = (int) (SetUp.getDefaultFontSize() / 4 + 1);
        String htmlFontFace = SetUp.getDefaultFontName();
        return "<html><font face=\"" + htmlFontFace + "\" size=\"" + htmlFontSize + "\"> " + tabName + "  </font></html>";
    }

    private void setAudioTablePanel( JPanel panel ) {

        singleDoneAudioCheckBox = getCheckBoxBold( "播放單一任務完成的音效", SetUp.getPlaySingleDoneAudio() );
        CommonGUI.setToolTip( singleDoneAudioCheckBox, "是否在單一任務下載完成後播放音效" );

        singleDoneAudioTextField = getTextField( SetUp.getSingleDoneAudioFile() );
        CommonGUI.setToolTip( singleDoneAudioTextField, "單一任務完成後所播放的音效檔" );

        playSingleDoneAudioLabel = new JLabel( new CommonGUI().getImageIcon( Common.playAudioPic ) );
        CommonGUI.setToolTip( playSingleDoneAudioLabel, "測試播放單一任務完成的音效" );
        playSingleDoneAudioLabel.addMouseListener( this );

        JPanel singleDoneAudioCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.LEADING, 1, 1 ) );
        singleDoneAudioCheckBoxPanel.add( singleDoneAudioCheckBox );
        singleDoneAudioCheckBoxPanel.add( playSingleDoneAudioLabel );
        singleDoneAudioCheckBoxPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        singleDoneAudioButton = getButton( "外部音效" );
        CommonGUI.setToolTip( singleDoneAudioButton, "選擇單一任務完成後要播放的外部音效檔" );

        defaultSingleDoneAudioButton = getButton( "預設音效" );
        CommonGUI.setToolTip( defaultSingleDoneAudioButton, "使用預設單一任務完成後要播放的外部音效檔" );

        JPanel singleDoneAudioButtonPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        singleDoneAudioButtonPanelHorizontal.add( defaultSingleDoneAudioButton );
        singleDoneAudioButtonPanelHorizontal.add( singleDoneAudioButton );
        singleDoneAudioButtonPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel singleDoneAudioPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        singleDoneAudioPanelHorizontal.add( singleDoneAudioCheckBoxPanel );
        singleDoneAudioPanelHorizontal.add( singleDoneAudioButtonPanelHorizontal );
        singleDoneAudioPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        allDoneAudioCheckBox = getCheckBoxBold( "播放全部任務完成的音效", SetUp.getPlayAllDoneAudio() );
        CommonGUI.setToolTip( allDoneAudioCheckBox, "是否在全部任務下載完成後播放音效" );

        allDoneAudioTextField = getTextField( SetUp.getAllDoneAudioFile() );
        CommonGUI.setToolTip( allDoneAudioTextField, "全部任務完成後所播放的音效檔" );

        playAllDoneAudioLabel = new JLabel( new CommonGUI().getImageIcon( Common.playAudioPic ) );
        CommonGUI.setToolTip( playAllDoneAudioLabel, "測試播放全部任務完成的音效" );
        playAllDoneAudioLabel.addMouseListener( this );

        JPanel allDoneAudioCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.LEADING, 1, 1 ) );
        allDoneAudioCheckBoxPanel.add( allDoneAudioCheckBox );
        allDoneAudioCheckBoxPanel.add( playAllDoneAudioLabel );
        allDoneAudioCheckBoxPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        defaultAllDoneAudioButton = getButton( "預設音效" );
        CommonGUI.setToolTip( defaultAllDoneAudioButton, "使用預設全部任務完成後要播放的外部音效檔" );

        allDoneAudioButton = getButton( "外部音效" );
        CommonGUI.setToolTip( allDoneAudioButton, "選擇全部任務完成後要播放的外部音效檔" );

        JPanel allDoneAudioButtonPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        allDoneAudioButtonPanelHorizontal.add( defaultAllDoneAudioButton );
        allDoneAudioButtonPanelHorizontal.add( allDoneAudioButton );
        allDoneAudioButtonPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel allDoneAudioPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        allDoneAudioPanelHorizontal.add( allDoneAudioCheckBoxPanel );
        allDoneAudioPanelHorizontal.add( allDoneAudioButtonPanelHorizontal );
        allDoneAudioPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel otherPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        otherPanel.add( singleDoneAudioPanelHorizontal );
        otherPanel.add( singleDoneAudioTextField );
        otherPanel.add( allDoneAudioPanelHorizontal );
        otherPanel.add( allDoneAudioTextField );
        otherPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( otherPanel );
    }

    private void setBackgroundTablePanel( JPanel panel ) {

        usingBackgroundPicOfMainFrameCheckBox = getCheckBox( "是否設置主視窗的背景圖片", SetUp.getUsingBackgroundPicOfMainFrame() );
        CommonGUI.setToolTip( usingBackgroundPicOfMainFrameCheckBox, "由於介面特性使然，開啟後將強制使用Napkin介面" );

        usingBackgroundPicOfInformationFrameCheckBox = getCheckBox( "是否設置資訊視窗的背景圖片", SetUp.getUsingBackgroundPicOfInformationFrame() );
        CommonGUI.setToolTip( usingBackgroundPicOfInformationFrameCheckBox, "建議使用Napkin介面" );

        usingBackgroundPicOfOptionFrameCheckBox = getCheckBox( "是否設置選項視窗的背景圖片", SetUp.getUsingBackgroundPicOfOptionFrame() );
        CommonGUI.setToolTip( usingBackgroundPicOfOptionFrameCheckBox, "由於介面特性使然，開啟後將強制使用Napkin介面" );

        usingBackgroundPicOfChoiceFrameCheckBox = getCheckBox( "是否設置選擇集數視窗的背景圖片", SetUp.getUsingBackgroundPicOfChoiceFrame() );
        CommonGUI.setToolTip( usingBackgroundPicOfChoiceFrameCheckBox, "由於介面特性使然，開啟後將強制使用Napkin介面" );


        setBackgroundPicOfMainFrameButton = getButton( "設定頁面" );
        CommonGUI.setToolTip( setBackgroundPicOfMainFrameButton, "設定主視窗的背景圖片和字體顏色搭配" );
        setBackgroundPicOfInformationFrameButton = getButton( "設定頁面" );
        CommonGUI.setToolTip( setBackgroundPicOfInformationFrameButton, "設定資訊視窗的背景圖片和字體顏色搭配" );
        setBackgroundPicOfOptionFrameButton = getButton( "設定頁面" );
        CommonGUI.setToolTip( setBackgroundPicOfOptionFrameButton, "設定選項視窗的背景圖片和字體顏色搭配" );
        setBackgroundPicOfChoiceFrameButton = getButton( "設定頁面" );
        CommonGUI.setToolTip( setBackgroundPicOfChoiceFrameButton, "設定選擇集數視窗的背景圖片和字體顏色搭配" );


        JPanel backgroundPicOfMainFramePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        backgroundPicOfMainFramePanelHorizontal.add( usingBackgroundPicOfMainFrameCheckBox );
        backgroundPicOfMainFramePanelHorizontal.add( setBackgroundPicOfMainFrameButton );

        JPanel backgroundPicOfInformationFramePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        backgroundPicOfInformationFramePanelHorizontal.add( usingBackgroundPicOfInformationFrameCheckBox );
        backgroundPicOfInformationFramePanelHorizontal.add( setBackgroundPicOfInformationFrameButton );

        JPanel backgroundPicOfOptionFramePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        backgroundPicOfOptionFramePanelHorizontal.add( usingBackgroundPicOfOptionFrameCheckBox );
        backgroundPicOfOptionFramePanelHorizontal.add( setBackgroundPicOfOptionFrameButton );

        JPanel backgroundPicOfChoiceFramePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        backgroundPicOfChoiceFramePanelHorizontal.add( usingBackgroundPicOfChoiceFrameCheckBox );
        backgroundPicOfChoiceFramePanelHorizontal.add( setBackgroundPicOfChoiceFrameButton );


        JPanel backgroundPicPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        backgroundPicPanel.add( backgroundPicOfMainFramePanelHorizontal );
        backgroundPicPanel.add( backgroundPicOfInformationFramePanelHorizontal );
        backgroundPicPanel.add( backgroundPicOfOptionFramePanelHorizontal );
        backgroundPicPanel.add( backgroundPicOfChoiceFramePanelHorizontal );
        backgroundPicPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( backgroundPicPanel );
    }

    private void setOtherTablePanel( JPanel panel ) {

        tempDirLabel = getLabel( "目前暫存檔目錄：       " );

        tempDirTextField = getTextField( SetUp.getTempDirectory() );
        CommonGUI.setToolTip( tempDirTextField, "暫存資料夾的存放位置，執行時會新增暫存資料夾，等程式關閉後便自動刪除" );

        tempDirButton = getButton( "選擇新目錄" );
        CommonGUI.setToolTip( tempDirButton, "選擇暫存資料夾的存放位置，執行時會新增暫存資料夾，等程式關閉後便自動刪除" );

        JPanel tempDirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        tempDirPanelHorizontal.add( tempDirLabel );
        tempDirPanelHorizontal.add( tempDirButton );
        tempDirPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        recordDirLabel = getLabel( "目前記錄檔目錄：       " );

        recordDirTextField = getTextField( SetUp.getRecordFileDirectory() );
        CommonGUI.setToolTip( recordDirTextField, "downloadList.dat、bookmarkList.dat和recordList.dat這三個記錄檔的存放位置" );

        recordDirButton = getButton( "選擇新目錄" );
        CommonGUI.setToolTip( recordDirButton, "選擇downloadList.dat、bookmarkList.dat和recordList.dat這三個記錄檔的存放位置" );

        JPanel recordDirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        recordDirPanelHorizontal.add( recordDirLabel );
        recordDirPanelHorizontal.add( recordDirButton );
        recordDirPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel otherPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        otherPanel.add( recordDirPanelHorizontal );
        otherPanel.add( recordDirTextField );
        otherPanel.add( tempDirPanelHorizontal );
        otherPanel.add( tempDirTextField );
        otherPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( otherPanel );
    }

    private void setViewTablePanel( JPanel panel ) {
        viewPicFileLabel = getLabel( "預設開啟圖片的程式：       " );

        viewPicFileTextField = getTextField( SetUp.getOpenPicFileProgram() );

        viewPicFileButton = getButton( "選擇新程式" );
        CommonGUI.setToolTip( viewPicFileButton, "選擇可以開啟圖片的瀏覽程式，最好也能支援直接開啟壓縮檔" );

        JPanel viewPicFIlePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        viewPicFIlePanelHorizontal.add( viewPicFileLabel );
        viewPicFIlePanelHorizontal.add( viewPicFileButton );
        viewPicFIlePanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );


        viewZipFileLabel = getLabel( "預設開啟壓縮檔的程式：       " );

        viewZipFileTextField = getTextField( SetUp.getOpenZipFileProgram() );

        viewZipFileButton = getButton( "選擇新程式" );

        JPanel viewZipFilePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        viewZipFilePanelHorizontal.add( viewZipFileLabel );
        viewZipFilePanelHorizontal.add( viewZipFileButton );
        viewZipFilePanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );
        
        
        viewTextFileLabel = getLabel( "預設開啟文件的程式：       " );

        viewTextFileTextField = getTextField( SetUp.getOpenTextFileProgram() );

        viewTextFileButton = getButton( "選擇新程式" );
        CommonGUI.setToolTip( viewTextFileButton, "選擇偏好的文字編輯器" );

        JPanel viewTextFIlePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        viewTextFIlePanelHorizontal.add( viewTextFileLabel );
        viewTextFIlePanelHorizontal.add( viewTextFileButton );
        viewTextFIlePanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );


        JPanel viewPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        viewPanel.add( viewPicFIlePanelHorizontal );
        viewPanel.add( viewPicFileTextField );
        viewPanel.add( viewTextFIlePanelHorizontal );
        viewPanel.add( viewTextFileTextField );
        //viewPanel.add( viewZipFilePanelHorizontal );
        //viewPanel.add( viewZipFileTextField );
        viewPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( viewPanel );
    }

    private void setFileTablePanel( JPanel panel ) {
        dirLabel = getLabel( "目前下載目錄：       " );

        dirTextField = getTextField( SetUp.getOriginalDownloadDirectory() );

        dirButton = getButton( "選擇新目錄" );

        JPanel dirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        dirPanelHorizontal.add( dirLabel );
        dirPanelHorizontal.add( dirButton );
        dirPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        compressCheckBox = getCheckBoxBold( "自動產生壓縮檔", SetUp.getAutoCompress() );
        CommonGUI.setToolTip( compressCheckBox, "下載完成後進行壓縮，壓縮檔名與資料夾名稱相同" );

        JPanel compressPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        setCompressPanel( compressPanelHorizontal );

        deleteCheckBox = getCheckBox( "自動刪除圖片檔", SetUp.getDeleteOriginalPic() );
        CommonGUI.setToolTip( deleteCheckBox, "下載完成後便刪除圖檔，此選項應與『自動產生壓縮檔』搭配使用" );

        urlCheckBox = getCheckBox( "輸出下載位址文件檔", SetUp.getOutputUrlFile() );
        CommonGUI.setToolTip( urlCheckBox, "解析所有圖片的真實下載位址後彙整輸出為txt文件檔，檔名與資料夾名稱相同" );

        downloadCheckBox = getCheckBoxBold( "分析後下載圖檔（預設）", SetUp.getDownloadPicFile() );
        CommonGUI.setToolTip( downloadCheckBox, "如果沒有勾選就不會有下載行為，建議要勾選（但若只想輸出真實下載位址，就不要勾選此選項）" );

        
        
        JPanel textFormatPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        setTextFormatPanel( textFormatPanelHorizontal );
        

        JPanel filePanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        filePanel.add( dirPanelHorizontal );
        filePanel.add( dirTextField );
        filePanel.add( compressPanelHorizontal );
        filePanel.add( textFormatPanelHorizontal );
        filePanel.add( urlCheckBox );
        filePanel.add( downloadCheckBox );
        filePanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( filePanel );
    }
    
    private void setTextFormatPanel( JPanel textFormatPanelHorizontal ) {
        boolean choiceTxt = false, choiceHtml = false;
        if ( SetUp.getDefaultTextOutputFormat() == FileFormatEnum.HTML ) {
            choiceHtml = true;
            choiceTxt = false;
        }
        else {
            choiceHtml = false;
            choiceTxt = true;
        }
        
        txtRadioButtion = getRadioButton( "txt", choiceTxt );
        CommonGUI.setToolTip( txtRadioButtion, "輸出txt檔" );
        htmlRadioButtion = getRadioButton( "html", choiceHtml );
        CommonGUI.setToolTip( htmlRadioButtion, "輸出html檔" );
        ButtonGroup textFormatGroup = new ButtonGroup();
        textFormatGroup.add( txtRadioButtion );
        textFormatGroup.add( htmlRadioButtion );
        
        JLabel textFormatLabel = getLabel( "文章輸出格式：", "批次下載網頁（文字檔）時，預設的輸出格式" );

        JPanel textFormatPanel = new JPanel();
        textFormatPanel.add( textFormatLabel );
        textFormatPanel.add( txtRadioButtion );
        textFormatPanel.add( htmlRadioButtion );

        textFormatPanelHorizontal.add( deleteCheckBox );
        textFormatPanelHorizontal.add( textFormatPanel );
        textFormatPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );
    }
    
    private void setCompressPanel( JPanel compressPanelHorizontal ) {
        boolean choiceZip = false, choiceCbz = false;
        if ( "zip".equals( SetUp.getCompressFormat() ) ) {
            choiceZip = true;
            choiceCbz = false;
        }
        else {
            choiceZip = false;
            choiceCbz = true;
        }

        zipRadioButtion = getRadioButton( "zip", choiceZip );
        CommonGUI.setToolTip( zipRadioButtion, "壓縮為zip檔" );
        cbzRadioButtion = getRadioButton( "cbz", choiceCbz );
        CommonGUI.setToolTip( cbzRadioButtion, "壓縮為cbz檔" );
        ButtonGroup compressGroup = new ButtonGroup();
        compressGroup.add( zipRadioButtion );
        compressGroup.add( cbzRadioButtion );
        
        JLabel compressLabel = getLabel( "壓縮格式：", "預設的壓縮格式" );

        JPanel compressPanel = new JPanel();
        compressPanel.add( compressLabel );
        compressPanel.add( zipRadioButtion );
        compressPanel.add( cbzRadioButtion );

        
        compressPanelHorizontal.add( compressCheckBox );
        compressPanelHorizontal.add( compressPanel );
        compressPanelHorizontal.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );
    }

    private void setConnectionTablePanel( JPanel panel ) {
        JLabel proxyServerLabel = getLabel( "設定代理伺服器位址：", "若是中華電信用戶，可輸入proxy.hinet.net" );
        proxyServerTextField = getTextField( SetUp.getProxyServer() );
        proxyServerTextField.addMouseListener( this );
        CommonGUI.setToolTip( proxyServerTextField, "若是中華電信用戶，可輸入proxy.hinet.net" );

        JPanel proxyServerPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        proxyServerPanel.add( proxyServerLabel );
        proxyServerPanel.add( proxyServerTextField );
        proxyServerPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JLabel proxyPortLabel = getLabel( "設定代理伺服器連接阜：", "若是中華電信用戶，可輸入80" );
        proxyPortTextField = getTextField( SetUp.getProxyPort() );
        proxyPortTextField.addMouseListener( this );
        CommonGUI.setToolTip( proxyPortTextField, "若是中華電信用戶，可輸入80" );

        JPanel proxyPortPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        proxyPortPanel.add( proxyPortLabel );
        proxyPortPanel.add( proxyPortTextField );
        proxyPortPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        retryTimesSlider = new JSlider( JSlider.HORIZONTAL, 0, 5, 1 );
        retryTimesSlider.addChangeListener( new SliderHandler() );
        retryTimesSlider.setMajorTickSpacing( 1 );
        //retryTimesSlider.setPaintTicks(true);
        retryTimesSlider.setPaintLabels( true );
        retryTimesSlider.setValue( SetUp.getRetryTimes() );
        CommonGUI.setToolTip( retryTimesSlider, "通常下載失敗是伺服器異常或網路速度過慢所致，立即重試的成功機率其實不高" );
        retryTimesLabel = getLabel( "下載失敗重試次數：" + retryTimesSlider.getValue() + "次" );
        CommonGUI.setToolTip( retryTimesLabel, "通常下載失敗是伺服器異常或網路速度過慢所致，立即重試的成功機率其實不高" );
        retryTimesSlider.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel retryTimesPortPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        retryTimesPortPanel.add( retryTimesLabel );
        retryTimesPortPanel.add( retryTimesSlider );
        retryTimesPortPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        timeoutSlider = new JSlider( JSlider.HORIZONTAL, 0, 100, 10 );
        timeoutSlider.addChangeListener( new SliderHandler() );
        timeoutSlider.setMajorTickSpacing( 20 );
        //timeoutSlider.setPaintTicks(true);
        timeoutSlider.setPaintLabels( true );
        timeoutSlider.setValue( SetUp.getTimeoutTimer() );
        CommonGUI.setToolTip( timeoutSlider, "超過此時間會中斷此連線，直接下載下一個檔案，建議只在下載GOOGLE圖片時使用，其他時候建議設為0，代表沒有逾時限制" );
        timeoutLabel = getLabel( "連線逾時時間：" + timeoutSlider.getValue() + "秒" );
        CommonGUI.setToolTip( timeoutLabel, "超過此時間會中斷此連線，直接下載下一個檔案，建議只在下載GOOGLE圖片時使用，其他時候建議設為0，代表沒有逾時限制" );
        timeoutSlider.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        JPanel timeoutPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        timeoutPanel.add( timeoutLabel );
        timeoutPanel.add( timeoutSlider );
        timeoutPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );


        JPanel connectionPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        connectionPanel.add( proxyServerPanel );
        connectionPanel.add( proxyPortPanel );
        connectionPanel.add( retryTimesPortPanel );
        connectionPanel.add( timeoutPanel );
        connectionPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( connectionPanel );
    }

    private void setInterfaceTablePanel( JPanel panel ) {
        JLabel chooseFontLabel = getLabel( "目前字型：" + SetUp.getDefaultFontName() + SetUp.getDefaultFontSize() );
        chooseFontButton = getButton( "選擇新字型" );
        CommonGUI.setToolTip( chooseFontLabel, "選定字型後需關閉重啟，方能看到新設定的字型" );
        CommonGUI.setToolTip( chooseFontButton, "選定字型後需關閉重啟，方能看到新設定的字型" );

        JPanel chooseFontPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        chooseFontPanel.add( chooseFontLabel );
        chooseFontPanel.add( chooseFontButton );
        chooseFontPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );


        // set language
        
        languageClassNames = new CommonGUI().getClassNames(); // 取得所有介面類別名稱
        languageStrings = new CommonGUI().getLanguageStrings(); // 取得所有介面名稱
        languageBox = new JComboBox( languageStrings );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            languageBox.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            languageBox.setOpaque( false );
            languageBox.addMouseListener( this );
        }

        ListCellRenderer languageRenderer = new ComplexCellRenderer();
        languageBox.setRenderer( languageRenderer );
        languageBox.setSelectedIndex( SetUp.getDefaultLanguage() );
        languageBox.setFont( SetUp.getDefaultFont() );

        languageLabel = getLabel( "選擇語言：" );
        CommonGUI.setToolTip( languageLabel, "選定介面語言後需關閉重啟，方能看到新設定的語言" );

        languageBox.addItemListener( new ItemHandler() ); // change skin if change skinBox
        CommonGUI.setToolTip( languageBox, "選定介面語言後需關閉重啟，方能看到新設定的語言" );

        // the order: skinLabel skinBox
        JPanel languagePanel = new JPanel();
        languagePanel.setLayout( new GridLayout( 1, 2, 5, 5 ) );
        languagePanel.add( languageLabel );
        languagePanel.add( languageBox );
        languagePanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );
        
        
        // set skin
        
        skinClassNames = new CommonGUI().getClassNames(); // 取得所有介面類別名稱
        skinStrings = new CommonGUI().getSkinStrings(); // 取得所有介面名稱
        skinBox = new JComboBox( skinStrings );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            skinBox.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            skinBox.setOpaque( false );
            skinBox.addMouseListener( this );
        }

        ListCellRenderer skinRenderer = new ComplexCellRenderer();
        skinBox.setRenderer( skinRenderer );
        skinBox.setSelectedIndex( getSkinIndex( SetUp.getSkinClassName() ) );
        skinBox.setFont( SetUp.getDefaultFont() );

        skinLabel = getLabel( "選擇介面：" );
        CommonGUI.setToolTip( skinLabel, "可選擇您喜好的介面風格" );

        skinBox.addItemListener( new ItemHandler() ); // change skin if change skinBox
        CommonGUI.setToolTip( skinBox, "可選擇您喜好的介面風格" );

        // the order: skinLabel skinBoxoyoahalnf.jar
        JPanel skinPanel = new JPanel();
        skinPanel.setLayout( new GridLayout( 1, 2, 5, 5 ) );
        skinPanel.add( skinLabel );
        skinPanel.add( skinBox );
        skinPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        trayMessageCheckBox = getCheckBoxBold( "縮小到系統列時顯示下載完成訊息", SetUp.getShowDoneMessageAtSystemTray() );
        CommonGUI.setToolTip( trayMessageCheckBox, "如果沒有勾選，縮小到系統列後就不會再有下載完畢的提示訊息" );

        logCheckBox = getCheckBox( "開啟除錯訊息視窗", SetUp.getOpenDebugMessageWindow() );
        CommonGUI.setToolTip( logCheckBox, "開啟後可檢視更詳細的程式運作細節與例外錯誤訊息" );

        JPanel interfacePanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        interfacePanel.add( chooseFontPanel );
        interfacePanel.add( languagePanel );
        interfacePanel.add( skinPanel );
        interfacePanel.add( trayMessageCheckBox );
        interfacePanel.add( logCheckBox );
        interfacePanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( interfacePanel );
    }

    private void setMissionTablePanel( JPanel panel ) {
        keepUndoneCheckBox = getCheckBoxBold( "保留未完成任務", SetUp.getKeepUndoneDownloadMission() );
        CommonGUI.setToolTip( keepUndoneCheckBox, "這次沒下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepDoneCheckBox = getCheckBox( "保留已完成任務", SetUp.getKeepDoneDownloadMission() );
        CommonGUI.setToolTip( keepDoneCheckBox, "這次已經下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepRecordCheckBox = getCheckBoxBold( "保留任務記錄", SetUp.getKeepRecord() );
        CommonGUI.setToolTip( keepRecordCheckBox, "若紀錄過多而影響效能，請取消勾選或刪除recordList.dat" );

        choiceAllVolumeCheckBox = getCheckBox( "預設勾選全部集數", SetUp.getChoiceAllVolume() );
        CommonGUI.setToolTip( choiceAllVolumeCheckBox, "本來預設都不勾選（除了單集），但若勾選此選項，便會全部勾選" );

        JPanel missionPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        missionPanel.add( keepUndoneCheckBox );
        missionPanel.add( keepDoneCheckBox );
        missionPanel.add( keepRecordCheckBox );
        missionPanel.add( choiceAllVolumeCheckBox );
        missionPanel.setOpaque( !SetUp.getUsingBackgroundPicOfOptionFrame() );

        panel.add( missionPanel );
    }

    private int getSkinIndex( String skinClassName ) {
        int index = 0;
        for ( String skinName : skinStrings ) {
            // 因為在選單裡面的界面名稱都是xx.xx（ex. JTattoo.Noire），所以這邊就只擷取後面那段
            String simpleSkinName = skinName.split( "\\." )[1];
            if ( skinClassName.matches( ".*" + simpleSkinName + ".*" ) ) {
                break;
            }
            index++;
        }

        if ( skinStrings.length > index ) {
            return index;
        }
        else {
            Common.errorReport( "超出範圍: " + index + " " + skinClassName );
            return 0;
        }
    }

    private void setSkin( int index ) {
        boolean continueChange = true;
        String className = skinClassNames[index];

        // 檢查是否有設定背景圖片，若有則發出提醒通知，且不繼續改變介面
        if ( checkSettingOfBackgroundPic( index ) ) {
            int napkinIndex = new CommonGUI().getSkinOrderBySkinClassName( "napkin.NapkinLookAndFeel" );
            skinBox.setSelectedIndex( napkinIndex ); // 回到Napkin介面
            
            return;
        }

        SetUp.setSkinClassName( className ); // 紀錄到設定值

        if ( !CommonGUI.checkSkin() ) {
            return; // 如果找不到相對應的JAR檔，就不繼續設定了。
        }

        try {
            //className = "com.pagosoft.plaf.PgsLookAndFeel";

            /*
            // oyoaha介面
            com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel laf =
            new com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel();
            File themeFile = new File(
            "/home/surveyork/文件/程式/JAVA/NetBeansProjects/" + ""
            + "JComicDownloader/lib/oyoahalnf/metal/green1.theme" );
            laf.setOyoahaTheme( themeFile );
             */

            /*
            // Tiny介面
            de.muntjak.tinylookandfeel.TinyLookAndFeel laf =
            new de.muntjak.tinylookandfeel.TinyLookAndFeel();
            
            
            // TINY_STYLE = 0;int W99_STYLE = 1;YQ_STYLE = 2;
            File themeFile = new File("/home/surveyork/文件/程式/JAVA/" + ""
            + "NetBeansProjects/JComicDownloader/" + ""
            + "lib/tinylaf-1_4_0/tiny_theme/99 Lego.theme" );
            
            //de.muntjak.tinylookandfeel.Theme.loadTheme( themeFile );
            laf.setCurrentTheme( null );
            //laf.setCurrentTheme(
            com.pagosoft.plaf.ThemeFactory.getThemeByName( "silver" ) );
            UIManager.setLookAndFeel( laf );
             */

            // substance介面
            // org.jvnet.substance.skin.SubstanceBusinessLookAndFeel laf = 
            //    org.jvnet.substance.skin.SubstanceBusinessLookAndFeel();

            CommonGUI.setLookAndFeelByClassName( className );

        }
        catch ( Exception ex ) {
            Common.debugPrintln( "無法使用" + className + "介面!!" );

            className = ComicDownGUI.getDefaultSkinClassName(); // 回歸預設介面
            CommonGUI.setLookAndFeelByClassName( className );
        }
        ComicDownGUI.setDefaultSkinClassName( className );

        // change the skin of Option frame
        //SwingUtilities.updateComponentTreeUI( this );
        CommonGUI.updateUI( this ); // 更新介面


        // change the skin of main frame
        //SwingUtilities.updateComponentTreeUI( ComicDownGUI.mainFrame );
        CommonGUI.updateUI( ComicDownGUI.mainFrame ); // 更新介面

        if ( InformationFrame.thisFrame != null ) // change the skin of information frame
        {
            //SwingUtilities.updateComponentTreeUI( InformationFrame.thisFrame );
            CommonGUI.updateUI( InformationFrame.thisFrame ); // 更新介面
        }

        if ( ChoiceFrame.choiceFrame != null ) // change the skin of information frame
        {
            //SwingUtilities.updateComponentTreeUI( ChoiceFrame.choiceFrame );
            CommonGUI.updateUI( ChoiceFrame.choiceFrame ); // 更新介面
        }

        if ( LogFrame.thisFrame != null ) // change the skin of information frame
        {
            //SwingUtilities.updateComponentTreeUI( LogFrame.logFrame );
            CommonGUI.updateUI( LogFrame.thisFrame ); // 更新介面
        }


        Common.debugPrintln( "改為" + className + "面板" );

        Common.debugPrintln( "目前面板名稱: " + UIManager.getLookAndFeel().getName() );
    }

    // 檢查是否有設定背景圖片，若有則發出提醒通知
    private boolean checkSettingOfBackgroundPic( int index ) {
        if ( !skinClassNames[index].matches( ".*Napkin.*" ) &&
                (usingBackgroundPicOfMainFrameCheckBox.isSelected()
                || usingBackgroundPicOfOptionFrameCheckBox.isSelected()
                || usingBackgroundPicOfChoiceFrameCheckBox.isSelected())
                && (SetUp.getSkinClassName().matches( ".*napkin.*" )) ) {
            CommonGUI.showMessageDialog( OptionFrame.thisFrame,
                    "若欲讓此介面選擇生效，請取消佈景頁面中背景圖片的設定勾選",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return true;
        }
        else {
            return false;
        }
    }

    // 檢查是否有勾選背景圖片的設定，若有則把laf設為napkin.NapkinLookAndFeel
    // 也就是如果有開啟背景圖片，則只能使用napkin介面。
    private void setNapkinIfBackgroundPicInOperation() {
        if ( usingBackgroundPicOfMainFrameCheckBox.isSelected()
                || usingBackgroundPicOfOptionFrameCheckBox.isSelected()
                || usingBackgroundPicOfChoiceFrameCheckBox.isSelected() ) {
            //int num = new CommonGUI().getSkinOrderBySkinClassName( "napkin.NapkinLookAndFeel" );
            //setSkin( num );
            SetUp.setSkinClassName( "napkin.NapkinLookAndFeel" ); // 紀錄到設定值
        }

    }

    private void setUpeListener() {
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    // mouse event
    @Override
    public void mouseClicked( MouseEvent e ) {
    }

    @Override
    public void mousePressed( MouseEvent event ) {
        if ( event.getSource() == playSingleDoneAudioLabel ) {
            Common.playSingleDoneAudio( singleDoneAudioTextField.getText() ); // 播放單一任務完成音效
        }
        else if ( event.getSource() == playAllDoneAudioLabel ) {
            Common.playAllDoneAudio( allDoneAudioTextField.getText() ); // 播放全部任務完成音效
        }
        else if ( event.getSource() == proxyServerTextField ) {
            proxyServerTextField.setText( "" ); // 滑鼠點擊後會清空
        }
        else if ( event.getSource() == proxyPortTextField ) {
            proxyPortTextField.setText( "" ); // 滑鼠點擊後會清空
        }

    }

    @Override
    public void mouseReleased( MouseEvent e ) {
    }

    @Override
    public void mouseExited( MouseEvent event ) {
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) {
            ((JComponent) event.getSource()).setForeground( SetUp.getOptionFrameOtherDefaultColor() );
        }
    }

    @Override
    public void mouseEntered( MouseEvent event ) {
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) {
            ((JComponent) event.getSource()).setForeground( SetUp.getOptionFrameOtherMouseEnteredColor() );
        }
    }

    // -------------  Listener  ---------------
    private class ActionHandler implements ActionListener {

        public void actionPerformed( ActionEvent event ) {
            if ( event.getSource() == chooseFontButton ) {
                // 選擇字型和大小
                new Thread( new Runnable() {

                    public void run() {
                        SwingUtilities.invokeLater( new Runnable() {

                            public void run() {

                                JFontChooser fontChooser = new JFontChooser();
                                Font font = fontChooser.showDialog( OptionFrame.thisFrame, "選擇字型" );

                                if ( font != null ) {
                                    SetUp.setDefaultFontName( font.getName() );
                                    SetUp.setDefaultFontSize( font.getSize() );
                                    //SwingUtilities.updateComponentTreeUI( fontChooser );
                                }

                                if ( font != null ) {
                                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "你選擇的字型是" + font.getName() + "　"
                                            + "大小為" + font.getSize() + "（需重新開啟才會啟用新設定）" );
                                }

                            }
                        } );
                    }
                } ).start();
            }
            if ( event.getSource() == dirButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.DIRECTORIES_ONLY,
                        "選擇下載目錄", dirTextField, SetUp.getOriginalDownloadDirectory() );
            }
            else if ( event.getSource() == tempDirButton ) {
                int endIndex = SetUp.getTempDirectory().lastIndexOf( Common.getSlash() + "temp" ) + 1;
                String path = SetUp.getTempDirectory().substring( 0, endIndex );

                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.DIRECTORIES_ONLY,
                        "選擇暫存目錄", tempDirTextField, path );
            }
            else if ( event.getSource() == recordDirButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.DIRECTORIES_ONLY,
                        "選擇記錄檔存放目錄", recordDirTextField, SetUp.getRecordFileDirectory() );
            }
            else if ( event.getSource() == viewPicFileButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.FILES_ONLY,
                        "選擇可瀏覽圖片的程式", viewPicFileTextField, SetUp.getOpenPicFileProgram() );
            }
            else if ( event.getSource() == viewTextFileButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.FILES_ONLY,
                        "選擇可瀏覽文件檔的程式", viewTextFileTextField, SetUp.getOpenTextFileProgram() );
            }
            else if ( event.getSource() == viewZipFileButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.FILES_ONLY,
                        "選擇可瀏覽壓縮檔的程式", viewZipFileTextField, SetUp.getOpenZipFileProgram() );
            }
            else if ( event.getSource() == singleDoneAudioButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.FILES_ONLY,
                        "選擇音效檔", singleDoneAudioTextField,
                        SetUp.getSingleDoneAudioFile(), new AudioFileFilter() );
            }
            else if ( event.getSource() == allDoneAudioButton ) {
                CommonGUI.chooseFile( OptionFrame.thisFrame, JFileChooser.FILES_ONLY,
                        "選擇音效檔", allDoneAudioTextField,
                        SetUp.getAllDoneAudioFile(), new AudioFileFilter() );
            }
            else if ( event.getSource() == defaultSingleDoneAudioButton ) {
                singleDoneAudioTextField.setText( Common.defaultSingleDoneAudio );
            }
            else if ( event.getSource() == defaultAllDoneAudioButton ) {
                allDoneAudioTextField.setText( Common.defaultAllDoneAudio );
            }


            // 詳細設定背景圖片和字體顏色搭配
            if ( event.getSource() == setBackgroundPicOfMainFrameButton ) {
                new BackgroundSettingFrame( FrameEnum.MAIN_FRAME );
            }
            else if ( event.getSource() == setBackgroundPicOfInformationFrameButton ) {
                new BackgroundSettingFrame( FrameEnum.INFORMATION_FRAME );
            }
            else if ( event.getSource() == setBackgroundPicOfOptionFrameButton ) {
                new BackgroundSettingFrame( FrameEnum.OPTION_FRAME );
            }
            else if ( event.getSource() == setBackgroundPicOfChoiceFrameButton ) {
                new BackgroundSettingFrame( FrameEnum.CHOICE_FRAME );
            }


            if ( event.getSource() == confirmButton ) {
                SetUp.setProxyServer( proxyServerTextField.getText() );
                SetUp.setProxyPort( proxyPortTextField.getText() );

                SetUp.setRetryTimes( retryTimesSlider.getValue() ); // 設定重新嘗試次數
                SetUp.setTimeoutTimer( timeoutSlider.getValue() ); // 設定逾時時間

                SetUp.setOriginalDownloadDirectory( dirTextField.getText() ); // 紀錄到設定值
                SetUp.setTempDirectory( tempDirTextField.getText() ); // 紀錄到設定值

                SetUp.setOpenPicFileProgram( viewPicFileTextField.getText() ); // 紀錄到設定值
                SetUp.setOpenTextFileProgram( viewTextFileTextField.getText() ); // 紀錄到設定值
                SetUp.setOpenZipFileProgram( viewPicFileTextField.getText() ); // 紀錄到設定值
                SetUp.setRecordFileDirectory( recordDirTextField.getText() ); // 紀錄到設定值

                // 除非有此檔案，否則一律歸初始值
                if ( !new File( singleDoneAudioTextField.getText() ).exists() ) {
                    singleDoneAudioTextField.setText( Common.defaultSingleDoneAudio );
                }
                SetUp.setSingleDoneAudioFile( singleDoneAudioTextField.getText() ); // 紀錄到設定值
                if ( !new File( allDoneAudioTextField.getText() ).exists() ) {
                    allDoneAudioTextField.setText( Common.defaultAllDoneAudio );
                }
                SetUp.setAllDoneAudioFile( allDoneAudioTextField.getText() ); // 紀錄到設定值


                SetUp.setPlaySingleDoneAudio( singleDoneAudioCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setPlayAllDoneAudio( allDoneAudioCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setAutoCompress( compressCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setDeleteOriginalPic( deleteCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setOutputUrlFile( urlCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setDownloadPicFile( downloadCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setKeepDoneDownloadMission( keepDoneCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setKeepUndoneDownloadMission( keepUndoneCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setShowDoneMessageAtSystemTray( trayMessageCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setKeepRecord( keepRecordCheckBox.isSelected() ); // 紀錄到設定值
                SetUp.setChoiceAllVolume( choiceAllVolumeCheckBox.isSelected() ); // 紀錄到設定值
                
                SetUp.setDefaultLanguage( languageBox.getSelectedIndex() ); // 記錄到設定值

                String compressFormatString = "";
                if ( zipRadioButtion.isSelected() )
                    compressFormatString = "zip";
                else
                    compressFormatString = "cbz";
                SetUp.setCompressFormat( compressFormatString ); // 紀錄到設定值
                
                if ( htmlRadioButtion.isSelected() )
                    SetUp.setDefaultTextOutputFormat( FileFormatEnum.HTML ); // 紀錄到設定值
                else
                    SetUp.setDefaultTextOutputFormat( FileFormatEnum.TEXT ); // 紀錄到設定值

                Boolean tempBool = new Boolean( usingBackgroundPicOfMainFrameCheckBox.isSelected() );
                if ( !tempBool.equals( SetUp.getUsingBackgroundPicOfMainFrame() ) ) {
                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "主視窗背景設定已改變，請重新開啟程式",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                }

                // 是否使用背景圖片 
                SetUp.setUsingBackgroundPicOfMainFrame(
                        usingBackgroundPicOfMainFrameCheckBox.isSelected() );
                SetUp.setUsingBackgroundPicOfInformationFrame(
                        usingBackgroundPicOfInformationFrameCheckBox.isSelected() );
                SetUp.setUsingBackgroundPicOfOptionFrame(
                        usingBackgroundPicOfOptionFrameCheckBox.isSelected() );
                SetUp.setUsingBackgroundPicOfChoiceFrame(
                        usingBackgroundPicOfChoiceFrameCheckBox.isSelected() );

                setNapkinIfBackgroundPicInOperation(); // 檢查是否有勾選背景圖片的設定

                if ( proxyServerTextField.getText() != null
                        && !proxyServerTextField.getText().equals( "" )
                        && proxyPortTextField.getText() != null
                        && !proxyPortTextField.getText().equals( "" ) ) {
                    Common.setHttpProxy( SetUp.getProxyServer(), SetUp.getProxyPort() );
                    Common.debugPrintln( "設定代理伺服器："
                            + SetUp.getProxyServer() + " "
                            + SetUp.getProxyPort() );
                }
                else {
                    Common.closeHttpProxy();
                    Common.debugPrintln( "代理伺服器資訊欠缺位址或連接阜，因此不加入" );
                }
                SetUp.writeSetFile(); // 將目前的設定存入設定檔（set.ini）



                dispose();
            }
            else if ( event.getSource() == cencelButton ) {
                dispose();
            }
            else if ( event.getSource() == defaultButton ) {
            }

        }
    }

    private class SliderHandler implements ChangeListener {

        public void stateChanged( ChangeEvent event ) {

            if ( event.getSource() == retryTimesSlider && OptionFrame.retryTimesLabel != null ) {
                OptionFrame.retryTimesLabel.setText( "下載失敗重試次數：" + retryTimesSlider.getValue() + "次" );
            }
            if ( event.getSource() == timeoutSlider && OptionFrame.timeoutLabel != null ) {
                OptionFrame.timeoutLabel.setText( "連線逾時時間：" + timeoutSlider.getValue() + "秒" );
            }

            //Common.debugPrintln( "改變重試次數：" + retryTimesSlider.getValue() );
        }
    }

    private class ItemHandler implements ItemListener {

        public void itemStateChanged( ItemEvent event ) {

            if ( SetUp.getSkinClassName().matches( ".*napkin.*" ) ) {
                repaint();
            }

            if ( event.getSource() == usingBackgroundPicOfMainFrameCheckBox ) {
                if ( usingBackgroundPicOfMainFrameCheckBox.isSelected()
                        && (SetUp.getBackgroundPicPathOfMainFrame() == null
                        || SetUp.getBackgroundPicPathOfMainFrame().equals( "" )) ) {
                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "背景圖片尚未選擇，請先點擊右方的設定頁面按鈕做設定",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    usingBackgroundPicOfMainFrameCheckBox.setSelected( false );
                }
            }
            else if ( event.getSource() == usingBackgroundPicOfInformationFrameCheckBox ) {
                if ( usingBackgroundPicOfInformationFrameCheckBox.isSelected()
                        && (SetUp.getBackgroundPicPathOfInformationFrame() == null
                        || SetUp.getBackgroundPicPathOfInformationFrame().equals( "" )) ) {
                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "背景圖片尚未選擇，請先點擊右方的設定頁面按鈕做設定",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    usingBackgroundPicOfInformationFrameCheckBox.setSelected( false );
                }
            }
            else if ( event.getSource() == usingBackgroundPicOfOptionFrameCheckBox ) {
                if ( usingBackgroundPicOfOptionFrameCheckBox.isSelected()
                        && (SetUp.getBackgroundPicPathOfOptionFrame() == null
                        || SetUp.getBackgroundPicPathOfOptionFrame().equals( "" )) ) {
                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "背景圖片尚未選擇，請先點擊右方的設定頁面按鈕做設定",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    usingBackgroundPicOfOptionFrameCheckBox.setSelected( false );
                }
            }
            else if ( event.getSource() == usingBackgroundPicOfChoiceFrameCheckBox ) {
                if ( usingBackgroundPicOfChoiceFrameCheckBox.isSelected()
                        && (SetUp.getBackgroundPicPathOfChoiceFrame() == null
                        || SetUp.getBackgroundPicPathOfChoiceFrame().equals( "" )) ) {
                    CommonGUI.showMessageDialog( OptionFrame.thisFrame, "背景圖片尚未選擇，請先點擊右方的設定頁面按鈕做設定",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                    usingBackgroundPicOfChoiceFrameCheckBox.setSelected( false );
                }
            }

            if ( event.getSource() == deleteCheckBox ) {

                if ( deleteCheckBox.isSelected() ) {
                    compressCheckBox.setSelected( true ); // 勾選自動刪除就要連帶勾選自動壓縮
                }
            }
            else if ( event.getSource() == compressCheckBox ) {
                if ( !compressCheckBox.isSelected() ) {
                    deleteCheckBox.setSelected( false ); // 勾選自動刪除就要連帶勾選自動壓縮
                }
            }
            else if ( event.getSource() == downloadCheckBox ) {
                if ( !downloadCheckBox.isSelected() ) {
                    String message = "取消後就不會進行下載，確定取消？";
                    int choice = JOptionPane.showConfirmDialog( OptionFrame.thisFrame, message, "提醒訊息", JOptionPane.YES_NO_OPTION );
                    if ( choice == JOptionPane.NO_OPTION ) { // agree to remove the title in the download list
                        downloadCheckBox.setSelected( true );
                    }
                    else {
                        downloadCheckBox.setSelected( false );
                    }
                }
            }


            if ( event.getSource() == logCheckBox ) {
                if ( logCheckBox.isSelected() ) {
                    Common.debugPrintln( "改由logFrame來輸出資訊" );
                    Debug.commandDebugMode = false;
                }
                else {
                    Common.debugPrintln( "改由cmd來輸出資訊" );
                    Debug.commandDebugMode = true;
                }

                new Thread( new Runnable() {

                    public void run() {
                        SwingUtilities.invokeLater( new Runnable() {

                            public void run() {
                                ComicDownGUI.logFrame.setVisible( logCheckBox.isSelected() );
                            }
                        } );
                    }
                } ).start();
                SetUp.setOpenDebugMessageWindow( logCheckBox.isSelected() ); // 紀錄到設定值

            }


            //Common.debugPrintln( "getDownloadPicFile: " + SetUp.getDownloadPicFile() +
            //                     "\ngetOutputUrlFile: " + SetUp.getOutputUrlFile() );

            if ( event.getSource() == languageBox
                    && event.getStateChange() == ItemEvent.SELECTED ) {
                if ( languageBox.getSelectedIndex() == LanguageEnum.TRADITIONAL_CHINESE )
                    Common.debugPrintln( "介面文字設定為正體中文" );
                else if ( languageBox.getSelectedIndex() == LanguageEnum.SIMPLIFIED_CHINESE )
                    Common.debugPrintln( "介面文字設定為簡體中文" );
            }
            
            if ( event.getSource() == skinBox
                    && event.getStateChange() == ItemEvent.SELECTED ) {
                String nowSelectedSkin = skinBox.getSelectedItem().toString();

                if ( !SetUp.getSkinClassName().matches( ".*" + nowSelectedSkin + ".*" ) ) {

                    setSkin( skinBox.getSelectedIndex() );

                }
            }
        }

    }

    private JCheckBox getCheckBox( String string, boolean selected ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JCheckBox checkBox = new JCheckBox( string, selected );
        checkBox.setFont( SetUp.getDefaultFont() );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            checkBox.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            checkBox.setOpaque( false );
        }
        checkBox.addItemListener( new ItemHandler() );

        return checkBox;
    }

    private JCheckBox getCheckBoxBold( String string, boolean selected ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JCheckBox checkBox = new JCheckBox( string, selected );
        checkBox.setFont( SetUp.getDefaultBoldFont() );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            checkBox.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            checkBox.setOpaque( false );
        }
        checkBox.addItemListener( new ItemHandler() );

        return checkBox;
    }

    private JLabel getLabel( String string ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JLabel label = new JLabel( string );
        label.setFont( SetUp.getDefaultFont() );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            label.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            label.setOpaque( false );
        }

        return label;
    }

    private JLabel getLabel( String string, String toolTipString ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JLabel label = new JLabel( string );
        label.setFont( SetUp.getDefaultFont() );
        CommonGUI.setToolTip( label, toolTipString );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            label.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            label.setOpaque( false );
        }

        return label;
    }

    private JButton getButton( String string ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JButton button = new JButton( string );
        button.setFont( SetUp.getDefaultFont() );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            button.setOpaque( false );
            button.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            button.addMouseListener( this );
        }
        button.addActionListener( new ActionHandler() );

        return button;
    }

    private JButton getButton( String string, String picName ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JButton button = new JButton( string, new CommonGUI().getImageIcon( picName ) );
        button.setFont( SetUp.getDefaultFont() );
        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            button.setOpaque( false );
            button.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            button.addMouseListener( this );
        }

        return button;
    }

    private JTextField getTextField( String string ) {
        //string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JTextField textField = new JTextField( string, 20 );
        textField.setFont( SetUp.getDefaultFont( -1 ) );
        textField.setHorizontalAlignment( JTextField.LEADING );

        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            textField.setOpaque( false );
            textField.setForeground( SetUp.getOptionFrameOtherDefaultColor() );
            textField.addMouseListener( this );
        }
        return textField;
    }

    private JRadioButton getRadioButton( String string, boolean selected ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JRadioButton radioButton = new JRadioButton( string, selected );
        radioButton.setFont( SetUp.getDefaultFont() );
        
        if ( SetUp.getUsingBackgroundPicOfChoiceFrame() ) { // 若設定為透明，就用預定字體。
            radioButton.setOpaque( false );
            radioButton.setForeground( SetUp.getChoiceFrameOtherDefaultColor() );
            radioButton.addMouseListener( this );

        }
        radioButton.addItemListener( new ItemHandler() );

        if ( SetUp.getSkinClassName().matches( ".*napkin.*" ) ) {
            // 因為napkin的預設字型不太清楚，所以用選定字型
            radioButton.setFont( SetUp.getDefaultFont( - 2 ) );
        }

        return radioButton;
    }
}

class ComplexCellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent( JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus ) {
        Font theFont = null;
        Color theForeground = null;
        Icon theIcon = null;
        String theText = null;

        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent( list, value, index,
                isSelected, cellHasFocus );

        if ( value instanceof String ) {
            theText = (String) value;
        }
        else {
            theFont = list.getFont();
            theForeground = list.getForeground();
            theText = "";
        }

        if ( !isSelected ) {
            renderer.setForeground( theForeground );
        }
        if ( theIcon != null ) {
            renderer.setIcon( theIcon );
        }

        renderer.setText( theText );
        renderer.setFont( SetUp.getDefaultFont() );

        if ( SetUp.getUsingBackgroundPicOfOptionFrame() ) { // 若設定為透明，就用預定字體。
            renderer.setForeground( SetUp.getOptionFrameOtherDefaultColor() );

            if ( isSelected ) {
                renderer.setForeground( SetUp.getOptionFrameOtherMouseEnteredColor() );
            }
        }


        return renderer;
    }
}
