package com.ai.assistant.presentation.controller;

import com.ai.assistant.core.dto.ApiResponse;
import com.ai.assistant.dto.response.conversation.ConversationResponseDTO;
import com.ai.assistant.dto.request.conversation.ConversationUpdateRequestDTO;
import com.ai.assistant.security.annotation.CurrentUser;
import com.ai.assistant.usecase.service.ConversationService;
import com.ai.assistant.usecase.service.ParticipantService;
import com.ai.assistant.external.ai.service.AiStreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Path.Conversation.CONVERSATION_V1;
import static com.ai.assistant.core.Constants.Path.Conversation.Method.AI;
import static com.ai.assistant.core.Constants.Path.Conversation.Method.DIRECT;
import static com.ai.assistant.core.Constants.Path.Conversation.Method.GROUP;
import static com.ai.assistant.core.Constants.Path.Conversation.Method.NO_PROJECT;

@Slf4j
@RestController
@RequestMapping(CONVERSATION_V1)
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "Conversation endpoints for creating and managing conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final AiStreamingService aiStreamingService;


    @PostMapping(path = AI, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new conversation with AI",
            description = "Creates a new conversation that includes the AI as a participant. The user is automatically added as the creator."
    )
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> createConversationWithAI(@CurrentUser Long userId) {
        log.debug("[conversation:create:request] userId={} action=createWithAI", userId);
        return conversationService.createConversationWithAI(userId).map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new conversation",
            description = "Creates a new conversation with AI included by default"
    )
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> createConversation(@CurrentUser Long userId) {
        log.debug("[conversation:create:request] userId={} action=createDefault", userId);
        return conversationService.createConversationWithAI(userId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @PostMapping(path = DIRECT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a direct conversation")
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> createDirectConversation(
            @CurrentUser Long userId,
            @RequestParam("participantId") Long participantId
    ) {
        log.debug("[conversation:create:request] userId={} action=createDirect participantId={}", userId, participantId);
        return conversationService.createDirectConversation(userId, participantId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @PostMapping(path = GROUP, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a group conversation")
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> createGroupConversation(
            @CurrentUser Long userId,
            @RequestParam("participantIds") Set<Long> participantIds,
            @RequestParam(defaultValue = "false") boolean includeAi
    ) {
        log.debug("[conversation:create:request] userId={} action=createGroup includeAi={}", userId, includeAi);
        Mono<ConversationResponseDTO> mono = includeAi
                ? conversationService.createGroupConversationWithAI(userId, participantIds)
                : conversationService.createGroupConversation(userId, participantIds);
        return mono.map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List conversations")
    public Mono<ResponseEntity<ApiResponse<java.util.List<ConversationResponseDTO>>>> getConversations(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Mono<java.util.List<ConversationResponseDTO>> items = conversationService.fetchConversations(userId, page, size).collectList();
        Mono<Long> count = conversationService.countConversations(userId);
        return Mono.zip(items, count)
                .map(t -> {
                    int totalPages = (int) Math.ceil((double) t.getT2() / size);
                    java.util.Map<String, Object> meta = new java.util.HashMap<>();
                    meta.put("page", page);
                    meta.put("size", size);
                    meta.put("totalPages", totalPages);
                    return ResponseEntity.ok(ApiResponse.success(t.getT1(), meta));
                });
    }

    @GetMapping(path = NO_PROJECT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List conversations without project")
    public Mono<ResponseEntity<ApiResponse<java.util.List<ConversationResponseDTO>>>> getNoProjectConversations(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Mono<java.util.List<ConversationResponseDTO>> items = conversationService.fetchNoProjectConversations(userId, page, size).collectList();
        Mono<Long> count = conversationService.countNoProjectConversations(userId);
        return Mono.zip(items, count)
                .map(t -> {
                    int totalPages = (int) Math.ceil((double) t.getT2() / size);
                    java.util.Map<String, Object> meta = new java.util.HashMap<>();
                    meta.put("page", page);
                    meta.put("size", size);
                    meta.put("totalPages", totalPages);
                    return ResponseEntity.ok(ApiResponse.success(t.getT1(), meta));
                });
    }

    @PutMapping(path = "/{conversationId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update conversation title")
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> updateConversation(
            @PathVariable Long conversationId,
            @RequestBody ConversationUpdateRequestDTO request
    ) {
        return conversationService.updateTitle(conversationId, request.title())
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @DeleteMapping(path = "/{conversationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Leave conversation")
    public Mono<ResponseEntity<ApiResponse<Void>>> leaveConversation(
            @CurrentUser Long userId,
            @PathVariable Long conversationId
    ) {
        return conversationService.leaveConversation(conversationId, userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PostMapping(path = com.ai.assistant.core.Constants.Path.Conversation.Stop.BASE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Stop AI generation for a conversation")
    public Mono<ResponseEntity<ApiResponse<Void>>> stopAiGeneration(
            @PathVariable Long conversationId
    ) {
        return aiStreamingService.stopStreaming(conversationId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }


    @GetMapping(path = com.ai.assistant.core.Constants.Path.Conversation.Participant.BASE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List participants for a conversation")
    public Mono<ResponseEntity<ApiResponse<java.util.List<com.ai.assistant.dto.response.user.UserSummaryDTO>>>> getParticipants(
            @PathVariable Long conversationId
    ) {
        return participantService.fetchParticipants(conversationId)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    @PostMapping(path = com.ai.assistant.core.Constants.Path.Conversation.Participant.BASE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add participant to conversation")
    public Mono<ResponseEntity<ApiResponse<Void>>> addParticipant(
            @PathVariable Long conversationId,
            @RequestParam("userId") Long userId
    ) {
        return participantService.addParticipant(conversationId, userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @DeleteMapping(path = com.ai.assistant.core.Constants.Path.Conversation.Participant.USER,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove participant from conversation")
    public Mono<ResponseEntity<ApiResponse<Void>>> removeParticipant(
            @PathVariable Long conversationId,
            @PathVariable Long userId
    ) {
        return participantService.removeParticipant(conversationId, userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }
}
