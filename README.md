# 💬 ChatPlay

Java Swing 기반의 실시간 멀티 클라이언트 채팅 애플리케이션입니다.  
채팅, 게임, 날씨, 뉴스 등 다양한 기능을 제공합니다.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Swing](https://img.shields.io/badge/Swing-Desktop-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ 주요 기능

### 💬 채팅 기능
- **실시간 채팅**: 다중 사용자 간 실시간 메시지 송수신
- **멀티 윈도우**: 여러 채팅방을 독립 창으로 동시 관리
- **이미지 전송**: 채팅방에서 이미지 파일 공유
- **이모지**: 다양한 이모지로 감정 표현
- **채팅봇**: 날씨, 뉴스 정보 제공 및 멀티플레이 게임

### 🎮 게임 기능
- **캐치마인드**: 그림 그리기 기반 추측 게임
- **요트다이스**: 주사위 기반 보드 게임
- **실시간 게임**: 여러 사용자가 동시에 참여 가능
- **게임 상태 관리**: 참여자 관리 및 게임 진행 상태 추적

### 📰 부가 기능
- **날씨 정보**: 오늘의 날씨 및 7일 예보
- **뉴스**: 카테고리별 최신 뉴스 제공
- **프로필 관리**: 사용자 프로필 이미지 및 상태 메시지 설정

---

## 🛠️ 기술 스택

- **언어**: Java
- **UI 프레임워크**: Java Swing
- **네트워크**: Socket 통신 (TCP/IP)
- **데이터 형식**: JSON (org.json)
- **아키텍처**: 클라이언트-서버 모델

---

## 📁 프로젝트 구조

```
ChatPlay/
├── src/
│   ├── chatPlay/          # 채팅 애플리케이션 핵심
│   │   ├── ChatClientMain.java    # 클라이언트 메인
│   │   ├── ChatServer.java        # 서버
│   │   ├── ChatPanel.java         # 채팅방 목록
│   │   ├── ChatRoomPanel.java     # 채팅방 UI
│   │   └── ...
│   ├── game/              # 게임 관리 시스템
│   │   ├── GameManager.java       # 게임 관리자
│   │   ├── GameInstance.java      # 게임 인터페이스
│   │   └── ...
│   ├── catchmind/         # 캐치마인드 게임
│   ├── yacht/             # 요트다이스 게임
│   ├── weather/            # 날씨 기능
│   └── news/              # 뉴스 기능
├── lib/                    # 외부 라이브러리
│   └── json-20250517.jar
└── README.md
```

---

## 🚀 실행 방법

### 1. 사전 요구사항
- Java JDK 8 이상
- IDE (IntelliJ IDEA, Eclipse 등) 또는 명령줄

### 2. 서버 실행
```bash
# 서버 클래스 실행
java -cp "bin;lib/json-20250517.jar" chatPlay.ChatServer
```

서버가 시작되면:
- 기본 포트: `30000`
- 서버 로그 창에서 연결 상태 확인

### 3. 클라이언트 실행
```bash
# 클라이언트 클래스 실행
java -cp "bin;lib/json-20250517.jar" chatPlay.ChatClientMain
```

클라이언트 실행 후:
1. 사용자명 입력
2. 프로필 이미지 선택 (선택사항)
3. "회원가입" 버튼 클릭하여 서버 연결

### 4. 사용 방법
- **채팅방 생성**: 채팅 목록에서 `+` 버튼 클릭
- **게임 시작**: 채팅방에서 챗봇에게 `@채팅봇` 입력 후 게임 선택
- **날씨/뉴스**: 챗봇 메뉴에서 원하는 기능 선택

---

## 🎯 주요 클래스 설명

### 서버 측
- **`ChatServer`**: 클라이언트 연결 관리, 채팅방 관리, 게임 관리
- **`UserService`**: 개별 클라이언트와의 통신 처리
- **`Room`**: 채팅방 데이터 및 참여자 관리

### 클라이언트 측
- **`ChatClientMain`**: 메인 프레임, 서버 통신, UI 관리
- **`ChatRoomPanel`**: 채팅방 UI, 메시지 표시, 입력 처리
- **`GameMessageHandler`**: 게임 관련 메시지 처리

### 게임 시스템
- **`GameManager`**: 게임 인스턴스 생성 및 관리
- **`GameInstance`**: 게임 공통 인터페이스
- **`CatchMindGame`**: 캐치마인드 게임 로직
- **`YachtGame`**: 요트다이스 게임 로직

---

## 📝 프로토콜

### 주요 명령어
- `/login [username]` - 로그인
- `/makeroom [roomName] [users...]` - 채팅방 생성
- `/roommsg [roomId] [message]` - 메시지 전송
- `/catchmind_join [roomId]` - 캐치마인드 참여
- `/yacht_join [roomId]` - 요트다이스 참여
- `/upload_image [roomId] [fileName]` - 이미지 업로드

---

## 🎨 UI 특징

- **그라데이션 배경**: 부드러운 색상 전환
- **커스텀 스크롤바**: 세련된 UI 디자인
- **반응형 레이아웃**: 다양한 화면 크기 지원
- **이미지 버블**: 채팅 메시지를 버블 형태로 표시

---

## 📦 의존성

- `json-20250517.jar` - JSON 데이터 처리

---

## 🔧 개발 환경 설정

1. 프로젝트 클론
```bash
git clone [repository-url]
cd ChatPlay
```

2. IDE에서 프로젝트 열기
   - IntelliJ IDEA: `File > Open > 프로젝트 폴더 선택`
   - Eclipse: `File > Import > Existing Projects into Workspace`

3. 라이브러리 경로 설정
   - `lib/json-20250517.jar`를 클래스패스에 추가

4. 빌드 및 실행
   - IDE에서 `ChatServer` 먼저 실행
   - 이후 `ChatClientMain` 실행
