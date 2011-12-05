/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/10/25
 ----------------------------------------------------------------------------------------------------
 ChangeLog:

----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.enums.*;
import jcomicdownloader.*;

/**
 * 解析網址是屬於哪一個網站
 * */
public class ParseWebPage {
    private String webSite;
    private int siteID;

    /**
 *
 * @author user
 */
    private ParseWebPage() {}
    public ParseWebPage( String webSite ) {
        this();
        this.webSite = webSite;

        parseSiteID( webSite );
    }

    private void downFile() {}

    private void parseSiteID( String webSite ) {
        if ( webSite.matches( "(?s).*89890.com(?s).*" ) )
            siteID = Site.CC;
        else if ( webSite.matches( "(?s).*kukudm.com(?s).*" ) ||
                  webSite.matches( "(?s).*socomic.com(?s).*" ) ||
                  webSite.matches( "(?s).*socomic.net(?s).*" ) )
            siteID = Site.KUKU;
        else if ( webSite.matches( "(?s).*e-hentai(?s).*" ) )
            siteID = Site.EH;
        else if ( webSite.matches( "(?s).*exhentai.org(?s).*" ) )
            siteID = Site.EX;
        else if ( webSite.matches( "(?s).*99manga.com(?s).*" ) )
            siteID = Site.NINENINE_MANGA;
        else if ( webSite.matches( "(?s).*99comic.com(?s).*" ) )
            siteID = Site.NINENINE_COMIC;
        else if ( webSite.matches( "(?s).*99mh.com(?s).*" ) )
            siteID = Site.NINENINE_MH;
        else if ( webSite.matches( "(?s).*99770.cc(?s).*" ) )
            siteID = Site.NINENINE_99770;
        else if ( webSite.matches( "(?s).*cococomic.com(?s).*" ) )
            siteID = Site.NINENINE_COCO;
        else if ( webSite.matches( "(?s).*1mh.com(?s).*" ) )
            siteID = Site.NINENINE_1MH;
        else if ( webSite.matches( "(?s).*3gmanhua.com(?s).*" ) )
            siteID = Site.NINENINE_3G;
        //else if ( webSite.matches( "(?s).*\\.178.com(?s).*" ) )
        //    siteID = Site.ONE_SEVEN_EIGHT;
        else if ( webSite.matches( "(?s).*\\.8comic.com(?s).*" ) ) {
            if ( webSite.matches( "(?s).*photo(?s).*" ) || 
                webSite.matches( "(?s).*PHOTO(?s).*" ) ||
                webSite.matches( "(?s).*Photo(?s).*" ) ) // 圖集
                siteID = Site.EIGHT_COMIC_PHOTO;
            else // 漫畫
                siteID = Site.EIGHT_COMIC;
        }
        else if ( webSite.matches( "(?s).*\\.jumpcn.com.cn(?s).*" ) )
            siteID = Site.JUMPCNCN;
        else if ( webSite.matches( "(?s).*dmeden\\.(?s).*" ) )
            siteID = Site.DMEDEN;
        else if ( webSite.matches( "(?s).*\\.jumpcn.com/(?s).*" ) )
            siteID = Site.JUMPCN;
        else if ( webSite.matches( "(?s).*\\.mangafox.com/(?s).*" ) )
            siteID = Site.MANGAFOX;
        else if ( webSite.matches( "(?s).*\\.manmankan.com/(?s).*" ) )
            siteID = Site.MANMANKAN;
        else if ( webSite.matches( "(?s).*xindm.cn/(?s).*" ) )
            siteID = Site.XINDM;
        else if ( webSite.matches( "(?s).*comic.92wy.com(?s).*" ) ) 
            siteID = Site.WY;
        else if ( webSite.matches( "(?s).*google.com(?s).*" ) ) 
            siteID = Site.GOOGLE_PIC;
        else if ( webSite.matches( "(?s).*nanadm.com(?s).*" ) ) 
            siteID = Site.NANA;
        else if ( webSite.matches( "(?s).*citymanga.com(?s).*" ) ) 
            siteID = Site.CITY_MANGA;
        else if ( webSite.matches( "(?s).*iibq.com(?s).*" ) ) 
            siteID = Site.IIBQ;
        
        else
            siteID = Site.UNKNOWN;
    }
    public int getSiteID() {
        return siteID;
    }
}

