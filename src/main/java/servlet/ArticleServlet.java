package servlet;

import db.ArticleDataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.annotation.MyServletMapping;
import util.HttpRequestUtils;

import java.util.Map;

@MyServletMapping(url = "/questions")
public class ArticleServlet extends BaseServlet {
    private static final Logger log = LoggerFactory.getLogger(ArticleServlet.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Article article = getArticle(request);
        Long articleId = ArticleDataBase.save(article);
        log.debug("[articleID] : {}", articleId);
        response.redirection("/");
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.forward("/qna/form.html");
    }

    private Article getArticle(HttpRequest request) {
        String requestBody = request.getBody();
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(requestBody);
        return new Article(parameters.get("writer"), parameters.get("title"), parameters.get("contents"));
    }
}
