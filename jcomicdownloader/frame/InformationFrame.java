/*
 * InformationFrame.java
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/10/29
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 2.13: 1. 修改最新版本下載按鈕，使其按下去可以直接下載最新版本。
 2.01: 1. 增加支援網站列表的資訊。
 1.14: 1. 修改文字顯示方式，使用setFont而不使用html語法，避免在某些情況下出現亂碼。
 2. 修復official.html無法刪除的bug。 
 1.08: 修復在讀取最新版本資訊之前，無法點擊按鈕的bug

 ----------------------------------------------------------------------------------------------------

 */
package jcomicdownloader.frame;

import jcomicdownloader.tools.*;
import jcomicdownloader.module.*;
import jcomicdownloader.*;

/**

 顯示訊息視窗
 */
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

public class InformationFrame extends JFrame implements ActionListener, MouseListener {

    private JPanel informationPanel;
    private JLabel informationLabel;
    private JLabel versionLabel, dateLabel;
    private JLabel supportedSiteLabel;
    private String resourceFolder;
    private JButton versionButton;
    private JButton downloadButton;
    private String officialName;
    private String officialURL;
    public static JFrame thisFrame; // for change look and feel
    private static boolean downloadLock = false; // 用來檢查是否已取得最新版本資訊，之後才可以下載最新版本
    private Dimension frameDimension;

    /**

     @author user
     */
    public InformationFrame() {
        super( "關於本程式" );

        thisFrame = this; // for change look and feel
        resourceFolder = "resource/";
        officialName = "official.html";
        officialURL = "https://sites.google.com/site/jcomicdownloader/";

        setUpUIComponent();
        setUpeListener();
        setVisible( true );
        
        deleteOfficialHtml(); // 刪除官方網頁檔案

        setNewestVersion(); // 檢查是否有新版本

    }

    private void setUpUIComponent() {
        String picFileString = SetUp.getBackgroundPicPathOfInformationFrame();
        // 檢查背景圖片是否存在
        if ( SetUp.getUsingBackgroundPicOfInformationFrame() && 
            !new File( picFileString ).exists() ) {
            CommonGUI.showMessageDialog( this, picFileString
                + "\n背景圖片不存在，重新設定為原始佈景",
                "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            SetUp.setUsingBackgroundPicOfInformationFrame( false );
        }
        
        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) {
            
            frameDimension = CommonGUI.getDimension( picFileString );
            //setSize( frameDimension );
            int width = ( int ) frameDimension.getWidth() + CommonGUI.widthGapOfBackgroundPic;
            int height = ( int ) frameDimension.getHeight() + CommonGUI.heightGapOfBackgroundPic;
            setSize( width, height );
            setResizable( false );
        }
        else {
            setSize( 470, 640 );
            setResizable( true );
        }

        setDefaultLookAndFeelDecorated( false ); // 讓標題欄可以隨look and feel改變
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) );

        Container contentPane;
        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) {
            ( ( JPanel ) getContentPane() ).setOpaque( false );
            contentPane = CommonGUI.getImagePanel( picFileString );
            contentPane.setPreferredSize( frameDimension );
            getContentPane().add( contentPane, BorderLayout.CENTER );
        }
        else {
            contentPane = getContentPane();
        }

        setTextLayout( contentPane );
    }

    private void setTextLayout( Container contentPane ) {
        //textPanel = new JPanel( new BorderLayout() );

        String informationText = "";
        informationLabel = new JLabel( informationText );

        JLabel informLabel = getLabel( "目前最新版本: " );
        versionLabel = getLabel( "偵測中..." );
        dateLabel = getLabel( "" );

        JLabel informLabel2 = getLabel( "目前總共支援: " );
        supportedSiteLabel = getLabel( "偵測中..." );

        JPanel versionPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        versionPanel.add( informLabel );
        versionPanel.add( versionLabel );
        versionPanel.add( dateLabel );
        versionPanel.setOpaque( !SetUp.getUsingBackgroundPicOfInformationFrame() );

        JPanel supportedSitePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        supportedSitePanel.add( informLabel2 );
        supportedSitePanel.add( supportedSiteLabel );
        supportedSitePanel.setOpaque( !SetUp.getUsingBackgroundPicOfInformationFrame() );

        JPanel updatePanel = new JPanel( new GridLayout( 2, 1, 0, 0 ) );
        updatePanel.add( versionPanel );
        updatePanel.add( supportedSitePanel );
        updatePanel.setOpaque( !SetUp.getUsingBackgroundPicOfInformationFrame() );

        JButton supportedSiteButton = getButton( " 支援網站列表", "information_supportedSite.png",
            "https://sites.google.com/site/jcomicdownloader/home" );

        downloadButton = getButton( " 最新版本下載", "information_download.png",
            null );
        downloadButton.addActionListener( this );

        JButton teachingButton = getButton( " 線上使用教學", "information_manual.png",
            "https://sites.google.com/site/jcomicdownloader/step-by-step" );
        JButton searchButton = getButton( " 漫畫搜尋引擎", "information_search.png",
            "http://www.google.com/cse/home?cx=002948535609514911011:ls5mhwb6sqa&hl=zh-TW" );
        JButton messageButton = getButton( " 疑難問題回報", "information_report.png",
            "http://jcomicdownloader.blogspot.com/2012/01/bug-report-1.html" );

        JLabel authorLabel = getLabel( "作者：surveyorK （abc9070410@gmail.com）" );

        informationPanel = new JPanel( new GridLayout( 7, 1, 5, 5 ) );
        //informationPanel.add( versionPanel );
        informationPanel.add( updatePanel );
        informationPanel.add( downloadButton );
        informationPanel.add( supportedSiteButton );
        informationPanel.add( teachingButton );
        informationPanel.add( searchButton );
        informationPanel.add( messageButton );
        informationPanel.add( authorLabel );
        informationPanel.setOpaque( !SetUp.getUsingBackgroundPicOfInformationFrame() );

        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) {
            informationPanel.setPreferredSize( frameDimension );
            contentPane.add( new CommonGUI().getCenterPanel( informationPanel ), BorderLayout.CENTER );
        }
        else {
            JScrollPane informationScrollPane = new JScrollPane( new CommonGUI().getCenterPanel( informationPanel ) );

            contentPane.add( informationScrollPane, BorderLayout.CENTER );
        }

    }

    private String getHtmlString( String str ) {
        return "<html><font size=\"5\">" + str + "</font></html>";
    }

    private String getHtmlStringWithColor( String str, String colorName ) {
        return "<font color=\"" + colorName + "\" size=\"5\">" + str + "</font>";
    }

    // 直接抓取官網網頁
    private void downloadOfficialHtml() {
        Run.isAlive = true;
        Common.downloadFile( officialURL, SetUp.getTempDirectory(), officialName, false, "" );
    }

    // 刪除官方網頁
    private void deleteOfficialHtml() {
        File file = new File( SetUp.getTempDirectory() + officialName );
        if ( file.exists() ) {
            file.delete();
        }
    }

    // 回傳最新版本的字串
    private String getUpdateVersionString() {
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), officialName );

        // 先找出最新的是第幾號版本
        int endIndex = allPageString.indexOf( "版發佈" );
        int beginIndex = allPageString.substring( 0, endIndex ).lastIndexOf( ">" ) + 1;
        String versionString = allPageString.substring( beginIndex, endIndex );

        return Common.getStringUsingDefaultLanguage( versionString );
    }

    // 回傳更新日期的字串
    private String getUpdateDateString() {
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), officialName );

        // 再找出發佈最新版本的日期
        int tempIndex = allPageString.indexOf( "countdown-fromdateutc" );
        String[] tokens = allPageString.substring( tempIndex, allPageString.length() ).split( "-|\"" );
        String dateString = "（" + tokens[2] + "年" + tokens[3] + "月" + tokens[4] + "日發佈）";

        return Common.getStringUsingDefaultLanguage( dateString ); // 使用預設語言 dateString;
    }

    // 回傳目前已經支援網站數目的字串
    private String getUpdateSupportedSiteString() {
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), officialName );

        // 找出目前支援列表數目
        int supportedSiteAmount = allPageString.split( "<td style=" ).length / 2 + 1;

        String supportedSiteString = supportedSiteAmount + " 個網站";

        return Common.getStringUsingDefaultLanguage( supportedSiteString );
    }

    public void setNewestVersion() {
        Thread versionThread = new Thread( new Runnable() {

            public void run() {

                // 取得介面設定值（不用UIManager.getLookAndFeel().getName()是因為這樣才能讀到_之後的參數）
                String nowSkinName = SetUp.getSkinClassName();
                downloadOfficialHtml(); // 下載官方網頁
                versionLabel.setText( getUpdateVersionString() ); // 從官方網頁提取更新版本資訊
                synchronized ( InformationFrame.thisFrame ) { // lock main frame
                    InformationFrame.thisFrame.notifyAll();
                    InformationFrame.downloadLock = false;
                }

                dateLabel.setText( getUpdateDateString() ); // 從官方網頁提取更新日期資訊

                supportedSiteLabel.setText( getUpdateSupportedSiteString() ); // 從官方網頁提取支援網站資訊
                repaint();
                
                if ( !CommonGUI.isDarkSytleSkin( nowSkinName ) ) {
                    versionLabel.setForeground( Color.RED );
                    dateLabel.setForeground( Color.BLUE );
                    supportedSiteLabel.setForeground( Color.DARK_GRAY );
                }

                if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) { // 若設定為透明，就用白色字體。
                    versionLabel.setForeground( SetUp.getInformationFrameOtherDefaultColor() );
                    dateLabel.setForeground( SetUp.getInformationFrameOtherDefaultColor() );
                    supportedSiteLabel.setForeground( SetUp.getInformationFrameOtherDefaultColor() );
                }

                //Thread.currentThread().interrupt();

                deleteOfficialHtml(); // 刪除官方網頁檔案

            }
        } );
        versionThread.start();
    }

    private void setUpeListener() {
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    }

    public static void main( String[] args ) {
        InformationFrame frame = new InformationFrame();
    }

    private JLabel getLabel( String string ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JLabel label = new JLabel( string );
        label.setFont( SetUp.getDefaultFont() );

        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) { // 若設定為透明，就用白色字體。
            label.setForeground( SetUp.getInformationFrameOtherDefaultColor() );
        }

        label.setOpaque( !SetUp.getUsingBackgroundPicOfInformationFrame() );

        return label;
    }

    private JButton getButton( String string, String picName, final String urlString ) {
        string = Common.getStringUsingDefaultLanguage( string ); // 使用預設語言 
        
        JButton button = new JButton( string, new CommonGUI().getImageIcon( picName ) );
        button.setFont( SetUp.getDefaultFont( 3 ) );
        if ( urlString != null ) {
            button.addActionListener( new ActionListener() {

                public void actionPerformed( ActionEvent e ) {
                    new RunBrowser().runBroswer( urlString );
                }
            } );
        }

        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) { // 若設定為透明，就用預定字體。
            button.setForeground( SetUp.getInformationFrameOtherDefaultColor() );
            button.setOpaque( false );
            button.addMouseListener( this );
        }



        return button;
    }

    // 下載最新版本的JComicDownloader
    private void downloadLastestVersion() {
        new Thread( new Runnable() {

            public void run() {
                SwingUtilities.invokeLater( new Runnable() {

                    public void run() {

                        Common.debugPrint( "檢查是否已取得最新版本資訊：" );

                        if ( versionLabel.getText().matches( ".*偵測中.*" ) ) {
                            InformationFrame.downloadLock = true;
                            Common.debugPrint( "Not yet..." );
                        }
                        else {
                            InformationFrame.downloadLock = false;
                            Common.debugPrintln( "OK" );
                        }

                        synchronized ( InformationFrame.thisFrame ) { // lock main frame
                            while ( InformationFrame.downloadLock ) {
                                try {
                                    InformationFrame.thisFrame.wait();
                                }
                                catch ( InterruptedException ex ) {
                                    ex.printStackTrace();
                                }
                            }
                            Common.debugPrintln( "OK" );
                        }

                        String fileName = "JComicDownloader_" + versionLabel.getText() + ".jar";

                        if ( ComicDownGUI.versionString.matches( ".*" + versionLabel.getText() + ".*" ) ) {
                            CommonGUI.showMessageDialog( thisFrame, "目前程式已是最新版本！",
                                "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                        }
                        else if ( new File( Common.getNowAbsolutePath() + fileName ).exists() ) {
                            CommonGUI.showMessageDialog( thisFrame, "最新版本已存在於程式資料夾！",
                                "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                        }
                        else {
                            Common.debugPrintln( "開始下載最新版本" );

                            String frontURL = "https://sites.google.com/site/jcomicdownloader/release/";
                            String backURL = "?attredirects=0&amp;d=1";
                            String lastestVersionURL = frontURL + fileName + backURL;
                            Common.downloadFile( lastestVersionURL, Common.getNowAbsolutePath(), fileName, false, null );


                            Object[] options = {"確定", "開啟存放程式的資料夾"};
                            int choice = JOptionPane.showOptionDialog( thisFrame, "<html>最新版本 <font color=red>" + fileName + "</font> 已下載完畢！</html>",
                                "告知視窗",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0] );

                            if ( choice == 1 ) {
                                if ( Common.isWindows() ) {
                                    // 開啟資料夾並將最新版本jar檔反白
                                    Common.runUnansiCmd( "explorer /select, ", Common.getNowAbsolutePath() + fileName );
                                }
                                else if ( Common.isMac() ) {
                                    Common.runCmd( "Finder", Common.getNowAbsolutePath(), true );
                                }
                                else {
                                    Common.runCmd( "nautilus", Common.getNowAbsolutePath(), true );
                                }
                            }
                        }
                    }
                } );

            }
        } ).start();
    }

    @Override
    public void actionPerformed( ActionEvent event ) {
        if ( event.getSource() == downloadButton ) {
            downloadLastestVersion();
        }
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
    }

    @Override
    public void mousePressed( MouseEvent e ) {
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
    }

    @Override
    public void mouseExited( MouseEvent event ) {
        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) {
            ( ( JComponent ) event.getSource() ).setForeground( SetUp.getInformationFrameOtherDefaultColor() );
        }
    }

    @Override
    public void mouseEntered( MouseEvent event ) {
        if ( SetUp.getUsingBackgroundPicOfInformationFrame() ) {
            ( ( JComponent ) event.getSource() ).setForeground( SetUp.getInformationFrameOtherMouseEnteredColor() );
        }
    }
}
