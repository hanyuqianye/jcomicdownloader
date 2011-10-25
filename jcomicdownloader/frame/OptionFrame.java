/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.frame;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class OptionFrame extends JFrame {

    // about skin
    private String skinStrings[];
    private UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
    private JLabel skinLabel;
    private JComboBox skinBox;

    // about directory
    private JLabel dirLabel;
    private JTextField dirTextField;
    private JButton dirButton;

    private JCheckBox compressCheckBox; // about compress
    private JCheckBox deleteCheckBox;  // about delete
    private JCheckBox logCheckBox; // about log
    private JCheckBox urlCheckBox; // about output the url file
    private JCheckBox downloadCheckBox; // about download the pic file

    private JCheckBox keepDoneCheckBox;  // 是否保持已完成任務到下次開啟
    private JCheckBox keepUndoneCheckBox;  // 是否保持未完成任務到下次開啟
    private JCheckBox trayMessageCheckBox;  // 縮小到系統框後是否顯示下載完成訊息
    
    private JTextField proxyServerTextField; // 輸入代理伺服器位址 ex. proxy.hinet.net
    private JTextField proxyPortTextField; // 輸入代理伺服器連接阜 ex. 80

    private JButton confirmButton;  // about confirm

    private String defaultColor; // 預設的建議設定顏色


    public static JFrame optionFrame; // use by other frame
    private JFrame thisFrame; // use by self

    /**
 *
 * @author user
 */
    public OptionFrame() {
        super( "選項設定" );
        OptionFrame.optionFrame = thisFrame = this; // for close the frame

        setUpUIComponent();


        setVisible(true);
    }

    private void setUpUIComponent() {
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

        setSize( 370, 710 );
        setResizable( false );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) );

        defaultColor = "black";

        JPanel centerPanel = new JPanel( new GridLayout( 5, 1, 5, 5 ) );
        JPanel wholePanel = new CommonGUI().getCenterPanel( centerPanel );
        contentPane.add( wholePanel, BorderLayout.CENTER );

        setDirectoryUI( centerPanel );
        setCheckUI( centerPanel );
        setLogUI( centerPanel );
        setProxyUI( centerPanel );
        setSkinUI( centerPanel );
        //confirmUI( centerPanel );

        setUpeListener();
    }

    private void setSpaceLayout( JPanel panel ) {
        //getFixedTansparentLabel();
    }

    private String getHtmlString( String str ) {
        return "<html><font size=\"5\">" + str + "</font></html>";
    }
    private String getHtmlStringWithColor( String str, String colorName ) {
        return "<html><font color=\"" + colorName + "\" size=\"5\"><b>" + str + "</b></font></html>";
    }

    private void setDirectoryUI( JPanel panel ) {
        dirLabel = new JLabel( getHtmlString( "預設下載目錄：           " ) );

        dirTextField = new JTextField( SetUp.getOriginalDownloadDirectory(), 25 );
        dirTextField.setFont( new Font( "新細明體", Font.PLAIN, 17 ) );
        dirTextField.setHorizontalAlignment( JTextField.LEADING );

        dirButton = new JButton( getHtmlString( "選擇新目錄" ) );
        dirButton.addActionListener( new ActionHandler() );

        JPanel dirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        dirPanelHorizontal.add( dirLabel );
        dirPanelHorizontal.add( dirButton );

        JPanel dirPanelVertical = new JPanel( new GridLayout( 3, 1, 5, 5 ) );
        dirPanelVertical.add( dirPanelHorizontal );
        dirPanelVertical.add( dirTextField );

        panel.add( dirPanelVertical );
        //panel.add( dirTextField );
    }

    private void setCheckUI( JPanel panel ) {
        compressCheckBox = new JCheckBox( getHtmlStringWithColor( "自動產生壓縮檔", defaultColor ),
                                          SetUp.getAutoCompress() );
        compressCheckBox.addItemListener( new ItemHandler() );
        compressCheckBox.setToolTipText( "下載完成後進行壓縮，壓縮檔名與資料夾名稱相同" );

        deleteCheckBox = new JCheckBox( getHtmlString( "自動刪除圖檔" ),
                                          SetUp.getDeleteOriginalPic() );
        deleteCheckBox.addItemListener( new ItemHandler() );
        deleteCheckBox.setToolTipText( "下載完成後便刪除，此選項應與『自動產生壓縮檔』搭配使用" );

        keepUndoneCheckBox = new JCheckBox( getHtmlStringWithColor( "保留未完成任務", defaultColor ),
                                          SetUp.getKeepUndoneDownloadMission() );
        keepUndoneCheckBox.addItemListener( new ItemHandler() );
        keepUndoneCheckBox.setToolTipText( "這次沒下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepDoneCheckBox = new JCheckBox( getHtmlString( "保留已完成任務" ),
                                          SetUp.getKeepDoneDownloadMission() );
        keepDoneCheckBox.addItemListener( new ItemHandler() );
        keepDoneCheckBox.setToolTipText( "這次已經下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout( new GridLayout( 4, 1 ) );//FlowLayout( FlowLayout.CENTER, 14, 30 ) );
        checkPanel.add( compressCheckBox );
        checkPanel.add( deleteCheckBox );
        checkPanel.add( keepUndoneCheckBox );
        checkPanel.add( keepDoneCheckBox );

        panel.add( checkPanel );
    }


    private void setLogUI( JPanel panel ) {
        urlCheckBox = new JCheckBox( getHtmlString( "輸出下載位址文件檔" ),
                                     SetUp.getOutputUrlFile() );
        urlCheckBox.addItemListener( new ItemHandler() );
        urlCheckBox.setToolTipText( "解析所有圖片的真實下載位址後彙整輸出為txt文件檔，檔名與資料夾名稱相同" );

        downloadCheckBox = new JCheckBox( getHtmlStringWithColor( "分析後下載圖檔（預設）", defaultColor ),
                                          SetUp.getDownloadPicFile() );
        downloadCheckBox.addItemListener( new ItemHandler() );
        downloadCheckBox.setToolTipText( "如果沒有勾選就不會有下載行為，建議要勾選（但若只想輸出真實下載位址，就不要勾選此選項）" );

        logCheckBox = new JCheckBox( getHtmlString( "開啟除錯訊息記錄視窗" ),
                                     SetUp.getOpenDebugMessageWindow() );
        logCheckBox.addItemListener( new ItemHandler() );
        logCheckBox.setToolTipText( "可顯示程式判斷流程與下載詳細進度，僅供除錯研究之用" );

        trayMessageCheckBox = new JCheckBox( getHtmlStringWithColor( "縮小到系統列時顯示下載完成訊息", defaultColor ),
                                     SetUp.getShowDoneMessageAtSystemTray() );
        trayMessageCheckBox.addItemListener( new ItemHandler() );
        trayMessageCheckBox.setToolTipText( "如果沒有勾選，縮小到系統列後就不會再有下載完畢的提示訊息" );

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout( new GridLayout( 4, 1 ) );//FlowLayout( FlowLayout.CENTER, 14, 20 ) );
        checkPanel.add( trayMessageCheckBox );
        checkPanel.add( urlCheckBox );
        checkPanel.add( downloadCheckBox );
        checkPanel.add( logCheckBox );


        panel.add( checkPanel );
    }
    
    private void setProxyUI( JPanel panel ) {
        JPanel proxyServerPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        proxyServerTextField = new JTextField( SetUp.getProxyServer(), 25 );
        proxyServerTextField.setFont( new Font( "新細明體", Font.PLAIN, 17 ) );
        proxyServerTextField.setHorizontalAlignment( JTextField.LEADING );
        
        proxyServerPanel.add( new JLabel( getHtmlString( "設定代理伺服器位址(Host)：" ) ) );
        proxyServerPanel.add( proxyServerTextField );
        
        JPanel proxyPortPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        proxyPortTextField = new JTextField( SetUp.getProxyPort(), 4 );
        proxyPortTextField.setFont( new Font( "新細明體", Font.PLAIN, 17 ) );
        proxyPortTextField.setHorizontalAlignment( JTextField.LEADING );
        
        proxyPortPanel.add( new JLabel( getHtmlString( "設定代理伺服器連接阜(Port)：" ) ) );
        proxyPortPanel.add( proxyPortTextField );
        
        JPanel proxyPanel = new JPanel( new GridLayout( 2, 1 ) );
        proxyPanel.add( proxyServerPanel );
        proxyPanel.add( proxyPortPanel );
        
        panel.add( proxyPanel );
    }

    private void setSkinUI( JPanel panel ) {
        skinStrings = getSkinStrings();
        skinBox = new JComboBox( skinStrings );
        skinBox.setSelectedIndex( getSkinIndex( SetUp.getSkinClassName() ) );

        skinLabel = new JLabel( getHtmlString( "選擇介面：" ) );

        skinBox.addItemListener( new ItemHandler() ); // change skin if change skinBox
        skinBox.setToolTipText( "可選擇您喜好的樣式風格" );

        confirmButton = new JButton( getHtmlString( "確定" ) );
        confirmButton.addActionListener( new ActionHandler() );
        JPanel confirmPanel = new JPanel();
        confirmPanel.setLayout( new FlowLayout( FlowLayout.CENTER, 0, 20 ) );
        confirmPanel.add( confirmButton );

        // the order: skinLabel skinBox
        JPanel skinPanel = new JPanel();
        skinPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 10 ) );
        skinPanel.add( skinLabel );
        skinPanel.add( skinBox );

        JPanel skinAndConfirmPanel = new JPanel();
        skinAndConfirmPanel.setLayout( new GridLayout( 2, 1 ) );
        skinAndConfirmPanel.add( skinPanel );
        skinAndConfirmPanel.add( confirmPanel );

        panel.add( skinAndConfirmPanel );
    }

    private void confirmUI( JPanel panel ){


        //panel.add( confirmPanel );
    }

    private String[] getSkinStrings() {
        String skin = "";
        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                //System.out.println( info.getName() );
                skin += info.getName() + "###";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return skin.split( "###" );
    }

    private int getSkinIndex( String skinClassName ) {
        int index = 0;
        for ( String skinName : skinStrings ) {
            if ( skinClassName.matches( ".*" + skinName + ".*" ) )
                break;
            index ++;
        }

        if ( skinStrings.length > index )
            return index;
        else
            return 0;
    }

    private void changeSkin( int value ) {
        CommonGUI.setLookAndFeelByClassName( looks[value].getClassName() );
        ComicDownGUI.setDefaultSkinClassName( looks[value].getClassName() );
        SetUp.setSkinClassName( looks[value].getClassName() ); // 紀錄到設定值

        // change the skin of Option frame
        SwingUtilities.updateComponentTreeUI( this );

        // change the skin of main frame
        SwingUtilities.updateComponentTreeUI( ComicDownGUI.mainFrame );


        if ( InformationFrame.informationFrame != null )
            // change the skin of information frame
            SwingUtilities.updateComponentTreeUI( InformationFrame.informationFrame );

        if ( ChoiceFrame.choiceFrame != null )
            // change the skin of information frame
            SwingUtilities.updateComponentTreeUI( ChoiceFrame.choiceFrame );

        if ( LogFrame.logFrame != null )
            // change the skin of information frame
            SwingUtilities.updateComponentTreeUI( LogFrame.logFrame );

        Common.debugPrintln( "改為" + looks[value].getClassName() + "面板" );
    }

    private void chooseFile() {
        final Component tempThisComponent = this;

        SwingUtilities.invokeLater( new Runnable(){ public void run() {

        JFileChooser dirChooser = new JFileChooser( SetUp.getOriginalDownloadDirectory() );
        dirChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        dirChooser.setDialogTitle( "請選擇新的下載目錄" );

        try {
            int result = dirChooser.showDialog( tempThisComponent, "確定" );

            if ( result == JFileChooser.APPROVE_OPTION ) {
                File file = dirChooser.getSelectedFile();

                SetUp.setOriginalDownloadDirectory( file.getPath() + Common.getSlash() ); // 紀錄到設定值
                dirTextField.setText( file.getPath() );
            }
        } catch ( HeadlessException ex ) { ex.printStackTrace(); }

        } } );

    }

    private void setUpeListener() {
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    // -------------  Listener  ---------------

    private class ActionHandler implements ActionListener {
        public void actionPerformed( ActionEvent event ) {
            if ( event.getSource() == dirButton ) {
                chooseFile();
            }
            else if ( event.getSource() == confirmButton ) {
                SetUp.setProxyServer( proxyServerTextField.getText() );
                SetUp.setProxyPort( proxyPortTextField.getText() );
                
                if ( proxyServerTextField.getText() != null && 
                    !proxyServerTextField.getText().equals( "" ) && 
                    proxyPortTextField.getText() != null && 
                    !proxyPortTextField.getText().equals( "" ) ) {
                    Common.setHttpProxy( SetUp.getProxyServer(), SetUp.getProxyPort() );
                    Common.debugPrintln( "設定代理伺服器：" + 
                                           SetUp.getProxyServer() + " " +
                                           SetUp.getProxyPort() );
                }
                else {
                    Common.closeHttpProxy();
                    Common.debugPrintln( "代理伺服器資訊欠缺位址或連接阜，因此不加入" );
                }
                SetUp.writeSetFile(); // 將目前的設定存入設定檔（set.ini）
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

            if ( event.getSource() == compressCheckBox ) {
                SetUp.setAutoCompress( compressCheckBox.isSelected() ); // 紀錄到設定值
            }
            if ( event.getSource() == deleteCheckBox ) {
                SetUp.setDeleteOriginalPic( deleteCheckBox.isSelected() ); // 紀錄到設定值
            }
            if ( event.getSource() == urlCheckBox ) {
                SetUp.setOutputUrlFile( urlCheckBox.isSelected() ); // 紀錄到設定值
            }
            if ( event.getSource() == downloadCheckBox ) {
                SetUp.setDownloadPicFile( downloadCheckBox.isSelected() ); // 紀錄到設定值
            }

            if ( event.getSource() == keepDoneCheckBox ) {
                SetUp.setKeepDoneDownloadMission( keepDoneCheckBox.isSelected() ); // 紀錄到設定值
            }
            if ( event.getSource() == keepUndoneCheckBox ) {
                SetUp.setKeepUndoneDownloadMission( keepUndoneCheckBox.isSelected() ); // 紀錄到設定值
            }
            if ( event.getSource() == trayMessageCheckBox ) {
                SetUp.setShowDoneMessageAtSystemTray( trayMessageCheckBox.isSelected() ); // 紀錄到設定值
            }

            if ( event.getSource() == logCheckBox ) {
                new Thread( new Runnable() { public void run() {
                    ComicDownGUI.logFrame.setVisible( logCheckBox.isSelected() );
                } } ).start();
                SetUp.setOpenDebugMessageWindow( logCheckBox.isSelected() ); // 紀錄到設定值

            }


            //Common.debugPrintln( "getDownloadPicFile: " + SetUp.getDownloadPicFile() +
            //                     "\ngetOutputUrlFile: " + SetUp.getOutputUrlFile() );

            if ( event.getStateChange() == ItemEvent.SELECTED ) {
                //skinLabel.setText( skinStrings[skinBox.getSelectedIndex()] );
                String nowSelectedSkin = skinBox.getItemAt( skinBox.getSelectedIndex() ).toString();

                if ( !SetUp.getSkinClassName().matches( ".*" + nowSelectedSkin + ".*" ) )
                    changeSkin( skinBox.getSelectedIndex() );
            }
        }
    }

}
