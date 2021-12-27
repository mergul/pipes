package com.streams.pipes.model;

import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TopThreeHundredNews implements Iterable<NewsPayload> {
    private final ConcurrentSkipListMap<String, NewsPayload> currentNews = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListSet<NewsPayload> topHundred = new ConcurrentSkipListSet<>((o1, o2) -> {
        final int result = o2.getCount().compareTo(o1.getCount());
        if (result != 0) {
            return result;
        }
        return o1.getDate().compareTo(o2.getDate());
    });

    public void add(final NewsPayload payload) {
        if(currentNews.containsKey(payload.getNewsId().toHexString())) {
            topHundred.remove(currentNews.remove(payload.getNewsId().toHexString()));
        }
        topHundred.add(payload);
        currentNews.put(payload.getNewsId().toHexString(), payload);
        if (topHundred.size() > 300) {
            final NewsPayload last = topHundred.last();
            currentNews.remove(last.getNewsId().toHexString());
            topHundred.remove(last);
        }
    }

    public void remove(final NewsPayload value) {
        topHundred.remove(value);
        currentNews.remove(value.getNewsId().toHexString());
    }
    @NonNull
    @Override
    public Iterator<NewsPayload> iterator() {
        return topHundred.iterator();
    }

    public Collection<NewsPayload> getList() {
        return this.topHundred;
    }
//    public Collection<NewsPayload> getTopList() {
//        return ImmutableList.copyOf(Iterables.limit(topHundred, 5));
//    }
//    public Collection<? extends NewsPayload> getRemained() {
//        return ImmutableList.copyOf(Iterables.skip(topHundred, 5));
//    }
}
