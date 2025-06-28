package com.ai.assistant.presentation.controller;

import com.ai.assistant.core.dto.ApiResponse;
import com.ai.assistant.event.MessageEvent;
import com.ai.assistant.usecase.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ai.assistant.security.annotation.CurrentUser;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ai.assistant.core.Constants.Path.Conversation.CONVERSATION_V1;
import static com.ai.assistant.core.Constants.Path.Conversation.Message.BASE;
import static com.ai.assistant.core.Constants.Path.Conversation.Message.UNREAD;

@RestController
@RequestMapping(CONVERSATION_V1 + BASE)
@RequiredArgsConstructor
@Tag(name = "Message", description = "Endpoints for retrieving messages")
public class MessageController {
    private final MessageService messageService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get conversation messages")
    public Mono<ResponseEntity<ApiResponse<List<MessageEvent>>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Mono<List<MessageEvent>> messages = messageService.fetchMessages(conversationId, page, size)
                .collectList();
        Mono<Long> count = messageService.countMessages(conversationId);
        return Mono.zip(messages, count)
                .map(t -> {
                    List<MessageEvent> data = t.getT1();
                    long total = t.getT2();
                    int totalPages = (int) Math.ceil((double) total / size);
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("page", page);
                    meta.put("size", size);
                    meta.put("totalPages", totalPages);
                    return ResponseEntity.ok(ApiResponse.success(data, meta));
                });
    }

    @GetMapping(path = UNREAD, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get unread messages for the current user")
    public Mono<ResponseEntity<ApiResponse<List<MessageEvent>>>> getUnreadMessages(
            @PathVariable Long conversationId,
            @CurrentUser Long userId
    ) {
        return messageService.fetchUnreadMessages(conversationId, userId)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }
}
