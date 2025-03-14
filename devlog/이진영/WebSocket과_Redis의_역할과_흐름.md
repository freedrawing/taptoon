# 프로젝트에서 WebSocket과 Redis(Pub/Sub)의 역할과 흐름

## 1. WebSocket의 역할

WebSocket은 실시간 채팅의 주요 통신 수단으로, 클라이언트와 서버 간 양방향 연결을 제공합니다:

- **연결 설정**: 사용자가 채팅방에 입장하면 WebSocket을 통해 서버와 실시간 통신 채널을 엽니다.
- **메시지 전송**: 사용자가 텍스트 메시지를 입력하면 클라이언트가 WebSocket으로 서버에 직접 보냅니다.
- **메시지 수신**: 서버가 다른 사용자의 메시지를 처리한 후, WebSocket을 통해 클라이언트로 실시간 전달하여 화면에 표시합니다.

## 2. Redis(Pub/Sub)의 역할

Redis는 메시지 배포와 상태 관리를 위한 브로커 및 저장소로 기능합니다:

- **Pub/Sub 브로커**: 각 채팅방마다 고유한 채널(예: `chatroom-123`)을 만들어 메시지를 모든 연결된 사용자에게 배포합니다.
- **상태 저장**: 사용자가 마지막으로 읽은 메시지(`lastReadMessage`)를 저장하여 읽지 않은 메시지 수를 계산합니다.

## 3. 전체 흐름

1. **사용자 접속**
   - 클라이언트가 WebSocket을 통해 서버에 연결 요청을 보냅니다. 이 과정에서 사용자 인증 정보(예: JWT 토큰)를 함께 전달합니다.
   - 서버는 사용자를 해당 채팅방의 활성 세션 목록에 추가하고, Redis에서 이전 메시지와 읽음 상태를 가져와 초기 화면을 준비합니다.

2. **메시지 전송**
   - 사용자가 메시지 입력창에 텍스트를 쓰거나 이미지를 업로드한 후 "보내기"를 누릅니다.
   - 클라이언트는 WebSocket 연결을 통해 메시지를 서버로 전송합니다.
   - 예:
     ```json
     { "senderId": 1, "message": "안녕!", "type": "TEXT" }
     ```

3. **서버 처리**
   - 서버는 WebSocket으로 받은 메시지를 처리합니다. 텍스트는 데이터베이스에 저장하고, 이미지는 업로드 상태를 업데이트합니다.
   - 처리된 메시지를 JSON 형식으로 변환하여 Redis의 채팅방별 채널(예: `chatroom-123`)에 발행(Publish)합니다.

4. **메시지 배포**
   - Redis는 구독 중인 서버에 발행된 메시지를 전달합니다.
   - 서버는 WebSocket을 통해 해당 채팅방에 연결된 모든 사용자에게 메시지를 브로드캐스트합니다.

5. **읽음 처리**
   - 메시지를 보낸 사용자는 WebSocket 연결을 통해 즉시 읽음 처리됩니다.
   - 서버는 Redis에 사용자의 마지막 읽은 메시지 ID(`lastReadMessage`)를 업데이트하여 읽음 상태를 동기화합니다.

6. **클라이언트 화면 업데이트**
   - 클라이언트는 WebSocket으로 수신한 메시지를 화면에 추가합니다.
   - 삭제된 메시지는 제외하고, 중복 메시지는 무시하여 사용자 경험을 최적화합니다.

# WebSocket & Redis 기반 실시간 채팅 흐름

아래는 WebSocket과 Redis Pub/Sub을 활용한 실시간 채팅의 흐름을 나타낸 다이어그램입니다.


```mermaid
sequenceDiagram
    participant C1 as 사용자 1 (클라이언트)
    participant C2 as 사용자 2 (클라이언트)
    participant S as 서버
    participant R as Redis

    C1->>S: WebSocket 연결 (chatroom-123)
    C2->>S: WebSocket 연결 (chatroom-123)
    S->>R: 구독 요청 (chatroom-123)

    C1->>S: WebSocket 메시지 전송 ("안녕!", type: TEXT)
    S->>R: 메시지 발행 (chatroom-123, "안녕!")
    R-->>S: 발행된 메시지 전달
    S-->>C1: 메시지 브로드캐스트 ("안녕!")
    S-->>C2: 메시지 브로드캐스트 ("안녕!")

    S->>R: 사용자 1의 읽음 상태 업데이트 (lastReadMessage)
    C2->>S: 읽음 확인 요청 (WebSocket)
    S->>R: 사용자 2의 읽음 상태 업데이트 (lastReadMessage)