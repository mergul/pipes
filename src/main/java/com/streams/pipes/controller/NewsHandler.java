package com.streams.pipes.controller;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.streams.pipes.chat.RoomEntre;
import com.streams.pipes.model.NewsPayload;
import com.streams.pipes.model.RecordSSE;
import com.streams.pipes.model.TopHundredNews;
import com.streams.pipes.model.TopThreeHundredNews;
import com.streams.pipes.service.Receiver;
import com.streams.pipes.service.Sender;
import lombok.SneakyThrows;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Component
public class NewsHandler {
    // private static final Logger logger = LoggerFactory.getLogger(NewsHandler.class);

    private final StreamsBuilderFactoryBean factoryBean;
    private final Sender kafkaSender;
    private static final String TOP_USERS_STORE = "windowed-users-stores";
    private static final String TOP_NEWS_STORE = "windowed-news-stores";
    private static final String USER_STORE = "stream-users-stores";
    @Value("${kafka.topics.receiver-topics}")
    private String receiverTopic;

    final ListeningExecutorService pool = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(8)
    );
    private final RoomEntre<?> entre;

    public NewsHandler(StreamsBuilderFactoryBean factoryBean, Sender kafkaSender, @Qualifier(value = "roomEntre") RoomEntre<?> entre) {
        this.factoryBean = factoryBean;
        this.kafkaSender = kafkaSender;
        this.entre = entre;
    }

    @SneakyThrows
    Flux<NewsPayload> getTopNewsList(String epithet) {
        final ListenableFuture<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsFuture = future(TOP_NEWS_STORE);
        Mono<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsStores = Mono.fromFuture(Receiver.toCompletableFuture(topNewsFuture));
        return topNewsStores.flatMapIterable(store -> store.get(epithet));
    }
    @SuppressWarnings("unchecked")
    public  Mono<Boolean> unsubscribeChatMessages(Mono<String> chatRoomMono) {
        RoomEntre<TopThreeHundredNews> chatRoomEntry = (RoomEntre<TopThreeHundredNews>) this.entre;
        return chatRoomMono.flatMap(chatRoomEntry::unsubscribe);
    }
    @SuppressWarnings("unchecked")
    public <T> Flux<ServerSentEvent<T>> subscribeChatMessages(@PathVariable("chatRoom") String chatRoom) {
        RoomEntre<T> chatRoomEntry = (RoomEntre<T>) this.entre;
        return setLists(chatRoomEntry, chatRoom.substring(7)).flatMapMany(booleans -> chatRoomEntry.subscribe(chatRoom));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T> Mono<Boolean> setLists(RoomEntre<T> roomEntry, String num) {
        // logger.info("Number of sub topologies => {}", this.factoryBean.getTopology().describe());
        RoomEntre<TopThreeHundredNews> chatRoomEntry = (RoomEntre<TopThreeHundredNews>) roomEntry;
        final ListenableFuture<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsFuture = future(TOP_NEWS_STORE);
        Mono<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsStores = Mono.fromFuture(Receiver.toCompletableFuture(topNewsFuture));
        Mono<Boolean> fir = topNewsStores.map(store -> {
            TopThreeHundredNews newsPayloads = store.get("main");
            chatRoomEntry.getNewsIds().put("main", newsPayloads == null ? Collections.emptyList() : newsPayloads.getList().stream().map(newsPayload -> newsPayload.getNewsId().toHexString()).collect(Collectors.toList()));
            chatRoomEntry.onPostMessage(newsPayloads, "main", null, "top-news-" + num);
            return true;
        });
        RoomEntre<TopHundredNews> chatRoomEntry1 = (RoomEntre<TopHundredNews>) roomEntry;
        final ListenableFuture<ReadOnlyKeyValueStore<String, TopHundredNews>> newsFuture = future(TOP_USERS_STORE);
        Mono<ReadOnlyKeyValueStore<String, TopHundredNews>> topUsersStores = Mono.fromFuture(Receiver.toCompletableFuture(newsFuture));
        Mono<Boolean> firs = topUsersStores.map(store -> {
            TopHundredNews thn = store.get("top-tags");
            chatRoomEntry1.onPostMessage(thn, "top-tags", null, "top-tags");
            chatRoomEntry1.getNewsIds().put("top-tags", thn == null ? Collections.emptyList() : thn.getList().stream().map(RecordSSE::getKey).collect(Collectors.toList()));
            return true;
        });
        return fir.then(firs);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Mono<Boolean> setUser(String id, String random) {
        RoomEntre<TopThreeHundredNews> chatRoomEntry = (RoomEntre<TopThreeHundredNews>) this.entre;
        final ListenableFuture<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsFuture = future(TOP_NEWS_STORE);
        Mono<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsStores = Mono.fromFuture(Receiver.toCompletableFuture(topNewsFuture));
        Mono<Boolean> ff = topNewsStores.map(store -> {
            chatRoomEntry.onPostMessage(store.get(id), "me", null, "top-news-" + id + '-' + random);
            return true;
        });
        RoomEntre<RecordSSE> chatRoomEntry1 = (RoomEntre<RecordSSE>) this.entre;
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Long>> usersFuture = future(USER_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], Long>> usersStores = Mono.fromFuture(Receiver.toCompletableFuture(usersFuture));
        Mono<Boolean> af = usersStores.map(store -> {
            chatRoomEntry1.onPostMessage(new RecordSSE(id, store.get(id.getBytes())), id, null, "user-counts-" + id);
            return true;
        });

        return ff.then(af);
    }
    Mono<Boolean> setNewsCounts(Mono<NewsPayload> payloadMono) {
        return payloadMono.flatMap(newsPayload -> this.kafkaSender.send(receiverTopic, newsPayload, newsPayload.getNewsId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic()));
    }
    public <K, V> ListenableFuture<ReadOnlyKeyValueStore<K, V>> future(final String storeName) {
        return pool.submit(() -> Receiver.waitUntilStoreIsQueryable(storeName, QueryableStoreTypes.keyValueStore(), this.factoryBean.getKafkaStreams()));
    }
}

//    Mono<Boolean> setNewsCounts(Mono<NewsPayload> newsMono) {
//        return newsMono.flatMap(newsPayload ->  sender
//                .send(receiverTopic, newsPayload, newsPayload.getNewsId().toHexString().getBytes(), true));
//    }
//    @SneakyThrows
//    public Mono<Boolean> setUserInterests(List<String> ids) {
//        List<String> midas = ids.subList(1, ids.size() - 1);
//        String meId = ids.get(ids.size() - 1);
//        TopThreeHundredNews titan= new TopThreeHundredNews();
//        RoomEntre<TopThreeHundredNews> chatRoomEntry=this.entre;
//        if (midas.size() == 0) {
//            chatRoomEntry.onPostMessage(new TopThreeHundredNews(), ids.get(0), null, "top-news-" + meId);
//        } else {
//            final ListenableFuture<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsFuture = future(TOP_NEWS_STORE);
//            Mono<ReadOnlyKeyValueStore<String , TopThreeHundredNews>> topNewsStores = Mono.fromFuture(toCompletableFuture(topNewsFuture));
//            return topNewsStores.flatMapMany(store -> Flux.fromIterable(ids)
//                    .map(sid -> store.get(sid))).map(TopThreeHundredNews::getList)
//                    .collectMap(newsPayloads -> titan.getList().addAll(newsPayloads))
//                    .then().map(unused -> {
//                        chatRoomEntry.onPostMessage(titan, ids.get(0), null, "top-news-" + meId);
//
//                        return true;
//                    });
//        }
//        return Mono.just(true);
//    }
