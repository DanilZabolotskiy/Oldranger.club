package ru.java.mentor.oldranger.club.service.article;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.java.mentor.oldranger.club.dto.ArticleCommentDto;
import ru.java.mentor.oldranger.club.model.article.Article;
import ru.java.mentor.oldranger.club.model.article.ArticleComment;
import ru.java.mentor.oldranger.club.model.article.ArticleTag;

import java.util.List;
import java.util.Set;

public interface ArticleService {

    List<Article> getAllArticles();

    Page<Article> getAllByTag(Set<ArticleTag> tagId, Pageable pageable);

    void addArticle(Article article);

    void deleteArticle(Long id);

    void deleteArticles(List<Long> ids);

    Article getArticleById(long id);

    void addCommentToArticle(ArticleComment articleComment);

    ArticleCommentDto conversionCommentToDto(ArticleComment articleComment);

    ArticleComment getCommentById(Long id);

    void updateArticleComment(ArticleComment articleComment);

    void deleteComment(Long id);

    Page<Article> getArticlesForAnon(Pageable pageable);

    long countLikes(long articleId);

    Set isUserLiked(long articleId, long userId);

    void saveLike(long articleId, long userId);

    void deleteLike(long articleId, long userId);
}