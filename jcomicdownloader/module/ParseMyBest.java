/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2012/5/11
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  3.19: 1. 新增對mybest的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.ComicDownGUI;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Encoding;

public class ParseMyBest extends ParseCKNovel {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseMyBest() {
        siteID = Site.MYBEST;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_mybest_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_mybest_encode_parse_", "html" );

        jsName = "index_mybest.js";
        radixNumber = 151261; // default value, not always be useful!!

        baseURL = "http://mybest.com.hk";
    }

    public ParseMyBest( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );
        Common.debugPrint( "開始解析這一集有幾頁 : " );

        int beginIndex = 0, endIndex = 0;
        
        beginIndex = allPageString.indexOf( "class=\"last\"" );

        if ( beginIndex > 0 ) { // 代表超過一頁
            beginIndex = allPageString.indexOf( " ", beginIndex ) + 1;
            endIndex = allPageString.indexOf( "<", beginIndex );
            String tempString = allPageString.substring( beginIndex, endIndex ).trim();
            totalPage = Integer.parseInt( tempString );
        }
        else {
            beginIndex = allPageString.indexOf( "class=\"pgt\"" );
            endIndex = allPageString.indexOf( "class=\"nxt\"", beginIndex );
            String tempString = allPageString.substring( beginIndex, endIndex );
            totalPage = tempString.split( "a href=" ).length - 1;
        }
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        String pageURL = webSite + "&extra=&page=";
        int p = 1; // 目前頁數
        for ( int i = 0 ; i < totalPage ; i++ ) {
            comicURL[i] = pageURL + p;
            //Common.debugPrintln( i + " " + comicURL[i] ); // debug
            
            // 每解析一個網址就下載一張圖
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[i], totalPage, p, 0 );
            p ++;
            
        }
        
        //System.exit( 0 ); // debug
        hadleWholeNovel();  // 處理小說主函式
        
    }
    
    // 處理小說主函式
    @Override
    public void hadleWholeNovel() {
        String allPageString = "";
        String allNovelText = ""; // 全部頁面加起來小說文字
        
        String[] fileList = new File( getDownloadDirectory() ).list(); // 取得下載資料夾內所有網頁名稱清單
       Arrays.sort( fileList ); // 對檔案清單作排序
        
        // int lastPage = fileList.length - 1;
        Common.debugPrintln( "共有" + ( fileList.length ) + "頁" );
        for ( int i = 0; i < fileList.length; i ++ ) {
            Common.debugPrintln( "處理第" + ( i + 1 ) + "頁: " + fileList[i] );
            Common.newEncodeFile( getDownloadDirectory(), 
                fileList[i], "utf8_" + fileList[i], Encoding.BIG5 );
            
            allPageString = Common.getFileString( getDownloadDirectory(), "utf8_" + fileList[i] );
            Common.deleteFile( getDownloadDirectory(), "utf8_" + fileList[i] ); // 刪掉utf8編碼的暫存檔
            
            allNovelText += getRegularNovel( allPageString ); // 每一頁處理過都加總起來 
            
            ComicDownGUI.stateBar.setText( getTitle() + 
                "合併中: " + ( i + 1 ) + " / " + fileList.length );
        }
        
        String tempString = getDownloadDirectory();
        tempString = tempString.substring( 0, tempString.length() - 1 );
        int endIndex = tempString.lastIndexOf( Common.getSlash() ) + 1;

        String textOutputDirectory = tempString.substring( 0, endIndex ); // 放在外面
        
        //Common.debugPrintln( "OLD: " + getDownloadDirectory() );
        //Common.debugPrintln( "NEW: " + textOutputDirectory );
        
        //allNovelText = Common.getBig5toUtf8( allNovelText );
        
        if ( SetUp.getDeleteOriginalPic() ) { // 若有勾選原檔就刪除原始未合併文件
            Common.deleteFolder( getDownloadDirectory() ); // 刪除存放原始網頁檔的資料夾
        }
        Common.outputFile(  allNovelText, textOutputDirectory, getWholeTitle() + ".txt" );
        
        textFilePath = textOutputDirectory + getWholeTitle() + ".txt";
    }
    
    

    @Override // 因為原檔就是utf8了，所以無須轉碼
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_mybest_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_mybest_encode_parse", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.BIG5 );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the MyBest module:     " );
        System.out.println( "|_______________________________\n" );
    }
}