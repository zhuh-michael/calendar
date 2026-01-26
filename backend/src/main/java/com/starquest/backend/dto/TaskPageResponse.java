package com.starquest.backend.dto;

import com.starquest.backend.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TaskPageResponse {
    private List<Task> tasks;
    private long total;
    private int page;
    private int size;
}

