import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.opencsv.CSVWriter;
import org.jsoup.select.Elements;

public class Main {



    private static List<String[]> csvData;

    public static void main(String[] args) throws Exception {
        chatGPT("Give me your thoughts on AI in 5 words?");
        String url = "https://weirandsons.ie";
        CreateCSV();
        crawl(5, url, new ArrayList<String>());
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
                    if (visited.contains(next_link) == false && next_link.contains("https://weirandsons.ie")
                                                            && !next_link.contains("tel:")
                                                            && !next_link.contains("#")
                                                            && !next_link.contains("/customer/")
                                                            && !next_link.contains("/catalog/")
                                                            && !next_link.contains("/checkout/")
                                                            && !next_link.contains("/index.php/")
                                                            && !next_link.contains("%2C")
                    )
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
                    System.out.println("Error updating .csv file");
                }

                System.out.println("Line:" + url + " | " + Title);
                v.add(url);
                return doc;
            }
            return null;
        }
        catch(IOException e){
            System.out.println("Error attempting URL: " + url);
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

    public static void chatGPT(String text) throws Exception {
        String url = "https://api.openai.com/v1/completions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer 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");

        JSONObject data = new JSONObject();
        data.put("model", "text-davinci-003");
        data.put("prompt", text);
        data.put("max_tokens", 4000);
        data.put("temperature", 1.0);

        con.setDoOutput(true);
        con.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

        System.out.println(new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text"));
    }
}