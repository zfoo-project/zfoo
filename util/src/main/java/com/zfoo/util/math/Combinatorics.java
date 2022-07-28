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

package com.zfoo.util.math;

/**
 * @author godotg
 * @version 3.0
 */
public class Combinatorics {// |kɒmbinəˋtɒ:riks|n.组合学


    //输出数组a的startIndex~endIndex(包括端点)的全排列
    public void permutation(int[] a, int startIndex, int endIndex) {

        if (startIndex == endIndex) {
            for (int i = 0; i <= endIndex; i++) {
                System.out.print(a[i]);
            }
            System.out.println();
        }
        for (int i = startIndex; i <= endIndex; i++) {
            swap(a, i, startIndex);
            permutation(a, startIndex + 1, endIndex);
            swap(a, i, startIndex);
        }
    }

    //输出数组a的全排列
    public void permutation(int[] a) {
        permutation(a, 0, a.length - 1);
    }


    //public void Combination(int[] a,int[] b,int)

    //交换数组中的两个元素
    public void swap(int[] a, int xIndex, int yIndex) {
        int temp = a[xIndex];
        a[xIndex] = a[yIndex];
        a[yIndex] = temp;
    }


}
