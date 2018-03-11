package me.fun.inspirational.crawl.flow;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

public class InspirationalCrawler {

    final static String SEED = "https://wall.alphacoders.com/by_sub_category.php?id=38404&name=Motivational+Wallpapers&page=1";
//    final static String SEED11 = "https://wall.alphacoders.com/by_sub_category.php?id=38404&name=Motivational+Wallpapers&page=12";
    final static String SELECTOR_TO_DETAIL_PAGE = "* div#container_page div[class=center] div[class=boxgrid] a";
    final static String SELECTOR_TO_CAPTURE = "* div#container_page div[class=row] span[class=btn btn-success download-button]";
    final static String OUTPUT_PATH = "G:\\Dev\\Test\\product_images";
    final static String MOTHOR_DOMAIN = "https://wall.alphacoders.com";
    final static String SELECTOR_NEXT_LNK = "* #container_page div[class=hidden-xs visible-sm] ul[class=pagination] li:last-child a";

    public static void main(String[] args) throws  IOException, InterruptedException{
        try {
            System.out.println("Start...");
            CloseableHttpClient reusedClient = null;
            reusedClient = HttpClientBuilder.create().build();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            CloseableHttpResponse seedResponse = downloadGetRequestAsResponse(reusedClient,InspirationalCrawler.createGetRequest(SEED));
            int page = 1;
            if(null != seedResponse){
                String initialHtmlResponseStr = EntityUtils.toString(seedResponse.getEntity(), "UTF-8");
                boolean hasNextLink = true;
                String[] nextLink = null;
                Document initDoc = Jsoup.parse(initialHtmlResponseStr,"UTF-8");
                while(hasNextLink){
                    System.out.println("Processing page number: " + page);
                    nextLink = loopRequest(initDoc);
                    System.out.println("Next link: " + MOTHOR_DOMAIN.concat("/").concat(nextLink[0]));

                    String newFolderPath = OUTPUT_PATH.concat(File.separator).concat(new DecimalFormat("000").format(page));
                    File newFolder = new File(newFolderPath);
                    if(!newFolder.mkdir())
                        throw new IOException("Cannot make new folder...");

                    String[] allDetailsPages = listDetailPages(initDoc);
                    if(null != allDetailsPages){
                        System.out.println("We got: " + allDetailsPages.length + " in this mother page...");
                        for (String detailUrl: allDetailsPages) {
                            System.out.println("Begin detail url:... " + MOTHOR_DOMAIN.concat("/").concat(detailUrl));
                            CloseableHttpResponse detailDocumentResponse = downloadGetRequestAsResponse(reusedClient, InspirationalCrawler.createGetRequest(MOTHOR_DOMAIN.concat("/").concat(detailUrl)));
                            String strDownloadUrl = extractImage(Jsoup.parse(EntityUtils.toString(detailDocumentResponse.getEntity(), "UTF-8")));
                            System.out.println("Crawling page: " + strDownloadUrl);
                            byte[] image = downloadGetRequestAsBytes(reusedClient, InspirationalCrawler.createGetRequest(strDownloadUrl));
                            //File name
                            String[] namePart = strDownloadUrl.split("/");
                            String fileName = namePart[namePart.length - 4];
                            System.out.println("Get Filename: " + fileName);
                            //Write
                            InspirationalCrawler.saveImages(image, fileName, newFolderPath);
                            System.out.println("Wrote images: " + fileName);
                            Thread.sleep((new Random()).nextInt(2000));
                        }
                    }
                    Thread.sleep((new Random()).nextInt(3000));

                    if(null != nextLink && 1 == nextLink.length && !nextLink[0].equals("#")){
                        initDoc = Jsoup.parse(EntityUtils.toString(downloadGetRequestAsResponse(reusedClient, InspirationalCrawler.createGetRequest(MOTHOR_DOMAIN.concat("/").concat(nextLink[0]))).getEntity(),"UTF-8"));
                        page++;
                        System.out.println("Has next: yes!");
                    }else {
                        hasNextLink = false;
                        System.out.println("Has next: no!, We're done");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpUriRequest createGetRequest(String getUrl){
        HttpUriRequest get = new HttpGet(getUrl);
        get.setHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"
                );
        return get;
    }

    private static String[] loopRequest(Document documentToSeed/*seed*/){
        documentToSeed.normalise();
        Elements nextLink = documentToSeed.select(SELECTOR_NEXT_LNK);
        String stringNextLink = nextLink.get(0).attr("href");
        if(1 == nextLink.size()){
            return new String[]{stringNextLink};
        }else
            return new String[]{};
    }

    private static String[] listDetailPages(Document docContainLink){
        String[] linkToReturn = null;
        Elements anchorsContainImgLnk = docContainLink.select(SELECTOR_TO_DETAIL_PAGE);
        if(anchorsContainImgLnk.size() > 0){
            linkToReturn = new String[anchorsContainImgLnk.size()];
            for (int indexAnchor = 0; indexAnchor < anchorsContainImgLnk.size(); indexAnchor++) {
                linkToReturn[indexAnchor] = anchorsContainImgLnk.get(indexAnchor).attr("href");
            }
        }
        return linkToReturn;
    }

    private static String extractImage(Document docToExtract){
        Element downloadButton = docToExtract.select(SELECTOR_TO_CAPTURE).first();
        if(null != downloadButton){
            String dataRef = downloadButton.attr("data-href");
            return  dataRef;
        }
        return null;
    }

    private static byte[] downloadGetRequestAsBytes(CloseableHttpClient client, HttpUriRequest getRequest) throws IOException{
        byte[] holder = null;
        CloseableHttpResponse response = client.execute(getRequest);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if(null != response){
            response.getEntity().writeTo(bytes);
            holder = bytes.toByteArray();
            return holder;
        }
        return holder;
    }

    private static CloseableHttpResponse downloadGetRequestAsResponse(CloseableHttpClient client, HttpUriRequest getRequest) throws IOException{
        CloseableHttpResponse response = client.execute(getRequest);
        if(null != response){
            return response;
        }
        return null;
    }

    private static void saveImages(byte[] bytedImg, String fileName, String folder) throws IOException{
        System.out.println("Start write file with length: " + bytedImg.length);
        FileOutputStream outputStream = new FileOutputStream(folder.concat(File.separator).concat(fileName).concat(".jpg"));
        outputStream.write(bytedImg);
    }

    private static boolean hasNextPage(String html){
        return false;
    }
}
