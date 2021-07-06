# 一、索引管理

- es所有的命令都是通过http完成的

- es的和传统的数据库对于规则如下：

```
db              es
数据库           索引
表              类型type
行              文档document
列              字段field
表结构schema     映射mapping
索引             全文索引
sql             http接口
```

- Text：会分词，然后进行索引，支持模糊、精确查询，不支持聚合

- keyword： 不分词，直接索引，支持模糊、精确查询，支持聚合

## 1.索引的增加

- put blog，索引必须全部小写，es7过后取消type概念，默认为_doc

```json
{
  "settings": {
    "number_of_shards": 1
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "content": {
        "type": "text"
      },
      "postTime": {
        "type": "date"
      },
      "title": {
        "type": "text"
      }
    }
  }
}
```

## 2.删除

- delete blog
- 全部删除

```
curl -X DELETE http://localhost:9200/_all
```

## 3.改

- put blog/_settings

```json
{
  "blocks.read": true
}
```

```
blocks.read，禁止对当前索引进行读操作
blocks.read_only，只允许读，不允许写
blocks.write，禁止写操作
```

- put blog/_mappings

```json
{
    "properties": {
        "id": {
            "type": "text"
        },
        "content": {
            "type": "text"
        },
        "postTime": {
            "type": "date"
        },
        "title": {
            "type": "text"
        },
        "aaa": {
            "type": "text"
        }
    }
}
```

## 4.查

- 查询全部的索引

```
curl -X GET http://localhost:9200/_cat/indices
```

- get blog/_settings
- get blog/_mappings

## 5.索引打开和关闭

- post blog/_close，已经关闭的索引不能进行任何读写操作
- post blog/_open

- post _all/_close
- post _all/_open，全部打开

# 二、文档管理

## 1.增加文档

- put blog/_doc/1，index/type/id

```json
{
  "id": 1,
  "title": "Java讲义",
  "postTime": "2019-06-09",
  "content": "全功能的一门编程语言"
}
```

- post blog/_doc，这种方式id会随机生成

```json
{
  "id": 3,
  "title": "Java讲义3",
  "postTime": "2019-06-09",
  "content": "全功能的一门编程语言3"
}
```

## 2.修改文档

- put blog/_doc/1

```json
{
  "title": "Java讲义bbb",
  "postTime": "2019-06-09",
  "content": "全功能的一门编程语言bbb"
}
```

## 3.删除文档

- delete blog/_doc/1

## 4.查询文档

- get blog/_doc/1

## 5.路由机制

- shard = hash(routing) % shard_num

```
routing默认为文档id，也可以指定routing，这样可以避免查询的时候广播查询，如下
get blog/article/1?routing=jay，routing值固定的话，可以让查询集中在一个片上
```

# 三、映射

- get blog/_mapping，查看映射信息，默认是动态映射

- put blog，创建索引时就添加静态映射，添加更精准，更详细的配置信息

```
boost为权重权重，默认为1
format匹配的格式
keyword不会被分词，ignore_above和keyword一起使用，指定分词和索引的最大长度，超过的部分不会被索引
index是否进行到排序索引，false不进行索引，也就不可搜索，默认开启，动态mapping解析出来为数字类型、布尔类型的字段除外
doc_values默认开启，会增加一个列存储索引，对于不需要排序和聚合的字段可以关闭
similarity默认使用BM25评分模型
{
    "settings": {
        "number_of_shards": 1
    },
    "mappings": {
        "properties": {
            "id": {
                "type": "long"
            },
            "title": {
                "type": "keyword",
                "ignore_above": 20
            },
            "content": {
                "type": "text",
                "boost": 2,
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_smart"
            },
            "postTime": {
                "type": "date",
                "format": "yyyy-MM-dd HH:mm:ss",
                "doc_values": true,
                "index": false
            }
        }
    }
}
```

- post _bulk，批量插入，一个document必须在一行，而且还要换行，下面的结构不要改

```
{"index":{ "_index": "blog", "_type": "_doc", "_id": "1" }}
{"id": 1, "title": "Java讲义", "postTime": "2017-06-09 12:30:00", "content": "全功能的一门编程语言，既可以做前端，也可以做后端"}

{"index":{ "_index": "blog", "_type": "_doc", "_id": "2" }}
{"id": 2, "title": "精通C#", "postTime": "2018-06-09 12:30:00", "content": "大部分用来在windows上做前端"}

{"index":{ "_index": "blog", "_type": "_doc", "_id": "3" }}
{"id": 3, "title": "Go实战", "postTime": "2019-06-09 12:30:00", "content": "Google全力主推的一门语言"}

```

# 四、复杂查询

- get/post，blog/_search或者/_search查询全部

```json
{
  "query": {
    "match_all": {}
  }
}
```

- get，/_search，分页查询，_source指定需要返回的字段，version默认不返回，min_score过滤最低的分数，highlight高亮显示，不指定sort默认用评分排序

```json
{
  "from": 0,
  "size": 100,
  "query": {
    "term": {
      "content": "前端"
    }
  },
  "_source": [
    "title",
    "content"
  ],
  "version": true,
  "min_score": 0.6,
  "highlight": {
    "fields": {
      "content": {
        "pre_tags": [
          "<strong>"
        ],
        "post_tags": [
          "</strong"
        ]
      }
    }
  },
  "sort": [
    {
      "id": {
        "order": "desc"
      }
    },
    {
      "title": {
        "order": "asc"
      }
    }
  ]
}
```

## 1.全文查询

### 1).match

- query会分词，operator分词为或关系

```json
{
    "query": {
        "match": {
            "content": {
                "query": "编程语言",
                "operator": "or"
            }
        }
    }
}
```

- multi_match是match的升级，可以指定多个字段

```json
{
    "query": {
        "multi_match": {
            "query": "语言",
            "fields": [
                "title",
                "content"
            ]
        }
    }
}
```

### 2).match_phrase

- 会分词，分词后的所有词项都要出现在该字段中；字段中的词项顺序要一致

```json
{
    "query": {
        "match_phrase": {
            "content": "编程语言"
        }
    }
}
```

- match_phrase_prefix与match_phrase类似，只不过支持最后一个词项前缀匹配

```json
{
    "query": {
        "match_phrase_prefix": {
            "content": "语言 既"
        }
    }
}
```

### 3).simple_query_string

```json
{
    "query": {
        "simple_query_string": {
            "query": "编程语言",
            "analyzer": "ik_max_word",
            "fields": ["title", "content"],
            "default_operator": "or"
        }
    }
}
```

## 2.词项查询

### 1).terms

- term，查询含有一个词项

```json
{
    "query": {
        "term": {
            "content": "前端"
        }
    }
}
```

- 查询content中包含语言的多个词项，含有一个词项只能有term

```json
{
  "query": {
    "terms": {
      "content": [
        "前端",
        "后端"
      ]
    }
  }
}
```

### 2).range query

- 查询title，因为keyword类型的不分词，如果查“讲义”是查询不到的，term结构化字段查询，匹配一个值，且输入的值不会被分词器分词。

```json
{
  "query": {
    "range": {
      "id": {
        "gt": 2,
        "lte": 3
      }
    }
  }
}
```

### 3).exists query

- 查询title，因为keyword类型的不分词，如果查“讲义”是查询不到的，term结构化字段查询，匹配一个值，且输入的值不会被分词器分词。

```json
{
    "query": {
        "exists": {
            "field": "title"
        }
    }
}
```

### 4).prefix query

- 查询某些以win开头的词项

```json
{
    "query": {
        "prefix": {
            "content": "win"
        }
    }
}
```

### 5).wildcard query

- 通配符查询，?匹配一个字符，*匹配任意多个字符

```json
{
    "query": {
        "wildcard": {
            "content": "win*"
        }
    }
}
```

### 6).regexp query

- 正则表达式查询，和java的正则表达式类似

```json
{
  "query": {
    "regexp": {
      "content": "windows"
    }
  }
}
```

### 7).fuzzy query

- 模糊查询效率不高，如下把windows写错了，仍然可以查到

```json
{
    "query": {
        "fuzzy": {
            "content": "windosw"
        }
    }
}
```

### 8).type query

- es7中默认的为_doc

```json
{
    "query": {
        "type": {
            "value": "_doc"
        }
    }
}
```

### 8).ids query

- 查询含有某些_id的文档

```json
{
    "query": {
        "ids": {
            "type": "_doc",
            "values": [1, 2]
        }
    }
}
```

## 3.复合查询

### 1).bool query

- 逻辑查询，must=and，should=or

```json
{
  "query": {
    "bool": {
      "minimum_should_match": 1,
      "must": {
        "match": {
          "title": "Java讲义"
        }
      },
      "should": {
        "match": {
          "content": "语言"
        }
      },
      "must_not": {
        "range": {
          "id": {
            "gte": 2
          }
        }
      }
    }
  }
}
```

# 五、聚合查询

## 1.指标聚合

### 1).max 和 min

```json
{
    "size": 0,
    "aggs": {
        "max_id_demo": {
            "max": {
                "field": "id"
            }
        }
    }
}
```

### 2).avg 和 sum

- avg和sum用法类似

```json
{
    "size": 0,
    "aggs": {
        "avg_id_demo": {
            "avg": {
                "field": "id"
            }
        }
    }
}
```

### 3).cardinality

- 类似distinct

```json
{
    "size": 0,
    "aggs": {
        "all_title_demo": {
              "cardinality": {
                  "field": "title"
              }
        }
    }
}
```

### 4).stats 和 extended_stats

- 会统计count，max，min，avg，sum，必须是整型，两者用法基本类似，后者返回更多的统计条目如平方差

```json
{
    "size": 0,
    "aggs": {
        "extended_id_demo": {
              "stats": {
                  "field": "id"
              }
        }
    }
}
```

### 5).percentiles

- 百分位统计

```json
{
    "size": 0,
    "aggs": {
        "percentiles_id_demo": {
              "percentiles": {
                  "field": "id"
              }
        }
    }
}
```

### 6).percentiles

- 按字段统计文档数量

```json
{
    "size": 0,
    "aggs": {
        "count_id_demo": {
              "value_count": {
                  "field": "id"
              }
        }
    }
}
```

## 2.分组聚合

### 1).terms aggregation

- 按id分组，统计每组的数量

```json
{
  "size": 0,
  "aggs": {
    "per_count_demo": {
      "terms": {
        "field": "id"
      }
    }
  }
}
```

- 按id分组，统计每组的数量和每组平均价格

```json
{
    "size": 0,
    "aggs": {
        "per_count_demo": {
              "terms": {
                  "field": "id"
              },
              "aggs": {
                  "avg_id_test": {
                      "avg": {
                          "field": "id"
                      }
                  }
              }
        }
    }
}
```

### 2).filter aggregation

- filter过滤符合条件的文档，将这些文档分到一个桶中，然后统计

```json
{
  "size": 0,
  "aggs": {
    "avg_id_demo": {
      "filter": {
        "term": {
          "content": "前端"
        }
      },
      "aggs": {
        "avg_id_test": {
          "avg": {
            "field": "id"
          }
        }
      }
    }
  }
}
```

- filters过滤符合条件的文档，将这些文档分到不同的filter桶中，然后统计

```json
{
  "size": 0,
  "aggs": {
    "avg_id_demo": {
      "filters": {
        "filters": [
          {
            "match": {
              "id": 1
            }
          },
          {
            "match": {
              "id": 2
            }
          },
          {
            "match": {
              "id": 3
            }
          }
        ]
      },
      "aggs": {
        "avg_id_test": {
          "avg": {
            "field": "id"
          }
        }
      }
    }
  }
}
```

### 3).range aggregation

- 数值类型的范围聚合，反映数据的分布情况

```json
{
    "size": 0,
    "aggs": {
        "range_id_demo": {
            "range": {
                "field": "id",
                "ranges": [
                    {
                        "to": 2
                    },
                    {
                        "from": 2,
                        "to": 3
                    },
                    {
                        "from": 3
                    }
                ]
            }
        }
    }
}
```

- 日期类型的范围聚合

```json
{
  "size": 0,
  "aggs": {
    "range_date_demo": {
      "date_range": {
        "field": "postTime",
        "format": "yyyy-MM-dd HH:mm:ss",
        "ranges": [
          {
            "to": "2018-01-01 16:00:00"
          },
          {
            "from": "2018-01-01 16:00:00",
            "to": "2019-01-01 16:00:00"
          },
          {
            "from": "2019-01-01 16:00:00"
          }
        ]
      }
    }
  }
}
```
