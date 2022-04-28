package com.week3team2.lectureservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LectureRepositoryTest {

    @Autowired
    LectureRepository repository;


    @Test
    void readsAllEntitiesCorrectly() {
        StepVerifier.create(repository.findAll())
                .assertNext(o -> assertEquals(o.getLectureName(), "testName"))
                .assertNext(o -> assertEquals(o.getLectureName(), "testName"))
                .assertNext(o -> assertEquals(o.getLectureName(), "testName"))
                .verifyComplete();
    }

}