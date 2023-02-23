package com.luv2code.springmvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class GradbookControllerTest {

    private static MockHttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    StudentAndGradeService studentAndGradeService;

    @Autowired
    private JdbcTemplate jdbc;

    private static final  MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollegeStudent student;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Autowired
    private StudentAndGradeService studentService;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
        request.addParameter("firstname", "Mouloud");
        request.addParameter("lastname", "Dernoun");
        request.addParameter("emailaddress", "mouloud.dernoun@gmail.com");
    }

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    void getStudentHttpRequest() throws Exception{
        
        student.setFirstname("Mouloud");
        student.setLastname("Dernoun");
        student.setFirstname("mouloud.dernoun@gmail.com");
        entityManager.persist(student);
        entityManager.flush();

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(2)))
            .andReturn();
    }

    @Test
    void createStudentHttpRequest() throws Exception{
        student.setFirstname("Oumnia");
        student.setLastname("tafer");
        student.setEmailAddress("taferoumnia@gmail.com");

        mockMvc.perform(post("/")
            .contentType(APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(student)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andReturn();

        CollegeStudent verifyStudent = studentDao.findByEmailAddress("taferoumnia@gmail.com");
        assertNotNull(verifyStudent);
    }

    @Test
    void deleteStudentHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent(), "Check first if the student exists");

        mockMvc.perform(delete("/student/{id}", 1)
                                        .contentType(APPLICATION_JSON_UTF8))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(0)))
                                        .andReturn();
        assertFalse(studentDao.findById(1).isPresent(), "Check first if the student exists");
    }

    @Test
    void deleteStudenDoesNotExiststHttpRequest() throws Exception {
        assertFalse(studentDao.findById(0).isPresent(), "Check first if the student exists");

        mockMvc.perform(delete("/student/{id}", 0)
                                        .contentType(APPLICATION_JSON_UTF8))
                                        .andExpect(status().is4xxClientError())
                                        .andExpect(jsonPath("$.status", is(404)))
                                        .andReturn();
    }

    @Test
    void studentInformationtHttpRequest() throws Exception{
        Optional<CollegeStudent> student = studentDao.findById(1);
        assertTrue(student.isPresent());

        mockMvc.perform(get("/studentInformation/{id}", 1))
                            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                            .andExpect(jsonPath("$.id", is(1)))
                            .andExpect(jsonPath("$.firstname", is("Mouloud")))
                            .andExpect(status().isOk())
                            .andReturn();
    }

    @Test
    void studentInformationtDoesNotHttpRequest() throws Exception{
        Optional<CollegeStudent> student = studentDao.findById(0);
        assertFalse(student.isPresent());

        mockMvc.perform(get("/studentInformation/{id}", 0))
                            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                            .andExpect(jsonPath("$.status", is(404)))
                            .andExpect(status().is4xxClientError())
                            .andReturn();
    }

    @AfterEach
    void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
}
