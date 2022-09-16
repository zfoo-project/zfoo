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
package httputil

import (
	"io/ioutil"
	"net/http"
	"strings"
)

func PostWithHeader(url string, msg []byte, headers map[string]string) (string, error) {
	client := &http.Client{}

	req, err := http.NewRequest("POST", url, strings.NewReader(string(msg)))
	if err != nil {
		return "", err
	}
	for key, header := range headers {
		req.Header.Set(key, header)
	}
	resp, err := client.Do(req)
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	return string(body), nil
}

func PostWithAuthorization(url, authorization string, msg []byte) (string, error) {
	headers := make(map[string]string)
	headers["Authorization"] = authorization
	headers["Content-Type"] = "application/json"
	return PostWithHeader(url, msg, headers)
}
