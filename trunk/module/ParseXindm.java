/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/9
----------------------------------------------------------------------------------------------------
ChangeLog:
1.15: 新增對xindm.cn的支援
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.encode.Encoding;

public class ParseXindm extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;

    /**
     *
     * @author user
     */
    public ParseXindm() {
        siteID = Site.XINDM;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_xindm_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_xindm_encode_parse_", "html" );

        jsName = "index_xindm.js";
        radixNumber = 185273; // default value, not always be useful!!
    }

    public ParseXindm( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String allPageString = getAllPageString( webSite );
            int beginIndex = Common.getIndexOfOrderKeyword( allPageString, ">>", 4 ) + 2;
            int endIndex = allPageString.indexOf( "<", beginIndex );
            String title = allPageString.substring( beginIndex, endIndex ).trim();

            setWholeTitle( Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = getAllPageString( webSite );
        Common.debugPrint( "開始解析這一集有幾頁 : " );

        totalPage = allPageString.split( "</option>" ).length - 1;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        // ex. http://76.manmankan.com/2011/201111/1916/43124/001.jpg
        String baseURL = "http://mh2.xindm.cn";


        int beginIndex = allPageString.indexOf( "../" ) + 2;
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        String firstPicFrontURL = allPageString.substring( beginIndex, endIndex );

        endIndex = firstPicFrontURL.lastIndexOf( "_" ) + 1;
        String frontURL = baseURL + firstPicFrontURL.substring( 0, endIndex );

        beginIndex = firstPicFrontURL.indexOf( "." );
        String extensionName = firstPicFrontURL.substring( beginIndex, firstPicFrontURL.length() );

        NumberFormat formatter = new DecimalFormat( "000" );

        // 須取得cookie才能下載圖片（防盜連專家....）
        String[] cookies = Common.getCookieStrings( webSite );
        String cookieString = "";

        int cookieCount = 0; // 取得前兩組cookie就可以了
        cookieString = cookies[0] + "; " + cookies[1];

        for ( int p = 1 ; p <= totalPage ; p++ ) {
            /* // 原本想用這種方法比較有彈性，但問題是最後一頁都會錯誤（500），只好放棄......
            allPageString = getAllPageString( webSite + "&page=" + p );
            if ( p == totalPage )
            allPageString = getAllPageString( webSite.replace( "mh.xindm.cn", "www.xindm.cn" ) + "&page=" + p );
            
            beginIndex = allPageString.indexOf( "../" ) + 2;
            endIndex = allPageString.indexOf( "\"", beginIndex );
            String fontURL = allPageString.substring( beginIndex, endIndex );
            comicURL[p-1] = baseURL + fontURL;
            
            // 每解析一個網址就下載一張圖
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[p-1], totalPage, p, 0 );
             */

            comicURL[p - 1] = getFixedChineseURL( frontURL + formatter.format( p - 1 ) + 
                                                        formatter.format( p ) + extensionName );
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, 0, true, cookieString );

            //Common.debugPrintln( (p) + " " + comicURL[p - 1] ); // debug
        }
        //System.exit(1); // debug
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_xindm_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_xindm_encode_", "html" );
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        return Common.getFileString( Common.tempDirectory, indexEncodeName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex.http://mh2.xindm.cn/display.asp?id=55200
        if ( urlString.matches( "(?s).*display.asp(?s).*" ) ) {
            return true;
        } else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://mh2.xindm.cn/display.asp?id=55200轉為
        //     http://xindm.cn/type.asp?typeid=5114 // 太難做不到

        return "";
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String allPageString = getAllPageString( urlString );
        int beginIndex = Common.getIndexOfOrderKeyword( allPageString, ">>", 3 ) + 2;
        int endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, "[", ">>" );
        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = Common.getIndexOfOrderKeyword( allPageString, ">>", 1 ) + 2;
        int endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, "[", ">>" );
        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "http://mh2.xindm" ) - 1;
        int endIndex = allPageString.lastIndexOf( "http://mh.xindm.cn" );
        String listString = allPageString.substring( beginIndex, endIndex );

        String[] tokens = listString.split( "'|>|<" );

        int volumeCount = 0; // 計算總共集數

        for ( int i = 0 ; i < tokens.length ; i++ ) {

            if ( tokens[i].matches( "http://mh2.xindm(?s).*" ) ) {
                // 取得單集位址
                urlList.add( tokens[i] );

                // 取得單集名稱
                volumeList.add( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( tokens[i + 4].trim() ) ) );

                volumeCount++;
            }
        }

        Common.debugPrintln( "共有" + volumeCount + "集" );

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
        System.out.println( " _________________________________" );
        System.out.println( "|                              |" );
        System.out.println( "| Run the XinDM module:     |" );
        System.out.println( "|__________________________________|\n" );
    }
}