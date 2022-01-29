package com.streams.pipes.chat;

import com.google.common.collect.Lists;
import com.streams.pipes.model.NewsPayload;
import com.streams.pipes.model.TopHundredNews;
import com.streams.pipes.model.TopThreeHundredNews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.lang.NonNull;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class RoomEntre<T> implements ChatRoomMessageListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(RoomEntre.class);
    private final Flux<ServerSentEvent<T>> hotFlux;
    private final Sinks.Many<ServerSentEvent<T>> sink;
    private Date lastNewsEmit;
    private Date lastTagsEmit;
    private Disposable disposable;
    private Disposable disposableNews;
    private ServerSentEvent<T> lastRecord;
    private final Map<String, List<String>> newsIds;
    private final Map<String, BaseSubscriber<ServerSentEvent<T>>> subscriberMap;
    private final Sinks.EmitFailureHandler emitFailureHandler = (signalType, emitResult) -> emitResult
            .equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);
    public RoomEntre(@Qualifier("hotFlux") Flux<ServerSentEvent<T>> hotFlux,
                     @Qualifier("sink") Sinks.Many<ServerSentEvent<T>> sink) {
        this.hotFlux = hotFlux;
        this.sink = sink;
        this.newsIds = new HashMap<>();
        this.subscriberMap = new HashMap<>();
        this.disposable = Mono.just("h").delayElement(Duration.ofSeconds(55)).subscribe(s1 -> {
            if (this.lastRecord!=null) onPostMessage(this.lastRecord.data(), this.lastRecord.event(), null, this.lastRecord.id());
        });
        // this.hotFlux.subscribe(this.processor::onNext);
    }

    private void emitHeartBeat(String s) {
        this.disposable.dispose();
        this.disposable = Mono.just("h").delayElement(Duration.ofSeconds(55))
                .subscribe(s1 -> onPostMessage(this.lastRecord.data(), this.lastRecord.event(), null, this.lastRecord.id()));
    }

    @Override
    public void onPostMessage(T msg, String key, Date date, String ev) {
        if (msg instanceof TopThreeHundredNews) {
            if (key.equals("me") || (this.lastNewsEmit == null || date == null)) {
                TopThreeHundredNews threeHundredNews = new TopThreeHundredNews();
                threeHundredNews.getList().addAll(getTopList(((TopThreeHundredNews)msg).getList()));
                this.disposableNews= Mono.fromCallable(() -> this.sink.tryEmitNext(getMyEvent((T) threeHundredNews, key, ev)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .thenMany(getPartialEvents(getSkipList(((TopThreeHundredNews) msg).getList()), key, ev))
                        .delayElements(Duration.ofSeconds(1L))
                        .flatMapSequential(tEvent -> Mono.fromCallable(() -> this.sink.tryEmitNext(tEvent)))
                        .subscribeOn(Schedulers.parallel())
                        .subscribe();
                this.lastNewsEmit = date;
                logger.info("emit news {} -- {} -- {}", key, date, ev);
            }
        } else if (msg instanceof TopHundredNews) {
            if (this.lastTagsEmit == null || date == null
                    || ((date.getTime() - this.lastTagsEmit.getTime()) / (1000) % 60) > 30) {
                this.sink.tryEmitNext(getMyEvent(msg, key, ev));
                this.lastTagsEmit = date;
            }
        } else {
          //  logger.info("finished BalanceRecord key {}, event {}, total balance --> {}", key, ev, ((BalanceRecord) msg).getTotalBalance());
            this.sink.tryEmitNext(getMyEvent(msg, key, ev));
        }
        this.lastRecord=getMyEvent(msg, key, ev);
        emitHeartBeat(key);
    }

    public Flux<ServerSentEvent<T>> subscribe(String lastEventId) {
        BaseSubscriber<ServerSentEvent<T>> busses = new BaseSubscriber<ServerSentEvent<T>>() {
            @Override
            protected void hookFinally(@NonNull SignalType type) {
                super.hookFinally(type);
            }
        };
        this.hotFlux.subscribe(busses);
        this.subscriberMap.put(lastEventId, busses);
        // this.hotFlux.subscribe(this.sink::tryEmitNext);
        return this.hotFlux;
    }

    public Map<String, List<String>> getNewsIds() {
        return newsIds;
    }

    public ServerSentEvent<T> getMyEvent(T msg, String key, String ev) {
        return ServerSentEvent.<T>builder().event(ev).data(msg).id(key).build();
    }

    public Collection<NewsPayload> getTopList(Collection<NewsPayload> list) {
        return list.stream().limit(10).collect(Collectors.toList());
    }

    public List<NewsPayload> getSkipList(Collection<NewsPayload> list) {
        return list.stream().skip(10).collect(Collectors.toList());
    }

    public Flux<ServerSentEvent<T>> getPartialEvents(List<NewsPayload> msgList, String key, String ev) {
        Iterable<List<NewsPayload>> lists = Lists.partition(msgList, 10);
        return Flux.fromIterable(lists).publishOn(Schedulers.boundedElastic()).flatMap(list -> {
            TopThreeHundredNews titan = new TopThreeHundredNews();
            titan.getList().addAll(list);
         //   logger.info("finished bounded {}, -- {}, -- {}", key, ev, list.size());
            return Mono.fromCallable(() -> getMyEvent((T) titan, key, ev));
        });
    }

    public Mono<Boolean> unsubscribe(String chatRoom) {
        if (!this.subscriberMap.isEmpty()) {
            BaseSubscriber<ServerSentEvent<T>> busses = this.subscriberMap.get(chatRoom);
            if (busses!=null) {
                busses.cancel();
                this.subscriberMap.remove(chatRoom);
                this.disposable.dispose();
                this.disposableNews.dispose();
                logger.info("BaseSubscriber to cancel chatRoom {}, subscriberMap size {}", chatRoom, subscriberMap.size());
            }
        }
        return Mono.just(true);
    }

//    public Flux<ServerSentEvent<T>> getPartialEvent(T msg, String key, String ev) {
//        ServerSentEvent<T>[] mitop;
//        if (((TopThreeHundredNews) msg).getList().size() > 5) {
//            Iterable<List<NewsPayload>> lists = Iterables.partition((TopThreeHundredNews) msg, 5);
//            return Flux.fromIterable(lists).publishOn(Schedulers.boundedElastic()).flatMap(list -> {
//                TopThreeHundredNews titan = new TopThreeHundredNews();
//                titan.getList().addAll(list);
//                logger.info("finished bounded {}, -- {}, -- {}", key, ev, list.size());
//                return Mono.fromCallable(() -> getMyEvent((T) titan, key, ev));
//            });
//        } else {
//            ServerSentEvent<T> top = getMyEvent(msg, key, ev);
//            mitop = new ServerSentEvent[]{top};
//            return Flux.fromArray(mitop);
//        }
//    }
    // public Mono<FluxSink<ServerSentEvent<T>>> getPartial(T msg, String key, Date
    // date, String ev) {
    // if (((TopThreeHundredNews)msg).getList().size() > 5) {
    // Iterable<List<NewsPayload>> lists =
    // Iterables.partition((TopThreeHundredNews)msg, 5);
    // TopThreeHundredNews titan = new TopThreeHundredNews();
    // titan.getList().addAll(lists.iterator().next());
    // return Mono.fromCallable(()->this.sink.next(getMyEvent((T) titan, key, date,
    // ev)));
    // } else {
    // return Mono.fromCallable(()->this.sink.next(getMyEvent(msg, key, date, ev)));
    // }
    // }

    //                Flux<ServerSentEvent<T>> eventFlux = getPartialEvent(msg, key, ev);
//                eventFlux.take(1).single().flatMap(tEvent -> Mono.fromCallable(() -> this.sink.next(tEvent)))
//                        .subscribeOn(Schedulers.boundedElastic())
//                        .thenMany(eventFlux.skip(1).delayElements(Duration.ofSeconds(1L))
//                                .flatMapSequential(tEvent -> Mono.fromCallable(() -> this.sink.next(tEvent)))
//                                .subscribeOn(Schedulers.parallel()))
//                        .subscribe(z -> logger.info("finished {}, -- {}", key, ev));
    // getPartial(msg, key, date, ev).delayElement(Duration.ofSeconds(2L))
    // .thenMany(getPartialEvent(msg, key, date,
    // ev).subscribeOn(Schedulers.boundedElastic())
    // .flatMapSequential(tEvent ->
    // Mono.fromCallable(()->this.sink.next(tEvent)))).subscribe();
    // if (key.equals("main") || key.equals("me") || key.equals("people")) {
    // TopThreeHundredNews threeHundredNews=new TopThreeHundredNews();
    // threeHundredNews.getList().addAll(getTopList(((TopThreeHundredNews)
    // msg).getList()));
    // Mono.fromCallable(() -> this.sink.next(getMyEvent((T) threeHundredNews, key,
    // ev)))
    // .delayElement(Duration.ofSeconds(2L)).then(Mono.fromCallable(() ->
    // this.sink.next(getMyEvent(msg, key, ev)))).subscribe();
    // } else
    // this.sink.next(getMyEvent(msg, key, ev));
}
