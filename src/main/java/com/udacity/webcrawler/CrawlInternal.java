package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class CrawlInternal extends RecursiveTask<List> {
    private Set<String> visitedUrls;
    private Instant deadline;
    private Clock clock;
    private PageParserFactory parserFactory;
    private Duration timeout;
    private int popularWordCount;
    private int maxDepth;
    private List<Pattern> ignoredUrls;
    private String url;
    private Map<String, Integer> counts;
    public CrawlInternal(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {
        this.deadline=deadline;
        this.url=url;
        this.counts=counts;
        this.visitedUrls=visitedUrls;
        this.maxDepth = maxDepth;
    }
    @Override
    protected List compute() {
        Set<String> visitedUrls = new HashSet<>();
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return (List) counts;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return (List) counts;
            }
        }
        if (visitedUrls.contains(url)) {
            return (List) counts;
        }
        visitedUrls.add(url);
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        List<CrawlInternal> subtasks = new ArrayList();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls));
        }
        invokeAll(subtasks);
        return (List) counts;
    }
}