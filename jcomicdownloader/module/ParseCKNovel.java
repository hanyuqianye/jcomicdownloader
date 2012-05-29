/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2012/5/10
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 *  3.19: 1. 新增對ck101的支援。
 ----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.ComicDownGUI;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Encoding;

public class ParseCKNovel extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;
    protected int floorCountInOnePage; // 一頁有幾層樓

    /**

     @author user
     */
    public ParseCKNovel() {
        siteID = Site.CK_NOVEL;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_novel_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_novel_encode_parse_", "html" );

        jsName = "index_ck_novel.js";
        radixNumber = 151261; // default value, not always be useful!!

        baseURL = "http://ck101.com";
        
        floorCountInOnePage = 10; // 一頁有幾層樓
    }

    public ParseCKNovel( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );
        

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );

        
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );
        Common.debugPrint( "開始解析這一集有幾頁 : " );
        
        // 將
        //setDownloadDirectory( SetUp.getOriginalDownloadDirectory() + getTitle() + Common.getSlash() );
        System.out.println( getDownloadDirectory() );

        int beginIndex = 0, endIndex = 0;

        beginIndex = allPageString.indexOf( "class=\"last\"" );

        if ( beginIndex > 0 ) { // 代表超過一面( > 10 )
            beginIndex = allPageString.indexOf( " ", beginIndex ) + 1;
            endIndex = allPageString.indexOf( "<", beginIndex );
            String tempString = allPageString.substring( beginIndex, endIndex ).trim();
            totalPage = Integer.parseInt( tempString );
        }
        else {
            beginIndex = allPageString.indexOf( "class=\"pgt\"" );
            endIndex = allPageString.indexOf( "class=\"nxt\"", beginIndex );
            
            if ( endIndex > 0 ) { // 超過一頁
                String tempString = allPageString.substring( beginIndex, endIndex );
                totalPage = tempString.split( "a href=" ).length - 1;
            }
            else
                totalPage = 1; // 只有一頁
        }
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        String pageURL = webSite;
        int p = 1; // 目前頁數
        for ( int i = 0; i < totalPage && Run.isAlive; i++ ) {
            pageURL = pageURL.replaceAll( "-" + i + "-", "-" + p + "-" );

            comicURL[i] = pageURL;
            Common.debugPrintln( i + " " + comicURL[i] ); // debug

            // 每解析一個網址就下載一張圖
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[i], totalPage, p, 0 );
            p++;
            //Common.downloadFile( comicURL[p - 1], "", p + ".jpg", false, "" );

        }

        hadleWholeNovel( webSite );  // 處理小說主函式
        //System.exit( 0 ); // debug
    }

    // 處理小說主函式
    public void hadleWholeNovel( String url ) {
        String allPageString = "";
        String allNovelText = getInformation( title, url ); // 全部頁面加起來小說文字
        
        String[] fileList = new File( getDownloadDirectory() ).list(); // 取得下載資料夾內所有網頁名稱清單
        Arrays.sort( fileList ); // 對檔案清單作排序

        // int lastPage = fileList.length - 1;
        Common.debugPrintln( "共有" + ( fileList.length ) + "頁" );
        for ( int i = 0; i < fileList.length; i++ ) {
            Common.debugPrintln( "處理第" + ( i + 1 ) + "頁: " + fileList[i] );
            allPageString = Common.getFileString( getDownloadDirectory(), fileList[i] );

            allNovelText += getRegularNovel( allPageString, i ); // 每一頁處理過都加總起來 
        
            ComicDownGUI.stateBar.setText( getTitle() + 
                "合併中: " + ( i + 1 ) + " / " + fileList.length );
        }

        String tempString = getDownloadDirectory();
        tempString = tempString.substring( 0, tempString.length() - 1 );
        int endIndex = tempString.lastIndexOf( Common.getSlash() ) + 1;

        String textOutputDirectory = tempString.substring( 0, endIndex ); // 放在外面

        //Common.debugPrintln( "OLD: " + getDownloadDirectory() );
        //Common.debugPrintln( "NEW: " + textOutputDirectory );

        if ( SetUp.getDeleteOriginalPic() ) { // 若有勾選原檔就刪除原始未合併文件
            Common.deleteFolder( getDownloadDirectory() ); // 刪除存放原始網頁檔的資料夾
        }
        Common.outputFile( allNovelText, textOutputDirectory, getWholeTitle() + "." + Common.getDefaultTextExtension() );

        textFilePath = textOutputDirectory + getWholeTitle() + "." + Common.getDefaultTextExtension();
    }

    // 處理小說網頁，將標籤去除
    public String getRegularNovel( String allPageString, int nowPage ) {
        int beginIndex = 0;
        int endIndex = 0;
        int amountOfFloor = 10; // 一頁有幾樓
        String oneFloorText = ""; // 單一樓層的文字
        String allFloorText = ""; // 所有樓層的文字加總

        for ( int i = 0; i < amountOfFloor; i++ ) {
            beginIndex = endIndex;
            beginIndex = allPageString.indexOf( "class=\"t_fsz\"", beginIndex );
            if ( beginIndex > 0 ) {
                beginIndex = allPageString.indexOf( "<table", beginIndex );
                endIndex = allPageString.indexOf( "</table>", beginIndex );
                oneFloorText = allPageString.substring( beginIndex, endIndex );

                if ( SetUp.getDefaultTextOutputFormat() == FileFormatEnum.HTML ) {
                    oneFloorText = replaceProcessToHtml( oneFloorText );
                    allFloorText += oneFloorText + 
                    "<br><br>" + ( i + nowPage * floorCountInOnePage ) + "<br><hr><br>"; // 每一樓的文字加總起來
                }
                else {
                    oneFloorText = replaceProcessToText( oneFloorText );
                    allFloorText += oneFloorText + 
                    "\n\n--------------------------------------------" + 
                        ( i + nowPage * floorCountInOnePage ) + "\n"; // 每一樓的文字加總起來
                }
                //Common.debugPrintln( "\n\n第" + i +  "樓\n\n" );
                //Common.debugPrintln( oneFloorText );

                

                Common.debugPrint( i + " " );

            }
        }
        return allFloorText;
    }

    

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override // 因為原檔就是utf8了，所以無須轉碼
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_", "html" );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        return Common.getFileString( SetUp.getTempDirectory(), indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://ck101.com/thread-2081113-1-1.html
        // 都判斷為主頁，並直接下載。
        return false;
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        return volumeURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex, endIndex;

        beginIndex = allPageString.indexOf( "name=\"keywords\"" );
        beginIndex = allPageString.indexOf( "content=", beginIndex );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );

        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( title );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();



        // 取得單集名稱
        String volumeTitle = getTitle();
        volumeList.add( Common.getStringRemovedIllegalChar( volumeTitle.trim() ) );

        // 取得單集位址
        urlList.add( urlString );

        totalVolume = 1;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, SetUp.getTempDirectory(), Common.tempVolumeFileName );
        Common.outputFile( urlList, SetUp.getTempDirectory(), Common.tempUrlFileName );
    }

    @Override
    public String[] getTempFileNames() {
        return new String[]{indexName, indexEncodeName, jsName};
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the CK101 Novel module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
