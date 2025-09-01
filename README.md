# SPRING ADVANCED

# 1. 프로젝트 개요

- 기존 코드의 문제점을 분석 및 개선하는 프로세스와, 테스트 코드를 작성하는 방법을 익히는 것에 초점을 맞춘 과제입니다.
- Spring Boot와 Spring Web MVC 그리고 Spring Data JPA를 이용하여 개발했습니다.
- 개발 일정: 2025년 8월 26일 ~ 2025년 9월 1일
- 개발 인원: 1인

# 2. 주요 기능 밎 주안점

### 2.1. 주요 기능

- **Lv 0. 프로젝트 세팅 - 에러 분석**
    - 애플리케이션 실행 시 발생하는 에러의 원인을 분석하고 해결하여 정상적으로 실행되도록 조치합니다.
- **Lv 1. ArgumentResolver**
    - 동작하지 않는 `AuthUserArgumentResolver`의 로직을 수정하여 정상적으로 기능하도록 구현합니다.
- **Lv 2. 코드 개선**
    - **Early Return 적용:** 불필요한 `passwordEncoder`의 동작을 방지하도록 로직 순서를 변경합니다.
    - **불필요한 if-else 제거:** 가독성 및 유지보수를 위해 복잡한 if-else 구조를 개선합니다.
    - **Validation 적용:** 서비스 레이어에 위치한 유효성 검증 로직을 요청 DTO에서 처리하도록 `@Valid`를 활용하여 개선합니다.
- **Lv 3. N+1 문제 해결**
    - JPQL `fetch join`으로 구현된 코드를 `@EntityGraph`를 사용하여 동일하게 동작하도록 수정하여 N+1 문제를 해결합니다.
- **Lv 4. 테스트코드 연습**
    - 잘못 작성되었거나, 서비스 로직 변경으로 인해 실패하는 테스트 코드를 의도에 맞게 성공하도록 수정합니다.
- **Lv 5.API 로깅**
    - `Interceptor` 와 `AOP`를 활용하여 어드민 권한이 필요한 특정 API에 대한 접근 로그를 기록합니다.
- **Lv 6. 문제 정의 및 해결**
    - 코드의 개선점을 스스로 정의하고, 해결 방안을 수립하여 적용한 뒤 문서를 통해 과정을 기록합니다.
- **Lv 7. 테스트 커버리지**
    - 테스트 코드를 작성합니다. 그리고 테스트 커버리지를 측정하고, 리포트에 이미지를 첨부합니다.

### 2.2. 주안점

- **Spring 프레임워크의 이해:** `HttpMessageConverter`, `ArgumentResolver` 와 `Filter`, `Interceptor`, `AOP` 등의 동작 원리를 학습하고 적용하는
  데 중점을 두었습니다.
- **JPA 학습:** N+1 문제의 원인을 이해하고, `fetch join` 및 `@EntityGraph`를 활용하여 성능을 최적화하는 방법을 익히기 위해 노력했습니다.
- **코드 개선:** Early Return, Validation 적용 등 과제에서 제시하는 코드 작성 방법을 바탕으로 리팩토링을 진행했습니다.
- **테스트 코드 작성:** JUnit과 Given-When-Then 패턴을 적용하여 단위 테스트를 작성하는 방법을 익히기 위해 노력했습니다.

# 3. 개발 환경

- **언어:** Java 17
- **프레임워크:** Spring Boot 3.2.x
- **ORM:** Spring Data JPA
- **빌드 도구:** Gradle
- **데이터베이스:** MySQL 8.x
- **테스트:** JUnit 5, Mockito
- **기타:** Lombok, [BCrypt Password Hashing Function](https://mvnrepository.com/artifact/at.favre.lib/bcrypt)

# 4. 실행 방법

- `datasource, jwt.secret.key`에 대한 `application-{}.yml`파일 작성
- 작성한 `application-{}.yml` 을 프로파일 활성화하여 실행

# 5. 과제 해결 과정 및 회고

[https://seonrizee.github.io/blog/2025-09-01-ch4-refactor](https://seonrizee.github.io/blog/2025-08-14-ch3-adv-schedule-api/)

| **과제**                                   | **해결 과정**                                                              |
|------------------------------------------|------------------------------------------------------------------------|
| **Lv 0. 프로젝트 세팅 - 에러 분석**                | application.yml에 jwt.secret.key, datasource 설정                         |
| **Lv 1. ArgumentResolver**               | WebMvcConfigurer를 구현하는 WebConfig 생성 후 addArgumentResolvers메소드를 이용하여 등록 |
| **Lv 2. 코드 개선**                          | 회고 참조                                                                  |
| **Lv 3. N+1 문제**                         | 회고 참조                                                                  |
| **Lv 4. 테스트코드 연습**                       | 회고 참조                                                                  |
| **Lv 5. API 로깅**                         | Interceptor와 aop 모두 사용                                                 |
| **Lv 6. 위 제시된 기능 이외 ‘내’가 정의한 문제와 해결 과정** | 회고 참조                                                                  |
| **Lv 7. 테스트 커버리지**                       | 회고 참조                                                                  |

# 6. 브랜치 전략 및 커밋 컨벤션

**6.1 브랜치 전략**

- main 브랜치 commit, 별도의 PR 사용하지 않음

**6.2 커밋 컨벤션**

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 코드 리펙토링
docs: 문서 추가 및 수정
chore: code와 관련 없는 설정, 변경
test: 테스트 코드 관련 작성
```

- `타입(레벨): 제목`형식으로 작성
- 일부 메시지는 상세 설명 작성
- [**Conventional Commits 참고**](https://www.conventionalcommits.org/ko/v1.0.0/)