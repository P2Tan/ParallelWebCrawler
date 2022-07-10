package com.udacity.webcrawler;

import com.sun.jdi.Value;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import javax.print.attribute.HashPrintJobAttributeSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class CrawlInternal extends RecursiveTask<CrawlInternalHelper> {
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
    @Inject
    CrawlInternal(
            Clock clock,
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls,
            List<Pattern>ignoredUrls,
            PageParserFactory parserFactory) {
        this.clock=clock;
        this.deadline=deadline;
        this.url=url;
        this.maxDepth = maxDepth;
        this.counts=counts;
        this.visitedUrls=visitedUrls;
        this.ignoredUrls=ignoredUrls;
        this.parserFactory=parserFactory;
    }
    @Override
    protected CrawlInternalHelper compute() {
        Set<String> visitedUrls = new HashSet<>();
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return new CrawlInternalHelper(counts,visitedUrls);
        }

        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return new CrawlInternalHelper(counts,visitedUrls);
            }
        }
        if (visitedUrls.contains(url)) {
            return new CrawlInternalHelper(counts,visitedUrls);
        }
        visitedUrls.add(url);
        System.out.println(visitedUrls);
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
            System.out.println(counts);
        }
        List<CrawlInternal> subtasks = new ArrayList();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlInternal(clock,link, deadline, maxDepth - 1, counts, visitedUrls,ignoredUrls,parserFactory));
        }
        invokeAll(subtasks);
        return new CrawlInternalHelper(counts,visitedUrls);
    }
}