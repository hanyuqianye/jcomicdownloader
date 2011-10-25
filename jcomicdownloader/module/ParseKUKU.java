/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.util.*;
import java.net.*;

public class ParseKUKU extends ParseOnlineComicSite {
    private String baseURL;
    private int nowNo;

    private synchronized int getNowNo() {
        return ++ nowNo;
    }

    /**
 *
 * @author user
 */
    public ParseKUKU() {
        siteID = Site.KUKU;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_kuku_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_kuku_encode_parse_", "html" );

        baseURL = "http://cc.kukudm.com/";
    }



    public ParseKUKU( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }
    
    public void setURL( String url ) {
        this.webSite = url;
    }
    public void setTitle( String title ) {
        this.title = title;
    }
    public void setWholeTitle( String wholeTitle ) {
        this.wholeTitle = wholeTitle;
    }
    public void setDownloadDirectory( String downloadDirectory ) {
        this.downloadDirectory = downloadDirectory;
    }


    public synchronized void setParameters() {
        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        String tempStr = Common.getFileString( Common.tempDirectory, indexEncodeName );
        String[] lines = tempStr.split( "\n" );

        for ( int i = 0; i < lines.length; i ++ ) {
            String line = Common.getTraditionalChinese( lines[i] );

            // ".": contain all characters except "\r" and "\n"
            // "(?s).": contain all characters
            if ( line.matches( "(?s).*title(?s).*" ) ) {
                // get title ex.<title>尸錄 4話</title>
                String[] temp = line.split( "<|>" );
                wholeTitle = Common.getStringRemovedIllegalChar( temp[2] );
                //title = wholeTitle.split( "\\d|\\s|_|\\[|\\]" )[0];
                //title = Common.getStringRemovedIllegalChar( title );
            }
            else if ( line.matches( "(?s).*page(?s).*" ) ) {
                //System.out.println( line );
                // get total page ex. | 共34頁 |
                int beginIndex = line.indexOf( "共" );
                int endIndex = line.indexOf( "頁" );

                String temp = line.substring( beginIndex + 1, endIndex );
                totalPage = Integer.parseInt( temp );

                break;
            }
        }


        comicURL = new String [totalPage]; // totalPage = amount of comic pic
        SetUp.setWholeTitle( wholeTitle );
    }
    public synchronized void parseComicURL() {
        System.out.print( "parse the pic URL:" );

        for ( int i = 0; i < totalPage; i ++ ) {
            int endIndex = webSite.lastIndexOf( "/" );
            String tempWebSite = webSite.substring( 0, endIndex + 1 ) + ( i + 1 ) + ".htm";

            Common.downloadFile( tempWebSite, Common.tempDirectory, indexName, false, "" );
            Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

            String tempStr = Common.getFileString( Common.tempDirectory, indexEncodeName );
            String[] lines = tempStr.split( "\n" );

            for ( int count = 0; count < lines.length; count ++ ) {
                String line = lines[count];

                if ( line.matches( "(?s).*document.write(?s).*" ) ) {
                    String[] temp = line.split( "'\"|\"|'" );

                    // replace %20 from white space in URL
                    String frontURL = temp[3].replaceAll( "\\s", "%20" );
                    comicURL[i] = getFixedChineseURL( baseURL + frontURL );
                    System.out.print( i + " " );
                    Common.debugPrintln( comicURL[i] );

                    break;
                }
            }

        }
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "title  = " + title );
        Common.debugPrintln( "wholeTitle  = " + wholeTitle );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    public String getFixedChineseURL( String url ) {
        // ex. "收?的十二月" should be changed into
        //     "%E6%94%B6%E8%8E%B7%E7%9A%84%E5%8D%81%E4%BA%8C%E6%9C%88"

        try {
            String temp = "";

            for ( int k = 0; k < url.length(); k ++ ) {
                // \u0080-\uFFFF -> 中日韓3byte以上的字符
                if ( url.substring( k, k + 1 ).matches( "(?s).*[\u0080-\uFFFF]+(?s).*" ) )
                    temp += URLEncoder.encode( url.substring( k, k + 1 ), "UTF-8" );
                else
                    temp += url.substring( k, k + 1 );

            }
            url = temp;
        } catch ( Exception e ) { e.printStackTrace(); }


        url = fixSpecialCase( url );

        return url;
    }

    public String fixSpecialCase( String url ) {
        //  第一數（%E6%95%B8）要改成第一話（%E8%A9%B1）...不曉得是否為特例...
        url = url.replaceAll( "%E6%95%B8", "%E8%A9%B1" );
        // 話（%E6%95%B8）要改成?（%E8%AF%9D）...不曉得是否為特例...
        url = url.replaceAll( "%A9%B1", "%AF%9D" );
        //  石黑正?（%EF%BF%BD）要改成石黑正數（%E6%95%B8）...不曉得是否為特例...
        url = url.replaceAll( "%EF%BF%BD", "%E6%95%B8" );  // ex. http://kukudm.com/comiclist/1247/23363/1.htm

        return url;
    }
    
    
    public boolean isSingleVolumePage( String urlString ) {
        return urlString.matches( "(?s).*\\d+\\.htm(?s).*" );
    }
    
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_KUKU_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_KUKU_encode_", "html" );    
        
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        return Common.getFileString( Common.tempDirectory, indexEncodeName ); 
    }
    
    public String getTitleOnSingleVolumePage( String urlString ) {      
        return getTitle();
    }
    
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        String[] lines = allPageString.split( "\n" );

        int beginIndex = lines[0].indexOf( "<title>", 1 ) + 7;
        int endIndex = lines[0].indexOf( "_", beginIndex ) - 4;
        
        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( lines[0].substring( beginIndex, endIndex ) ) );
    }
    
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.
    
        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();
        
        String[] lines = allPageString.split( "\n" );
        
        int beginIndex = 0;
        int endIndex = 0;
        
        for ( int i = 0; i < lines.length; i ++ ) {
            String line = lines[i];

            if ( line.matches( "(?s).*socomic.com(?s).*" ) ) {
                Common.debugPrint( "開始解析各集位址：" );

                String[] temp = line.split( "'" );

                for ( String str : temp ) {
                    if ( str.matches( "(?s).*socomic.com(?s).*" ) )
                        urlList.add( str );
                }
                Common.debugPrint( "  ......解析各集位址完畢!!" );

                Common.debugPrintln( "開始解析各集名稱：" );
                
                beginIndex = endIndex = 0;
                while ( endIndex != -1 ) { // endIndex = -1 if no matches
                    int preEndIndex = endIndex;
                    if ( line.substring( beginIndex, endIndex ).length() < 100 &&
                         line.substring( beginIndex, endIndex ).length() > 2 &&
                         endIndex > 0 ) {
                        //System.out.println( Common.getTraditionalChinese( line.substring( beginIndex + 1, endIndex ) ) );
                        volumeList.add( Common.getTraditionalChinese( line.substring( beginIndex + 1, endIndex ) ) );
                    }

                    endIndex = line.indexOf( "</A>", preEndIndex + 1 );
                    //System.out.println( endIndex );
                    beginIndex = line.lastIndexOf( ">", endIndex );
                    //System.out.println( beginIndex );
                }
                Common.debugPrintln( "  ......解析各集名稱完畢!!" );

            }
        }
 
        combinationList.add( volumeList );
        combinationList.add( urlList );
            
        return combinationList;
    }
    
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, Common.tempDirectory, Common.tempVolumeFileName );
        Common.outputFile( urlList, Common.tempDirectory, Common.tempUrlFileName );
    }
    
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName };
    }
    
    public void printLogo() {
        System.out.println( " ______________________" );
        System.out.println( "|                      |" );
        System.out.println( "| Run the KUKU module: |" );
        System.out.println( "|______________  ______|\n" );
    }
}

