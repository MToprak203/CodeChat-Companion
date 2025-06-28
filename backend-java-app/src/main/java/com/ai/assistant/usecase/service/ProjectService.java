package com.ai.assistant.usecase.service;

import com.ai.assistant.event.ProjectUploadedEvent;
import com.ai.assistant.event.ProjectCleanupEvent;
import com.ai.assistant.persistence.relational.entity.Project;
import com.ai.assistant.persistence.relational.repository.ProjectRepository;
import com.ai.assistant.persistence.relational.helper.AuditorAwareProvider;
import com.ai.assistant.usecase.service.OutboxService;
import com.ai.assistant.usecase.service.ProjectFileService;
import com.ai.assistant.usecase.service.SystemNotificationService;
import com.ai.assistant.usecase.service.ProjectParticipantService;
import com.ai.assistant.dto.response.project.ProjectResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileService fileService;
    private final OutboxService outboxService;
    private final SystemNotificationService systemNotificationService;
    private final ProjectParticipantService projectParticipantService;

    public Mono<Long> createProject(String name, Flux<FilePart> files) {
        Project project = Project.builder().name(name).build();
        return projectRepository.save(project)
                .flatMap(p -> AuditorAwareProvider.getAuditorAware().getCurrentAuditor()
                        .flatMap(uid -> projectParticipantService.addParticipant(p.getId(), uid).thenReturn(p))
                )
                .flatMap(p -> fileService.saveToTempDir(files)
                        .onErrorResume(e -> projectRepository.deleteById(p.getId()).then(Mono.error(e)))
                        .flatMap(dir -> outboxService.dispatchEvent(
                                        new ProjectUploadedEvent(p.getId(), p.getName(), dir.toString(), LocalDateTime.now())
                                )
                                .onErrorResume(e -> fileService.cleanupTempDir(dir)
                                        .onErrorResume(err -> Mono.empty())
                                        .then(projectRepository.deleteById(p.getId()))
                                        .then(Mono.error(e)))
                                .thenReturn(p.getId())
                        )
                );
    }

    public Mono<Void> syncProject(Long id, String name, Flux<FilePart> files) {
        Mono<Void> updateName = Mono.empty();
        if (name != null && !name.isBlank()) {
            updateName = projectRepository.findById(id)
                    .flatMap(p -> {
                        p.setName(name);
                        return projectRepository.save(p).then();
                    });
        }

        return updateName.then(
                fileService.saveToTempDir(files)
                        .flatMap(dir -> outboxService.dispatchEvent(
                                        new ProjectUploadedEvent(id, name, dir.toString(), LocalDateTime.now())
                                )
                                .onErrorResume(e -> fileService.cleanupTempDir(dir)
                                        .onErrorResume(err -> Mono.empty())
                                        .then(Mono.error(e)))
                        )
        );
    }

    public Mono<List<String>> listFiles(Long id) {
        return fileService.listFiles(id);
    }

    public Mono<String> readFile(Long id, String path) {
        return fileService.readFiles(id, List.of(path))
                .map(list -> list.isEmpty() ? "" : list.get(0));
    }

    public Mono<Void> storeFilesFromDir(Long id, Path dir) {
        return fileService.storeFilesFromDir(id, dir)
                .then(projectRepository.findById(id))
                .flatMap(project -> {
                    Long userId = project.getCreatedById();
                    if (userId == null) return Mono.empty();
                    return systemNotificationService.notifyUser(userId, "Project upload completed");
                })
                .then();
    }

    public Flux<ProjectResponseDTO> listProjects(Long userId) {
        return projectRepository.findAllActiveByUserId(userId)
                .map(p -> new ProjectResponseDTO(p.getId(), p.getName()));
    }

    public Mono<Void> leaveProject(Long id, Long userId) {
        Long deleter = userId == null ? 0L : userId;
        return projectParticipantService.removeParticipant(id, userId)
                .then(outboxService.dispatchEvent(
                        new ProjectCleanupEvent(id, deleter, LocalDateTime.now())
                ));
    }
}
