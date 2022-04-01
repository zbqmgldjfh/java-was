package servlet;

import db.ArticleDataBase;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.Article;
import model.User;
import servlet.annotation.MyServletMapping;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

@MyServletMapping(url = "/")
public class HomeServlet extends BaseServlet {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        Collection<Article> articles = ArticleDataBase.findAll();
        try {
            String bodyString = getBodyString();
            byte[] newBody = addUserInfoAtBody(articles, bodyString);
            response.forward(newBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] addUserInfoAtBody(Collection<Article> articles, String bodyString) {
        int startIndex = bodyString.indexOf("<tbody>");
        int endIndex = bodyString.indexOf("</tbody>");

        StringBuilder sb = new StringBuilder(bodyString.substring(0, startIndex + 7));
        for (Article article : articles) {
            sb.append("<li>")
                    .append("<td>" + article.getTitle() + "</td>")
                    .append("<td>" + article.getWriter() + "</td>")
                    .append("<td>" + article.getContents() + "</td>")
                    .append("</tr>");
        }
        sb.append(bodyString.substring(endIndex));
        String articleBody = sb.toString();
        try {
            return URLDecoder.decode(articleBody, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBodyString() throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp/index.html").toPath());
        return new String(body, "UTF-8");
    }
}
