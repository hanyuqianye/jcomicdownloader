/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        else
            siteID = Site.UNKNOWN;
    }
    public int getSiteID() {
        return siteID;
    }
}

