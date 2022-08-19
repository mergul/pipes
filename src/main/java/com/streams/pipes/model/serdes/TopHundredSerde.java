package com.streams.pipes.model.serdes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streams.pipes.model.RecordSSE;
import com.streams.pipes.model.TopHundredNews;
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
public class TopHundredSerde implements Serde<TopHundredNews> {

    @Autowired
    private final ObjectMapper objectMapper;

    public TopHundredSerde(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(final Map<String, ?> map, final boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<TopHundredNews> serializer() {
      return new Serializer<TopHundredNews>() {
        @Override
        public void configure(final Map<String, ?> map, final boolean b) {
        }

        @Override
        public byte[] serialize(final String s, final TopHundredNews topHundredSongs) {
            try {
                List<RecordSSE> list= new ArrayList<>();
            for (RecordSSE songPlayCount : topHundredSongs) {
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
    public Deserializer<TopHundredNews> deserializer() {
      return new Deserializer<TopHundredNews>() {
        @Override
        public void configure(final Map<String, ?> map, final boolean b) {

        }

        @Override
        public TopHundredNews deserialize(final String s, final byte[] bytes) {
          if (bytes == null || bytes.length == 0) {
            return null;
          }
          final TopHundredNews result = new TopHundredNews();
          try {
              List<RecordSSE> sseList=objectMapper
                      .readValue(bytes, new TypeReference<List<RecordSSE>>() { });
              for (RecordSSE sse : sseList){
                  result.add(sse);
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

