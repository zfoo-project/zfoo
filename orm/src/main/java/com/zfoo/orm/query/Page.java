/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.query;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author godotg
 */
public class Page {

    /**
     * 第几页
     */
    private int page;
    /**
     * 页容量
     */
    private int itemsPerPage;

    /**
     * 数据库记录总数量
     */
    private long totalSize;

    public static Page valueOf(int page, int itemsPerPage, long totalSize) {
        if (page <= 0) {
            throw new IllegalArgumentException(StringUtils.format("分页数必须大于0，[page:{}]", page));
        }
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException(StringUtils.format("页容量必须大于0，[size:{}]", itemsPerPage));
        }
        if (totalSize < 0) {
            throw new IllegalArgumentException(StringUtils.format("总数两必须大于等于0，[size:{}]", totalSize));
        }
        var p = new Page();
        p.page = page;
        p.totalSize = totalSize;
        p.itemsPerPage = itemsPerPage;
        return p;
    }


    private Page() {
    }

    public <T> List<T> currentPageList(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        var skip = skipNum();
        var to = skip + itemsPerPage;

        if (skip >= list.size()) {
            return Collections.emptyList();
        }

        if (to >= list.size()) {
            to = list.size();
        }

        return list.subList(skip, to);
    }

    public int skipNum() {
        return (page - 1) * itemsPerPage;
    }

    public int totalPage() {
        return (int) ((totalSize + itemsPerPage - 1) / itemsPerPage);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
