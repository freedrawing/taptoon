# Elasticsearch 적용기

## 목차

* [1. Elasticsearch 도입 배경](#1-elasticsearch-도입-배경)
* [2. Elasticsearch와 RBBMS를 혼합한 방식의 페이징 한계](#2-elasticsearch와-rdbms를-혼합한-검색-방식의-페이징-한계)

## 1. Elasticsearch 도입 배경

[이전 글](인덱스를_활용한_검색_속도_향상_여정.md)에서 인덱스 최적화를 통해 검색 API 성능을 개선하는 과정을 정리했었다. 내가 원했던 검색 방식은 특정 keyword가 title 혹은 description에 포함되었을 때, 해당 게시글을 필터링하는 것이었다.
즉, `LIKE '%keyword%'` 같은 패턴을 사용해 원하는 문서를 찾는 것이었지만, [앞선 글](인덱스를_활용한_검색_속도_향상_여정.md)에서에서도 설명했듯이 이 방식에는 다음과 같은 문제가 있었다.

🔹 `LIKE '%keyword%'` 방식의 문제점

* ✅ B+Tree 인덱스는 좌측부터 검색할 때만 최적화된다.
* ✅ LIKE '%keyword%'는 앞부분이 불확실한 검색이므로 인덱스를 전혀 활용하지 못한다.
* ✅ 결국 Full Table Scan(전체 테이블 탐색)이 발생해 검색 속도가 크게 저하된다.
* ✅ 특히 한국어 검색에서는 띄어쓰기, 복합어 처리가 어려워 정확도가 떨어진다.

이를 해결하기 위해 MySQL Full-Text Index를 적용해봤지만, 한국어 지원이 부족했고, 형태소 분석 기능이 없어 정밀한 검색이 어려웠다.
결국 더 나은 검색 성능과 한국어 형태소 분석을 지원하는 Elasticsearch를 도입하게 되었다.

## 2. Elasticsearch를 활용한 검색 개선

Elasticsearch는 역색인(Inverted Index) 방식을 사용하여 텍스트 검색을 최적화한다.
즉, 기존 B+Tree 기반의 LIKE '%keyword%' 방식보다 훨씬 빠른 검색 성능을 제공할 수 있다. (역색인 방식으로 데이터를 저장하는 것은 Full-Text Index와 동일하다)

**✅ Elasticsearch 도입 후 변경된 검색 방식**

기존에는 RDBMS에서 `LIKE '%keyword%'`를 사용하여 전체 데이터를 탐색하는 방식이었지만, Elasticsearch에서는 형태소 분석기를 활용하여 보다 정교한 검색이 가능해졌다.

**🔹 Elasticsearch의 주요 장점**

* ✅ 역색인(Inverted Index) 방식으로 빠른 검색 속도 제공
* ✅ 형태소 분석기(Analyzer) 지원 → 한국어 검색 정확도 향상
* ✅ 검색 키워드에 대한 토큰화(Tokenization) 및 불용어(Stopword) 처리 가능 (한국의 경우 Nori Tokenizer에서 제공)
* ✅ 복합어(Compound Word) 검색 지원 → 띄어쓰기 오류 보정 가능

MySQL / MariaDB 같은 RDBMS에서 제공하는 Full-Text Index와 Elasticsearch가 어떤 점이 다른지 간단히 짚고 넘어가보자.

MySQL Full-Text Index 새

```sql
CREATE FULLTEXT INDEX ft_idx_post_search ON matching_post (title, description) WITH PARSER ngram;
INSERT INTO matching_post (title) ... ('웹툰작가 구합니다!!', ....);
```

MySQL의 Full-Text Index의 경우 NGram을 Parser로 채택하면 아래와 같이 인덱싱이 된다. 아래 결과에서 볼 수 있는 것처럼 의미 단위가 아닌 Default인 2글자마다 인덱싱이 되며, 불필요한 단어도 인덱싱이 되는 것을 알 수 있다.
<img width="1127" alt="Image" src="https://github.com/user-attachments/assets/7d68750a-abe5-479b-8c26-bc3d82784fc1" />

반면, Elasticsearch의 한국어 형태소 분석기인 'Nori Tokenizer'를 사용하면 아래와 같이 역인덱싱이 된다. 결과에서 볼 수 있는 것처럼 형태소 단위로 인덱싱이 되는 것을 알 수 있다. 용언, 조사, 체언, 어간, 어미 등 한국어의 특성에 맞게 토큰화가 되어 MySQL의 인덱싱보다 보다 효율적으로 인덱싱이 된 것을 알 수 있다. (조사나 어미 역시도 설정을 통해 제외할 수 있다)

```plaintext
POST _analyze
{
  "tokenizer": "nori_tokenizer",
  "text": "웹툰작가를 모집합니다!!"
}
```

```json
{
  "tokens": [
    {
      "token": "웹툰",
      "start_offset": 0,
      "end_offset": 2,
      "type": "word",
      "position": 0
    },
    {
      "token": "작가",
      "start_offset": 2,
      "end_offset": 4,
      "type": "word",
      "position": 1
    },
    {
      "token": "를",
      "start_offset": 4,
      "end_offset": 5,
      "type": "word",
      "position": 2
    },
    {
      "token": "모집",
      "start_offset": 6,
      "end_offset": 8,
      "type": "word",
      "position": 3
    },
    {
      "token": "하",
      "start_offset": 8,
      "end_offset": 11,
      "type": "word",
      "position": 4
    },
    {
      "token": "ᄇ니다",
      "start_offset": 8,
      "end_offset": 11,
      "type": "word",
      "position": 5
    }
  ]
}
```

Full-Text Index와 Elasticsearch는 모두 역인덱스 방식을 사용하지만 위에서 보는 것처럼 Elasticsearch는 토큰화를 하는 데 있어서 세밀한 조정이 가능하므로 저장공간을 아낌과 동시에 보다

두 방식의 차이점을 간단히 표현하면 아래 표와 같다.


| 비교 항목  | MySQL Full-Text Index | Elasticsearch       |
|--------|-----------------------|---------------------|
| 검색 방식  | NGram 기반 단순 토큰화       | 형태소 분석 기반 검색        |
| 한국어 지원 | ❌ 기본 지원 없음            | ✅ Nori Tokenizer 지원 |
| 불용어 처리 | ❌ 직접 추가해야 함           | ✅ 기본 제공             |
| 복합어 처리 | ❌ 띄어쓰기 오류 대응 불가       | ✅ 띄어쓰기 오류에도 검색 가능   |
| 검색 속도  | ⚠️ 대량 데이터에서 속도 저하     | ✅ 빠른 검색 속도          |
| 검색 정확도 | ⚠️ 단순 매칭, 검색 정확도 낮음   | ✅ 문맥 기반 검색 가능       |

## 3. Elasticsearch와 RDBMS를 혼합한 검색 방식의 페이징 한계

처음에는 구상했던 설계는 title과 description만 Elasticsearch에 저장하고, 검색을 통해 매칭된 문서의 ID를 가져온 뒤, 이를 기반으로 RDBMS에서 데이터를 조회하는 방식이다. 즉 검색에 필요한 필드인 title과 description만 Elasticsearch로 조회한 후, 매칭되는 문서의 ID값을 가지고 DB에서 전체 문서를 조회하는 방식이다. 이 방식을 택한 이유는 다음과 같다.

1. Elasticsearch의 인덱스는 RDBMS의 테이블과 대응되는 개념이다. 하지만 RDBMS와 달리 인덱스는 한 번 스키마를 설정하면 그 구조를 바꾸는 게 불가능하다. 즉, RDBMS는 테이블이 생성된 이후에도 데이터 타입 혹은 Column을 추가하는 게 가능한데 인덱스는 그것이 불가하기에 유연성이 떨어지기에 향후 확장성을 고려하면 구조 변경이 어려운 Elasticsearch에 모든 정보를 저장하는 게 좋지 않다고 판단했다.
2. Elasticsearch는 고속 검색에 최적화되어 있지만, 데이터의 일관성을 보장하는 데는 RDBMS만큼 강력하지 않다. 특히 Elasticsearch는 Eventually Consistent(궁극적 일관성) 모델을 사용하기 때문에, 즉각적인 데이터 정합성이 중요한 경우 문제가 발생할 수 있다. 반면, RDBMS는 강한 정합성(Strong Consistency) 을 제공하기 때문에, 신뢰성이 필요한 데이터는 여전히 RDBMS에 저장하는 것이 적절하다고 판단했다. 따라서 검색에 필요한 최소한의 정보만 Elasticsearch에 저장하고, 나머지 상세한 데이터는 RDBMS에서 관리하는 것이 데이터 정합성과 성능을 모두 고려한 설계라고 보았다.

<img width="500" alt="Image" src="https://github.com/user-attachments/assets/5b015b47-7573-4d6f-ae2a-f16e3771fb08" />

하지만 위 방식 역시 문제가 있다. 바로 페이징 처리다. 현재 내가 개발한 부분은 검색과 더불어 다량의 게시글 리스트를 반환하는 로직인데, 게시글 개수가 필히 많아질 것이기에 페이징 처리가 불가피하다. 또한 Elasticsearch를 검색 전용으로만 사용할 경우, ① Elasticsearch에서 키워드를 검색한 후 매칭된 ID 목록을 가져오고, ② 해당 ID를 기반으로 RDBMS에서 데이터를 조회하는 과정을 거쳐야 한다. 즉, 한 번의 요청에 대해 두 번의 데이터베이스 접근이 필요해 효율성이 떨어진다. 더 심각한 문제는 대량의 ID를 처리할 때 발생하는 성능 저하다. 예를 들어, 필터링된 데이터가 1,000개라고 하면, Elasticsearch에서 1,000개의 ID를 가져와 `... WHERE id IN (1, 2, 3, ..., 1000)`과 같은 SQL 쿼리를 실행해야 한다. 하지만 IN 절에 많은 ID를 포함하면 쿼리 실행 시간이 길어지고, 인덱스 활용이 비효율적으로 작용한다. 특히, 10,000개 이상의 ID를 처리할 경우 쿼리가 더욱 복잡해지고 데이터베이스 부하가 급격히 증가하며, 페이징 로직 구현이 어려워진다. 뿐만 아니라, Elasticsearch에서는 한 번에 가져올 수 있는 데이터가 10,000개로 제한된다. 따라서 특정 키워드에 대한 관련 문서가 10,000개를 초과하면 전체 데이터를 한 번에 가져올 수 없고, 이전 페이지를 기억하는 데도 제한이 생긴다. 이러한 문제로 인해 Elasticsearch에서 검색 후 RDBMS에서 세부 데이터를 조회하는 방식은 적절하지 않다고 판단했다.

## 3. Elasticsearch RDBMS의 Trade-Off

지금까지의 내용을 요약하면,

1. Elasticsearch와 RDBMS를 혼합한 검색 방식의 페이징 한계
   * 📌 검색과 함께 다량의 게시글을 반환해야 하므로 페이징 처리가 필수적이다.
   * 🔄 Elasticsearch를 검색 전용으로 사용할 경우, ① 검색 후 ID 조회 → ② RDBMS에서 데이터 조회의 2단계를 거쳐야 해 비효율적이다.
2. IN 절을 이용한 RDBMS 조회의 성능 저하
   * 🔍 검색 결과가 많을수록 `... WHERE id IN (1, 2, 3, ..., N)` 쿼리가 길어지며, ⏳ 쿼리 실행 시간이 증가하고 인덱스 활용이 ⚡ 비효율적이 된다.
   * ⚠️ 특히, 10,000개 이상의 ID를 처리할 경우, 🚀 RDBMS의 부하가 급격히 증가하며 페이징 로직 구현이 어려워진다.
3. Elasticsearch의 검색 결과 개수 제한
   * 📏 Elasticsearch는 한 번의 요청에서 최대 10,000개까지 데이터 조회가 가능하다.
   * 🚧 따라서 검색 결과가 10,000개를 초과하면 전체 데이터를 가져올 수 없으며, 이전 페이지를 기억하는 데도 제한이 발생한다.

지금까지 여러 방법들을 모색한 결과, 페이징 문제만 해결된다면 Elasticsearch와 RDBMS를 혼합하는 방식이 이상적으로 보인다. 하지만 페이징 처리가 필수적이므로, 차선책으로 Elasticsearch를 메인 DB로 사용하고, RDBMS를 백업처럼 운영하는 방식을 택했다. 그렇기에 동작 시나리오는 다음과 같다.

1. 사용자가 게시글글을 등록하면 RDBMS에 먼저 저장이 된다. RDBMS가 백업처럼 사용되기에 Elasticsearch에 먼저 저장하는 게 자연스러워 보이지만, Elasticsearch는 Transaction 및 Rollback에 대해 RDBMS보다 취약하기에 RDBMS에 먼저 저장하는 방법을 택했다.
2. RDBMS에 성공적으로 데이터가 저장되면, Elasticsearch에 동일한 정보를 저장한다.

![Image](https://github.com/user-attachments/assets/75c26c33-9689-44a2-9f89-855a45be7c9a)

✅ 위 로직에 대한 상세 코드는 아래와 같다.

```java




    // 매칭 포스트 등록 (수정 작업을 하지만 사실상 등록 로직임)
    @Transactional
    public MatchingPostResponse registerMatchingPost(Long memberId, Long matchingPostId, RegisterMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(memberId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다.");
        }

        findMatchingPost.registerMe(request);
        registerMatchingPostImages(request.matchingPostImageIds());

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    /*
        * MatchingPost 이미지 ID로 PENDING -> REGISTERED로 변경
        * `modifyMatchingPost()`의 트랜잭션 하에서 실행됨
     */
    private void registerMatchingPostImages(List<Long> matchingPostImageIds) {
        if (matchingPostImageIds.isEmpty()) return;

        List<MatchingPostImage> uploadedImages = matchingPostImageRepository.findAllById(matchingPostImageIds);
        if (uploadedImages.isEmpty() == false) {
            uploadedImages.forEach(MatchingPostImage::registerMe);
        }
    }

// -------------------------------------------------------- 

 void upsertToElasticsearchAfterCommit(MatchingPost updatedMatchingPost) {
     TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 업데이트할 문서 생성
                    MatchingPostDocument updatedDocument = MatchingPostDocument.from(updatedMatchingPost);

                    // ES에 기존 문서가 있는지 확인하고 있으면 업데이트, 없으면 새로 저장
                    elasticMatchingPostRepository.save(updatedDocument);

                    log.info("✅ ES 문서 저장(업데이트 또는 신규 추가) 성공: matchingPostId={}", updatedMatchingPost.getId());
                } catch (Exception e) {
                    log.error("❌ ES 업데이트 중 오류 발생: matchingPostId={}, error={}", updatedMatchingPost.getId(), e.getMessage(), e);
                }
            }
        });
    }
```

### ⚠️ 3.1 위 방식 역시 완벽하지 않다. 데이터 정합성 문제는 여전히 남아 있다.

1️⃣ 데이터 동기화 지연 문제

* 트랜잭션이 커밋된 이후 Elasticsearch에 데이터를 저장하기 때문에, RDBMS와 Elasticsearch 간의 데이터 정합성이 즉시 맞춰지지 않을 가능성이 있다.
* 특히 검색을 실행하는 시점과 데이터가 Elasticsearch에 반영되는 시점 사이의 간격(Lag) 이 발생할 수 있다.

2️⃣ 장애 발생 시 데이터 불일치 문제

* 만약 Elasticsearch 저장 과정에서 예외(Exception)가 발생하면, RDBMS에는 데이터가 존재하지만 Elasticsearch에는 저장되지 않는 문제가 발생할 수 있다.
* 이런 경우, 별도의 재시도 로직(Retry Mechanism) 또는 비정상 데이터 동기화 프로세스(Failure Recovery Process) 가 필요하다. 이럴 때를 대비해 RDBMS에 우선적으로 데이터를 저장한 것이지만, 해당 문제가 발생하면 향후 로그 등을 분석해 대응 방법을 고민해야 한다.

## 4. (Sub Issue - 1) 않이, Document `@Setting`이 왜 않 먹는 거니? (feat. 해결 못함...)

아래 코드는 Elasticsearch에 저장되는 Document 클래스다. JPA `ddl-auto: create` 옵션이 있는 것처럼 Elasticsearch 역시 Index가 정의되지 않은 상태에서 애플리케이션을 실행하면 자동으로 인덱스가 생성되게 하고 싶었다. `@Setting` 애노테이션을 사용하면 미리 설정된 인덱스 스키마를 자동으로 Elasticsearch에 생성해준다. 그런데, 이것이 아무리 해도 작동을 안 하는 것이었다!!

```java
@Getter
@Document(indexName = "matching_post")
//@Setting(settingPath = "/elastic/matchingpost-setting.json") // Elasticsearch 버전에 오류가 많아서 이 방법은 안 될 듯하다...
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long, index = false)
    private Long authorId;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private ArtistType artistType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"), // 이건 ES에 미리 안 만들어지면 서버 동작 안 함
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english"),
            }
    )
    private String title;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private WorkType workType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english"),
            }
    )
    private String description;

    @Field(type = FieldType.Long) // 정렬하려면 인덱싱 돼야 함
    private Long viewCount;

    @Field(type = FieldType.Nested, index = false)
    private List<MatchingPostImageResponse> imageList; // Document가 DTO를 가지고 있는 게 좋아 보이지는 않는다.

    // 나중에 정렬할 때 속도가 너무 느리면 `epoch_millis`로 바꾸자
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime updatedAt;


    @Builder
    public MatchingPostDocument(Long id, Long authorId, ArtistType artistType, String title,
                                WorkType workType, String description, Long viewCount, List<MatchingPostImageResponse> imageList,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = id;
        this.authorId = authorId;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.description = description;
        this.viewCount = viewCount;
        this.imageList = imageList;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
```

여러 삽질과 Elasticsearch 깃허브 이슈를 뒤져보며 얻은 결론은 현재 사용하고 있는 Elasticsearch가 버그 아닌 버그가 있다는 것이었다... :sob:

스웨거를 사용하기 위해 특정 스프링 버전을 사용하였고, Elasticsearch는 해당 스프링 버전과 갖아 호환이 되는 버전을 사용했는데, 이런 오류가 있을 줄이야. 지금까지 사용하던 라이브러리는 버전별로 이렇게 오류가 있던 적이 없었는데, 알아보니 Elasticsearch는 버전별로 꽤나 버그가 많은 편이라고 한다...

여튼 여러 삽질 끝에 `@Setting` 기능을 이용해 자동으로 인덱스를 생성해주는 건 실패했고, 결국 수동으로 인덱스 관리를 하는 것이 현재 프로젝트의 단점이라면 단점이다. 그러니 앞으로는 라이브러리를 택할 때 버전도 잘 고려를 해야할 듯하다

## 5. (Sub Issue - 2) Autocomplete

... (추후 추가 예정)

## 결론

Elasticsearch를 도입함으로써 검색의 유연성을 확보할 수 있었지만, 현재 개발 중인 도메인과 결합되니 완벽한 해결책이라고 보기는 어려운 듯하다. 기존 RDBMS 기반의 `LIKE '%keyword%'` 방식에 비해 검색의 유연성은 확연히 향상되었지만, 데이터 정합성 문제, 페이징 처리의 어려움, 그리고 동기화 과정에서의 장애 가능성이 남아있었다. 특히, RDBMS와 Elasticsearch를 혼합하여 검색을 수행하는 방식에서는 페이징 시 성능 저하가 주요 이슈로 떠올랐다. IN 절을 이용한 대량 ID 조회 방식이 효율적이지 않았고, Elasticsearch에서 제공하는 기본적인 페이징 방식도 대량 데이터에서는 한계가 있었다. 따라서 Elasticsearch를 메인 DB로 활용하고 RDBMS를 보조적인 백업 역할로 사용하는 방식이 차선책으로 채택했다.

하지만 이러한 방식 역시 완벽하지 않았다. 트랜잭션 후 Elasticsearch에 데이터를 저장하는 과정에서 동기화 지연이 발생할 수 있으며, 장애가 발생할 경우 Elasticsearch에 데이터가 정상적으로 저장되지 않는 문제가 남아 있었다. 이를 해결하기 위해서는 재시도 로직(Retry Mechanism) 및 데이터 정합성 유지 방안을 추가로 고려해야 한다.

궁극적으로 Elasticsearch는 강력한 검색 성능을 제공하지만 RDBMS를 완전히 대체할 수는 없으며, 보완적인 기술로 활용하는 것이 현실적인 접근이라는 결론을 내렸다. 검색 성능을 개선하기 위해 Elasticsearch를 도입하되, 데이터 정합성을 유지하기 위해 RDBMS와의 조화를 유지하는 것이 중요하다.

🚀 Elasticsearch 도입을 통해 얻은 교훈은, 기술 스택을 선택할 때 성능뿐만 아니라 데이터 일관성, 운영 비용, 장애 대응 계획까지 고려해야 한다는 점이다.
