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

package org.apache.pulsar.manager.fanatics.dcs;

import com.fanatics.dcs.client.ConfigEntry;
import com.fanatics.dcs.client.DcsConfig;
import com.fanatics.dcs.client.rest.DcsRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class UserTopicPermissionReader {

    private final FanaticsDCSClientFactory dcsClientFactory;

    public UserTopicPermissionReader() {
        dcsClientFactory = FanaticsDCSClientFactory.factory();
    }
    public boolean hasDeleteSchemaPermission(String user, String topic) throws IOException {
        UserTopicPermissions userTopicPermissions = getUserTopicPermissions(topic);
        return userTopicPermissions.getDelete_schema() != null && userTopicPermissions.getDelete_schema().containsKey(user) && userTopicPermissions.getDelete_schema().get(user);
    }

    public boolean hasClearBacklogPermission(String user, String topic, String subscription) throws IOException {
        UserTopicPermissions userTopicPermissions = getUserTopicPermissions(topic);
        return userTopicPermissions.getClear_backlog() != null && userTopicPermissions.getClear_backlog().containsKey(subscription) && userTopicPermissions.getClear_backlog().get(subscription).containsKey(user) && userTopicPermissions.getClear_backlog().get(subscription).get(user);
    }

    private UserTopicPermissions getUserTopicPermissions(String topic) throws IOException {
        DcsRestClient dcsRestClient = dcsClientFactory.getDcsRestClient();
        ConfigEntry configEntry = dcsClientFactory.getDcsClientConfig().getBase();
        String config = String.format("schema/%s/pulsar-manager.yml", topic);
        configEntry.setConfig(config);
        DcsConfig dcsConfig = dcsRestClient.getLatest(configEntry).get();
        UserTopicPermissions userTopicPermissions = dcsConfig.toObject(UserTopicPermissions.class);
        System.out.println(userTopicPermissions.getClear_backlog());
        System.out.println(userTopicPermissions.getDelete_schema());
        return userTopicPermissions;
    }
}