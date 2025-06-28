package com.ai.assistant.external.kafka.listener;

import com.ai.assistant.event.ProjectUploadedEvent;
import com.ai.assistant.usecase.service.ProjectService;
import com.ai.assistant.usecase.service.ProjectFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class ProjectEventListener {

    private final ProjectService projectService;
    private final ProjectFileService fileService;

    public ProjectEventListener(ProjectService projectService, ProjectFileService fileService) {
        this.projectService = projectService;
        this.fileService = fileService;
    }

    @Bean
    public Consumer<List<ProjectUploadedEvent>> projectUploaded() {
        return events -> {
            log.info("[kafka:project] Received {} project events", events.size());
            Flux.fromIterable(events)
                    .flatMap(event -> {
                        Path dir = Path.of(event.tempDir());
                        return projectService.storeFilesFromDir(event.projectId(), dir)
                                .doOnError(e -> log.error("[kafka:project] Failed to store files for project {}", event.projectId(), e))
                                .publishOn(Schedulers.boundedElastic())
                                .doFinally(sig -> fileService.cleanupTempDir(dir)
                                        .doOnError(err -> log.error("[kafka:project] Failed to clean temp dir {}", dir, err))
                                        .subscribe());
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnComplete(() -> log.info("[kafka:project] processed {} events", events.size()))
                    .subscribe();
        };
    }
}
