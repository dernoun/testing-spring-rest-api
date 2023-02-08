package com.luv2code.springmvc.controller;

import com.luv2code.springmvc.exceptionhandling.StudentOrGradeErrorResponse;
import com.luv2code.springmvc.exceptionhandling.StudentOrGradeNotFoundException;
import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GradebookController {

    private static final String STUDENT_OR_GRADE_WAS_NOT_FOUND = "Student or Grade was not found";

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private Gradebook gradebook;

    @GetMapping(value = "/")
    public List<GradebookCollegeStudent> getStudents() {
        gradebook = studentService.getGradebook();
        return gradebook.getStudents();
    }

    @GetMapping("/studentInformation/{id}")
    public GradebookCollegeStudent studentInformation(@PathVariable int id) {

        if (!studentService.checkIfStudentIsNull(id)) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        return studentService.studentInformation(id);
    }

    @PostMapping(value = "/")
    public List<GradebookCollegeStudent> createStudent(@RequestBody CollegueStudentDto student) {

        studentService.createStudent(student.getFirstname(), student.getLastname(), student.getEmailAddress());
        gradebook = studentService.getGradebook();
        return gradebook.getStudents();
    }

    @DeleteMapping("/student/{id}")
    public List<GradebookCollegeStudent> deleteStudent(@PathVariable int id) {

        if (!studentService.checkIfStudentIsNull(id)) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        studentService.deleteStudent(id);
        gradebook = studentService.getGradebook();
        return gradebook.getStudents();
    }

    @PostMapping(value = "/grades")
    public GradebookCollegeStudent createGrade(@RequestParam("grade") double grade,
            @RequestParam("gradeType") String gradeType,
            @RequestParam("studentId") int studentId) {

        if (!studentService.checkIfStudentIsNull(studentId)) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        boolean success = studentService.createGrade(grade, studentId, gradeType);

        if (!success) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        GradebookCollegeStudent studentEntity = studentService.studentInformation(studentId);

        if (studentEntity == null) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        return studentEntity;
    }

    @DeleteMapping("/grades/{id}/{gradeType}")
    public GradebookCollegeStudent deleteGrade(@PathVariable int id, @PathVariable String gradeType) {

        int studentId = studentService.deleteGrade(id, gradeType);

        if (studentId == 0) {
            throw new StudentOrGradeNotFoundException(STUDENT_OR_GRADE_WAS_NOT_FOUND);
        }

        return studentService.studentInformation(studentId);
    }

    @ExceptionHandler
    public ResponseEntity<StudentOrGradeErrorResponse> handleException(StudentOrGradeNotFoundException exc) {

        StudentOrGradeErrorResponse error = new StudentOrGradeErrorResponse();

        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(exc.getMessage());
        error.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<StudentOrGradeErrorResponse> handleException(Exception exc) {

        StudentOrGradeErrorResponse error = new StudentOrGradeErrorResponse();

        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(exc.getMessage());
        error.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}