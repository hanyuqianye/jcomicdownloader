/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/1
----------------------------------------------------------------------------------------------------
ChangeLog:
1.16: 勾選自動刪除就要連帶勾選自動壓縮。
1.14: 增加可選擇字型和字體大小的選項
1.09: 加入是否保留記錄的選項
1.08: 讓logCheckBox來決定由cmd或由logFrame來輸出資訊
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.frame;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * 選項視窗
 */
public class OptionFrame extends JFrame {

    // about skin
    private String skinStrings[];
    private UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
    private JLabel skinLabel;
    private JComboBox skinBox;
    // about directory
    private JLabel dirLabel;
    private JTextField dirTextField;
    private JButton dirButton, chooseFontButton;
    private JCheckBox compressCheckBox; // about compress
    private JCheckBox deleteCheckBox;  // about delete
    private JCheckBox logCheckBox; // about log
    private JCheckBox keepRecordCheckBox; // 保留記錄
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


        setVisible( true );
    }

    private void setUpUIComponent() {
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

        setSize( 390, 710 );
        setResizable( false );
        setLocationRelativeTo( this );  // set the frame in middle position of screen
        setIconImage( new CommonGUI().getImage( "main_icon.png" ) );

        defaultColor = "black";

        GridLayout grid = new GridLayout( 5, 1, 5, 5 );
        JPanel centerPanel = new JPanel( new GridLayout( 0, 1, 5, 5 ) );
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
        dirLabel = getLabel( "目前下載目錄：       " );

        dirTextField = new JTextField( SetUp.getOriginalDownloadDirectory(), 25 );
        dirTextField.setFont( SetUp.getDefaultFont( -1 ));
        dirTextField.setHorizontalAlignment( JTextField.LEADING );

        dirButton = getButton( "選擇新目錄" );
        dirButton.addActionListener( new ActionHandler() );

        JPanel dirPanelHorizontal = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        dirPanelHorizontal.add( dirLabel );
        dirPanelHorizontal.add( dirButton );

        //JLabel lineLabel = new JLabel( "<html>_________________________________________________<hr></html>" );

        JLabel chooseFontLabel = getLabel( "目前字型：" + SetUp.getDefaultFontName() );
        chooseFontButton = getButton( "選擇新字型" );
        chooseFontButton.addActionListener( new ActionHandler() );

        JPanel chooseFontPanel = new JPanel( new GridLayout( 1, 2, 5, 5 ) );
        chooseFontPanel.add( chooseFontLabel );
        chooseFontPanel.add( chooseFontButton );

        JPanel dirPanelVertical = new JPanel( new GridLayout( 3, 1, 5, 5 ) );
        dirPanelVertical.add( dirPanelHorizontal );
        dirPanelVertical.add( dirTextField );
        dirPanelVertical.add( chooseFontPanel );


        panel.add( dirPanelVertical );
    }

    private void setCheckUI( JPanel panel ) {
        compressCheckBox = getCheckBoxBold( "自動產生壓縮檔", SetUp.getAutoCompress() );
        compressCheckBox.addItemListener( new ItemHandler() );
        compressCheckBox.setToolTipText( "下載完成後進行壓縮，壓縮檔名與資料夾名稱相同" );

        deleteCheckBox = getCheckBox( "自動刪除圖檔", SetUp.getDeleteOriginalPic() );
        deleteCheckBox.addItemListener( new ItemHandler() );
        deleteCheckBox.setToolTipText( "下載完成後便刪除圖檔，此選項應與『自動產生壓縮檔』搭配使用" );

        keepUndoneCheckBox = getCheckBoxBold( "保留未完成任務", SetUp.getKeepUndoneDownloadMission() );
        keepUndoneCheckBox.addItemListener( new ItemHandler() );
        keepUndoneCheckBox.setToolTipText( "這次沒下載完畢的任務，下次開啟時仍會出現在任務清單當中" );

        keepDoneCheckBox = getCheckBox( "保留已完成任務", SetUp.getKeepDoneDownloadMission() );
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
        urlCheckBox = getCheckBox( "輸出下載位址文件檔", SetUp.getOutputUrlFile() );
        urlCheckBox.addItemListener( new ItemHandler() );
        urlCheckBox.setToolTipText( "解析所有圖片的真實下載位址後彙整輸出為txt文件檔，檔名與資料夾名稱相同" );

        downloadCheckBox = getCheckBoxBold( "分析後下載圖檔（預設）", SetUp.getDownloadPicFile() );
        downloadCheckBox.addItemListener( new ItemHandler() );
        downloadCheckBox.setToolTipText( "如果沒有勾選就不會有下載行為，建議要勾選（但若只想輸出真實下載位址，就不要勾選此選項）" );

        logCheckBox = getCheckBox( "開啟除錯訊息記錄視窗", SetUp.getOpenDebugMessageWindow() );
        logCheckBox.addItemListener( new ItemHandler() );
        logCheckBox.setToolTipText( "可顯示程式判斷流程與下載詳細進度，僅供除錯研究之用" );

        keepRecordCheckBox = getCheckBoxBold( "保留任務記錄", SetUp.getKeepRecord() );
        keepRecordCheckBox.addItemListener( new ItemHandler() );
        keepRecordCheckBox.setToolTipText( "若紀錄過多而影響效能，請取消勾選或刪除recordList.dat" );

        trayMessageCheckBox = getCheckBoxBold( "縮小到系統列時顯示下載完成訊息", SetUp.getShowDoneMessageAtSystemTray() );
        trayMessageCheckBox.addItemListener( new ItemHandler() );
        trayMessageCheckBox.setToolTipText( "如果沒有勾選，縮小到系統列後就不會再有下載完畢的提示訊息" );

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout( new GridLayout( 4, 1 ) );//FlowLayout( FlowLayout.CENTER, 14, 20 ) );
        checkPanel.add( trayMessageCheckBox );
        checkPanel.add( urlCheckBox );
        checkPanel.add( downloadCheckBox );
        checkPanel.add( keepRecordCheckBox );
        //checkPanel.add( logCheckBox );


        panel.add( checkPanel );
    }

    private void setProxyUI( JPanel panel ) {
        JPanel proxyServerPanel = new JPanel( new GridLayout( 2, 1 ) );
        proxyServerTextField = new JTextField( SetUp.getProxyServer(), 22 );
        proxyServerTextField.setFont( SetUp.getDefaultFont( -1 ) );
        proxyServerTextField.setHorizontalAlignment( JTextField.LEADING );
        proxyServerTextField.setToolTipText( "若是中華電信用戶，可輸入proxy.hinet.net" );

        proxyServerPanel.add( getLabel( "設定代理伺服器位址(Host)：" ) );
        proxyServerPanel.add( proxyServerTextField );

        JPanel proxyPortPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        proxyPortTextField = new JTextField( SetUp.getProxyPort(), 4 );
        proxyPortTextField.setFont( SetUp.getDefaultFont( -1 ) );
        proxyPortTextField.setHorizontalAlignment( JTextField.LEADING );
        proxyPortTextField.setToolTipText( "若是中華電信用戶，可輸入80" );

        proxyPortPanel.add( getLabel( "設定代理伺服器連接阜(Port)：" ) );
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

        skinLabel = getLabel( "選擇介面：" );

        skinBox.addItemListener( new ItemHandler() ); // change skin if change skinBox
        skinBox.setToolTipText( "可選擇您喜好的樣式風格" );

        confirmButton = getButton( "  確定  " );
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

    private void confirmUI( JPanel panel ) {
        //panel.add( confirmPanel );
    }

    private String[] getSkinStrings() {
        String skin = "";
        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                //System.out.println( info.getName() );
                skin += info.getName() + "###";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return skin.split( "###" );
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
        CommonGUI.setLookAndFeelByClassName( looks[value].getClassName() );
        ComicDownGUI.setDefaultSkinClassName( looks[value].getClassName() );
        SetUp.setSkinClassName( looks[value].getClassName() ); // 紀錄到設定值

        // change the skin of Option frame
        SwingUtilities.updateComponentTreeUI( this );

        // change the skin of main frame
        SwingUtilities.updateComponentTreeUI( ComicDownGUI.mainFrame );


        if ( InformationFrame.informationFrame != null ) // change the skin of information frame
        {
            SwingUtilities.updateComponentTreeUI( InformationFrame.informationFrame );
        }

        if ( ChoiceFrame.choiceFrame != null ) // change the skin of information frame
        {
            SwingUtilities.updateComponentTreeUI( ChoiceFrame.choiceFrame );
        }

        if ( LogFrame.logFrame != null ) // change the skin of information frame
        {
            SwingUtilities.updateComponentTreeUI( LogFrame.logFrame );
        }

        Common.debugPrintln( "改為" + looks[value].getClassName() + "面板" );
    }

    private void chooseFile() {
        final Component tempThisComponent = this;

        SwingUtilities.invokeLater( new Runnable() {

            public void run() {

                JFileChooser dirChooser = new JFileChooser( SetUp.getOriginalDownloadDirectory() );
                dirChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

                dirChooser.setDialogTitle( "請選擇新的下載目錄" );

                try {
                    int result = dirChooser.showDialog( tempThisComponent, "確定" );

                    if ( result == JFileChooser.APPROVE_OPTION ) {
                        File file = dirChooser.getSelectedFile();

                        String path = "";
                        if ( file.getPath().matches( "(?s).*" + Common.getRegexSlash() ) ) {
                            path = file.getPath();
                        } else {
                            path = file.getPath() + Common.getSlash();
                        }

                        SetUp.setOriginalDownloadDirectory( path ); // 紀錄到設定值
                        dirTextField.setText( file.getPath() );
                    }
                } catch ( HeadlessException ex ) {
                    ex.printStackTrace();
                }

            }
        } );

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
                
                if ( font != null )
                    JOptionPane.showMessageDialog( thisFrame, "你選擇的字型是" + font.getName() + "　"
                            + "大小為" + font.getSize() + "（需重新開啟才會啟用新設定）" );
            }
            if ( event.getSource() == dirButton ) {
                chooseFile();
            } else if ( event.getSource() == confirmButton ) {
                SetUp.setProxyServer( proxyServerTextField.getText() );
                SetUp.setProxyPort( proxyPortTextField.getText() );

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
                if ( deleteCheckBox.isSelected() )
                    compressCheckBox.setSelected( true ); // 勾選自動刪除就要連帶勾選自動壓縮
                
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
            if ( event.getSource() == keepRecordCheckBox ) {
                SetUp.setKeepRecord( keepRecordCheckBox.isSelected() ); // 紀錄到設定值
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
                //skinLabel.setText( skinStrings[skinBox.getSelectedIndex()] );
                String nowSelectedSkin = skinBox.getItemAt( skinBox.getSelectedIndex() ).toString();

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

    private JButton getButton( String string ) {
        JButton button = new JButton( string );
        button.setFont( SetUp.getDefaultFont() );

        return button;
    }
}
