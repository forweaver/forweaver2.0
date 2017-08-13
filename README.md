학생들을 위한 소셜 코딩 포위버!
=======

포위버는 학생들을 위한 소셜 코딩을 목표로 Spring MVC + MongoDB를 활용하여 만든 깃 저장소 관리 사이트입니다. 

해당 프로젝트는 학생들에게 어려울 수 있는 버젼 관리 시스템이나 개발자용 커뮤니티(위키 & 이슈트래커 등)를
학생 시각에서 다룰 수 있도록 도와주려는 목적으로 만들어졌습니다.
또한 GIT 저장소를 보다 손쉽게 수업에 활용하게 하는 것을 목표로 하고 있습니다.

실행 데모 및 설명은 아래 내용과 스크린샷을 참조해주세요.

배포 라이센스는 MIT License하에서 배포되며 그 외 저희가 사용한 라이브러리는 NOTICE.txt를 참조하시면 됩니다.

## 실행방법

1. 자바 개발환경을 구축합니다.
	1. JDK 설치 - [사이트로 가기](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
	2. JDK 환경변수 설정 - [참고 블로그](http://prolite.tistory.com/175)

2. git clone 명령어로 forweaver를 로컬 저장소에 내려받습니다.
> $ git clone https://github.com/goesang/forweaver.git

3. 몽고디비를 설치하고 실행합니다.
	- 세부사항 설정 파일 위치: `/src/main/resources/spring/applicationContext.xml`

4. 리눅스 환경이라면 `/src/main/webapp/WEB-INF/web.xml`에서 git 저장소 `/home/git/`를 설정합니다.


5. war파일 생성 후 실행하거나 메이븐에서 다음 명령어를 실행합니다.
> mvn tomcat7:run


6. 셋팅을 마치면 아래 주소로 접속해 테스트합니다.
> http://127.0.0.1:8080


## 주요 스크린샷
### 로그인 화면
![login_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/login_0.5.17.png)

### 프로젝트 관리 화면
![project_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/project_0.5.17.png)

### 태그 게시판 화면 
![tagcommunity1_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/tagcommunity1_0.5.17.png)
***************************************
![tagcommunity2_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/tagcommunity2_0.5.17.png)

### 숙제 저장소 화면
![lecture_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/lecture_0.5.17.png)
***************************************
![lecture-branch_0.5.17.png](https://raw.githubusercontent.com/goesang/forweaver/test/screenshots/lecture-branch_0.5.17.png)