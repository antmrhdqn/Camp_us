# CAMP US

### 팀명: COMM1T

### 팀원

- 🐹 팀원 : **김동환**
[<img src="https://img.shields.io/badge/Github-Link-181717?logo=Github">](https://github.com/antmrhdqn)

- 🎀 팀원 : **박찬호**
[<img src="https://img.shields.io/badge/Github-Link-181717?logo=Github">](https://github.com/Yuharee)

- 🦊 팀원 : **백동현**
[<img src="https://img.shields.io/badge/Github-Link-181717?logo=Github">](https://github.com/dongh810)

- 🧸 팀원 : **손세림**
[<img src="https://img.shields.io/badge/Github-Link-181717?logo=Github">](https://github.com/bucky1005)


---

# 프로젝트 개요

바쁜 현대사회에서 힐링을 하기위해 캠핑을 찾는 사람들이 많아지는 요즘 캠핑장을 하나씩 찾아가며 예약하기 어려울 수 있겠다 생각하여 
고캠핑 API를 사용하여 전국의 캠핑장 정보를 받아와 캠핑장 정보를 조회하고 예약을 할 수 있는 통합 캠핑장 예약서비스를 구현하였습니다.

---

# Skill Set
BackEnd - Java 21, SpringBoot 3.3.1, Spring Security 6.3.1, AWS Cognito 2.17.81
DB/Cache - MySQL 8.4, Redis, AWS DynamoDB
Infra - AWS LoadBalancer, AWS EC2, AWS Lambda
Test - Locust

---

# 프로젝트

## Summary
- Spring Security와 AWS Cognito를 사용한 유저, 관리자 회원 체계 분리
- MySQL 과 AWS DynamoDB를 사용한 하이브리드 데이터베이스 구축
- Redis를 사용한 동시성 처리
- Caffein Cache를 이용한 캐싱 처리
- Locust를 활용한 부하테스트
- Spring Scheduler와 AWS Lambda를 이용한 캠핑장 데이터 최신화


## WBS
GitHub Projects를 활용하여 스프린트 계획 수립 및 전체 프로젝트 일정을 체계적으로 관리하였습니다.  
**프로젝트 일정** : 2024.07.04 ~ 2024.08.21 

![ANY_0822190051](https://github.com/user-attachments/assets/3ec386ee-4864-4e84-92ca-7bba6fc07984)
[프로젝트 보러 가기](https://github.com/orgs/1COMM1T/projects/2/views/4)

## DB 모델링

![upscaled_image](https://github.com/user-attachments/assets/35838876-2387-499c-a295-f996d87a1353)

노란색은 MySQL 테이블의 테이블이고, 보라색은 MySQL에 작성된 서머리 테이블입니다.
초록색은 DynamoDB 테이블입니다.

## 시스템 아키텍쳐
![image](https://github.com/user-attachments/assets/a1bc41f8-a38a-484c-bd25-d56ba5c99f33)

## 예약 시퀀스 아키텍쳐

## API 명세서
![image](https://github.com/user-attachments/assets/29656bfc-81dd-430b-bdac-f6cb875aa8f8)


## 고도화 방안

- 성능 테스트의 튜닝 포인트를 기반으로 next plan 작성
- 캐시를 도입하여 조회 성능을 향상
- AI 챗봇 도입


