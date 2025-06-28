package com.ai.assistant.presentation.controller;

import com.ai.assistant.core.dto.ApiResponse;
import com.ai.assistant.dto.response.project.ProjectResponseDTO;
import com.ai.assistant.dto.response.conversation.ConversationResponseDTO;
import com.ai.assistant.usecase.service.ProjectService;
import com.ai.assistant.usecase.service.ProjectSelectedFileService;
import com.ai.assistant.usecase.service.ConversationService;
import com.ai.assistant.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.ai.assistant.core.Constants.Path.Project.PROJECT_V1;
import static com.ai.assistant.core.Constants.Path.Project.Method.TREE;
import static com.ai.assistant.core.Constants.Path.Project.Method.SYNC;
import static com.ai.assistant.core.Constants.Path.Project.Method.CONVERSATION;
import static com.ai.assistant.core.Constants.Path.Project.Method.SELECTED_FILES;
import static com.ai.assistant.core.Constants.Path.Project.Method.FILE;

@Slf4j
@RestController
@RequestMapping(PROJECT_V1)
@RequiredArgsConstructor
@Tag(name = "Project", description = "Upload and manage user projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ConversationService conversationService;
    private final ProjectSelectedFileService selectedFileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List projects")
    public Mono<ResponseEntity<ApiResponse<java.util.List<ProjectResponseDTO>>>> listProjects(
            @CurrentUser Long userId
    ) {
        return projectService.listProjects(userId)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload a new project")
    public Mono<ResponseEntity<ApiResponse<ProjectResponseDTO>>> uploadProject(
            @RequestPart("name") String name,
            @RequestPart("files") Flux<FilePart> files
    ) {
        return projectService.createProject(name, files)
                .map(id -> ResponseEntity.ok(ApiResponse.success(new ProjectResponseDTO(id, name))));
    }

    @PostMapping(path = SYNC, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sync project files")
    public Mono<ResponseEntity<ApiResponse<Void>>> syncProject(
            @PathVariable Long projectId,
            @RequestPart("files") Flux<FilePart> files,
            @RequestPart(value = "name", required = false) String name
    ) {
        return projectService.syncProject(projectId, name, files)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @GetMapping(path = TREE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get project file tree")
    public Mono<ResponseEntity<ApiResponse<List<String>>>> getTree(@PathVariable Long projectId) {
        return projectService.listFiles(projectId)
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    @GetMapping(path = FILE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get content of project file")
    public Mono<ResponseEntity<ApiResponse<String>>> getFile(
            @PathVariable Long projectId,
            @RequestParam String path
    ) {
        return projectService.readFile(projectId, path)
                .map(content -> ResponseEntity.ok(ApiResponse.success(content)));
    }

    @GetMapping(path = CONVERSATION, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get conversations for project")
    public Mono<ResponseEntity<ApiResponse<java.util.List<ConversationResponseDTO>>>> getProjectConversations(
            @CurrentUser Long userId,
            @PathVariable Long projectId
    ) {
        return conversationService.findProjectConversations(projectId, userId)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    @PostMapping(path = CONVERSATION, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create conversation for project")
    public Mono<ResponseEntity<ApiResponse<ConversationResponseDTO>>> createProjectConversation(
            @CurrentUser Long userId,
            @PathVariable Long projectId
    ) {
        return conversationService.createProjectConversation(userId, projectId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @PostMapping(path = SELECTED_FILES, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set selected files for project")
    public Mono<ResponseEntity<ApiResponse<Void>>> setSelectedFiles(
            @CurrentUser Long userId,
            @PathVariable Long projectId,
            @RequestBody List<String> files
    ) {
        return selectedFileService.saveSelectedFiles(userId, projectId, files)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @GetMapping(path = SELECTED_FILES, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get selected files for project")
    public Mono<ResponseEntity<ApiResponse<List<String>>>> getSelectedFiles(
            @PathVariable Long projectId
    ) {
        return selectedFileService.fetchSelectedFiles(projectId)
                .map(list -> ResponseEntity.ok(ApiResponse.success(list)));
    }

    @DeleteMapping(path = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Leave project")
    public Mono<ResponseEntity<ApiResponse<Void>>> leaveProject(
            @CurrentUser Long userId,
            @PathVariable Long projectId
    ) {
        return projectService.leaveProject(projectId, userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }
}
