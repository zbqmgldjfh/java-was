package db;

import model.Article;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArticleDataBase {
    private static Map<Long, Article> articleStore = new ConcurrentHashMap<>();
    private static Long sequence = 0L;

    public static Long save(Article article) {
        sequence++;
        article.setId(sequence);
        articleStore.put(sequence, article);
        return sequence;
    }
}
