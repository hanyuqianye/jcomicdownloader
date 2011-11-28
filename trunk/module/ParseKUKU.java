/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/10/25
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 1.15: 修正編碼為GBK
 1.11: 1. 改成一邊解析網址一邊下載。
 *    2. 修復kuku的美食的俘虜解析網址錯誤的bug。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.util.*;
import java.net.*;
import jcomicdownloader.encode.Encoding;

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


    @Override
    public synchronized void setParameters() {
        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName, Encoding.GBK );

        String tempStr = Common.getFileString( Common.tempDirectory, indexEncodeName );
        String[] lines = tempStr.split( "\n" );

        for ( int i = 0; i < lines.length; i ++ ) {
            String line = Common.getTraditionalChinese( lines[i] );

            // ".": contain all characters except "\r" and "\n"
            // "(?s).": contain all characters
            if ( line.matches( "(?s).*title(?s).*" ) ) {
                // get title ex.<title>尸錄 4話</title>
                String[] temp = line.split( "<|>" );
                
                if ( getWholeTitle() == null || getWholeTitle().equals(  "" ) )
                    setWholeTitle( Common.getStringRemovedIllegalChar( temp[2] ) );
            }
            else if ( line.matches( "(?s).*page(?s).*" ) ) {
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
    
    @Override
    public synchronized void parseComicURL() {
        System.out.print( "parse the pic URL:" );

        for ( int i = 0; i < totalPage; i ++ ) {
            // 檢查下一張圖是否存在同個資料夾，若存在就跳下一張
            if ( !Common.existPicFile( getDownloadDirectory(), i + 2 ) ) {
                int endIndex = webSite.lastIndexOf( "/" );
                String tempWebSite = webSite.substring( 0, endIndex + 1 ) + ( i + 1 ) + ".htm";

                Common.downloadFile( tempWebSite, Common.tempDirectory, indexName, false, "" );
                Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName, Encoding.GBK );

                String tempStr = Common.getFileString( Common.tempDirectory, indexEncodeName );
                String[] lines = tempStr.split( "\n" );

                for ( int count = 0; count < lines.length; count ++ ) {
                    String line = lines[count];

                    if ( line.matches( "(?s).*document.write(?s).*" ) ) {
                        String[] temp = line.split( "'\"|\"|'" );

                        // replace %20 from white space in URL
                        String frontURL = temp[3].replaceAll( "\\s", "%20" );
                        comicURL[i] = getFixedChineseURL( baseURL + frontURL );
                        Common.debugPrintln( i + " " + comicURL[i] ); // debug

                        // 每解析一個網址就下載一張圖
                        singlePageDownload( getTitle(), getWholeTitle(), comicURL[i], totalPage, i + 1, 0 );

                        break;
                    }
                }
            }
        }
        //System.exit( 0 ); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "title  = " + title );
        Common.debugPrintln( "wholeTitle  = " + wholeTitle );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }
    
    @Override
    public boolean isSingleVolumePage( String urlString ) {
        return urlString.matches( "(?s).*\\d+\\.htm(?s).*" );
    }
    
    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_KUKU_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_KUKU_encode_", "html" );    
        
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName, Encoding.GBK );

        return Common.getFileString( Common.tempDirectory, indexEncodeName ); 
    }
    
    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {      
        return getTitle();
    }
    
    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        String[] lines = allPageString.split( "\n" );

        int beginIndex = lines[0].indexOf( "<title>", 1 ) + 7;
        int endIndex = lines[0].indexOf( "_", beginIndex ) - 4;
        
        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( lines[0].substring( beginIndex, endIndex ) ) );
    }
    
    @Override
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
    
    @Override
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, Common.tempDirectory, Common.tempVolumeFileName );
        Common.outputFile( urlList, Common.tempDirectory, Common.tempUrlFileName );
    }
    
    @Override
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName };
    }
    
    @Override
    public void printLogo() {
        System.out.println( " ______________________" );
        System.out.println( "|                      |" );
        System.out.println( "| Run the KUKU module: |" );
        System.out.println( "|______________  ______|\n" );
    }
}

