package com.zfoo.orm.model.query;

import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.model.Pair;

import java.util.List;

/*
EQ、=	等于（=）
NE、<>	不等于（<>）
GT、>	大于（>）
GTE、>=	大于等于（>=）
LT、<	小于（<）
lte、<=	小于等于（<=）
LIKE	模糊查询
[n] in	（不在）IN 查询
 */
public interface IQueryBuilder<E extends IEntity>{
    IQueryBuilder<E> eq(String fieldName, Object fieldValue);
    IQueryBuilder<E> ne(String fieldName, Object fieldValue);
    IQueryBuilder<E> in(String fieldName, List<?> fieldValueList);
    IQueryBuilder<E> nin(String fieldName, List<?> fieldValueList);
    IQueryBuilder<E> like(String fieldName, String fieldValue);
    IQueryBuilder<E> lt(String fieldName, Object fieldValue);
    IQueryBuilder<E> lte(String fieldName, Object fieldValue);
    IQueryBuilder<E> gt(String fieldName, Object fieldValue);
    IQueryBuilder<E> gte(String fieldName, Object fieldValue);
    List<E> queryAll();
    E find();


    /**
     * 分页查询，默认按照id排序
     *
     * @param page         第几页
     * @param itemsPerPage 每页容量
     */
    Pair<Page, List<E>> page(int page, int itemsPerPage);
}
