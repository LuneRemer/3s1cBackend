package com.s1c.rtp.service;

import com.s1c.rtp.entity.news;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.s1c.rtp.repository.*;
import com.s1c.rtp.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class KeywordService {

    @Autowired
    KeywordRepository keywordRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    AgeRepository ageRepository;

    @Autowired
    NewsService newsService;

    @Autowired
    CommentsRepository commentsRepository;

    @Autowired
    RelKeywordService relKeywordService;

    @Transactional
    public List<KeywordDto> findBiggerThanRate(double rate){
        List<KeywordDto> listKeyword = keywordRepository.findAllKeyword();
        ArrayList<KeywordDto> listBiggerThanRate = new ArrayList<KeywordDto>();
        for(KeywordDto obj : listKeyword){
            if (obj.getRate() > rate) {
                listBiggerThanRate.add(obj);
            }
        }
        return listBiggerThanRate;
    }

    @Transactional
    public List<KeywordDto> findBiggerThanCount(int count){
        List<KeywordDto> listKeyword = keywordRepository.findAllKeyword();
        ArrayList<KeywordDto> listBiggerThanCount = new ArrayList<KeywordDto>();
        for(KeywordDto obj : listKeyword){
            if (obj.getMentions() > count) {
                listBiggerThanCount.add(obj);
            }
        }
        return listBiggerThanCount;
    }

    @Transactional
    public List<RtpDto> findRealTimePopularity() {
        Pageable pageableSize20 = PageRequest.of(0, 15);
        Page<KeywordDto> keywordDtos = keywordRepository.findTopKeyword(pageableSize20);
        List<RtpDto> rtpDtoList = new ArrayList<>();
        int keywordId = 1; // ?????? keywordId??? ?????????, ?????????????????? ???????????? ?????? ?????? id

        for(KeywordDto keywordDto : keywordDtos) {

            Page<String> pageBrefNews = newsService.findBriefNewsByKeyword(keywordDto.getKeyword());
            HashMap<String, Double> genderRatio = newsService.findGenderRatioByKeyword(keywordDto.getKeyword());
            HashMap<String, Double> ageRatio = newsService.findAgeRatioByKeyword(keywordDto.getKeyword());
            List<String> tags = relKeywordService.findRelatedKeyword(keywordDto.getKeyword());

            List<String> brefList = pageBrefNews.getContent();
            String brefNews;
            if (brefList.size() == 0) {
                brefNews = "?????? ???????????? ??????????????? ????????? ???????????? ????????? ???????????????.";
            }
            else {
                brefNews =  brefList.get(0);
            }

            if (genderRatio.get("female") == 0.0 && genderRatio.get("male") == 0.0) {
                genderRatio = newsService.findGenderRatioByBref(brefNews);
                ageRatio = newsService.findAgeRatioByBref(brefNews);
            }

            String keyword = keywordDto.getKeyword();
            int rank = keywordDto.getRanks();
            double positive = keywordDto.getPositive();
            double negative = keywordDto.getNegative();

            HashMap<String, Double> sentiment = new HashMap<>();
            sentiment.put("positive", Math.round(positive*100)/100.0);
            sentiment.put("negative", Math.round(negative*100)/100.0);

            RtpDto rtpDto = new RtpDto(keywordId, rank, keyword, brefNews,genderRatio, ageRatio, sentiment, tags);
            rtpDtoList.add(rtpDto);
            keywordId++;
        }
        return rtpDtoList;
    }

    @Transactional
    public HashMap<String, Integer> findRanksAndMentions(String keyword) {

        HashMap<String, Integer> hashMap = new HashMap<>();
        String key = keywordRepository.findKeywordByKeyword(keyword);
        KeywordDto searchedKeywordDTO = keywordRepository.findKeywordDTOByKeyword(key);

        int rank = searchedKeywordDTO.getRanks();
        int metions = searchedKeywordDTO.getMentions();

        hashMap.put("Rank", rank);
        hashMap.put("Mentions", metions);

        return hashMap;
    }

    @Transactional
    public List<RelatedNewsDto> findRelatedArticles(String keyword) {

        Pageable pageableSize3 = PageRequest.of(0, 3);
        Page<Integer> newsIdDtos =newsRepository.findNewsIdByKeyword2(keyword, pageableSize3);

        List<RelatedNewsDto> relatedNewsDtoList = new ArrayList<>();
        int id =0; // newsId??? ??????, ????????????????????? ????????? ?????? ????????? id
        for(int newsId : newsIdDtos){
            news news = newsRepository.findnewsByNewsId(newsId);
            String title = news.getTitle();
            String url = news.getUrl();
            int commentsNumber = commentsRepository.findCommentsNumberByNewsId(news.getNewsId());

            id++;
            RelatedNewsDto relatedNewsDto = new RelatedNewsDto(id, newsId, title, url, commentsNumber);
            relatedNewsDtoList.add(relatedNewsDto);
        }

        return relatedNewsDtoList;
    }

    @Transactional
    public KeywordDto2 findKeywordEmoticon(String keyword) {
        int like = 0;
        int sad = 0;
        int angry = 0;
        int warm = 0;

        List<Integer> newsIds = newsRepository.findNewsIdByKeyword3(keyword);

        for (Integer newsId : newsIds) {
            NewsDto3 temp = keywordRepository.findKeywordNews(newsId);
            like += temp.getLike();
            sad += temp.getSad();
            angry += temp.getAngry();
            warm += temp.getWarm();
        }

        return new KeywordDto2(keyword, like, sad, angry, warm);
    }

    public HashMap<String, Double> findSentimentByKeyword(String keyword) {
        SentimentDto sentimentDto = keywordRepository.findSentimentByKeyword(keyword);

        HashMap<String, Double> hashMap = new HashMap<>();
        hashMap.put("negative", sentimentDto.getNegative());
        hashMap.put("positive", sentimentDto.getPositive());

        return hashMap;
    }

}
