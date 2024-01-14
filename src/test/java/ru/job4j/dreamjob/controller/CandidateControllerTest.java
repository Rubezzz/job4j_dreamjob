package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CandidateControllerTest {
    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        var candidate2 = new Candidate(2, "test2", "desc2", now(), false, 3, 4);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualVacancies = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualVacancies).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidateByIdPageThenGetPageWithCandidate() {
        var id = 1;
        var candidate = new Candidate(id, "test1", "desc1", now(), true, 1, 2);
        when(candidateService.findById(id)).thenReturn(Optional.of(candidate));

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, id);
        var actualCandidate = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCandidate).isEqualTo(candidate);
    }

    @Test
    public void whenUpdateCandidateWithFileThenSameDataAndRedirectToCandidatesPage() {
        var candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenUpdateCandidateWithFileThenNotUpdateAndRedirectTo404Page() {
        var candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Резюме с указанным идентификатором не найдено");
    }

    @Test
    public void whenDeleteCandidateThenDeleteAndRedirectToCandidatesPage() {
        var id = 1;
        when(candidateService.deleteById(id)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, id);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenDeleteCandidateThenNotDeleteAndRedirectTo404Page() {
        var id = 1;
        when(candidateService.deleteById(id)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, id);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Резюме с указанным идентификатором не найдена");
    }

}