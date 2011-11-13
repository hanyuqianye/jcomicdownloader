/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/2
----------------------------------------------------------------------------------------------------
ChangeLog:
1.13: 新增對mangafox.com的支援
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;

public class ParseMangaFox extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;

    /**
     *
     * @author user
     */
    public ParseMangaFox() {
        siteID = Site.DMEDEN;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_mangaFox_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_mangaFox_encode_parse_", "html" );

        jsName = "index_mangaFox.js";
        radixNumber = 185271; // default value, not always be useful!!
    }

    public ParseMangaFox( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            setWholeTitle( "NULL_VOLUME_TITLE" );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = getAllPageString( webSite );
        Common.debugPrint( "開始解析這一集有幾頁 : " );
        int beginIndex = allPageString.indexOf( "</option>" );
        beginIndex = allPageString.indexOf( "of ", beginIndex ) + 3;
        int endIndex = allPageString.indexOf( "<", beginIndex );
        totalPage = Integer.parseInt( allPageString.substring( beginIndex, endIndex ).trim() );
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        // ex. http://www.mangafox.com/manga/kingdom_hearts/
        endIndex = webSite.lastIndexOf( "/" ) + 1;
        String baseURL = webSite.substring( 0, endIndex );

        for ( int p = 1 ; p <= totalPage ; p++ ) {
            // 檢查下一張圖是否存在同個資料夾，若存在就跳下一張
            if ( !Common.existPicFile( getDownloadDirectory(), p + 1 ) ) {
                allPageString = getAllPageString( baseURL + p + ".html" );

                beginIndex = allPageString.indexOf( "Back to" );
                beginIndex = allPageString.indexOf( "src=\"http", beginIndex ) + 5;
                endIndex = allPageString.indexOf( "\"", beginIndex );
                comicURL[p - 1] = allPageString.substring( beginIndex, endIndex );
                //Common.debugPrintln( p + " " + comicURL[p-1] ); // debug

                // 每解析一個網址就下載一張圖
                singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, 0 );
            }
        }
        //System.exit(0); // debug
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_mangaFox_", "html" );

        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );

        return Common.getFileString( Common.tempDirectory, indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex.http://www.mangafox.com/manga/kingdom_hearts/c030/1.html
        if ( urlString.split( "/" ).length > 6 ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        int endIndex = Common.getIndexOfOrderKeyword( urlString, "/", 5 ) + 1;
        String mainUrlString = urlString.substring( 0, endIndex );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {

        String[] tokens = allPageString.split( ">|<" );

        int beginIndex = allPageString.indexOf( "<h1>" ) + 4;
        int endIndex = allPageString.indexOf( "</h1>", beginIndex );
        String englishTitle = allPageString.substring( beginIndex, endIndex );

        beginIndex = allPageString.indexOf( "<td>", beginIndex ) + 4;
        endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, ";", "<" );
        String japanTitle = allPageString.substring( beginIndex, endIndex );

        String title = englishTitle; // + "(" + japanTitle + ")"; // 不加了，因為日文資料夾打不開...

        return Common.getStringRemovedIllegalChar( title );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int totalVolume = allPageString.split( "class=\"ch\"" ).length - 1;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        int beginIndex = allPageString.indexOf( "Chapter Name</th>" );
        String baseURL = "http://www.mangafox.com";

        int endIndex = 0;

        for ( int i = 0 ; i < totalVolume ; i++ ) {
            // 取得單集位址
            beginIndex = allPageString.indexOf( "href=\"/", beginIndex ) + 6;
            endIndex = allPageString.indexOf( "\"", beginIndex );
            String volumeUrl = baseURL + allPageString.substring( beginIndex, endIndex );
            urlList.add( volumeUrl );

            // 取得單集名稱
            beginIndex = allPageString.indexOf( "class=\"ch\"", beginIndex );
            beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
            //endIndex = allPageString.indexOf( "</td>", beginIndex );
            endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, "<span", "</td" );
            String volumeTitle = allPageString.substring(
                    beginIndex, endIndex ).trim().replaceAll( "</a>", "" ).replaceAll( ": ", "：" );
            volumeList.add( volumeTitle );
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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ____________________________________" );
        System.out.println( "|                                 |" );
        System.out.println( "| Run the MangaFox module:     |" );
        System.out.println( "|_____________________________________|\n" );
    }
}
