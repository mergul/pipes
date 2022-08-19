package com.streams.pipes.service;

import com.google.common.util.concurrent.*;
import com.streams.pipes.chat.RoomEntre;
import com.streams.pipes.config.streams.KStreamConf;
import com.streams.pipes.model.*;
import lombok.SneakyThrows;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class Receiver {
   // private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    private static final String TOP_NEWS_STORE = "windowed-news-stores";
    private static final String USER_STORE = "stream-users-stores";
    private static final String BALANCE_STORE = "share-balance-stores";
    private static final String BALANCE_HISTORY_STORE = "share-balance-history-stores";
    private static final String USER_BALANCE_STORE = "share-user-balance-stores";
    private static final String MY_USER_STORE = "stream-musers-stores";

    @Value("${kafka.topics.payments-in}")
    private String paymentsTopics;
    @Value("${kafka.topics.balances-in}")
    private String balancesTopics;

    private final Sender kafkaSender;
  //  private final Flux<BalanceRecord> outputEvents;
    private final StreamsBuilderFactoryBean factoryBean;
    private final RoomEntre<?> entre;
    private final CountDownLatch latch = new CountDownLatch(1);
    final ListeningExecutorService pool = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(8)
    );

    public Receiver(Sender kafkaSender/*, @Qualifier(value = "miFlux") Flux<BalanceRecord> outputEvents*/, StreamsBuilderFactoryBean factoryBean, @Qualifier(value = "roomEntre") RoomEntre<?> entre) {
        this.kafkaSender = kafkaSender;
     //   this.outputEvents = Flux.from(outputEvents);
        this.factoryBean = factoryBean;
        this.entre = entre;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @KafkaListener(topics = "${kafka.topics.auths}", properties = {"spring.json.value.default.type=com.bros.streaming.model.UserPayload", "spring.json.use.type.headers=false"})
    public void receiveFoo(UserPayload userPayload) {
      //  logger.info("received userPayload id = '{}' tags = '{}' users = '{}'", userPayload.getId(), userPayload.getTags(), userPayload.getUsers());
        String meId = "@" + userPayload.getId();
        RoomEntre<TopThreeHundredNews> chatRoomEntry = (RoomEntre<TopThreeHundredNews>) this.entre;
        final ListenableFuture<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsFuture = future(TOP_NEWS_STORE);
        Mono<ReadOnlyKeyValueStore<String, TopThreeHundredNews>> topNewsStores = Mono.fromFuture(toCompletableFuture(topNewsFuture));

        if (userPayload.getIndex().equals(0)) {
            List<Mono<Boolean>> allMono = new ArrayList<>();
            TopThreeHundredNews titan = new TopThreeHundredNews();
            Mono<Boolean> myTags = topNewsStores.flatMapMany(store -> {
                TopThreeHundredNews thn = store.get(meId);
                chatRoomEntry.getNewsIds().put(meId, thn == null ? Collections.emptyList() : thn.getList().stream().map(newsPayload -> newsPayload.getNewsId().toHexString()).collect(Collectors.toList()));
                chatRoomEntry.onPostMessage(thn, "me", null, "top-news-" + meId + '-' + userPayload.getRandom());
                return Flux.fromIterable(userPayload.getTags())
                        .publishOn(Schedulers.boundedElastic()).map(sid -> {
                            TopThreeHundredNews ttt = store.get('#' + sid);
                            if (ttt != null) {
                                titan.getList().addAll(ttt.getList());
                                chatRoomEntry.getNewsIds().put('#' + sid, ttt.getList().stream().map(newsPayload -> newsPayload.getNewsId().toHexString()).collect(Collectors.toList()));
                            }
                           // logger.info("MY tags list :'{}'", titan.getList());
                            return true;
                        });
            }).collectList().map(booleans -> {
                chatRoomEntry.onPostMessage(titan, "tags", null, "top-news-tags-" + meId + '-' + userPayload.getRandom());
                return true;
            });
            allMono.add(myTags);
            TopThreeHundredNews python = new TopThreeHundredNews();
            Mono<Boolean> myUsers = topNewsStores.flatMapMany(store -> Flux.fromIterable(userPayload.getUsers())
                    .publishOn(Schedulers.boundedElastic()).map(sid -> {
                        TopThreeHundredNews ttt = store.get('@' + sid);
                        if (ttt != null) {
                            python.getList().addAll(ttt.getList());
                            chatRoomEntry.getNewsIds().put('@' + sid, ttt.getList().stream().map(newsPayload -> newsPayload.getNewsId().toHexString()).collect(Collectors.toList()));
                        }
                    //    logger.info("MY users list :'{}'", python.getList());
                        return true;
                    })).collectList().map(bulls -> {
                chatRoomEntry.onPostMessage(python, "people", null, "top-news-people-" + meId + '-' + userPayload.getRandom());
                return true;
            });
            allMono.add(myUsers);
            RoomEntre<RecordSSE> chatRoomEntry1 = (RoomEntre<RecordSSE>) this.entre;
            final ListenableFuture<ReadOnlyKeyValueStore<byte[], Long>> usersFuture = future(USER_STORE);
            Mono<ReadOnlyKeyValueStore<byte[], Long>> usersStores = Mono.fromFuture(toCompletableFuture(usersFuture));
            Mono<Boolean> mySSE = usersStores.map(store -> {
                chatRoomEntry1.onPostMessage(new RecordSSE(meId, store.get(meId.getBytes())), meId, null, "user-counts-" + meId);
              //  logger.info("MY record-sse list :'{}'", meId);
                return true;
            });
            allMono.add(mySSE);
            RoomEntre<List<BalanceRecord>> roomEntry = (RoomEntre<List<BalanceRecord>>) this.entre;
            // Flux<BalanceRecord> recs = getUserHistory(meId);
            Mono<Boolean> myHistory = getUserHistory(meId).collectList().map(balanceRecords -> {
                //  logger.info("MY balanceRecords :'{}'", balanceRecords.size());
                if (!balanceRecords.isEmpty())
                    roomEntry.onPostMessage(balanceRecords, "user-history", new Date(), "user-history-" + meId);
                return true;
            });
            allMono.add(myHistory);
            if (userPayload.getIsAdmin() != null && userPayload.getIsAdmin()) {
                Mono<Boolean> myRecords = hotBalanceRecords().collectList().map(balanceRecords -> {
                    //         logger.info("MYADMIN balanceRecords :'{}'", balanceRecords.size());
                    roomEntry.onPostMessage(balanceRecords, "hotRecords", new Date(), "hotRecords-" + meId);
                    return true;
                });
                allMono.add(myRecords);
            }
            Flux.fromIterable(allMono).flatMap(mono -> mono.subscribeOn(Schedulers.boundedElastic())).subscribe();
        } else {
            String tagging;
            if (userPayload.getIndex().equals(2)) tagging = userPayload.getUsers().get(0);
            else tagging = userPayload.getTags().get(0);
            topNewsStores.map(store -> {
                TopThreeHundredNews thn = store.get(tagging);
                if (thn != null) {
                    chatRoomEntry.getNewsIds().put(tagging, thn.getList().stream().map(newsPayload -> newsPayload.getNewsId().toHexString()).collect(Collectors.toList()));
                    chatRoomEntry.onPostMessage(thn, tagging.charAt(0) == '@' ? "me" : "tag", null, "top-news-" + tagging + '-' + userPayload.getRandom());
                }
                return true;
            }).subscribe();
        }
        latch.countDown();
    }

    @KafkaListener(topics = "${kafka.topics.partitioncom-in}", properties = {"spring.json.value.default.type=com.bros.streaming.model.PartitionCommand", "spring.json.use.type.headers=false"})
    public void receiveBar(PartitionCommand partitionCommand) {

        final Date date = new Date();
        String paymentKey = String.valueOf(date.getTime());
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Long>> myTotalFuture = future(MY_USER_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], Long>> myTotalStore = Mono.fromFuture(toCompletableFuture(myTotalFuture));

        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Long>> usersFuture = future(USER_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], Long>> myStore = Mono.fromFuture(toCompletableFuture(usersFuture));
        myStore.flatMap(store -> myTotalStore.map(longStore -> Tuples.of(store, longStore)))
                .flatMap(objects -> {
                    final Long totalView=objects.getT2().get("@total".getBytes());
                    Map<byte[], Long> nev = new HashMap<>();
                    KeyValueIterator<byte[], Long> iterator = objects.getT1().all();
                    while (iterator.hasNext()) {
                        KeyValue<byte[], Long> news = iterator.next();
                        nev.put(news.key, news.value);
                    }
                    iterator.close();
                    return this.kafkaSender.send(paymentsTopics, PaymentRecord.of()
                            .withKey(paymentKey).withPayment((double) Long.parseLong(partitionCommand.getValue())).withTotalViews(totalView)
                            .withIds(nev).withDate(date).build(), paymentKey.getBytes(), true).subscribeOn(Schedulers.boundedElastic());
                }).subscribe();
        // .thenMany(outputEvents.flatMap(balanceRecord -> {
        //                    logger.info("Partition Money outputEvents balance id => '{}' total balance => '{}'", balanceRecord.getKey(), balanceRecord.getTotalBalance());
        //                    return this.kafkaSender.send(balancesTopics, balanceRecord, balanceRecord.getKey().getBytes(), true).subscribeOn(Schedulers.parallel());
        //                }))
       // outputEvents.map(balanceRecord -> balanceRecord).subscribe(balanceRecord -> logger.info("last events --> {}",balanceRecord.getKey()));
//        myTotalStore.map(longStore -> longStore.get("@total".getBytes()))
//                .map(totalView -> myStore.flatMap(store -> {
//                    Map<byte[], Long> nev = new HashMap<>();
//                    KeyValueIterator<byte[], Long> iterator = store.all();
//                    while (iterator.hasNext()) {
//                        KeyValue<byte[], Long> news = iterator.next();
//                        nev.put(news.key, news.value);
//                    }
//                    iterator.close();
//                    logger.info("Partition Money COUNTS_STORE PageViews size => '{}' id => '{}'", nev.size(), partitionCommand.getId());
//                    return this.kafkaSender.send(paymentsTopics, PaymentRecord.of()
//                            .withKey(paymentKey).withPayment((double) Long.parseLong(partitionCommand.getValue())).withTotalViews(totalView)
//                            .withIds(nev).withDate(date).build(), paymentKey.getBytes(), true).subscribeOn(Schedulers.boundedElastic());
//                }).subscribeOn(Schedulers.boundedElastic()).thenMany(outputEvents.flatMap(balanceRecord ->
//                        this.kafkaSender.send(balancesTopics, balanceRecord, balanceRecord.getKey().getBytes(), true))
//                        .subscribeOn(Schedulers.boundedElastic())).subscribe())
//                .then(hotBalanceRecords().collectList().map(balanceRecords -> {
//                    chatRoomEntry.onPostMessage(balanceRecords, "hotRecords", new Date(), "hotRecords-@" + partitionCommand.getId());
//                    for (BalanceRecord balanceRecord : balanceRecords)
//                        logger.info("Partition Money balance id => '{}' total balance => '{}'", balanceRecord.getKey(), balanceRecord.getTotalBalance());
//                    return true;
//                })).subscribe(aBoolean -> logger.info("Partition Money bool => '{}'", aBoolean));
        latch.countDown();
    }

    @KafkaListener(topics = "${kafka.topics.paymentcom-in}", properties = {"spring.json.value.default.type=com.bros.streaming.model.PaymentCommand", "spring.json.use.type.headers=false"})
    public void receiveBaz(PaymentCommand paymentCommand) {

        final Date date = new Date();
        Map<byte[], Long> nesm = new HashMap<>();
        String paymentKey = String.valueOf(date.getTime());
        for (String il : paymentCommand.getValue()) {
            nesm.put(il.getBytes(), -1L);
        }
        this.kafkaSender.send(paymentsTopics, PaymentRecord.of()
                .withKey(paymentKey).withPayment(-1.0).withTotalViews(-1L)
                .withIds(nesm).withDate(date).build(), paymentKey.getBytes(), true)
                .subscribeOn(Schedulers.boundedElastic()).subscribe();
        latch.countDown();
    }

    public Flux<BalanceRecord> hotBalanceRecords() {
//        logger.info("Number of sub topologies => {}", this.factoryBean.getTopology().describe());
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], BalanceRecord>> usersFuture = future(BALANCE_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], BalanceRecord>> myStore = Mono.fromFuture(toCompletableFuture(usersFuture));
        return myStore.flatMapIterable(store -> {
            List<BalanceRecord> balanceRecords = new ArrayList<>();
            KeyValueIterator<byte[], BalanceRecord> iterator = store.all();
            while (iterator.hasNext()) {
                KeyValue<byte[], BalanceRecord> next = iterator.next();
                balanceRecords.add(next.value);
            }
            iterator.close();
            //   logger.info("BALANCE_STORE Balance Records size => {}", balanceRecords.size());
            return balanceRecords;
        });
    }

    public Flux<BalanceRecord> getBalanceHistory(List<byte[]> ids) {
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], BalanceRecord>> balanceHistory = future(BALANCE_HISTORY_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], BalanceRecord>> history = Mono.fromFuture(toCompletableFuture(balanceHistory));
        //  logger.info("BALANCE_HISTORY_STORE list size => {}", ids.size());
        return history.flatMapMany(maStore -> Flux.fromIterable(ids).flatMap(bytes -> Mono.fromCallable(() -> maStore.get(bytes)).subscribeOn(Schedulers.boundedElastic())));
    }

    public Flux<BalanceRecord> getUserHistory(String id) {
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], ByteDataAccu>> usersHistory = future(USER_BALANCE_STORE);
        Mono<ReadOnlyKeyValueStore<byte[], ByteDataAccu>> myHistory = Mono.fromFuture(toCompletableFuture(usersHistory));
        //     logger.info("USER_BALANCE_STORE User ID => {}", id);
        return myHistory.flatMapMany(store -> {
            ByteDataAccu list = store.get(id.getBytes());
            return list != null ? this.getBalanceHistory(list.getList()) : Flux.empty();
        });
    }

    public static <K, V> CompletableFuture<ReadOnlyKeyValueStore<K, V>> toCompletableFuture(ListenableFuture<ReadOnlyKeyValueStore<K, V>> listenableFuture) {
        final CompletableFuture<ReadOnlyKeyValueStore<K, V>> completableFuture = new CompletableFuture<>();
        Futures.addCallback(listenableFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(ReadOnlyKeyValueStore<K, V> result) {
                completableFuture.complete(result);
            }

            @Override
            @ParametersAreNonnullByDefault
            public void onFailure(Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public static <T> T waitUntilStoreIsQueryable(final String storeName,
                                                  final QueryableStoreType<T> queryableStoreType,
                                                  final KafkaStreams streams) throws InterruptedException {
        KStreamConf.startupLatch.await();
        return streams.store(StoreQueryParameters.fromNameAndType(storeName, queryableStoreType));
    }

    public <K, V> ListenableFuture<ReadOnlyKeyValueStore<K, V>> future(final String storeName) {
        return pool.submit(() -> waitUntilStoreIsQueryable(storeName, QueryableStoreTypes.keyValueStore(), this.factoryBean.getKafkaStreams()));
    }
}
