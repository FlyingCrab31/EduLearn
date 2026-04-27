package com.edulearn.assessment;

import com.edulearn.assessment.exception.ResourceNotFoundException;
import com.edulearn.assessment.resource.AssessmentResource;
import com.edulearn.assessment.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentResource.class)
public class AssessmentResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentService assessmentService;

    @Test
    void testGetQuizById_NotFound_Returns404() throws Exception {
        when(assessmentService.getQuizById(1L))
                .thenThrow(new ResourceNotFoundException("Quiz not found with id: 1"));

        mockMvc.perform(get("/api/assessment/quizzes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Quiz not found with id: 1"))
                .andExpect(jsonPath("$.path").value("/api/assessment/quizzes/1"));
    }
}
