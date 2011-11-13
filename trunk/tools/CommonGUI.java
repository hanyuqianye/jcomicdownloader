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


/**
 *
 * 少部份的通用方法放在這邊，大都與視窗介面有相關。
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
        URL url = getResourceURL( "tansparent.png" );
        return new JLabel( "<html><img align=\"center\" src=" + url + " width=\"15\" height=\"15\"/></html>" );
    }

    public JPanel getCenterPanel( Component comp ) { // make fixed border around
        JPanel panel = new JPanel( new BorderLayout() );

        panel.add( getFixedTansparentLabel(), BorderLayout.EAST );
        panel.add( getFixedTansparentLabel(), BorderLayout.WEST );
        panel.add( getFixedTansparentLabel(), BorderLayout.SOUTH );
        panel.add( getFixedTansparentLabel(), BorderLayout.NORTH );
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
            Image img = ImageIO.read( is ) ;
            icon = new ImageIcon( img );
        } catch ( IOException ex ) {}

        return icon;
    }

    public Image getImage( String picName ) {
        return ( (ImageIcon) getImageIcon( picName ) ).getImage();
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
        for ( int i = 0; i < bool.length; i ++ )
            if ( bool[i] )
                count ++;

        return count;
    }

    public static int getSumOfTrue( String[] boolStrings ) {
        int count = 0;

        for ( int i = 0; i < boolStrings.length; i ++ ) {
            if ( boolStrings[i] != null && boolStrings[i].equals( "true" ) )
                count ++;
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
        for ( int i = 0; i < looks.length; i ++ )
            if ( looks[i].getClassName().matches( ".*GTK.*" ) )
                gtkOrder = i;

        return gtkOrder;
    }

    // 找尋skinClassName在looks[i]中的順位，若沒找到就回傳-1
    public static int getSkinOrderBySkinClassName( String skinClassName ) {
        UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();

        int gtkOrder = -1;
        for ( int i = 0; i < looks.length; i ++ )
            if ( looks[i].getClassName().equals( skinClassName ) )
                gtkOrder = i;

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
            try {
                UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
            } catch ( Exception exx ) {
                exx.printStackTrace();
            }
            ex.printStackTrace();
        }
    }

}
