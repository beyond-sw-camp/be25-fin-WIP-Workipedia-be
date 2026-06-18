# 한국어 형태소 분석기(Nori) 플러그인을 포함한 Elasticsearch 이미지.
# 순정 이미지는 standard analyzer만 있어 한글을 공백 단위로만 끊는다.
# Nori를 설치해 "이름이" 같은 조사 결합 토큰을 "이름"으로도 검색되게 한다.
FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.8

RUN bin/elasticsearch-plugin install --batch analysis-nori
