# 📌 Elasticsearch 클러스터링 적용기

## 목차
* []()

## 개요
현재 Elasticsearch는 단일 노드로만 구동되고 있다. 그러나 실제 운영 환경에서는 노드가 종료될 경우 서비스 운영이 불가능해진다. 단일 노드로 운영할 경우 장애 발생 시 데이터 접근이 차단되는 단점이 있다. 따라서, 고가용성과 확장성을 확보하기 위해 Elasticsearch 클러스터링을 적용하는 것이 필수적이다.  이 글에서는 단일 노드에서 여러 개의 노드를 클러스터 환경으로 구성하는 과정에서 겪은 문제점과 해결 방안을 공유한다.


## 클러스터링이 필요한 이유
위에서도 설명했지만 단일 노드로 구성된 서비스는 소위 말해서 해당 노드가 뻗어버리면(?) 서비스가 중단된다. 그렇기 때문에 가용성 확보를 위해서라도 여러 개의 노드로 분산하는 것은 필수적이다. Elasticsearch는 분산 검색 엔진으로, 다수의 노드를 하나의 클러스터로 구성할 수 있다. 클러스터링을 적용하면 다음과 같은 이점을 얻을 수 있다:

1. 고가용성(High Availability): 한 노드가 다운되더라도 다른 노드가 데이터를 유지하므로 서비스 중단을 방지할 수 있다.
2. 수평 확장(Scalability): 데이터가 증가할수록 노드를 추가하여 처리 성능을 확장할 수 있다.
3. 부하 분산(Load Balancing): 검색 및 색인 작업을 여러 노드에 분산시켜 성능을 최적화할 수 있다.

## Elasticsearch 클러스터 구성
Elasticsearch 클러스터를 구성하기 위해 몇 가지 핵심 개념을 이해하고 설정해야 한다.

## 마스터 노드와 데이터 노드
Elasticsearch는 역할별로 노드를 구분하여 클러스터를 구성한다.

* 마스터 노드(Master Node): 클러스터 상태를 관리하고, 노드 추가 및 삭제를 조정하는 역할을 한다. 
* 데이터 노드(Data Node): 실제 데이터를 저장하고, 검색 및 색인 작업을 수행한다. 
* 코디네이팅 노드(Coordinating Node): 쿼리를 받아 적절한 데이터 노드에 요청을 분배하는 역할을 한다.

✅ 클러스터링 구성 예시

<img width="1200" alt="Image" src="https://github.com/user-attachments/assets/81fba3dc-b225-4809-9325-c58a36efacd1"/>

운영 환경에서는 마스터 노드를 최소 3개 이상 두어 장애에 대비하는 것이 일반적이다. 

🎯 TMI) Elasticsearch 7.x 버전에서는 클러스터에 단일 노드만 남아도 해당 노드가 마스터 노드가 될 수 있었다. 하지만 8.x 버전부터는 최소 2개 이상의 노드가 필요하며, 단일 노드로는 마스터 노드 선정이 불가능하도록 설계. 이는 단일 노드 장애로 인한 클러스터 불안정을 방지하기 위한 변경 사항이라고 함. 그러니 노드가 한 개만 남아버리면 마스터 노드 투표가 안 돼서 데이터 통신이 중지된다. 

## 샤딩과 레플리카
Elasticsearch는 데이터를 샤드(Shard) 단위로 나누어 저장하며, 각 샤드는 특정 노드에 할당된다.

* 기본 샤드(Primary Shard): 데이터를 저장하는 기본 단위 
* 레플리카 샤드(Replica Shard): 기본 샤드의 복제본으로, 데이터 손실을 방지하고 부하를 분산하는 역할

샤드 개수는 인덱스 생성 시 설정할 수 있으며, 기본적으로 5개의 기본 샤드와 1개의 레플리카 샤드가 설정된다.

샤딩과 레플리카 예시 (총 3개의 노드에 각각 하나의 샤드씩 저장하고, 추가적으로 1개의 레플리카가 3개 샤드로 나뉘어 보관되고 있다)

<img width="1200" alt="Image" src="https://github.com/user-attachments/assets/81fba3dc-b225-4809-9325-c58a36efacd1" />


## 로컬 환경에서 테스트
자, 이제 본격적으로 테스트를 해보자. 먼저 로컬 환경에서 테스트다. 로컬에서는 노드 컨테이너 3개로 클러스터링 환경을 구성했다.

먼저 Elasticsearch 컨테이너 간 통신을 위해 따로 별도의 네트워크를 구성했다.
```shell
docker network create elastic-network
```

그 후 노드별 컨테이너 세 개를 띄워야 한다. 다음은 Elasticsearch 컨테이너를 생성할 때 사용한 명령어다.
```shell

docker run -d --name es01 --network elastic-network -p 9200:9200 -p 9300:9300 \
  -e "node.name=es01" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=es02,es03" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v esdata01:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2


docker run -d --name es02 --network elastic-network -p 9201:9200 -p 9301:9300 \
  -e "node.name=es02" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=es01,es03" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v esdata02:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2
  
docker run -d --name es03 --network elastic-network -p 9202:9200 -p 9302:9300 \
  -e "node.name=es03" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=es01,es02" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v esdata03:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2
```
명령어가 많아서 복잡해 보이지만 사실 별거 없다. 차례대로 알아보자.
* `--network elastic-network`: 컨테이너 네트워크를 elastic-network로 연결 
* `discovery.seed_hosts=es01,es03`: Elasticsearch가 클러스터를 형성하기 위해 다른 노드를 발견(Discovery)할 때 참조하는 초기 호스트 목록(컨테이너 이름, DNS 이름 또는 IP:포트 형태 가능)
  * 동작:
    * (컨테이너가 es03일 경우) 노드가 시작될 떄, es01과 es02에 연결을 시도하여 클러스터에 참여
    * es01과 es02는 `--network elastic-network`를 통해 동일 네트워크에 있어야 함
    * 실제 es01:9300, es02:9300 (Transport 포트)로 통신 시도
    * 클러스터 환경에서는 필수임
* `cluster.initial_master_nodes=es01,es02,es03`: 클러스터가 처음 초기화될 때 마스터 노드를 선출하기 위한 초기 후보 노드 목록
  * 동작:
    * 클러스터가 처음 형성될 떄, 이 목록에 있는 노드 중 하나를 마스터로 선출
    * (컨테이너가 es03일 경우) es03이 시작ㅈ되면서, es01, es02와 함께 클러스터 구성
    * `minimum_master_nodes` 설정(기본값은 과반수)과 함꼐 마스터 선출 안정성 보장. 3개 노드 중 2개 이상이 가동 중이어야 마스터 선출 가능
* `bootstrap.memory_lock=true`: 메모리 잠금 활성화.
  * Elasticsearch가 시작 시 JVM이 사용하는 메모리를 운영 체제의 스왑(Swap) 공간으로 교체(Swap Out)되지 않도록 고정(Mlock)하는 설정
  * true 설정 시 메모리 잠금 활성화
  * 목적:
    * Elasticsearch는 검색 및 색인 작업에서 메모리를 많이 사용하며, 성능이 중요한 애플리케이션이기에 스왑이 발생하면 디스크 I/O가 증가해 성능 저하
    * 메모리 잠금을 통해 JVM 힙 메모리가 물리적 RAM에 고정되어 스왑을 방지, 성능 안정화
* `ES_JAVA_OPTS=-Xms256m -Xmx256m`: JVM 힙 메모리 설정. -> 256MB
* `xpack.security.enabled=false`: 보안 기능 비활성화 (실제 운영 환경에서는 활성화 하는 게 좋음)
* `--ulimit memlock=-1:-1`: 리눅스에서 프로세스가 사용할 수 있는 자원의 한계를 설정하는 옵션. memlock은 프로세스가 잠글 수 있는 메모리 양의 제한을 지정. 
  * bootstrap.memory_lock=true가 동작하려면, 컨테이너가 충분한 메모리를 잠글 수 있는 권한이 필요. 
  * memlock 제한을 제거해 컨테이너가 필요한 만큼 메모리를 잠글 수 있도록 보장.

현재 port의 9200과 9300 두 개를 열어두는 것을 볼 수 있는데 이는 서로 용도가 다르기 때문이다. 9200(9201, 9200)은 클라이언트와 Http 통신을 위해 사용되는 포트고, 9300은 컨테이너 간 통신을 위해 사용되는 포트다.

이로써 모든 컨테이너 설정을 마쳤다.

<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/b2413dfb-11d2-4d76-9671-0e2f1c082856" />

```shell
curl -X GET "http://localhost:9200/_cluster/health?pretty"
{
  "cluster_name" : "es-cluster",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 3,
  "number_of_data_nodes" : 3,
  "active_primary_shards" : 33,
  "active_shards" : 66,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0,
  "unassigned_primary_shards" : 0,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 100.0
}
```
클러스터링 환경이 정상적으로 구축됐으면 위와 같이 status가 green으로 표시된다. `number_of_nods` 역시 3개로 설정이 된 것을 알 수 있다.


<img width="900" alt="Image" src="https://github.com/user-attachments/assets/5a8f97ed-dd1b-4e4f-b716-8ae17349381c" />

노드의 경우 현재 es03이 마스터 노드 역할을 하고 있다. 그러면 현 상태에서 본격적으로 가용성 테스트를 해보자.

### 로컬 환경에서 고가용성 테스트
테스트를 위해 `matching_post`라는 인덱스를 생성했다. 가용성 테스트는 노드 중 일부가 종료되는 경우에도 데이터 READ/WRITE가 가능한지를 확인해야 하기 때문에 샤드를 나눠야 한다. 다음은 matching_post 인덱스의 샤드 구성 정보다.
```text
PUT /matching_post
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    .
    .
    .
```

하나의 인덱스를 총 3개의 샤드, 레플리카는 1개로 구성했다. 즉, 현재 3개의 노드에 각각 샤드1개와 레플리카 샤드가 저장되어 있다.

<img width="1200" alt="Image" src="https://github.com/user-attachments/assets/b3dfa035-3739-4e77-87a5-54e06dab2940" />



## 실제 운영 환경(?)에서 테스트
