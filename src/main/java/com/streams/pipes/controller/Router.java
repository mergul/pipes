package com.streams.pipes.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streams.pipes.model.MyParameterizedTypeImpl;
import com.streams.pipes.model.NewsPayload;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Component
public class Router {

    private final NewsHandler newsHandler;

    private final ObjectMapper objectMapper;

    public Router(NewsHandler newsHandler, ObjectMapper objectMapper) {
        this.newsHandler = newsHandler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public <T> RouterFunction<?> landingRouterFunction() {
        return RouterFunctions.nest(RequestPredicates.path("/sse"),
                RouterFunctions.route(RequestPredicates.GET("/topnewslist/{etiket}"),
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(newsHandler.getTopNewsList(request
                                        .pathVariable("etiket")), NewsPayload.class))
                        .andRoute(RequestPredicates.GET("/setUser/{id}/{random}"), request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(newsHandler.setUser(request.pathVariable("id"), request.pathVariable("random")), Boolean.class))
                        .andRoute(RequestPredicates.PATCH("/setNewsCounts"),
                                request -> ServerResponse.ok()
                                        .body(newsHandler.setNewsCounts(request.bodyToMono(NewsPayload.class)), Boolean.class))
                        .andRoute(RequestPredicates.PATCH("/unsubscribe"),
                                request -> ServerResponse.ok()
                                        .body(newsHandler.unsubscribeChatMessages(request.bodyToMono(String.class)), Boolean.class))
                        .andRoute(RequestPredicates.GET("/chat/room/{chatRoom}/subscribeMessages"),
                                request -> ServerResponse.ok().header("X-Accel-Buffering", "no")
                                        .contentType(MediaType.TEXT_EVENT_STREAM)
                                        .body(newsHandler.subscribeChatMessages(request.pathVariable("chatRoom")), new ParameterizedTypeReference<ServerSentEvent<T>>() {
                                            @NonNull
                                            public Type getType() {
                                                return new MyParameterizedTypeImpl((ParameterizedType) super.getType(), new Type[]{getClazz(request.pathVariable("chatRoom"))});
                                            }
                                        }))
                        .andOther(RouterFunctions.resources("/**", new ClassPathResource("static/"))));
    }

    @SuppressWarnings("unchecked")
    private <T> JavaType getClazz(String chatRoom) {
        String classType = chatRoom.startsWith("TopNews") ? "com.streams.pipes.model.TopThreeHundredNews" :
                (chatRoom.startsWith("TopTags") ? "com.streams.pipes.model.TopHundredNews" :
                        (chatRoom.startsWith("TopTas") ? "com.streams.pipes.model.RecordSSE":"com.streams.pipes.model.BalanceRecord"));
        Class<T> clazz = null;
        try {
            clazz = (Class<T>) ClassUtils.forName(classType, ClassUtils.getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Class<T> finalClazz = clazz;
        return objectMapper.constructType(finalClazz);
    }
}
