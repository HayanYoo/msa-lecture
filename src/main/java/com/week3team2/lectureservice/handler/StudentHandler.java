package com.week3team2.lectureservice.handler;

import com.week3team2.lectureservice.entity.LectureContent;
import com.week3team2.lectureservice.entity.LectureInfo;
import com.week3team2.lectureservice.repository.LectureInfoRepository;
import com.week3team2.lectureservice.service.Producer;
import com.week3team2.lectureservice.service.lecture.LectureService;
import lombok.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class StudentHandler {

    private final LectureService lectureService;
    private final LectureInfoRepository lectureInfoRepository;

    private final Producer producer;

    // 수강한 강의에 별점 남기기
    public Mono<ServerResponse> setLectureScore(ServerRequest request) {
        Mono<LectureInfo> lectureMono = request.bodyToMono(LectureInfo.class)
                .flatMap(lectureService::setLectureScore)
                .log()
                ;

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(lectureMono, LectureInfo.class)
                .onErrorResume(error -> ServerResponse.badRequest().build())
                ;
    }


    // 강의를 선택해서 수강 신청 (수강정보 : LectureInfo 생성)
    public Mono<ServerResponse> enrolment(ServerRequest request) {
        kafkaProducerMessage kafkaProducerMessage = new kafkaProducerMessage();
        Mono<LectureInfo> lectureInfoMono = request.bodyToMono(LectureInfo.class)
                .flatMap( lectureInfo -> {
                    kafkaProducerMessage.setMemberId(lectureInfo.getMemberId());
                    kafkaProducerMessage.setMessage("강의가 정상적으로 수강신청 되었습니다.");
                    this.producer.sendMessage(kafkaProducerMessage.toString());
                    return lectureService.enrolment(lectureInfo);
                });





        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(lectureInfoMono, LectureInfo.class)
                .onErrorResume(error -> ServerResponse.badRequest().build())
                ;
    }

    @ToString
    @NoArgsConstructor
    @Setter
    class kafkaProducerMessage {
        int memberId;
        String message;
    }


    // 수강 신청한 강의 컨텐츠 열람
    public Mono<ServerResponse> getLectureContents(ServerRequest request) {
        Mono<LectureContent> LectureContentMono = request.bodyToMono(LectureInfo.class)
                .flatMap(lectureService::getLectureContents)
                .log()
                ;

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(LectureContentMono, LectureContent.class)
                .onErrorResume(error -> ServerResponse.badRequest().build())
                ;
    }

    // 수강한 강의의 목록 조회
    public Mono<ServerResponse> getLectureOnTeacher(ServerRequest request) {
        Integer memberId = Integer.valueOf(request.pathVariable("memberId"));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return Mono.just(lectureInfoRepository.findAllByLectureInfo(memberId))
                .flatMap(lectureInfo -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(lectureInfo, LectureInfo.class)
                        .switchIfEmpty(notFound));
    }




}
