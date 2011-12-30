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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jcomicdownloader.module.Run;

/**
 *
 * 選項視窗
 */
public class OptionFrame extends JFrame {

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
    private JTextField dirTextField, recordDirTextField;
    static JTextField tempDirTextField;
    private JButton playSingleDoneAudioButton, playAllDoneAudioButton;
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
    private String defaultColor; // 預設的建議設定顏色
    public static JFrame optionFrame; // use by other frame
    private JFrame thisFrame; // use by self
    private JSlider retryTimesSlider, timeoutSlider;
    private JTabbedPane tabbedPane;
    private String tabLogoName = "tab_option.png";
    private JLabel viewPicFileLabel;
    private JTextField viewPicFileTextField;
    private JButton viewPicFileButton;
    private JLabel viewZipFileLabel;
    private JTextField viewZipFileTextField;
    private JButton viewZipFileButton;
    public static JLabel retryTimesLabel;
    public static JLabel timeoutLabel;

    /**
     *
     * @author user
     */
    public OptionFrame() {
        super( "選項設定" );
        OptionFrame.optionFrame = thisFrame = this; // for close the frame

        setUpUIComponent();

        setVisible( true );
    }

    private void setUpUIComponent() {
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

        setSize( 590, 380 );
        setResizable( true );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( Common.mainIcon ) );

        defaultColor = "black";

        JPanel wholePanel = new JPanel( new GridLayout( 0, 1, 5, 5 ) );
        setTabbedPane( wholePanel );

        contentPane.add( new CommonGUI().getFixedTansparentLabel(), BorderLayout.NORTH ); // 最上方留白
        contentPane.add( wholePanel, BorderLayout.CENTER ); // 設置主要頁面內容
        contentPane.add( getConfirmPanel(), BorderLayout.SOUTH ); // 設置確定按鈕

        setUpeListener();
    }

    // 設置最下方的確定按鈕
    private JPanel getConfirmPanel() {
        confirmButton = getButton( "   確定   " );
        confirmButton.addActionListener( new ActionHandler() );
        confirmButton.setToolTipText( "儲存目前的設定動作" );

        cencelButton = getButton( "   取消   " );
        cencelButton.addActionListener( new ActionHandler() );
        cencelButton.setToolTipText( "取消目前的設定動作" );

        defaultButton = getButton( "   還原預設值   " );
        defaultButton.addActionListener( new ActionHandler() );
        defaultButton.setToolTipText( "將所有的設定值還原回到原廠設定" );

        JPanel choicePanel = new JPanel( new GridLayout( 1, 2, 40, 40 ) );
        choicePanel.add( confirmButton );
        choicePanel.add( cencelButton );

        JPanel centerPanel = new CommonGUI().getCenterPanel( choicePanel, 10, 40 );

        return centerPanel;
    }

    private void setTabbedPane( JPanel panel ) {
        // 檔案相關、介面相關、連線相關、其他雜項

        tabbedPane = new JTabbedPane();

        JPanel fileTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 3, 1 ) ) );
        setFileTablePanel( fileTablePanel );

        tabbedPane.addTab( getTabeHtmlFontString( "檔案" ), null, fileTablePanel,
                "有關於檔案存放的設定" );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_1 );

        JPanel connectionTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setConnectionTablePanel( connectionTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "連線" ), null, connectionTablePanel,
                "有關於連線下載的設定" );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_2 );

        JPanel missionTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setMissionTablePanel( missionTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "任務" ), null,
                missionTablePanel, "有關於下載任務的設定" );
        tabbedPane.setMnemonicAt( 2, KeyEvent.VK_3 );

        JPanel interfaceTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setInterfaceTablePanel( interfaceTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "介面" ), null, interfaceTablePanel,
                "有關於視窗介面的設定" );
        tabbedPane.setMnemonicAt( 3, KeyEvent.VK_4 );

        JPanel viewTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setViewTablePanel( viewTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "瀏覽" ), null, viewTablePanel,
                "有關於開啟圖片或壓縮檔的設定" );
        tabbedPane.setMnemonicAt( 3, KeyEvent.VK_4 );

        JPanel audioTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setAudioTablePanel( audioTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "音效" ), null, audioTablePanel,
                "有關於開啟圖片或壓縮檔的設定" );
        tabbedPane.setMnemonicAt( 4, KeyEvent.VK_5 );

        JPanel otherTablePanel = new CommonGUI().getCenterPanel( new JPanel( new GridLayout( 1, 1 ) ) );
        setOtherTablePanel( otherTablePanel );
        tabbedPane.addTab( getTabeHtmlFontString( "其他" ), null, otherTablePanel,
                "有關於其他雜七雜八的設定" );
        tabbedPane.setMnemonicAt( 5, KeyEvent.VK_6 );


        panel.add( tabbedPane, BorderLayout.CENTER );
    }

    private String getTabeHtmlFontString( String tabName ) {
        int htmlFontSize = (int) (SetUp.getDefaultFontSize() / 4 + 1);
        String htmlFontFace = SetUp.getDefaultFontName();
        return "<html><font face=\"" + htmlFontFace + "\" size=\"" + htmlFontSize + "\"> " + tabName + "  </font></html>";
    }

    private void setAudioTablePanel( JPanel panel ) {

        singleDoneAudioCheckBox = getCheckBoxBold( "播放單一任務完成的音效", SetUp.getPlaySingleDoneAudio() );
        singleDoneAudioCheckBox.setToolTipText( "是否在單一任務下載完成後播放音效" );
        singleDoneAudioCheckBox.addItemListener( new ItemHandler() );

        singleDoneAudioTextField = new JTextField( SetUp.getSingleDoneAudioFile(), 20 );
        singleDoneAudioTextField.setFont( SetUp.getDefaultFont( -1 ) );
        singleDoneAudioTextField.setHorizontalAlignment( JTextField.LEADING );
        singleDoneAudioTextField.setToolTipText( "單一任務完成後所播放的音效檔" );

        JLabel playSingleDoneAudioLabel = new JLabel( new CommonGUI().getImageIcon( Common.playAudioPic ) );
        playSingleDoneAudioLabel.setToolTipText( "測試播放單一任務完成的音效" );
        playSingleDoneAudioLabel.addMouseListener( new java.awt.event.MouseAdapter() {

            public void mousePressed( java.awt.event.MouseEvent evt ) {
                Common.playSingleDoneAudio( singleDoneAudioTextField.getText() );
            }
        } );

        JPanel singleDoneAudioCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.LEADING, 1, 1 ) );
        singleDoneAudioCheckBoxPanel.add( singleDoneAudioCheckBox );
        singleDoneAudioCheckBoxPanel.add( playSingleDoneAudioLabel );

        singleDoneAudioButton = getButton( "外部音效" );
        singleDoneAudioButton.addActionListener( new ActionHandler() );
        singleDoneAudioButton.setToolTipText( "選擇單一任務完成後要播放的外部音效檔" );

        defaultSingleDoneAudioButton = getButton( "預設音效" );
        defaultSingleDoneAudioButton.addActionListener( new ActionHandler() );
        defaultSingleDoneAudioButton.setToolTipText( "使用預設單一任務完成後要播放的外部音效檔" );

        JPanel singleDoneAudioButtonPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        singleDoneAudioButtonPanelHorizontal.add( defaultSingleDoneAudioButton );
        singleDoneAudioButtonPanelHorizontal.add( singleDoneAudioButton );

        JPanel singleDoneAudioPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        singleDoneAudioPanelHorizontal.add( singleDoneAudioCheckBoxPanel );
        singleDoneAudioPanelHorizontal.add( singleDoneAudioButtonPanelHorizontal );


        allDoneAudioCheckBox = getCheckBoxBold( "播放全部任務完成的音效", SetUp.getPlayAllDoneAudio() );
        allDoneAudioCheckBox.setToolTipText( "是否在全部任務下載完成後播放音效" );
        allDoneAudioCheckBox.addItemListener( new ItemHandler() );

        allDoneAudioTextField = new JTextField( SetUp.getAllDoneAudioFile(), 25 );
        allDoneAudioTextField.setFont( SetUp.getDefaultFont( -1 ) );
        allDoneAudioTextField.setHorizontalAlignment( JTextField.LEADING );
        allDoneAudioTextField.setToolTipText( "全部任務完成後所播放的音效檔" );
        
        final JLabel playAllDoneAudioLabel = new JLabel( new CommonGUI().getImageIcon( Common.playAudioPic ) );
        playAllDoneAudioLabel.setToolTipText( "測試播放全部任務完成的音效" );
        playAllDoneAudioLabel.addMouseListener( new java.awt.event.MouseAdapter() {

            public void mousePressed( java.awt.event.MouseEvent evt ) {
                Common.playAllDoneAudio( allDoneAudioTextField.getText() );
            }
        } );
        
        JPanel allDoneAudioCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.LEADING, 1, 1 ) );
        allDoneAudioCheckBoxPanel.add( allDoneAudioCheckBox );
        allDoneAudioCheckBoxPanel.add( playAllDoneAudioLabel );

        defaultAllDoneAudioButton = getButton( "預設音效" );
        defaultAllDoneAudioButton.addActionListener( new ActionHandler() );
        defaultAllDoneAudioButton.setToolTipText( "使用預設全部任務完成後要播放的外部音效檔" );

        allDoneAudioButton = getButton( "外部音效" );
        allDoneAudioButton.addActionListener( new ActionHandler() );
        allDoneAudioButton.setToolTipText( "選擇全部任務完成後要播放的外部音效檔" );

        JPanel allDoneAudioButtonPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        allDoneAudioButtonPanelHorizontal.add( defaultAllDoneAudioButton );
        allDoneAudioButtonPanelHorizontal.add( allDoneAudioButton );

        JPanel allDoneAudioPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        allDoneAudioPanelHorizontal.add( allDoneAudioCheckBoxPanel );
        allDoneAudioPanelHorizontal.add( allDoneAudioButtonPanelHorizontal );


        JPanel otherPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        otherPanel.add( singleDoneAudioPanelHorizontal );
        otherPanel.add( singleDoneAudioTextField );
        otherPanel.add( allDoneAudioPanelHorizontal );
        otherPanel.add( allDoneAudioTextField );

        panel.add( otherPanel );
    }

    private void setOtherTablePanel( JPanel panel ) {

        tempDirLabel = getLabel( "目前暫存檔目錄：       " );

        tempDirTextField = new JTextField( SetUp.getTempDirectory(), 25 );
        tempDirTextField.setFont( SetUp.getDefaultFont( -1 ) );
        tempDirTextField.setHorizontalAlignment( JTextField.LEADING );
        tempDirTextField.setToolTipText( "暫存資料夾的存放位置，執行時會新增暫存資料夾，等程式關閉後便自動刪除" );

        tempDirButton = getButton( "選擇新目錄" );
        tempDirButton.addActionListener( new ActionHandler() );
        tempDirButton.setToolTipText( "選擇暫存資料夾的存放位置，執行時會新增暫存資料夾，等程式關閉後便自動刪除" );

        JPanel tempDirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        tempDirPanelHorizontal.add( tempDirLabel );
        tempDirPanelHorizontal.add( tempDirButton );


        recordDirLabel = getLabel( "目前記錄檔目錄：       " );

        recordDirTextField = new JTextField( SetUp.getRecordFileDirectory(), 25 );
        recordDirTextField.setFont( SetUp.getDefaultFont( -1 ) );
        recordDirTextField.setHorizontalAlignment( JTextField.LEADING );
        recordDirTextField.setToolTipText( "downloadList.dat、bookmarkList.dat和recordList.dat這三個記錄檔的存放位置" );

        recordDirButton = getButton( "選擇新目錄" );
        recordDirButton.addActionListener( new ActionHandler() );
        recordDirButton.setToolTipText( "選擇downloadList.dat、bookmarkList.dat和recordList.dat這三個記錄檔的存放位置" );

        JPanel recordDirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        recordDirPanelHorizontal.add( recordDirLabel );
        recordDirPanelHorizontal.add( recordDirButton );


        JPanel otherPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        otherPanel.add( recordDirPanelHorizontal );
        otherPanel.add( recordDirTextField );
        otherPanel.add( tempDirPanelHorizontal );
        otherPanel.add( tempDirTextField );

        panel.add( otherPanel );
    }

    private void setViewTablePanel( JPanel panel ) {
        viewPicFileLabel = getLabel( "預設開啟圖片的程式：       " );

        viewPicFileTextField = new JTextField( SetUp.getOpenPicFileProgram(), 25 );
        viewPicFileTextField.setFont( SetUp.getDefaultFont( -1 ) );
        viewPicFileTextField.setHorizontalAlignment( JTextField.LEADING );

        viewPicFileButton = getButton( "選擇新程式" );
        viewPicFileButton.addActionListener( new ActionHandler() );
        viewPicFileButton.setToolTipText( "選擇可以開啟圖片的瀏覽程式，最好也能支援直接開啟壓縮檔" );

        JPanel viewPicFIlePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        viewPicFIlePanelHorizontal.add( viewPicFileLabel );
        viewPicFIlePanelHorizontal.add( viewPicFileButton );


        viewZipFileLabel = getLabel( "預設開啟壓縮檔的程式：       " );

        viewZipFileTextField = new JTextField( SetUp.getOpenZipFileProgram(), 25 );
        viewZipFileTextField.setFont( SetUp.getDefaultFont( -1 ) );
        viewZipFileTextField.setHorizontalAlignment( JTextField.LEADING );

        viewZipFileButton = getButton( "選擇新程式" );
        viewZipFileButton.addActionListener( new ActionHandler() );

        JPanel viewZipFilePanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        viewZipFilePanelHorizontal.add( viewZipFileLabel );
        viewZipFilePanelHorizontal.add( viewZipFileButton );


        JPanel viewPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        viewPanel.add( viewPicFIlePanelHorizontal );
        viewPanel.add( viewPicFileTextField );
        //viewPanel.add( viewZipFilePanelHorizontal );
        //viewPanel.add( viewZipFileTextField );

        panel.add( viewPanel );
    }

    private void setFileTablePanel( JPanel panel ) {
        dirLabel = getLabel( "目前下載目錄：       " );

        dirTextField = new JTextField( SetUp.getOriginalDownloadDirectory(), 25 );
        dirTextField.setFont( SetUp.getDefaultFont( -1 ) );
        dirTextField.setHorizontalAlignment( JTextField.LEADING );

        dirButton = getButton( "選擇新目錄" );
        dirButton.addActionListener( new ActionHandler() );

        JPanel dirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        dirPanelHorizontal.add( dirLabel );
        dirPanelHorizontal.add( dirButton );

        compressCheckBox = getCheckBoxBold( "自動產生壓縮檔", SetUp.getAutoCompress() );
        compressCheckBox.addItemListener( new ItemHandler() );
        compressCheckBox.setToolTipText( "下載完成後進行壓縮，壓縮檔名與資料夾名稱相同" );

        deleteCheckBox = getCheckBox( "自動刪除圖片檔", SetUp.getDeleteOriginalPic() );
        deleteCheckBox.addItemListener( new ItemHandler() );
        deleteCheckBox.setToolTipText( "下載完成後便刪除圖檔，此選項應與『自動產生壓縮檔』搭配使用" );

        urlCheckBox = getCheckBox( "輸出下載位址文件檔", SetUp.getOutputUrlFile() );
        urlCheckBox.addItemListener( new ItemHandler() );
        urlCheckBox.setToolTipText( "解析所有圖片的真實下載位址後彙整輸出為txt文件檔，檔名與資料夾名稱相同" );

        downloadCheckBox = getCheckBoxBold( "分析後下載圖檔（預設）", SetUp.getDownloadPicFile() );
        downloadCheckBox.addItemListener( new ItemHandler() );
        downloadCheckBox.setToolTipText( "如果沒有勾選就不會有下載行為，建議要勾選（但若只想輸出真實下載位址，就不要勾選此選項）" );


        JPanel filePanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        filePanel.add( dirPanelHorizontal );
        filePanel.add( dirTextField );
        filePanel.add( compressCheckBox );
        filePanel.add( deleteCheckBox );
        filePanel.add( urlCheckBox );
        filePanel.add( downloadCheckBox );

        panel.add( filePanel );
    }

    private void setConnectionTablePanel( JPanel panel ) {
        JLabel proxyServerLabel = getLabel( "設定代理伺服器位址：", "若是中華電信用戶，可輸入proxy.hinet.net" );
        proxyServerTextField = new JTextField( SetUp.getProxyServer(), 22 );
        proxyServerTextField.setFont( SetUp.getDefaultFont( -1 ) );
        proxyServerTextField.setHorizontalAlignment( JTextField.LEADING );
        proxyServerTextField.setToolTipText( "若是中華電信用戶，可輸入proxy.hinet.net" );

        JPanel proxyServerPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        proxyServerPanel.add( proxyServerLabel );
        proxyServerPanel.add( proxyServerTextField );

        JLabel proxyPortLabel = getLabel( "設定代理伺服器連接阜：", "若是中華電信用戶，可輸入80" );
        proxyPortTextField = new JTextField( SetUp.getProxyPort(), 4 );
        proxyPortTextField.setFont( SetUp.getDefaultFont( -1 ) );
        proxyPortTextField.setHorizontalAlignment( JTextField.LEADING );
        proxyPortTextField.setToolTipText( "若是中華電信用戶，可輸入80" );

        JPanel proxyPortPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        proxyPortPanel.add( proxyPortLabel );
        proxyPortPanel.add( proxyPortTextField );


        retryTimesSlider = new JSlider( JSlider.HORIZONTAL, 0, 5, 1 );
        retryTimesSlider.addChangeListener( new SliderHandler() );
        retryTimesSlider.setMajorTickSpacing( 1 );
        //retryTimesSlider.setPaintTicks(true);
        retryTimesSlider.setPaintLabels( true );
        retryTimesSlider.setValue( SetUp.getRetryTimes() );
        retryTimesSlider.setToolTipText( "通常下載失敗是伺服器異常或網路速度過慢所致，立即重試的成功機率其實不高" );
        retryTimesLabel = getLabel( "下載失敗重試次數：" + retryTimesSlider.getValue() + "次" );
        retryTimesLabel.setToolTipText( "通常下載失敗是伺服器異常或網路速度過慢所致，立即重試的成功機率其實不高" );

        JPanel retryTimesPortPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        retryTimesPortPanel.add( retryTimesLabel );
        retryTimesPortPanel.add( retryTimesSlider );

        timeoutSlider = new JSlider( JSlider.HORIZONTAL, 0, 100, 10 );
        timeoutSlider.addChangeListener( new SliderHandler() );
        timeoutSlider.setMajorTickSpacing( 20 );
        //timeoutSlider.setPaintTicks(true);
        timeoutSlider.setPaintLabels( true );
        timeoutSlider.setValue( SetUp.getTimeoutTimer() );
        timeoutSlider.setToolTipText( "超過此時間會中斷此連線，直接下載下一個檔案，建議只在下載GOOGLE圖片時使用，其他時候建議設為0，代表沒有逾時限制" );
        timeoutLabel = getLabel( "連線逾時時間：" + timeoutSlider.getValue() + "秒" );
        timeoutLabel.setToolTipText( "超過此時間會中斷此連線，直接下載下一個檔案，建議只在下載GOOGLE圖片時使用，其他時候建議設為0，代表沒有逾時限制" );


        JPanel timeoutPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        timeoutPanel.add( timeoutLabel );
        timeoutPanel.add( timeoutSlider );


        JPanel connectionPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        connectionPanel.add( proxyServerPanel );
        connectionPanel.add( proxyPortPanel );
        connectionPanel.add( retryTimesPortPanel );
        connectionPanel.add( timeoutPanel );

        panel.add( connectionPanel );
    }

    private void setInterfaceTablePanel( JPanel panel ) {
        JLabel chooseFontLabel = getLabel( "目前字型：" + SetUp.getDefaultFontName() + SetUp.getDefaultFontSize() );
        chooseFontButton = getButton( "選擇新字型" );
        chooseFontButton.addActionListener( new ActionHandler() );
        chooseFontLabel.setToolTipText( "選定字型後需關閉重啟才能看到新設定的字型" );
        chooseFontButton.setToolTipText( "選定字型後需關閉重啟才能看到新設定的字型" );

        JPanel chooseFontPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        chooseFontPanel.add( chooseFontLabel );
        chooseFontPanel.add( chooseFontButton );

        skinClassNames = new CommonGUI().getClassNames(); // 取得所有介面類別名稱
        skinStrings = new CommonGUI().getSkinStrings(); // 取得所有介面名稱
        skinBox = new JComboBox( skinStrings );
        ListCellRenderer renderer = new ComplexCellRenderer();
        skinBox.setRenderer( renderer );
        skinBox.setSelectedIndex( getSkinIndex( SetUp.getSkinClassName() ) );

        skinLabel = getLabel( "選擇介面：" );

        skinBox.addItemListener( new ItemHandler() ); // change skin if change skinBox
        skinBox.setToolTipText( "可選擇您喜好的介面風格" );

        // the order: skinLabel skinBox
        JPanel skinPanel = new JPanel();
        skinPanel.setLayout( new GridLayout( 1, 2, 5, 5 ) );
        skinPanel.add( skinLabel );
        skinPanel.add( skinBox );

        trayMessageCheckBox = getCheckBoxBold( "縮小到系統列時顯示下載完成訊息", SetUp.getShowDoneMessageAtSystemTray() );
        trayMessageCheckBox.addItemListener( new ItemHandler() );
        trayMessageCheckBox.setToolTipText( "如果沒有勾選，縮小到系統列後就不會再有下載完畢的提示訊息" );

        logCheckBox = getCheckBox( "開啟除錯訊息視窗", SetUp.getOpenDebugMessageWindow() );
        logCheckBox.addItemListener( new ItemHandler() );
        logCheckBox.setToolTipText( "開啟後可檢視更詳細的程式運作細節與例外錯誤訊息" );

        JPanel interfacePanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        interfacePanel.add( chooseFontPanel );
        interfacePanel.add( skinPanel );
        interfacePanel.add( trayMessageCheckBox );
        interfacePanel.add( logCheckBox );

        panel.add( interfacePanel );
    }

    private void setMissionTablePanel( JPanel panel ) {
        keepUndoneCheckBox = getCheckBoxBold( "保留未完成任務", SetUp.getKeepUndoneDownloadMission() );
        keepUndoneCheckBox.addItemListener( new ItemHandler() );
        keepUndoneCheckBox.setToolTipText( "這次沒下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepDoneCheckBox = getCheckBox( "保留已完成任務", SetUp.getKeepDoneDownloadMission() );
        keepDoneCheckBox.addItemListener( new ItemHandler() );
        keepDoneCheckBox.setToolTipText( "這次已經下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepRecordCheckBox = getCheckBoxBold( "保留任務記錄", SetUp.getKeepRecord() );
        keepRecordCheckBox.addItemListener( new ItemHandler() );
        keepRecordCheckBox.setToolTipText( "若紀錄過多而影響效能，請取消勾選或刪除recordList.dat" );

        choiceAllVolumeCheckBox = getCheckBox( "預設勾選全部集數", SetUp.getChoiceAllVolume() );
        choiceAllVolumeCheckBox.addItemListener( new ItemHandler() );
        choiceAllVolumeCheckBox.setToolTipText( "本來預設都不勾選（除了單集），但若勾選此選項，便會全部勾選" );

        JPanel missionPanel = new JPanel( new GridLayout( 6, 1, 2, 2 ) );
        missionPanel.add( keepUndoneCheckBox );
        missionPanel.add( keepDoneCheckBox );
        missionPanel.add( keepRecordCheckBox );
        missionPanel.add( choiceAllVolumeCheckBox );

        panel.add( missionPanel );
    }

    private int getSkinIndex( String skinClassName ) {
        int index = 0;
        for ( String skinName : skinStrings ) {
            if ( skinClassName.matches( ".*" + skinName + ".*" ) ) {
                break;
            }
            index++;
        }

        if ( skinStrings.length > index ) {
            return index;
        } else {
            return 0;
        }
    }

    private void changeSkin( int value ) {
        // 檢查是否選擇的是jtattoo的介面

        boolean continueChange = true;
        String className = skinClassNames[value];
        SetUp.setSkinClassName( className ); // 紀錄到設定值

        if ( className.matches( "com.jtattoo.plaf.*" ) ) {
            if ( !new File( Common.getNowAbsolutePath() + "JTattoo.jar" ).exists() ) {
                continueChange = false; // 不繼續改變介面了
                new CommonGUI().downloadJTattoo(); // 下載JTattoo.jar
            } else {
                try {
                    CommonGUI.setLookAndFeelByClassName( className );
                } catch ( Exception ex ) {
                    Common.debugPrintln( "無法使用" + className + "介面!!" );

                    className = ComicDownGUI.defaultSkinClassName; // 回歸預設介面

                }
            }
        }

        //className = "org.jvnet.substance.skin.SubstanceModerateLookAndFeel";

        if ( continueChange ) {
            CommonGUI.setLookAndFeelByClassName( className );
            ComicDownGUI.setDefaultSkinClassName( className );

            // change the skin of Option frame
            SwingUtilities.updateComponentTreeUI( this );

            // change the skin of main frame
            SwingUtilities.updateComponentTreeUI( ComicDownGUI.mainFrame );


            if ( InformationFrame.thisFrame != null ) // change the skin of information frame
            {
                SwingUtilities.updateComponentTreeUI( InformationFrame.thisFrame );
            }

            if ( ChoiceFrame.choiceFrame != null ) // change the skin of information frame
            {
                SwingUtilities.updateComponentTreeUI( ChoiceFrame.choiceFrame );
            }

            if ( LogFrame.logFrame != null ) // change the skin of information frame
            {
                SwingUtilities.updateComponentTreeUI( LogFrame.logFrame );
            }

            Common.debugPrintln( "改為" + className + "面板" );

            Common.debugPrintln( "目前面板名稱: " + UIManager.getLookAndFeel().getName() );
        }
    }

    private void chooseFile( final int type, final JTextField textField, final String directoryString ) {
        chooseFile( type, textField, directoryString, null );
    }

    private void chooseFile( final int type, final JTextField textField, final String directoryString, final javax.swing.filechooser.FileFilter fileFilter ) {
        final Component tempThisComponent = this;

        new Thread( new Runnable() {

            public void run() {

                JFileChooser dirChooser = new JFileChooser( directoryString );
                dirChooser.setFileSelectionMode( type );

                if ( fileFilter != null ) {
                    dirChooser.addChoosableFileFilter( fileFilter );
                    dirChooser.setAcceptAllFileFilterUsed( false );
                }

                dirChooser.setDialogTitle( "請選擇新的下載目錄" );

                try {
                    int result = dirChooser.showDialog( tempThisComponent, "確定" );

                    if ( result == JFileChooser.APPROVE_OPTION ) {
                        File file = dirChooser.getSelectedFile();

                        String path = "";

                        if ( file.getPath().matches( "(?s).*" + Common.getRegexSlash() )
                                || type == JFileChooser.FILES_ONLY ) { // 若是檔案就不須在最後加斜線
                            path = file.getPath();
                        } else {
                            path = file.getPath() + Common.getSlash();
                        }

                        if ( textField == OptionFrame.tempDirTextField ) {
                            // 因為暫存資料夾在程式關閉後會刪除，所以還是另開新資料夾會比較安全
                            path += "temp" + Common.getSlash();
                        }

                        textField.setText( path );

                    }
                } catch ( HeadlessException ex ) {
                    ex.printStackTrace();
                }

            }
        } ).start();
    }

    private void setUpeListener() {
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    // -------------  Listener  ---------------
    private class ActionHandler implements ActionListener {

        public void actionPerformed( ActionEvent event ) {
            if ( event.getSource() == chooseFontButton ) {
                // 選擇字型和大小

                JFontChooser fontChooser = new JFontChooser();
                Font font = fontChooser.showDialog( thisFrame, "選擇字型" );

                if ( font != null ) {
                    SetUp.setDefaultFontName( font.getName() );
                    SetUp.setDefaultFontSize( font.getSize() );
                    //SwingUtilities.updateComponentTreeUI( fontChooser );
                }

                if ( font != null ) {
                    JOptionPane.showMessageDialog( thisFrame, "你選擇的字型是" + font.getName() + "　"
                            + "大小為" + font.getSize() + "（需重新開啟才會啟用新設定）" );
                }
            }
            if ( event.getSource() == dirButton ) {
                chooseFile( JFileChooser.DIRECTORIES_ONLY, dirTextField, SetUp.getOriginalDownloadDirectory() );
            } else if ( event.getSource() == tempDirButton ) {
                int endIndex = SetUp.getTempDirectory().lastIndexOf( Common.getSlash() + "temp" ) + 1;
                String path = SetUp.getTempDirectory().substring( 0, endIndex );

                chooseFile( JFileChooser.DIRECTORIES_ONLY, tempDirTextField, path );
            } else if ( event.getSource() == recordDirButton ) {
                chooseFile( JFileChooser.DIRECTORIES_ONLY, recordDirTextField, SetUp.getRecordFileDirectory() );
            } else if ( event.getSource() == viewPicFileButton ) {
                chooseFile( JFileChooser.FILES_ONLY, viewPicFileTextField, SetUp.getOpenPicFileProgram() );
            } else if ( event.getSource() == viewZipFileButton ) {
                chooseFile( JFileChooser.FILES_ONLY, viewZipFileTextField, SetUp.getOpenZipFileProgram() );
            } else if ( event.getSource() == singleDoneAudioButton ) {
                chooseFile( JFileChooser.FILES_ONLY, singleDoneAudioTextField,
                        SetUp.getSingleDoneAudioFile(), new AudioFileFilter() );
            } else if ( event.getSource() == allDoneAudioButton ) {
                chooseFile( JFileChooser.FILES_ONLY, allDoneAudioTextField,
                        SetUp.getAllDoneAudioFile(), new AudioFileFilter() );
            } else if ( event.getSource() == defaultSingleDoneAudioButton ) {
                singleDoneAudioTextField.setText( Common.defaultSingleDoneAudio );
            } else if ( event.getSource() == defaultAllDoneAudioButton ) {
                allDoneAudioTextField.setText( Common.defaultAllDoneAudio );
            }

            if ( event.getSource() == confirmButton ) {
                SetUp.setProxyServer( proxyServerTextField.getText() );
                SetUp.setProxyPort( proxyPortTextField.getText() );

                SetUp.setRetryTimes( retryTimesSlider.getValue() ); // 設定重新嘗試次數
                SetUp.setTimeoutTimer( timeoutSlider.getValue() ); // 設定逾時時間

                SetUp.setOriginalDownloadDirectory( dirTextField.getText() ); // 紀錄到設定值
                SetUp.setTempDirectory( tempDirTextField.getText() ); // 紀錄到設定值

                SetUp.setOpenPicFileProgram( viewPicFileTextField.getText() ); // 紀錄到設定值
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


                if ( proxyServerTextField.getText() != null
                        && !proxyServerTextField.getText().equals( "" )
                        && proxyPortTextField.getText() != null
                        && !proxyPortTextField.getText().equals( "" ) ) {
                    Common.setHttpProxy( SetUp.getProxyServer(), SetUp.getProxyPort() );
                    Common.debugPrintln( "設定代理伺服器："
                            + SetUp.getProxyServer() + " "
                            + SetUp.getProxyPort() );
                } else {
                    Common.closeHttpProxy();
                    Common.debugPrintln( "代理伺服器資訊欠缺位址或連接阜，因此不加入" );
                }
                SetUp.writeSetFile(); // 將目前的設定存入設定檔（set.ini）
                thisFrame.dispose();
            } else if ( event.getSource() == cencelButton ) {
                thisFrame.dispose();
            } else if ( event.getSource() == defaultButton ) {
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
            if ( event.getSource() == deleteCheckBox ) {
                if ( deleteCheckBox.isSelected() ) {
                    compressCheckBox.setSelected( true ); // 勾選自動刪除就要連帶勾選自動壓縮
                }
            }
            if ( event.getSource() == compressCheckBox ) {
                if ( !compressCheckBox.isSelected() ) {
                    deleteCheckBox.setSelected( false ); // 勾選自動刪除就要連帶勾選自動壓縮
                }
            }
            
            if ( event.getSource() == downloadCheckBox ) {
                if ( !downloadCheckBox.isSelected() ) {
                    String message = "取消後就不會進行下載，確定取消？";
                    int choice = JOptionPane.showConfirmDialog( thisFrame, message, "提醒訊息", JOptionPane.YES_NO_OPTION );
                    if ( choice == JOptionPane.NO_OPTION ) { // agree to remove the title in the download list
                        downloadCheckBox.setSelected( true );
                    }
                    else
                        downloadCheckBox.setSelected( false );
                }
            }
            

            if ( event.getSource() == logCheckBox ) {
                if ( logCheckBox.isSelected() ) {
                    Common.debugPrintln( "改由logFrame來輸出資訊" );
                    Debug.commandDebugMode = false;
                } else {
                    Common.debugPrintln( "改由cmd來輸出資訊" );
                    Debug.commandDebugMode = true;
                }

                new Thread( new Runnable() {

                    public void run() {
                        ComicDownGUI.logFrame.setVisible( logCheckBox.isSelected() );
                    }
                } ).start();
                SetUp.setOpenDebugMessageWindow( logCheckBox.isSelected() ); // 紀錄到設定值

            }


            //Common.debugPrintln( "getDownloadPicFile: " + SetUp.getDownloadPicFile() +
            //                     "\ngetOutputUrlFile: " + SetUp.getOutputUrlFile() );

            if ( event.getStateChange() == ItemEvent.SELECTED ) {
                String nowSelectedSkin = skinBox.getSelectedItem().toString();

                if ( !SetUp.getSkinClassName().matches( ".*" + nowSelectedSkin + ".*" ) ) {
                    changeSkin( skinBox.getSelectedIndex() );
                }
            }
        }
    }

    private JCheckBox getCheckBox( String string, boolean selected ) {
        JCheckBox checkBox = new JCheckBox( string, selected );
        checkBox.setFont( SetUp.getDefaultFont() );

        return checkBox;
    }

    private JCheckBox getCheckBoxBold( String string, boolean selected ) {
        JCheckBox checkBox = new JCheckBox( string, selected );
        checkBox.setFont( SetUp.getDefaultBoldFont() );

        return checkBox;
    }

    private JLabel getLabel( String string ) {
        JLabel label = new JLabel( string );
        label.setFont( SetUp.getDefaultFont() );

        return label;
    }

    private JLabel getLabel( String string, String toolTipString ) {
        JLabel label = new JLabel( string );
        label.setFont( SetUp.getDefaultFont() );
        label.setToolTipText( toolTipString );

        return label;
    }

    private JButton getButton( String string ) {
        JButton button = new JButton( string );
        button.setFont( SetUp.getDefaultFont() );

        return button;
    }

    private JButton getButton( String string, String picName ) {
        JButton button = new JButton( string, new CommonGUI().getImageIcon( picName ) );
        button.setFont( SetUp.getDefaultFont() );

        return button;
    }
}

// 用於改變下拉式介面選單的渲染機制
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
        } else {
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
        return renderer;
    }
}
