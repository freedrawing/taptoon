# 📌 Elasticsearch 클러스터링 적용기 🚀

## 목차

* [1. 개요](#-1-개요)
* [2. 클러스터링이 필요한 이유](#-2-클러스터링이-필요한-이유)
* [3. Elasticsearch 클러스터 구성](#-3-elasticsearch-클러스터-구성)
  * [3.1 마스터 노드와 데이터 노드](#-31-마스터-노드와-데이터-노드)
  * [3.2 샤딩과 레플리카](#-32-샤딩과-레플리카)
* [4. 로컬 환경에서 테스트](#-41-로컬-환경에서-고가용성-테스트)
  * [4.1 로컬 환경에서 고가용성 테스트](#-41-로컬-환경에서-고가용성-테스트)
* [5. 실제 운영 환경에서 테스트](#-5-실제-운영-환경에서-테스트)
  * [5.1 실제 운영 환경에서 고가용성 테스트](#-51-실제-운영-환경에서-고가용성-테스트)
*[6. 결론](#-6-결론)

## 🧐 1. 개요

현재 Elasticsearch는 단일 노드로만 구동되고 있다. 그러나 실제 운영 환경에서는 노드가 종료될 경우 서비스 운영이 불가능해진다. 단일 노드로 운영할 경우 장애 발생 시 데이터 접근이 차단되는 단점이 있다. 따라서, 고가용성과 확장성을 확보하기 위해 Elasticsearch 클러스터링을 적용하는 것이 필수적이다.  이 글에서는 단일 노드에서 여러 개의 노드를 클러스터 환경으로 구성하는 과정에서 겪은 문제점과 해결 방안을 공유한다.

<br>

## 💡 2. 클러스터링이 필요한 이유

위에서도 설명했지만 단일 노드로 구성된 서비스는 소위 말해서 해당 노드가 뻗어버리면(?) 서비스가 중단된다. 그렇기 때문에 가용성 확보를 위해서라도 여러 개의 노드로 분산하는 것은 필수적이다. Elasticsearch는 분산 검색 엔진으로, 다수의 노드를 하나의 클러스터로 구성할 수 있다. 클러스터링을 적용하면 다음과 같은 이점을 얻을 수 있다:

1. 고가용성(High Availability): 한 노드가 다운되더라도 다른 노드가 데이터를 유지하므로 서비스 중단을 방지할 수 있다.
2. 수평 확장(Scalability): 데이터가 증가할수록 노드를 추가하여 처리 성능을 확장할 수 있다.
3. 부하 분산(Load Balancing): 검색 및 색인 작업을 여러 노드에 분산시켜 성능을 최적화할 수 있다.

<br>

## 🏗️ 3. Elasticsearch 클러스터 구성

Elasticsearch 클러스터를 구성하기 위해 몇 가지 핵심 개념을 이해하고 설정해야 한다.

<br>

### 📌 3.1 마스터 노드와 데이터 노드

Elasticsearch는 역할별로 노드를 구분하여 클러스터를 구성한다.

* 🔹 마스터 노드(Master Node): 클러스터 상태를 관리하고, 노드 추가 및 삭제를 조정하는 역할을 한다.
* 🔹 데이터 노드(Data Node): 실제 데이터를 저장하고, 검색 및 색인 작업을 수행한다.
* 🔹 코디네이팅 노드(Coordinating Node): 쿼리를 받아 적절한 데이터 노드에 요청을 분배하는 역할을 한다.

✅ 클러스터링 구성 예시

<img width="1182" alt="Image" src="https://github.com/user-attachments/assets/5583fa09-57ab-4d53-9224-c14cb0d46f20" />

운영 환경에서는 마스터 노드를 최소 3개 이상 두어 장애에 대비하는 것이 일반적이다.

🚀 TMI) Elasticsearch 7.x 버전에서는 클러스터에 단일 노드만 남아도 해당 노드가 마스터 노드가 될 수 있었다. 하지만 8.x 버전부터는 최소 2개 이상의 노드가 필요하며, 단일 노드로는 마스터 노드 선정이 불가능하도록 설계. 이는 단일 노드 장애로 인한 클러스터 불안정을 방지하기 위한 변경 사항이라고 함. 그러니 노드가 한 개만 남아버리면 마스터 노드 투표가 안 돼서 데이터 통신이 중지된다.

<br>

### 🗂️ 3.2 샤딩과 레플리카

Elasticsearch는 데이터를 샤드(Shard) 단위로 나누어 저장하며, 각 샤드는 특정 노드에 할당된다.

* 📍 기본 샤드(Primary Shard): 데이터를 저장하는 기본 단위
* 📍 레플리카 샤드(Replica Shard): 기본 샤드의 복제본으로, 데이터 손실을 방지하고 부하를 분산하는 역할

샤딩과 레플리카 예시 (총 3개의 노드에 각각 하나의 샤드씩 저장하고, 추가적으로 1개의 레플리카가 3개 샤드로 나뉘어 보관되고 있다)

<img width="1125" alt="Image" src="https://github.com/user-attachments/assets/65844c6f-1b7d-43d7-9578-912f0e3ca371" />

<br>

## 🛠️ 4. 로컬 환경에서 테스트

자, 이제 본격적으로 테스트를 해보자. 먼저 로컬 환경에서 테스트다. 로컬에서는 노드 컨테이너 3개로 클러스터링 환경을 구성했다.

먼저 Elasticsearch 컨테이너 간 통신을 위해 따로 별도의 네트워크를 구성했다.

1️⃣ Docker 네트워크 생성

```shell
docker network create elastic-network
```

2️⃣ Elasticsearch 컨테이너 실행

다음 명령어를 사용해 세 개의 Elasticsearch 노드 컨테이너를 생성한다.

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

📌 명령어가 많아서 복잡해 보이지만 사실 별거 없다. 차례대로 알아보자.

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

<img width="1433" alt="Image" src="https://github.com/user-attachments/assets/4e48bfff-2907-41ed-b223-3867902f1ec2" />

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

<img width="1396" alt="Image" src="https://github.com/user-attachments/assets/fe5dc7ca-5ddb-4ea6-810a-5576ee4cc275" />

노드의 경우 현재 es03이 마스터 노드 역할을 하고 있다. 그러면 현 상태에서 본격적으로 가용성 테스트를 해보자.

<br>

### ⚡️ 4.1 로컬 환경에서 고가용성 테스트

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

하나의 인덱스를 총 3개의 샤드, 레플리카는 1개로 구성했다. 즉, 현재 3개의 노드에 각각 샤드1개와 레플리카 샤드가 저장되어 있다. (인덱스를 여러 개의 샤드를 통해 보관하려면 처음에 인덱스를 생성할 때 설정을 반드시 해줘야 한다)

<img width="863" alt="Image" src="https://github.com/user-attachments/assets/34ae5098-9140-427f-8ab8-5f4189dfc1cd" />

그림으로 나태내면 다음과 같다.

```text
<matching_post 인덱스 샤드 분포>

┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│       es01          │ │       es02          │ │       es03          │
│  172.31.18.0.3      │ │  172.31.18.0.4      │ │  172.31.18.0.2      │
├──────────┬──────────┤ ├──────────┬──────────┤ ├──────────┬──────────┤
│ Shard 1  │ Shard 2  │ │ Shard 0  │ Shard 1  │ │ Shard 0  │ Shard 2  │
│ (r, 2)   │ (p, 1)   │ │ (r, 4)   │ (p, 2)   │ │ (p, 4)   │ (r, 1)   │
└──────────┴──────────┘ └──────────┴──────────┘ └──────────┴──────────┘

(p: Primary, r: Replica, 숫자: 문서 수)
```

자, 그럼 현재 Primary Shard로서 문서를 가장 많이 저장하고 있는 es03 노드를 중지시켜보자. 과연 기존 문서들은 안전할까? 그리고 노드가 두 개인 상태에서 문서를 추가하면 어떻게 동작할까?

<img width="1115" alt="Image" src="https://github.com/user-attachments/assets/5e13673b-9be0-472e-9409-78496500f13a" />

📌 UNASSIGNED Status

* Shard가 할당된 노드가 없거나, 노드 장애 / 중지로 인해 할당되지 않은 상태
* es03의 중지로 Replica가 할당되지 않음.

위 이미지에서 볼 수 있는 것처럼, 기존의 마스터 노드였던 es03이 중지되자, 새로운 마스터 노드로서 es02가 선출됐다. 또한 샤드 역시 es03이 갖고 있던 부분이 'UNASSIGNED'로 상태가 바뀐 것을 알 수 있다.
샤드 구성을 보면 Primary Shard의 경우 기존의 각 노드별 1개씩 저장됐던 것에서, es01에 p2, es02에 p0와 p1이 저장됐음을 알 수 있다. 그림으로 나타내면 다음과 같다.

```text
<matching_post 인덱스 샤드 구성>

┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│       es01          │ │       es02          │ │       es03 (중지)    │
│  172.31.18.0.3      │ │  172.31.18.0.4      │ │  172.31.18.0.2      │
├──────────┬──────────┤ ├──────────┬──────────┤ ├──────────┬──────────┤
│ Shard 1  │ Shard 2  │ │ Shard 0  │ Shard 1  │ │          │          │
│ (r, 2)   │ (p, 1)   │ │ (p, 4)   │ (p, 2)   │ │          │          │
└──────────┴──────────┘ └──────────┴──────────┘ └──────────┴──────────┘

UNASSIGNED 샤드:
- Shard 0 (r)
- Shard 2 (r)
(p: Primary, r: Replica, 숫자: 문서 수)
```

이 상태에서 es01과 es02로 각각 데이터를 호출을 해보자.

<img width="1303" alt="Image" src="https://github.com/user-attachments/assets/ecdee29b-0e9d-4ee2-b222-b9d4b388f11e" />

첨부한 이미지에서 보는 것처럼 es01(9200), es02(9201)과는 통신이 되지만 es03(9202)로 요청을 보내면 요청이 실패하는 것을 알 수 있다.

자, 그럼 es03이 종료된 상태에서 현재 마스터 노드인 es02까지 종료하면 어떻게 될까?

<img width="1144" alt="Image" src="https://github.com/user-attachments/assets/ac39b774-903a-421a-897b-ca1f5a0fa6dc" />

보는 바와 같이 통신 자체가 실패함을 알 수 있다. 즉, 노드가 1개 남으면 (적어도 8.x 버전에서는...) 마스터 노드 투표가 불가하기에 노드가 통신 불가 상태가 된다. 그렇기 때문에 가용성 확보를 하기 위해서는 반드시 노드가 2개 이상 살아있어야 한다.

이제 다시 es02와 es03을 살려보자.

<img width="800" alt="Image" src="https://github.com/user-attachments/assets/47a9a30b-541a-4c87-83c9-a3c26caf67a2" />

다시 원래 샤드 구성으로 돌아왔다. 추측건대 복구 메커니즘이 작동한 듯하다. (로컬 테스트는 이걸로 끝!!!)

<br>

## 🏰️ 5. 실제 운영 환경(?)에서 테스트

사실 운영환경이라고 달라진 것은 없다. 로컬 환경을 그대로 옮긴 것뿐이니..

Elasticsearch용 인스턴스는 3개로, 각 인스턴스별 하나의 노드를 운영하려고 한다.

<img width="1215" alt="Image" src="https://github.com/user-attachments/assets/cfff07f6-f576-4bd4-b0dc-4831759095aa" />

```shell
# es01 (Elasticsearch)
docker run -d --name es01 --network host \
  -e "node.name=es01" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=172.31.1.162:9300,172.31.15.2:9300,172.31.5.22:9300" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "network.host=172.31.1.162" \
  -e "network.publish_host=172.31.1.162" \
  -e "http.port=9200" \
  -e "transport.port=9300" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v /var/lib/elasticsearch/es01:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2

# Kibana
docker run -d --name kibana --network host \
  -e "ELASTICSEARCH_HOSTS=[\"http://172.31.1.162:9200\",\"http://172.31.15.2:9200\",\"http://172.31.5.22:9200\"]" \
  -e "xpack.security.enabled=false" \
  -e "logging.verbose=true" \
  -e "SERVER_NAME=kibana" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/kibana/kibana:8.17.2

# Metricbeat
docker run -d --name metricbeat --network host \
  -e "setup.kibana.host=172.31.1.162:5601" \
  -e "output.elasticsearch.hosts=['http://172.31.1.162:9200','http://172.31.15.2:9200','http://172.31.5.22:9200']" \
  -e "metricbeat.modules=[{'module':'elasticsearch','metricsets':['node','node_stats'],'hosts':['http://172.31.1.162:9200','http://172.31.15.2:9200','http://172.31.5.22:9200'],'period':'10s'}]" \
  -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" \
  -v /var/lib/metricbeat:/usr/share/metricbeat/data \
  docker.elastic.co/beats/metricbeat:8.17.2

docker run -d --name es02 --network host \
  -e "node.name=es02" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=172.31.1.162:9300,172.31.15.2:9300,172.31.5.22:9300" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "network.host=172.31.15.2" \
  -e "network.publish_host=172.31.15.2" \
  -e "http.port=9200" \
  -e "transport.port=9300" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v /var/lib/elasticsearch/es02:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2
  
docker run -d --name es03 --network host \
  -e "node.name=es03" \
  -e "cluster.name=es-cluster" \
  -e "discovery.seed_hosts=172.31.1.162:9300,172.31.15.2:9300,172.31.5.22:9300" \
  -e "cluster.initial_master_nodes=es01,es02,es03" \
  -e "network.host=172.31.5.22" \
  -e "network.publish_host=172.31.5.22" \
  -e "http.port=9200" \
  -e "transport.port=9300" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
  -e "xpack.security.enabled=false" \
  --ulimit memlock=-1:-1 \
  -v /var/lib/elasticsearch/es03:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2

```

각 노드들을 같은 VPC 그룹에 위치시키고 Private IP로 연결시켜줬다. 로컬 환경에서는 따로 도커 네트워크를 만들어서 연결해줬지만 인스턴스에서는 다음과 같은 설정이 필요하다.

* -e "network.host=172.31.5.22"
* -e "network.publish_host=172.31.5.22"

Elasticsearch 클러스터를 로컬 환경과 AWS EC2 인스턴스에서 구성할 때 네트워크 설정 방식이 달라진다 로컬 환경에서는 Docker가 제공하는 네트워크(예: bridge 또는 사용자 정의 네트워크)를 통해 노드 간 통신을 자동으로 관리하므로 network.host나 network.publish_host를 명시하지 않아도 되지만, AWS EC2 인스턴스는 VPC 내 Private IP(예: 172.31.5.22)를 기반으로 통신하며, Docker --network host 모드를 사용할 경우 호스트 네트워크 스택을 직접 사용하므로 network.host와 network.publish_host를 Private IP로 설정해야 한다. 이는 VPC에서 노드 간 통신 경로를 명확히 정의하고, 다른 노드가 이 노드를 올바르게 발견할 수 있게 하기 위함이다.

또한, 현재 사용하고 있는 인스턴스 스펙이 Ubuntu 24.04 LTS인데, 해당 스펙에서 IPv6 관련 문제가 발생했다. Ubuntu 24.04 LTS는 기본적으로 IPv6를 지원하도록 설계되어 있어, 네트워크에서 IPv6 주소를 자동으로 인식한다. 즉, Docker를 사용해 Elasticsearch를 실행할 때 **--network host** 모드를 선택하면, Docker는 호스트(여기서는 EC2 인스턴스)의 네트워크 설정을 그대로 따르는데, 이때 Ubuntu가 IPv6 주소를 우선으로 인식하면, Elasticsearch가 IPv6 주소를 사용하려고 시도하므로 결과적으로 Elasticsearch가 제대로 클러스터를 형성하지 못하게 된다. 그렇기 때문에 컨테이너를 만들 때, `-e "network.host=172.31.5.22"`, `-e "network.publish_host=172.31.5.22"` 와 같이 IPv4 형태로 호스트 IP를 지정해줘야 VPC 그룹과 통신이 가능하다.


| 항목                 | 로컬 환경                        | AWS EC2 인스턴스                      |
| -------------------- | -------------------------------- | ------------------------------------- |
| 네트워크 관리        | Docker 네트워크(예: bridge) 사용 | VPC 및 Private IP 기반                |
| IP 설정              | Docker 내부 IP 자동 할당         | Private IP(예: 172.31.5.22) 명시 필요 |
| network.host         | 생략 가능                        | 명시 (호스트 IP로 설정)               |
| network.publish_host | 생략 가능                        | 명시 (클러스터 노드 발견용)           |
| IPv6 문제            | 드물게 발생 (격리된 환경)        | VPC 설정에 따라 발생 가능             |
| 설정 복잡성          | 낮음 (Docker가 관리)             | 높음 (수동 IP 설정 필요)              |

기본적인 설정은 끝났으니, 이제 컨테이너를 종료하면서 가용성 테스트를 해보자.

<br>

### 🚀 5.1 실제 운영 환경에서 고가용성 테스트

<img width="934" alt="Image" src="https://github.com/user-attachments/assets/6e4e1c19-995d-4b66-a43f-9821c3f9a300" />

인덱스 샤딩 역시 Primary Shard를 각 노드별 하나씩, 그리고 레플리카 역시 마찬가지로 1개 생성했다. 즉, 로컬 환경과 마찬가지로 3+3 환경이다.

<img width="886" alt="Image" src="https://github.com/user-attachments/assets/5c3100c9-9ea1-46f2-8916-ffb65b1ed261" />

(ip 주소가 private인 이유는 EC2 인스턴스 콘솔 내에서 실행하고 있기 때문이다)

es03 컨테이너를 종료해보자.
<img width="887" alt="Image" src="https://github.com/user-attachments/assets/e3e8cda7-2d40-4bfc-b490-b2cc4439c874" />

상태가 green -> yellow로 바뀌고 노드도 가용 노드도 3개에서 2개로 바뀌었음을 알 수 있다. 또한, 샤드 역시 es03이 담담하던 부분을 es01과 es02에게 재분배되었다.

<img width="757" alt="Image" src="https://github.com/user-attachments/assets/4471b7d6-7766-4c98-9cb0-90727d94b604" />

노드가 3개에서 2개로 줄어들더라도 클러스터링 환경에서는 데이터 송수신에는 무리가 없다. (이런 걸 가용성이 확보되었다고 해야 하나?)

<img width="1436" alt="Image" src="https://github.com/user-attachments/assets/2d05e6bb-a268-4bba-a2a1-e9b371f85ac5" />

<br>

## 🎯 6. 결론

로컬 환경과 AWS EC2 인스턴스에서 클러스터링을 테스트하며, 고가용성, 확장성, 부하 분산이라는 핵심 이점을 실질적으로 확인할 수 있었다. 특히, 로컬에서는 Docker 네트워크를 활용한 간단한 설정으로 클러스터를 구성할 수 있었고, 운영 환경에서는 VPC와 Private IP를 기반으로 한 수동 네트워크 설정이 필요하다는 점 역시 특기할 만하다. 또한, Ubuntu 24.04 LTS의 IPv6 지원과 Docker --network host 모드 간의 상호작용으로 발생한 문제를 network.host와 network.publish_host를 IPv4로 강제로 지정해 해결한 과정도 유익한 부분이었다.

클러스터링을 통해 한 노드가 중단되더라도 다른 노드가 데이터를 유지하며 서비스가 지속될 수 있음을 확인했으며, 노드 재분배 및 복구 메커니즘이 원활히 작동하는 것 역시 눈 여겨볼 만하다. 하지만 Elasticsearch가 꽤나 메모리를 많이 차지하는 듯하여 t2.medium 인스턴스의 4GB RAM 제한으로 인해 메모리 관리에 주의가 필요하다.
