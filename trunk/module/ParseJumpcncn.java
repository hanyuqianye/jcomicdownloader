/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/11/2
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 1.13: 修復jumpcn.com.cn因置換伺服器而解析錯誤的問題。
 1.10: 增加對於jumpcn.com.cn的支援
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.text.*;

public class ParseJumpcncn extends ParseOnlineComicSite {
    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String orinialWholeTitle; // 簡體的wholeTitle
    
    /**
 *
 * @author user
 */
    public ParseJumpcncn() {
        siteID = Site.JUMPCNCN;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_jumpcn_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_jumpcn_encode_parse_", "html" );

        jsName = "index_jumpcn.js";
        radixNumber = 185271; // default value, not always be useful!!
        orinialWholeTitle = "";
    }

    public ParseJumpcncn( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() { // let all the non-set attributes get values
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );
            
        
        String allPageString = Common.getFileString( Common.tempDirectory, indexEncodeName );

        String[] tokens = allPageString.split( "\\d*>\\d*|\\d*<\\d*" );

        for ( int i = 0; i < tokens.length; i ++ ) {
            if ( tokens[i].matches( "(?s).*" + webSite + "(?s).*" ) ) {
                tokens[i+1] = tokens[i+1].replaceAll( "\\.", "" ); // 因為之後wholeTitle會變成網址，不能有"."出現

                orinialWholeTitle = tokens[i+1];
                if ( getWholeTitle() == null || getWholeTitle().equals(  "" ) ) 
                    setWholeTitle( Common.getTraditionalChinese( orinialWholeTitle ) );
                
                break;
            }
        }


        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() ); 
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得前面的下載伺服器網址
        String[] lines = Common.getFileStrings( Common.tempDirectory, indexName );
        
        // ex. http://www.jumpcn.com.cn/comic/2749/33313/
        
        int beginIndex = Common.getIndexOfOrderKeyword( webSite, "/", 4 ) + 1;
        int endIndex = Common.getIndexOfOrderKeyword( webSite, "/", 5 );
        
        String idString = webSite.substring( beginIndex, endIndex );
        Common.debugPrintln( "漫畫代號為：" + idString );
        
        Common.debugPrint( "開始解析這一集有幾頁 :" );
        Common.downloadFile( webSite + "index.js", Common.tempDirectory, jsName, false, "" );
        String allJsPageString = Common.getFileString( Common.tempDirectory, jsName );
        String[] jsTokens = allJsPageString.split( "=|;" ); // ex. var total=23;
        for ( int i = 0; i < jsTokens.length; i ++ ) {
            if ( jsTokens[i].matches( "(?s).*total" ) ) {
                totalPage = Integer.parseInt( jsTokens[i+1] );
                Common.debugPrintln( "共 " + totalPage + " 頁" );
                comicURL = new String [totalPage]; // totalPage = amount of comic pic
                break;
            }
        }
        
        Common.debugPrintln( "取得圖片網址父目錄 :" );
        /*
        String allPageString = Common.getFileString( Common.tempDirectory, indexName );

        beginIndex = allPageString.indexOf( "/Scripts/picshow.js" );
        endIndex = allPageString.indexOf( "\"", beginIndex );
        String jsURL = "http://www.jumpcn.com.cn/" + allPageString.substring( beginIndex, endIndex ); 
        Common.debugPrintln( "picshow.js的網址 : jsURL" );
        */
        
        allJsPageString = getAllPageString( "http://www.jumpcn.com.cn/Scripts/picshow.js" );
        beginIndex = allJsPageString.indexOf( "http://" );
        endIndex = allJsPageString.indexOf( "'", beginIndex );
        String baseURL =  allJsPageString.substring( beginIndex, endIndex ) + idString + "/" + orinialWholeTitle + "/";
        baseURL = baseURL.replaceAll( "\\s", "%20" );

        Common.debugPrintln( "開始解析每一頁圖片的網址 :" );
        
        // 因為有的檔名是001.jpg有的是1.jpg，所以先對第一張測試連線，檢查是哪種檔名格式
        boolean noNeedToAddZero = false;
        if ( Common.urlIsOK( getFixedChineseURL( baseURL ) + "1.jpg" ) )
            noNeedToAddZero = true;

        for ( int p = 1; p <= totalPage; p ++ ) {
            String frontURL = String.valueOf( p ) + ".jpg";
            
            if ( noNeedToAddZero )
                comicURL[p-1] = getFixedChineseURL( baseURL + frontURL );
            else {
                NumberFormat formatter = new DecimalFormat( "000" );
                String fileName = formatter.format( p ) + ".jpg";
                comicURL[p-1] = getFixedChineseURL( baseURL + fileName );
            }
            //Common.debugPrintln( p + " " + comicURL[p-1] );
        }
        //System.exit(0);
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_jumpcn_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_jumpcn_encode_", "html" );    
        
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        return Common.getFileString( Common.tempDirectory, indexEncodeName ); 
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*/\\d+/\\d+/(?s).*")  ) // ex. http://www.jumpcn.com.cn/comic/2749/33313/
            return true;
        else
            return false;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        // http://www.jumpcn.com.cn/comic/1/222/轉為http://www.jumpcn.com.cn/comic/1/
        
        int endIndex = Common.getIndexOfOrderKeyword( urlString, "/", 5 ) + 1;
        
        String mainPageUrlString = urlString.substring( 0, endIndex );

        return getTitleOnMainPage( mainPageUrlString, getAllPageString( mainPageUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        
        String[] tokens = allPageString.split( ">|<" );
        String title = "";
        
        for ( int i = 0; i < tokens.length; i ++ ) {
            // ex. <h2 class='fleft blue'> title </h2>
            if ( tokens[i].matches( "(?s).*'fleft blue'(?s).*" ) ) {
                title = tokens[i+1];
                break;
            }
        }

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "\"bookList\"" );
        int endIndex = allPageString.indexOf( "</table>" );
        String tempString = allPageString.substring( beginIndex, endIndex ); 
        String[] tokens = tempString.split( "\\d*>\\d*|\\d*<\\d*|\"" );
        
        int volumeCount = 0;
        
        for ( int i = 0; i < tokens.length; i ++ ) {
            if ( tokens[i].matches( "(?s).*http://www.pcomic.com.cn/(?s).*" ) ) {
                String idString = tokens[i].split( "/" )[tokens[i].split( "/" ).length-1];
                urlList.add( urlString + idString + "/" );
                //System.out.println( urlString + idString );
                
                // 取得單集名稱
                String volumeTitle = "";
                if ( tokens[i-4].equals( "" ) || tokens[i-4] == null )
                    volumeTitle = tokens[i-10].replaceAll( "\\.", "" ); // 避免標題中出現.
                else
                    volumeTitle = tokens[i-4].replaceAll( "\\.", "" );

                volumeList.add( Common.getStringRemovedIllegalChar( 
                    Common.getTraditionalChinese( volumeTitle.trim() ) ) );
                
                volumeCount ++;
            }
        }
        
        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " __________________________________" );
        System.out.println( "|                               |" );
        System.out.println( "| Run the JumpCNCN module: |" );
        System.out.println( "|__________________________________|\n" );
    }
}
