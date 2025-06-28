package com.ai.assistant.usecase.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectFileService {

    private final MinioClient minioClient;
    private final String projectBucket;

    private static final Set<String> IGNORED_DIRS = Set.of(
            "node_modules",
            "target",
            "build",
            "dist",
            "out",
            ".git",
            ".gradle",
            ".idea",
            "venv",
            ".venv",
            ".mvn",
            "mvn"
    );

    private static final Set<String> ALLOWED_FILENAMES = Set.of(
            "pom.xml",
            "package.json",
            "package-lock.json",
            "tsconfig.json",
            "vite.config.ts",
            "tailwind.config.js",
            "postcss.config.js",
            "build.gradle",
            "settings.gradle",
            "gradle.properties",
            ".gitignore",
            "README.md",
            "readme.md"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".java",
            ".kt",
            ".ts",
            ".tsx",
            ".js",
            ".jsx",
            ".json",
            ".xml",
            ".yml",
            ".yaml",
            ".gradle",
            ".properties",
            ".md",
            ".html",
            ".css",
            ".scss",
            ".txt",
            ".sql"
    );

    private static boolean isIgnored(Path base, Path path) {
        Path rel = base.relativize(path);
        for (int i = 0; i < rel.getNameCount(); i++) {
            if (IGNORED_DIRS.contains(rel.getName(i).toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllowed(Path path) {
        String name = path.getFileName().toString();
        if (ALLOWED_FILENAMES.contains(name)) return true;
        int idx = name.lastIndexOf('.');
        if (idx == -1) return false;
        String ext = name.substring(idx);
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    public Mono<Path> saveToTempDir(Flux<FilePart> files) {
        return Mono.fromCallable(() -> Files.createTempDirectory("project-"))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(dir -> Flux.from(files)
                        .flatMap(fp -> {
                            String name = fp.filename().replace("\\", "/");
                            if (name.contains("..") || name.startsWith("/") || Path.of(name).isAbsolute()) {
                                return Mono.error(new IllegalArgumentException("Invalid path: " + name));
                            }
                            Path target = dir.resolve(name);
                            return Mono.fromCallable(() -> {
                                        Files.createDirectories(target.getParent());
                                        return target;
                                    })
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .then(fp.transferTo(target));
                        })
                        .then()
                        .thenReturn(dir));
    }

    public Mono<Void> deleteAllFiles(Long id) {
        return Mono.fromRunnable(() -> {
            try {
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(projectBucket)
                                .prefix("projects/" + id + "/")
                                .recursive(true)
                                .build());
                for (Result<Item> r : results) {
                    Item item = r.get();
                    if (item.objectName().endsWith("/")) continue;
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(projectBucket)
                                    .object(item.objectName())
                                    .build());
                }
            } catch (Exception e) {
                log.error("[project:file] Failed to delete existing files for project {}", id, e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> storeFilesFromDir(Long id, Path dir) {
        return deleteAllFiles(id)
                .then(Flux.using(
                        () -> Files.walk(dir),
                        stream -> Flux.fromStream(
                        stream.filter(p -> Files.isRegularFile(p)
                                && !isIgnored(dir, p)
                                && isAllowed(p))
                        ),
                        Stream::close
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(path -> Mono.fromCallable(() -> {
                    try (InputStream is = Files.newInputStream(path)) {
                        String objectName = "projects/" + id + "/" + dir.relativize(path).toString().replace("\\", "/");
                        minioClient.putObject(PutObjectArgs.builder()
                                .bucket(projectBucket)
                                .object(objectName)
                                .stream(is, Files.size(path), -1)
                                .build());
                    }
                    return (Void) null;
                }).subscribeOn(Schedulers.boundedElastic()), 4)
                .then());
    }

    public Mono<Void> cleanupTempDir(Path dir) {
        return Mono.fromRunnable(() -> {
            try (Stream<Path> paths = Files.walk(dir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (Exception ignore) {
                            }
                        });
            } catch (Exception ex) {
                log.error("[project:file] Failed to clean temp dir {}", dir, ex);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<List<String>> listFiles(Long id) {
        return Mono.fromCallable(() -> {
                    Iterable<Result<Item>> results = minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(projectBucket)
                                    .prefix("projects/" + id + "/")
                                    .recursive(true)
                                    .build());
                    List<String> items = new ArrayList<>();
                    for (Result<Item> r : results) {
                        Item item = r.get();
                        String objectName = item.objectName();
                        if (objectName.endsWith("/")) continue;
                        String relPath = objectName.substring(("projects/" + id + "/").length());
                        if (Arrays.stream(relPath.split("/"))
                                .anyMatch(IGNORED_DIRS::contains)) continue;
                        if (!isAllowed(Path.of(relPath))) continue;
                        items.add(relPath);
                    }
                    return items;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> readFiles(Long id) {
        return Mono.fromCallable(() -> {
                    Iterable<Result<Item>> results = minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(projectBucket)
                                    .prefix("projects/" + id + "/")
                                    .recursive(true)
                                    .build());
                    List<String> contents = new ArrayList<>();
                    for (Result<Item> r : results) {
                        Item item = r.get();
                        String objectName = item.objectName();
                        if (objectName.endsWith("/")) continue;
                        String relPath = objectName.substring(("projects/" + id + "/").length());
                        if (Arrays.stream(relPath.split("/"))
                                .anyMatch(IGNORED_DIRS::contains)) continue;
                        try (InputStream is = minioClient.getObject(
                                io.minio.GetObjectArgs.builder()
                                        .bucket(projectBucket)
                                        .object(objectName)
                                        .build())) {
                            contents.add(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                        }
                    }
                    return contents;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> readFiles(Long id, List<String> paths) {
        return Flux.fromIterable(paths)
                .filter(p -> isAllowed(Path.of(p)))
                .flatMap(path -> Mono.fromCallable(() -> {
                    try (InputStream is = minioClient.getObject(
                            io.minio.GetObjectArgs.builder()
                                    .bucket(projectBucket)
                                    .object("projects/" + id + "/" + path)
                                    .build())) {
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .collectList();
    }
}
