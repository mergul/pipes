package com.streams.pipes.service;

//import com.bros.streaming.config.streams.KStreamConf;
//import org.apache.kafka.streams.KafkaStreams;
//import org.apache.kafka.streams.StoreQueryParameters;
//import org.apache.kafka.streams.state.QueryableStoreType;
//import org.apache.kafka.streams.state.QueryableStoreTypes;
//import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
//import org.springframework.kafka.config.StreamsBuilderFactoryBean;
//import org.springframework.stereotype.Service;
//import org.springframework.util.concurrent.ListenableFutureTask;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.CompletableFuture;

//@Service
public class StoreService {

//    private final StreamsBuilderFactoryBean builder;
//
//    public StoreService(StreamsBuilderFactoryBean builder) {
//        this.builder = builder;
//    }
//
//    public <K, V> CompletableFuture<ReadOnlyKeyValueStore<K, V>> getStore(String storeName) {
//        Callable<ReadOnlyKeyValueStore<K, V>> callable = () -> waitUntilStoreIsQueryable(storeName, QueryableStoreTypes.keyValueStore(), this.builder.getKafkaStreams());
//        ListenableFutureTask<ReadOnlyKeyValueStore<K, V>> task = new ListenableFutureTask<>(callable);
//        return task.completable();
//    }
//    public static <T> T waitUntilStoreIsQueryable(final String storeName,
//                                                  final QueryableStoreType<T> queryableStoreType,
//                                                  final KafkaStreams streams) throws InterruptedException {
//        KStreamConf.startupLatch.await();
//        return streams.store(StoreQueryParameters.fromNameAndType(storeName, queryableStoreType));
//    }
}
