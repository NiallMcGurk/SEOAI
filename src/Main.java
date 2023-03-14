import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.opencsv.CSVWriter;
import org.jsoup.select.Elements;

public class Main {

    private static List<String[]> csvData;

    public static void main(String[] args)
    {
        String url = "https://weirandsons.ie";
        CreateCSV();
        crawl(1, url, new ArrayList<String>());
    }

    public static void CreateCSV()
    {
        csvData = Header();
        try (CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\niall\\Documents\\GitHub\\SEOAI\\Export\\export.csv")))
        {
            writer.writeAll(csvData);
        }
        catch(IOException e){
            System.out.println("Error creating Headers");
        }
    }

    private static void crawl(int level, String url, ArrayList<String> visited)
    {
        if(level <= 5)
        {
            Document doc = request(url, visited);

            if(doc != null)
            {
                for(Element link : doc.select("a[href]"))
                {
                    String next_link = link.absUrl("href");
                    if (visited.contains(next_link) == false && !next_link.contains("tel:") && !next_link.contains("#contentarea"))
                    {
                        crawl(level++, next_link, visited);
                    }
                }

            }
        }
    }

    private static Document request(String url, ArrayList<String> v)
    {
        try {
                Connection con = Jsoup.connect(url);
                Document doc = con.get();

            if(con.response().statusCode() == 200) {
                String urlPaste = url;
                String Title = doc.title();
                Elements h1 = doc.select("h1");
                String h1Paste = h1.text();
                Elements metaTags = doc.getElementsByTag("meta");
                String metaDescPaste = null;
                String metaKeyPaste = null;

                for (Element metaTag : metaTags) {
                    String name = metaTag.attr("name");
                    String content = metaTag.attr("content");


                    if (name.equals("description") ) {
                        metaDescPaste = content;
                    }
                    if (name.equals("keywords") ) {
                        metaKeyPaste = content;
                    }

                }
                String[] line = {urlPaste, Title, h1Paste, metaDescPaste, metaKeyPaste};

                csvData.add(line);
                try (CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\niall\\Documents\\GitHub\\SEOAI\\Export\\export.csv"))) {
                    writer.writeAll(csvData);
                } catch (IOException e) {
                    System.out.println("Error creating new Line");
                }

                System.out.println("Line:" + url + " | " + Title);
                System.out.println(metaTags);
                System.out.println("********************" + metaDescPaste);
                System.out.println("********************" + metaKeyPaste);
                v.add(url);
                return doc;
            }
            return null;
        }
        catch(IOException e){
            System.out.println("Error");
            return null;
        }
    }

    private static List<String[]> Header()
    {
        String[] header = {"URL", "Title", "H1", "Meta Description", "Meta Keywords"};
        List<String[]> list = new ArrayList<>();
        list.add(header);
        return list;
    }
}