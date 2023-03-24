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

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserTopicPermissionReaderTest {

    @SneakyThrows
    @Test
    public void testTopicPermissions() {
        String topic = "demotenant/demonamespace/demotopic";
        UserTopicPermissionReader userTopicPermissionReader = new UserTopicPermissionReader();
        Assert.assertFalse(userTopicPermissionReader.hasDeleteSchemaPermission("pgarule", topic));
        Assert.assertTrue(userTopicPermissionReader.hasDeleteSchemaPermission("PGarule", topic));
        Assert.assertTrue(userTopicPermissionReader.hasDeleteSchemaPermission("shrathore", topic));
        Assert.assertTrue(userTopicPermissionReader.hasClearBacklogPermission("PGarule", topic, "subsription01"));
        Assert.assertTrue(userTopicPermissionReader.hasClearBacklogPermission("cparikh", topic, "subsription01"));
        Assert.assertFalse(userTopicPermissionReader.hasClearBacklogPermission("shrathore", topic, "subsription01"));
        Assert.assertTrue(userTopicPermissionReader.hasClearBacklogPermission("ddcosta", topic, "subscription03"));
        Assert.assertFalse(userTopicPermissionReader.hasClearBacklogPermission("PGarule", topic, "subscription02"));
        Assert.assertTrue(userTopicPermissionReader.hasClearBacklogPermission("schandraprasad", topic, "subscription02"));
    }
}
