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

public class CrawlInternal extends RecursiveTask<ConcurrentHashMap> {
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
        this.counts=counts;
        this.visitedUrls=visitedUrls;
        this.maxDepth = maxDepth;
        this.ignoredUrls=ignoredUrls;
        this.parserFactory=parserFactory;
    }
    @Override
    protected ConcurrentHashMap compute() {
        Set<String> visitedUrls = new HashSet<>();
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return (ConcurrentHashMap) counts;
        }
        System.out.println(ignoredUrls);
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return (ConcurrentHashMap) counts;
            }
        }
        if (visitedUrls.contains(url)) {
            return (ConcurrentHashMap) counts;
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
        System.out.println("Round 1.4");
        System.out.println(Arrays.toString(result.getLinks().toArray()));
        for (String link : result.getLinks()) {
            System.out.println("Round 2");
            subtasks.add(new CrawlInternal(clock,link, deadline, maxDepth - 1, counts, visitedUrls,ignoredUrls,parserFactory));
        }
        invokeAll(subtasks);
        return (ConcurrentHashMap) counts;
    }
}