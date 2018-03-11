package me.fun.inspirational.crawl.vo;

public class DefaultFetcherVO {
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    private String url;//URL to fetch

    private String html;//fetchHTML;
    public String getHtml() {
        return html;
    }
    public void setHtml(String html) {
        this.html = html;
    }
}
