package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {

    private FileService fileService;
    private FileController fileController;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    public void whenGetByIdThenReturnResponseWithFile() {
        var id = 1;
        FileDto fileDto = new FileDto("file", "content".getBytes());
        when(fileService.getFileById(id)).thenReturn(Optional.of(fileDto));
        ResponseEntity<?> response = fileController.getById(id);
        assertThat(response).isEqualTo(ResponseEntity.ok(fileDto.getContent()));
    }

    @Test
    public void whenGetByIdThenNotFound() {
        var id = 1;
        when(fileService.getFileById(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = fileController.getById(id);
        assertThat(response).isEqualTo(ResponseEntity.notFound().build());
    }
}