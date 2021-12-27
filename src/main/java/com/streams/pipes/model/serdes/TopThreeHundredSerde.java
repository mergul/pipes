package com.streams.pipes.model.serdes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streams.pipes.model.NewsPayload;
import com.streams.pipes.model.TopThreeHundredNews;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonComponent
public class TopThreeHundredSerde implements Serde<TopThreeHundredNews> {

    @Autowired
    private final ObjectMapper objectMapper;

    public TopThreeHundredSerde(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(final Map<String, ?> map, final boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<TopThreeHundredNews> serializer() {
        return new Serializer<TopThreeHundredNews>() {
            @Override
            public void configure(final Map<String, ?> map, final boolean b) {
            }

            @Override
            public byte[] serialize(final String s, final TopThreeHundredNews topThreeHundredNews) {
                try {
                    List<NewsPayload> list= new ArrayList<>();
                    for (NewsPayload songPlayCount : topThreeHundredNews) {
                        list.add(songPlayCount);
                    }
                    return objectMapper.writeValueAsBytes(list);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public Deserializer<TopThreeHundredNews> deserializer() {
        return new Deserializer<TopThreeHundredNews>() {
            @Override
            public void configure(final Map<String, ?> map, final boolean b) {

            }

            @Override
            public TopThreeHundredNews deserialize(final String s, final byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                final TopThreeHundredNews result = new TopThreeHundredNews();
                try {
                    List<NewsPayload> newsList=objectMapper
                            .readValue(bytes, new TypeReference<List<NewsPayload>>() { });
                    for (NewsPayload news : newsList){
                        result.add(news);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return result;
            }

            @Override
            public void close() {

            }
        };
    }
}
