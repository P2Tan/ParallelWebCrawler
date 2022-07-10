package com.udacity.webcrawler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CrawlInternalHelper {
    private ConcurrentHashMap<String, Integer> counts;
    private ConcurrentSkipListSet<String> visitedUrls;
    public CrawlInternalHelper(
            Map<String,Integer>counts,
            Set<String>visitedUrls
    ){
        this.counts= (ConcurrentHashMap<String, Integer>) counts;
        this.visitedUrls= (ConcurrentSkipListSet<String>) visitedUrls;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }
    public Set<String> getVisitedUrls(){
        return visitedUrls;
    }

    public void setCounts(Map<String, Integer> counts) {
        this.counts = (ConcurrentHashMap<String, Integer>) counts;
    }

    public void setVisitedUrls(Set<String> visitedUrls) {
        this.visitedUrls = (ConcurrentSkipListSet<String>) visitedUrls;
    }
}
