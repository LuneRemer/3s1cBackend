package com.s1c.rtp.repository;


import com.s1c.rtp.dto.CommentsDto;
import com.s1c.rtp.dto.CommentsUserDto;
import com.s1c.rtp.dto.GroupByDto;
import com.s1c.rtp.dto.TimeCommentsDto;
import com.s1c.rtp.dto.*;
import com.s1c.rtp.entity.comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentsRepository extends JpaRepository<comments, Integer>{

    @Query("select new com.s1c.rtp.dto.CommentsDto(c.commentsId, n.title, c.contents, c.writer, c.date) from comments c join c.news_comments n where c.commentsId < 3")
    List<CommentsDto> findCommentsWithNews();

    @Query("select count(distinct c.writer) from comments c")
    Long returnNumOfWriters();

    @Query("select new com.s1c.rtp.dto.TimeCommentsDto(c.commentsId, c.date) from comments c")
    List<TimeCommentsDto> findAllCommentsNTime();

    @Query("select count(c.commentsId) from comments c join c.news_comments n where n.newsId = :newsId")
    int findCommentsNumberByNewsId(@Param("newsId") int newsId);

    @Query("select new com.s1c.rtp.dto.CommentsUserDto(count(c), c.writer) from comments c group by c.writer having count(c) > 10 order by count(c) desc ")
    Page<CommentsUserDto> retrieveHeavyUser(Pageable pageable);

    @Query("select new com.s1c.rtp.dto.CommentsDto2(c.news_comments.newsId, count(c.news_comments.newsId), c.news_comments.title) from comments c group by c.news_comments.newsId order by count(c.news_comments.newsId) desc")
    List<CommentsDto2> returnNewsIdNNumofComments();

    @Query("select new com.s1c.rtp.dto.CommentsDto3(count(c), c.news_comments.newsId) from comments c where c.contents Like concat('%', :keyword, '%') group by c.news_comments.newsId order by count(c) desc")
    Page<CommentsDto3> getCountByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
