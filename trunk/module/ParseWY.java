/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/2
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  1.16: 1. 新增新增對comic.92wy.com的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;

public class ParseWY extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseWY() {
        siteID = Site.WY;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_92wy_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_92wy_encode_parse_", "html" );

        jsName = "index_92wy.js";
        radixNumber = 18522371; // default value, not always be useful!!

        baseURL = "http://comic.92wy.com/go/";
    }

    public ParseWY( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String allPageString = Common.getFileString( Common.tempDirectory, indexName );

            int beginIndex = allPageString.indexOf( "<title>" ) + 7;
            beginIndex = allPageString.indexOf( ",", beginIndex ) + 1;
            int endIndex = allPageString.indexOf( ",", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex );

            setWholeTitle( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( tempTitleString.trim() ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = Common.getFileString( Common.tempDirectory, indexName );
        Common.debugPrint( "開始解析這一集有幾頁 : " );

        int beginIndex = allPageString.indexOf( "class=\"pages\"" );
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 2;
        int endIndex = allPageString.indexOf( "<", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex ).trim();

        totalPage = Integer.parseInt( tempString );
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        // 開始取得每一頁的基本網址 ex. http://comic.92wy.com/go/show_6766_205790_
        endIndex = webSite.lastIndexOf( "_" ) + 1;
        String fontURL = webSite.substring( 0, endIndex );

        for ( int p = 1 ; p <= totalPage ; p++ ) {

            // 檢查下一張圖是否存在同個資料夾，若存在就跳下一張
            if ( !Common.existPicFile( getDownloadDirectory(), p + 1 ) ) {
                String url = fontURL + p + ".htm";
                Common.downloadFile( url, Common.tempDirectory, indexName, false, "" );
                allPageString = Common.getFileString( Common.tempDirectory, indexName );

                beginIndex = allPageString.indexOf( "id=\"picture\"" );
                beginIndex = allPageString.indexOf( "src=\"", beginIndex ) + 5;
                endIndex = allPageString.indexOf( "\"", beginIndex );
                comicURL[p - 1] = allPageString.substring( beginIndex, endIndex ).trim();

                //Common.debugPrintln( p + " [" + comicURL[p-1] + "]" ); // debug

                // 每解析一個網址就下載一張圖
                singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, 0 );
            }
        }
        //System.exit(0); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_92wy_", "html" );

        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );

        return Common.getFileString( Common.tempDirectory, indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( !urlString.matches( "(?s).*/info_(?s).*" ) ) // ex. http://comic.92wy.com/go/info_6766.htm
        {
            return true;
        } else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://comic.92wy.com/go/show_6766_205790_1.htm轉為
        //     http://comic.92wy.com/go/info_6766.htm

        String idString = volumeURL.split( "_" )[1]; // 取第二個
        String mainPageURL = baseURL + "info_" + idString + ".htm";

        return mainPageURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = allPageString.indexOf( "<title>" ) + 7;
        int endIndex = allPageString.indexOf( ",", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex );

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "</h5>" );
        int endIndex = allPageString.indexOf( "</p>", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex );
        String[] tokens = tempString.split( "\"" );

        int volumeCount = 0;

        for ( int i = 0 ; i < tokens.length ; i++ ) {
            if ( tokens[i].matches( "(?s).*\\.htm(?s).*" ) ) {
                urlList.add( baseURL + tokens[i] );

                // 取得單集名稱
                String volumeTitle = tokens[i + 4];
                volumeList.add( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( volumeTitle.trim() ) ) );

                volumeCount++;
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
        System.out.println( " ______________________________" );
        System.out.println( "|                            |" );
        System.out.println( "| Run the 92wy module:     |" );
        System.out.println( "|_______________________________|\n" );
    }
}
