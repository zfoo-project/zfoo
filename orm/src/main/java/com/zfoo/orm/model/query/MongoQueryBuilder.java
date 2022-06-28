package com.zfoo.orm.model.query;

import com.mongodb.client.model.Filters;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.StringUtils;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MongoQueryBuilder<E extends IEntity<?>> implements IQueryBuilder<E>{
    private Class<E> entity;
    private Bson builer;
    MongoQueryBuilder(Class<E> entityClazz)
    {
        entity = entityClazz;
    }


    @Override
    public IQueryBuilder<E> eq(String fieldName, Object fieldValue) {
        var bson = Filters.eq(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }
    @Override
    public IQueryBuilder<E> ne(String fieldName, Object fieldValue) {
        var bson = Filters.ne(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }
    @Override
    public IQueryBuilder<E> lt(String fieldName, Object fieldValue) {
        var bson = Filters.lt(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }
    @Override
    public IQueryBuilder<E> lte(String fieldName, Object fieldValue) {
        var bson = Filters.lte(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }
    @Override
    public IQueryBuilder<E> gt(String fieldName, Object fieldValue) {
        var bson = Filters.gt(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }
    @Override
    public IQueryBuilder<E> gte(String fieldName, Object fieldValue) {
        var bson = Filters.gte(fieldName, fieldValue);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }

    @Override
    public IQueryBuilder<E> in(String fieldName, List<?> fieldValueList) {
        var bson = Filters.in(fieldName, fieldValueList);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }

    @Override
    public IQueryBuilder<E> nin(String fieldName, List<?> fieldValueList) {
        var bson = Filters.nin(fieldName, fieldValueList);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }

    @Override
    public IQueryBuilder<E> like(String fieldName, String fieldValue) {
        var regex = StringUtils.format("^{}.*", fieldValue);
        var bson = Filters.regex(fieldName, regex);
        if(builer!=null) builer = Filters.and(builer, bson);
        else builer = bson;
        return this;
    }

    @Override
    public List<E> queryAll() {
        var collection = OrmContext.getOrmManager().getCollection(entity);
        var list = new ArrayList<E>();
        var result = builer!=null?collection.find(builer):collection.find();
        result.forEach(new Consumer<IEntity<?>>() {
            @Override
            public void accept(IEntity<?> entity) {
                list.add((E)entity);
            }
        });
        return list;
    }

    @Override
    public E find() {
        var collection = OrmContext.getOrmManager().getCollection(entity);
        var list = builer!=null?collection.find(builer):collection.find();
        for (E row : list) {
            return row;
        }
        return null;
    }

    @Override
    public Pair<Page, List<E>> page(int page, int itemsPerPage) {
        var collection = OrmContext.getOrmManager().getCollection(entity);

        var p = Page.valueOf(page, itemsPerPage, collection.countDocuments());

        var result = builer!=null?collection.find(builer):collection.find();
        var list = new ArrayList<E>();
        result
                .skip(p.skipNum())
                .limit(p.getItemsPerPage())
                .forEach(new Consumer<IEntity<?>>() {
                    @Override
                    public void accept(IEntity<?> entity) {
                        list.add((E) entity);
                    }
                });

        return new Pair<>(p, list);
    }
}
