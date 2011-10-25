/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.frame;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.module.*;
import jcomicdownloader.*;

/**
 *
 * @author user
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class InformationFrame extends JFrame {
    private JPanel informationPanel;
    private JLabel informationLabel, versionLabel;
    private String resourceFolder;
    private JButton versionButton;

    public static JFrame informationFrame; // for change look and feel

    /**
 *
 * @author user
 */
    public InformationFrame() {
        super( "關於本程式" );

        informationFrame = this; // for change look and feel
        resourceFolder = "resource/";

        setUpUIComponent();
        setUpeListener();
        setVisible( true );
    }

    private void setUpUIComponent() {
        setSize( 420, 510 );
        setResizable( false );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) );

        Container contentPane = getContentPane();

        //setButton( contentPane );

        setText( contentPane );
    }


    private void setText( Container contentPane ) {
        //textPanel = new JPanel( new BorderLayout() );

        String informationText = "";
        informationLabel = new JLabel( informationText );

        versionLabel = new JLabel( getHtmlString( "目前最新版本: 偵測中..." ) );

        JButton downloadButton = new JButton( getHtmlString( " 最新版本下載" ), new CommonGUI().getImageIcon( "information_download.png" ) );
        downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RunBrowser().runBroswer( "https://sites.google.com/site/jcomicdownloader/release" );
        }});

        JButton teachingButton = new JButton( getHtmlString( " 線上使用教學" ), new CommonGUI().getImageIcon( "information_manual.png" ) );
        teachingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RunBrowser().runBroswer( "https://sites.google.com/site/jcomicdownloader/step-by-step" );
        }});

        JButton searchButton = new JButton( getHtmlString( " 漫畫搜尋引擎" ), new CommonGUI().getImageIcon( "information_search.png" ) );
        searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RunBrowser().runBroswer( "http://www.google.com/cse/home?cx=002948535609514911011:ls5mhwb6sqa&hl=zh-TW" );
        }});

        JButton messageButton = new JButton( getHtmlString( " 疑難問題回報" ), new CommonGUI().getImageIcon( "information_report.png" ) );
        messageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RunBrowser().runBroswer( "http://jcomicdownloader.blogspot.com/2011/10/introduction.html" );
        }});

        String authorText = getHtmlString( "作者　　：surveyorK<br>作者信箱：abc9070410@gmail.com" );
        JLabel authorLabel = new JLabel( authorText );

        //JPanel versionPanel = new CommonGUI().getCenterPanel( versionLabel );

        informationPanel = new JPanel( new GridLayout( 6, 1, 15, 15 ) );
        informationPanel.add( versionLabel );
        informationPanel.add( downloadButton );
        informationPanel.add( teachingButton );
        informationPanel.add( searchButton );
        informationPanel.add( messageButton );
        informationPanel.add( authorLabel );


        JScrollPane informationScrollPane = new JScrollPane( new CommonGUI().getCenterPanel( informationPanel ) );

        contentPane.add( informationScrollPane, BorderLayout.CENTER );

    }

    private String getHtmlString( String str ) {
        return "<html><font size=\"5\">" + str + "</font></html>";
    }
    private String getHtmlStringWithColor( String str, String colorName ) {
        return "<font color=\"" + colorName + "\" size=\"5\">" + str + "</font>";
    }

    // 直接抓取官網網頁，檢查有無最新版本的字串
    private String getNewestVersionString() {
        String officialName = "official.html";
        String officialURL = "https://sites.google.com/site/jcomicdownloader/";
        Run.isAlive = true;
        Common.downloadFile( officialURL, Common.tempDirectory, officialName, false, "" );
        String allPageString = Common.getFileString( Common.tempDirectory, officialName );

        // 先找出最新的是第幾號版本
        int endIndex = allPageString.indexOf( "版發佈" );
        int beginIndex = allPageString.substring( 0, endIndex ).lastIndexOf( ">" ) + 1;
        String versionString = allPageString.substring( beginIndex, endIndex );

        // 再找出發佈最新版本的日期
        int tempIndex = allPageString.indexOf( "countdown-fromdateutc" );
        String[] tokens = allPageString.substring( tempIndex, allPageString.length() ).split( "-|\"" );
        String dateString = "（" + tokens[2] + "年" + tokens[3] + "月" + tokens[4] + "日發佈）";

        return getHtmlStringWithColor( versionString, "red" ) +
               getHtmlStringWithColor( dateString, "blue" );
    }

    public void setNewestVersion() {
        versionLabel.setText( getHtmlString( "目前最新版本: " + getNewestVersionString() ) );
    }

    private void setUpeListener() {
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    }

    public static void main( String[] args ) {
        InformationFrame frame = new InformationFrame();
        //frame.setNewestVersion();
    }


}
