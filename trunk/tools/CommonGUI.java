/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/1
----------------------------------------------------------------------------------------------------
ChangeLog:
1.09: 加入書籤表格和紀錄表格相關的公用方法
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.tools;

import jcomicdownloader.encode.*;
import jcomicdownloader.tools.*;
import jcomicdownloader.*;

import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.net.URL;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import jcomicdownloader.module.Run;

/**
 *
 * 少部份的通用方法放在這邊，大都與視窗界面有相關。
 */
public class CommonGUI {

    private static String resourceFolder;
    public static String stateBarMainMessage;
    public static String stateBarDetailMessage;

    public CommonGUI() {
        resourceFolder = "resource/";
    }

    public static String getResourceFolder() {
        return resourceFolder;
    }

    public URL getResourceURL( String resourceName ) {  // for show pic on jar
        return getClass().getResource( resourceFolder + resourceName );
    }

    public JLabel getFixedTansparentLabel() {
        // set white space in the Label
        return getFixedTansparentLabel( 15 ); // 預設大小為15
    }

    public JLabel getFixedTansparentLabel( int size ) {
        // set white space in the Label
        URL url = getResourceURL( "tansparent.png" );
        return new JLabel( "<html><img align=\"center\" src="
                + url + " width=\"" + size + "\" height=\"" + size + "\"/></html>" );
    }

    public JPanel getCenterPanel( Component comp ) { // make fixed border around
        return getCenterPanel( comp, 15 ); // 預設大小為15
    }

    public JPanel getCenterPanel( Component comp, int size ) { // make fixed border around
        JPanel panel = new JPanel( new BorderLayout() );

        return getCenterPanel( comp, size, size );
    }
    
    public JPanel getCenterPanel( Component comp, int depth, int width ) { // make fixed border around
        JPanel panel = new JPanel( new BorderLayout() );

        panel.add( getFixedTansparentLabel( width ), BorderLayout.EAST );
        panel.add( getFixedTansparentLabel( width ), BorderLayout.WEST );
        panel.add( getFixedTansparentLabel( depth ), BorderLayout.SOUTH );
        panel.add( getFixedTansparentLabel( depth ), BorderLayout.NORTH );
        panel.add( comp, BorderLayout.CENTER );

        return panel;
    }

    public static String getButtonText( String word ) {
        return word;

        /*
        return "<html><font face='文鼎細明體' size='6' >" +
        word +
        "</font></html>";
         * 
         */
    }

    public Icon getImageIcon( String picName ) {
        Icon icon = new ImageIcon();
        try {
            InputStream is = getClass().getResourceAsStream( resourceFolder + picName );
            Image img = ImageIO.read( is );
            icon = new ImageIcon( img );
        } catch ( Exception ex ) {
            Common.errorReport( "找不到此資源：" + resourceFolder + picName );
        }

        return icon;
    }

    public Image getImage( String picName ) {
        return ((ImageIcon) getImageIcon( picName )).getImage();
    }

    public String getButtonPic( String picName ) {
        URL url = getResourceURL( picName );

        return "<html><img src=" + url + " width=\"76\" height=\"76\" /><br>";
    }

    public String getLabelPic( String picName, int width, int height ) {
        URL url = getResourceURL( picName );
        return "<html><img align=\"center\" src=" + url + " width=\"" + width + "\" height=\"" + height + "\"/></html>";
    }

    public String getTansparent( int width, int height ) {
        URL url = getResourceURL( "tansparent.png" );
        return "<html><img align=\"center\" src=" + url + " width=\"" + width + "\" height=\"" + height + "\"/></html>";
    }

    public static int getSumOfTrue( boolean[] bool ) {
        int count = 0;
        for ( int i = 0 ; i < bool.length ; i++ ) {
            if ( bool[i] ) {
                count++;
            }
        }

        return count;
    }

    public static int getSumOfTrue( String[] boolStrings ) {
        int count = 0;

        for ( int i = 0 ; i < boolStrings.length ; i++ ) {
            if ( boolStrings[i] != null && boolStrings[i].equals( "true" ) ) {
                count++;
            }
        }
        return count;
    }

    public static Vector<Object> getDownDataRow( int order, String title, String[] volumes, String[] needs, String[] URLs ) {
        Vector<Object> row = new Vector<Object>();

        row.add( new Integer( order ) );
        row.add( true );
        row.add( title );
        row.add( Common.getConnectStrings( volumes ) );
        row.add( Common.getConnectStrings( needs ) );
        row.add( "等待下載" );
        row.add( Common.getConnectStrings( URLs ) );

        return row;
    }

    public static Vector<Object> getBookmarkDataRow( int order, String title, String url ) {
        Date date = new Date(); // 取得目前時間
        DateFormat shortFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT );

        Vector<Object> row = new Vector<Object>();

        row.add( new Integer( order ) );
        row.add( title );
        row.add( url );
        row.add( shortFormat.format( date ) );
        row.add( "" );

        return row;
    }

    public static Vector<Object> getRecordDataRow( int order, String title, String url ) {
        Date date = new Date(); // 取得目前時間
        DateFormat shortFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT );

        Vector<Object> row = new Vector<Object>();

        row.add( new Integer( order ) );
        row.add( title );
        row.add( url );
        row.add( shortFormat.format( date ) );

        return row;
    }

    public static Vector<Object> getVolumeDataRow( String checkString, String volume ) {
        Vector<Object> row = new Vector<Object>();
        row.add( Boolean.valueOf( checkString ) );
        row.add( volume );
        return row;
    }

    public static int getGTKSkinOrder() {
        UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();

        int gtkOrder = -1;
        for ( int i = 0 ; i < looks.length ; i++ ) {
            if ( looks[i].getClassName().matches( ".*GTK.*" ) ) {
                gtkOrder = i;
            }
        }

        return gtkOrder;
    }

    // 取得JTattoo所有可用的界面類別名稱
    public String[] getJTattooClassNames() {
        String[] jtattooClassNames = {
            "com.jtattoo.plaf.noire.NoireLookAndFeel", // 柔和黑
            "com.jtattoo.plaf.smart.SmartLookAndFeel", // 木質感+xp風格
            "com.jtattoo.plaf.mint.MintLookAndFeel", // 橢圓按鈕+黃色按鈕背景
            "com.jtattoo.plaf.mcwin.McWinLookAndFeel", // 橢圓按鈕+綠色按鈕背景
            "com.jtattoo.plaf.luna.LunaLookAndFeel", // 純XP風格
            "com.jtattoo.plaf.hifi.HiFiLookAndFeel", // 黑色風格
            "com.jtattoo.plaf.fast.FastLookAndFeel", // 普通swing風格+藍色邊框
            "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel", // 黃色風格
            "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel", // 橢圓按鈕+翠綠色按鈕背景+金屬質感（預設）
            "com.jtattoo.plaf.aero.AeroLookAndFeel", // xp清新風格
            "com.jtattoo.plaf.acryl.AcrylLookAndFeel" // 布質感+swing純風格
        };

        return jtattooClassNames;
    }

    // 取得所有預設可用的界面類別名稱
    public String[] getDefaultClassNames() {
        String classNames = "";
        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                //System.out.println( info.getName() );
                classNames += info.getClassName() + "###";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return classNames.split( "###" );
    }

    // 取得預設所有skins和jtattoo所有skins的類別名稱
    public String[] getClassNames() {
        String[] defaultClassNames = getDefaultClassNames();
        String[] jtattooClassNames = getJTattooClassNames();

        int allClassAmount = defaultClassNames.length + jtattooClassNames.length;
        String[] allClassNames = new String[allClassAmount];

        int count = 0;
        for ( int i = 0 ; i < defaultClassNames.length ; i++ ) {
            allClassNames[count++] = defaultClassNames[i];
        }

        for ( int i = 0 ; i < jtattooClassNames.length ; i++ ) {
            allClassNames[count++] = jtattooClassNames[i];
        }

        return allClassNames;
    }

    // 取得JTattoo所有可用的界面名稱
    public String[] getJTattooSkinStrings() {
        String[] jtattooSkinStrings = {
            "Noire", // 柔和黑
            "Smart", // 木質感+xp風格
            "Mint", // 橢圓按鈕+黃色按鈕背景
            "McWin", // 橢圓按鈕+綠色按鈕背景
            "Luna", // 純XP風格
            "HiFi", // 黑色風格
            "Fast", // 普通swing風格+藍色邊框
            "Bernstein", // 黃色風格
            "Aluminium", // 橢圓按鈕+翠綠色按鈕背景+金屬質感（預設）
            "Aero", // xp清新風格
            "Acryl" // 布質感+swing純風格
        };

        return jtattooSkinStrings;
    }

    // 取得所有預設可用的界面名稱
    public String[] getDefaultSkinStrings() {
        String skinString = "";
        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                //System.out.println( info.getName() );
                skinString += info.getName() + "###";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return skinString.split( "###" );
    }

    // 取得預設所有skins和jtattoo所有skins的名稱
    public String[] getSkinStrings() {
        String[] defaultSkinStrings = getDefaultSkinStrings();
        String[] jtattooSkinStrings = getJTattooSkinStrings();

        int allSkinsAmount = defaultSkinStrings.length + jtattooSkinStrings.length;
        String[] allSkinStrings = new String[allSkinsAmount];

        int count = 0;
        for ( int i = 0 ; i < defaultSkinStrings.length ; i++ ) {
            allSkinStrings[count++] = defaultSkinStrings[i];
        }

        for ( int i = 0 ; i < jtattooSkinStrings.length ; i++ ) {
            allSkinStrings[count++] = jtattooSkinStrings[i];
        }

        return allSkinStrings;
    }

    // 找尋skinClassName在looks[i]中的順位，若沒找到就回傳-1
    public int getSkinOrderBySkinClassName( String skinClassName ) {
        String[] classNames = getClassNames();

        int gtkOrder = -1;
        for ( int i = 0 ; i < classNames.length ; i++ ) {
            if ( classNames[i].equals( skinClassName ) ) {
                gtkOrder = i;
            }
        }

        return gtkOrder;
    }

    public static void setLookAndFeelByClassOrder( int no ) {
        UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel( looks[no].getClassName() );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public static void setLookAndFeelByClassName( String className ) {
        try {
            UIManager.setLookAndFeel( className );
        } catch ( Exception ex ) {
            Common.errorReport( "無法使用" + className + "界面 !!" );
            try {
                UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
            } catch ( Exception exx ) {
                exx.printStackTrace();
            }
            ex.printStackTrace();
        }
    }

    // 下載JTattoo.jar檔
    public void downloadJTattoo() {
        String skinName = getSkinNameFromClassName( SetUp.getSkinClassName() );
        int choice = JOptionPane.showConfirmDialog( ComicDownGUI.mainFrame, "資料夾內未發現JTattoo.jar，無法使用"
                + skinName + "界面！\n\n請問是否要下載JTattoo.jar（634Kb）？",
                "提醒訊息", JOptionPane.YES_NO_OPTION );

        if ( choice == JOptionPane.YES_OPTION ) {
            Thread downThread = new Thread( new Runnable() {

                public void run() {
                    boolean backupValue = Run.isAlive; // 備份原值
                    Run.isAlive = true;

                    String fileURL = "https://sites.google.com/site/jcomicdownloader/release/JTattoo.jar?attredirects=0&d=1";
                    Common.downloadFile( fileURL, Common.getNowAbsolutePath(), "JTattoo.jar", false, "" );

                    Run.isAlive = backupValue; // 還原原值

                    JOptionPane.showMessageDialog( ComicDownGUI.mainFrame, "JTattoo.jar下載完畢，程式即將關閉，請您再次啟動",
                            "提醒訊息", JOptionPane.INFORMATION_MESSAGE );

                    ComicDownGUI.exit(); // 結束程式
                }
            } );
            downThread.start();
        } else {
            skinName = getSkinNameFromClassName( ComicDownGUI.getDefaultSkinClassName() );
            JOptionPane.showMessageDialog( ComicDownGUI.mainFrame, "不下載JTattoo.jar，使用預設的"
                    + skinName + "界面",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
        }
    }

    private String getSkinNameFromClassName( String className ) {
        String[] tempStrings = className.split( "\\." );
        return tempStrings[tempStrings.length - 1].replaceAll( "LookAndFeel", "" );
    }
}
