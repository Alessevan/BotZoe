package fr.bakaaless.botzoe.bot.music;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class YoutubeSearch {

    private static int count = 0;

    public static List<SearchResult> search(String customsite, String num, String input) throws IOException {
        List<SearchResult> results = new ArrayList<SearchResult>();

        String google = "http://www.google.com/search?q=";
        String defnum = "&num=";
        String charset = "UTF-8";
        String userAgent = "DiscordBot";

        Elements links = Jsoup.connect(google+ URLEncoder.encode(input,charset)+ defnum + num + customsite)
                .timeout(0)
                .userAgent(userAgent)
                .get()
                .select(".g>.r>a");

        for( Element link : links ) {
            String title = link.text();
            String url = link.absUrl("href");

            //decode link from link of google.
            String urlLink = URLDecoder.decode(url.substring(url.indexOf("=")+1 , url.indexOf("&")) , charset);

            if( ! urlLink.startsWith("http") ) {
                continue;    //ads/news etc
            }
            results.add(new SearchResult(title, null, urlLink, null, null));
            count++;
        }
        return results;
    }

    public static List<SearchResult> youtubeSearch(int num, String input) throws IOException {
        List<SearchResult> results = new ArrayList<>();

        String ytsite = "https://www.youtube.com/results?search_query=";
        Document doc = Jsoup.connect(ytsite + input).timeout(0).get();

        Elements p = doc.select("div#results").select("ol.item-section").select("li:not([class])");

        for( Element n : p ) {
            Elements block = n.select("div.yt-lockup-content");
            String urltitle = block.select(".yt-lockup-title").select(".yt-uix-tile-link").text();
            String url = "https://www.youtube.com" + block.select(".yt-lockup-title").select(".yt-uix-tile-link").select("a").attr("href");
            String author = block.select(".yt-lockup-byline").select("a").text();
            String text = block.select(".yt-lockup-description").text();

            if("".equals(urltitle))
                continue;

            results.add(new SearchResult(urltitle, author, url, text, null));
            count++;
            if(count == num) break;
        }
        return results;
    }

    @SuppressWarnings("unused")
    public static List<SearchResult> lyricsSearch(String input) throws IOException {
        List<SearchResult> results = new ArrayList<SearchResult>();

        String lyricsite = "https://genius.com/search?q=" + input.replaceAll(" ", "-");
        Document doc = Jsoup.connect(lyricsite).timeout(0).get();
        String title = doc.title();

        Elements p = doc.select("div.search_results_container").get(0).select("ul.search_results").select("li");
        for( Element n : p )
        {
            String urltitle = n.select("span.song_title").text();
            String url = n.select("a.song_link").attr("href");
            String author = n.select(".artist_name").text();

            results.add(new SearchResult(urltitle, author, url, null, null));
            count++;
        }

        System.out.println("Search#lyricsSearch --> " + input + " : " + count +" results");
        return results;
    }

    @SuppressWarnings("unused")
    public static List<SearchResult> IMDbSearch(String input) throws IOException
    {
        List<SearchResult> results = new ArrayList<>();

        String IMDBsite = "http://www.imdb.com/find?q=" + input.replaceAll(" ", "+");
        Document doc = Jsoup.connect(IMDBsite).timeout(0).get();

        Elements p = doc.select("div#root").get(0).select("div.pagecontent").select("div#content-2-wide").select("div.article");

        //Site Title
        String title = p.select("h1.findHeader").text();

        Elements section = p.select("div.findSection");

        int totalcount = 0;

        //Titles
        for( Element t : section ) {
            //Avoid Array IndexOutOfBoundsException which causes infinite printStackTrace
            while(count < t.select("tbody").select("tr").size() && count < 4) {
                String type = t.select(".findSectionHeader").text(); //Titles, names, or Characters
                String urltitle = t.select("tbody").select("tr").get(count).select("td.result_text").text();
                String url = "http://www.imdb.com" + t.select("tbody").select("tr").get(count).select("td.result_text>a").attr("href");

                results.add(new SearchResult(urltitle, null, url, type, null));
                count++;
            }
            count = 0;
            totalcount++;
        }

        return results;
    }


    public static class SearchResult {
        private String title;
        private String author;
        private String link;
        private String text;
        private String thumbnail;

        /**
         * Null SearchResult Constructor
         */
        public SearchResult()
        {
            this.title = null;
            this.author = null;
            this.link = null;
            this.text = null;
            this.thumbnail = null;
        }

        /**
         * @param title SearchResult title
         * @param link SearchResult link
         * @param author SearchResult lyrics author/artist
         * @param text SearchResult description
         * @param thumbnail SearchResult album picture, icon
         */
        public SearchResult(String title, String author, String link, String text, String thumbnail)
        {
            this.title = title;
            this.author = author;
            this.link = link;
            this.text = text;
            this.thumbnail = thumbnail;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getAuthor()
        {
            return author;
        }

        public void setAuthor(String info)
        {
            this.author = info;
        }

        public String getLink()
        {
            return link;
        }

        public void setLink(String link)
        {
            this.link = link;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

    }

}
