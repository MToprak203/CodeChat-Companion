package com.ai.assistant.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboxEventType {
    PROJECT_UPLOADED("projectUploaded-out", "projectUploaded-in-0"),
    CONVERSATION_CLEANUP("conversationCleanup-out", "conversationCleanup-in-0"),
    PROJECT_CLEANUP("projectCleanup-out", "projectCleanup-in-0");

    private final String outBindingName;
    private final String inBindingName;
}
