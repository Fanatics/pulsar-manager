/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.manager.fanatics.argos;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Slf4j
public class FanaticsArgosLogger {

    public static void logInfo(String message, String username)  {
        try {
            LogMessage logMessage = new LogMessage("info", message, username);
            String jsonString =  new ObjectMapper().writeValueAsString(logMessage);;
            HttpPost httpPost = new HttpPost(String.format("https://stable.argos-api.service.us-east-1.dynamic.%s.frgcloud.com:8443/v1/log", System.getenv("SHORT_ENV_GROUP")));
            httpPost.setEntity(new StringEntity(jsonString));
            httpPost.setHeader("Content-type", "application/json");
            try(
                    CloseableHttpClient client = HttpClients.createDefault();
                    CloseableHttpResponse response = client.execute(httpPost)
                    ) {
                final int statusCode = response.getStatusLine().getStatusCode();
                System.out.println(EntityUtils.toString(response.getEntity()));
                log.info(String.format("send log to argos: [%s], status: [%s]", message, statusCode));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
