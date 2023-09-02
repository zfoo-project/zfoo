/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.model;

import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 * @version 3.0
 */
public interface IEntity<PK extends Comparable<PK>> {

    /**
     * 一个文档的主键 primary key
     */
    PK id();


    /**
     * 一个文档的写入到数据库的version版本，version的get和set方法
     * <p>
     * 写入一条数据到数据库的时候会对比当前entity的vs和数据库的vs是不是一样，不是一样的话无法写入，一致的话就写入数据并且让vs自增+1.
     * 主要是为了防止多个服务器去操作同一条数据，保证在分布式环境的数据一致性；简单的说就是一个数据版本号的简单实现，版本号一致才能写入数据。
     * 在分布式环境中，会存在有状态服务器，比如网关路由一个玩家的数据到a服务器，这个时候你加了一个b服务器，有可能下一条数据就被路由到b。
     * 虽然用了一致性hash的负载均衡算法，但是一样有概率会让某些消息路由到不同服务器。
     * 这个时候版本号就可以保证只有一台服务器可以对数据库做操作，不用担心多个服务器去操作数据。
     * 这是一个容错的操作，真实环境下很少发生。为了高性能必须要把服务器做成有状态的，这个容错操作就是最后一道保证数据一致的方案。
     * mongodb更新的时候是原子的，并发更新同一条数据只有一条数据会写入，第二条数据写入的时候版本号已经不一致了，所以老版本号的数据无法写入。
     * 写入的时候只要发现版本号不一致，就让缓存失效，重新读取数据库最新的数据。
     * <p>
     * 分布式环境下，要想服务器性能好，就要做成有状态的，有状态就要遇到这种多服对同一条数据写入问题，这是一个千古难以解决的问题，进退两难。
     */
    default long gvs() {
        return 0L;
    }

    default void svs(long vs) {
    }

    /**
     * 判空：由于查询不存在时缓存中也会有一份，因此判断为空需要根据实际类型才能决定
     *
     * @return EntityCaches中取出的值在数据库中是否存在
     */
    default boolean empty() {
        PK idValue = id();
        if (idValue == null) {
            return true;
        }
        // id在启动的时候做过校验，只能是int long float double String几种类型
        if (idValue instanceof Number) {
            return ((Number) idValue).doubleValue() == 0D;
        } else {
            return StringUtils.isEmpty((CharSequence) idValue);
        }
    }

}
